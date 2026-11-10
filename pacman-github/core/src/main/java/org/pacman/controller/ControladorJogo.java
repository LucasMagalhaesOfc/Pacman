package org.pacman.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Array;

import java.util.ArrayDeque;

import org.pacman.model.Direcao;
import org.pacman.model.Fantasma;
import org.pacman.model.MapaJogo;
import org.pacman.model.Pacman;
import org.pacman.model.Pontuacao;

public class ControladorJogo {

    private final MapaJogo mapa;
    private final Pacman pacman;
    private final Array<Fantasma> fantasmas = new Array<Fantasma>();
    private final Pontuacao pontuacao = new Pontuacao();

    private final Sound somComer =
        Gdx.audio.newSound(Gdx.files.internal("sounds/comer.mp3"));
    private final Sound somEspecial =
        Gdx.audio.newSound(Gdx.files.internal("sounds/especial.mp3"));
    private final Sound somFruta =
        Gdx.audio.newSound(Gdx.files.internal("sounds/fruta.mp3"));

    private final ManipuladorEntrada entrada = new ManipuladorEntrada();

    private int vidas = 3;
    private boolean gameOver;
    private boolean venceu;
    private float tempoRespawnPacman;
    private float tempoProtecaoPacman;

    public ControladorJogo(MapaJogo mapa, Pacman pacman) {
        this.mapa = mapa;
        this.pacman = pacman;
        criarFantasmas();
    }

    /** Spawns vem do mapa V5, sem coordenadas rigidas ou exclusao silenciosa. */
    private void criarFantasmas() {
        for(int i=0;i<mapa.getQuantidadeFantasmas();i++) {
            int[] pos=mapa.getSpawnFantasma(i);
            if(mapa.ehParede(pos[0],pos[1]))
                throw new IllegalStateException("Fantasma "+i+" nasce em parede");
            fantasmas.add(new Fantasma(i,pos[0],pos[1],i*.10f));
            mapa.removerPonto(pos[0],pos[1]);
        }
    }

    public Pontuacao getPontuacao() { return pontuacao; }
    public Array<Fantasma> getFantasmas() { return fantasmas; }
    public int getVidas() { return vidas; }
    public boolean isGameOver() { return gameOver; }
    public boolean isVenceu() { return venceu; }
    public float getTempoProtecaoPacman() { return tempoProtecaoPacman; }

    public void atualizar(float delta) {
        if (gameOver || venceu || delta <= 0f) {
            return;
        }

        if (tempoProtecaoPacman > 0f) {
            tempoProtecaoPacman = Math.max(0f, tempoProtecaoPacman - delta);
        }

        if (!pacman.isVivo()) {
            atualizarRespawnPacman(delta);
            atualizarFantasmas(delta);
            return;
        }

        Direcao direcao = entrada.lerDirecao();
        if (direcao != Direcao.NENHUMA) {
            pacman.setDirecaoDesejada(direcao);
        }

        boolean modoAntes = pacman.isModoMonstro();
        pacman.atualizar(delta, mapa);
        if (modoAntes && !pacman.isModoMonstro()) {
            pontuacao.reiniciarComboFantasmas();
        }

        comerPonto();
        // Vitoria prevalece no mesmo quadro do ultimo coletavel, antes de
        // uma colisao tardia com fantasma poder matar o jogador.
        if (mapa.getBolinhasRestantes() == 0) {
            venceu=true;
            pontuacao.salvarRecorde();
            return;
        }
        atualizarFantasmas(delta);
        verificarColisaoPacmanFantasmas();
    }

    private void atualizarRespawnPacman(float delta) {
        tempoRespawnPacman -= delta;
        if (tempoRespawnPacman <= 0f && vidas > 0) {
            pacman.renascer();
            tempoProtecaoPacman = 1.5f;
            tempoRespawnPacman = 0f;
        }
    }

    private void atualizarFantasmas(float delta) {
        Fantasma[] grupo=new Fantasma[fantasmas.size];
        for(int i=0;i<fantasmas.size;i++)grupo[i]=fantasmas.get(i);
        // Movimento sequencial com reserva de destinos impede sobreposicao e swaps.
        for(Fantasma f:grupo) {
            boolean antes=f.isAtivo();
            f.atualizar(delta,mapa,pacman,grupo);
            if(!antes && f.isAtivo()) ajustarRenascerSeguro(f);
        }
    }

    /** Evita que um fantasma reapareca exatamente em cima do Pac-Man. */
    private void ajustarRenascerSeguro(Fantasma renascido) {
        int pacLinha = pacman.getLinha();
        int pacColuna = pacman.getColuna();
        if ((!pacman.isVivo() || Math.abs(renascido.getLinha() - pacLinha)
            + Math.abs(renascido.getColuna() - pacColuna) >= 4)
            && !ocupadoPorOutroFantasma(renascido,renascido.getLinha(),renascido.getColuna())) {
            return;
        }

        int linhas = mapa.getLinhas();
        int colunas = mapa.getColunas();
        boolean[][] visitado = new boolean[linhas][colunas];
        ArrayDeque<Integer> fila = new ArrayDeque<Integer>();
        fila.add(renascido.getLinha() * colunas + renascido.getColuna());
        visitado[renascido.getLinha()][renascido.getColuna()] = true;
        int[] dl = {-1, 1, 0, 0};
        int[] dc = {0, 0, -1, 1};

        while (!fila.isEmpty()) {
            int indice = fila.removeFirst();
            int linha = indice / colunas;
            int coluna = indice % colunas;
            int distancia = Math.abs(linha - pacLinha) + Math.abs(coluna - pacColuna);

            if (distancia >= 4 && !ocupadoPorOutroFantasma(renascido, linha, coluna)) {
                renascido.setPosicao(linha, coluna);
                return;
            }

            Direcao[] direcoes = {Direcao.CIMA, Direcao.BAIXO,
                Direcao.ESQUERDA, Direcao.DIREITA};
            for (int d = 0; d < direcoes.length; d++) {
                if (!mapa.podeMover(linha, coluna, direcoes[d])) {
                    continue;
                }
                int nr = linha + dl[d];
                int nc = coluna + dc[d];
                if (!visitado[nr][nc]) {
                    visitado[nr][nc] = true;
                    fila.add(nr * colunas + nc);
                }
            }
        }
    }

    private boolean ocupadoPorOutroFantasma(Fantasma origem, int linha, int coluna) {
        for (Fantasma outro : fantasmas) {
            if (outro == origem || !outro.isAtivo()) {
                continue;
            }
            if (outro.getLinha() == linha && outro.getColuna() == coluna) {
                return true;
            }
        }
        return false;
    }

    // V5: os fantasmas nao colidem entre si porque suas celulas sao reservadas.
    // A tontura por colisao repetida da V4 foi intencionalmente removida.

    private void verificarColisaoPacmanFantasmas() {
        if(!pacman.isVivo())return;
        float px=pacman.getPosicaoXEmCelulas();
        float py=pacman.getPosicaoYEmCelulas();
        // Hitbox coerente com o corredor de 1 celula. Nunca 2 celulas como na V4.
        final float raio=.63f;
        for(Fantasma f:fantasmas) {
            if(!f.isAtivo())continue;
            float dx=px-f.getPosicaoXEmCelulas();
            float dy=py-f.getPosicaoYEmCelulas();
            if(dx*dx+dy*dy > raio*raio)continue;
            if(pacman.isModoMonstro()) {
                f.comer();
                pontuacao.comerFantasma();
                somEspecial.play();
            } else if(tempoProtecaoPacman<=0f) {
                perderVida();
                return;
            }
        }
    }

    private void perderVida() {
        vidas--;
        pacman.morrer();
        pontuacao.reiniciarComboFantasmas();

        if (vidas <= 0) {
            vidas = 0;
            gameOver = true;
            pontuacao.salvarRecorde();
        } else {
            tempoRespawnPacman = 1.15f;
        }
    }

    private void comerPonto() {
        int linha = pacman.getLinha();
        int coluna = pacman.getColuna();
        if (!mapa.temPonto(linha, coluna)) {
            return;
        }

        boolean poder = mapa.temPoder(linha, coluna);
        boolean fruta = mapa.temFruta(linha, coluna);
        int pontos = mapa.pontosDoItem(linha, coluna);
        mapa.removerPonto(linha, coluna);
        pontuacao.adicionar(pontos);

        if (poder) {
            pacman.ativarModoMonstro(8f);
            pontuacao.reiniciarComboFantasmas();
            somEspecial.play();
        } else if (fruta) {
            somFruta.play();
        } else {
            somComer.play();
        }
    }

    public void dispose() {
        pontuacao.salvarRecorde();
        somComer.dispose();
        somEspecial.dispose();
        somFruta.dispose();
    }
}

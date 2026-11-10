package org.pacman.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Align;
import org.pacman.model.RankingPartidas;
import java.util.List;
import java.util.Locale;

import org.pacman.model.Direcao;
import org.pacman.model.Fantasma;
import org.pacman.model.MapaJogo;
import org.pacman.model.Pacman;
import org.pacman.model.Pontuacao;

public class RenderizadorJogo {

    private static final int TAMANHO_FRAME = 48;

    // Aumenta a presenca dos personagens sem alargar suas hitboxes logicas.
    private static final float ESCALA_PACMAN = 1.50f;
    private static final float ESCALA_FANTASMA = 1.40f;

    private final ShapeRenderer formas = new ShapeRenderer();
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont fonte = new BitmapFont();
    private final OrthographicCamera camera = new OrthographicCamera();

    private float deslocamentoX;
    private float deslocamentoY;
    private float tamanhoCelula;
    private int ultimaLargura = -1;
    private int ultimaAltura = -1;

    private Texture texturaMapa;
    private Texture texturaCereja;
    private Texture texturaPacman;
    private Texture texturaPacmanMonstro;
    private Texture texturaChapeu;
    private TextureRegion regiaoChapeu;
    private Texture texturaFantasmaAssustado;
    private TextureRegion[][] quadrosAssustados;
    private TextureRegion[][] quadrosPacman;
    private TextureRegion[][] quadrosPacmanMonstro;

    // Ordem das cores: roxo, laranja, verde e azul.
    private final Texture[] texturasFantasmas = new Texture[6];
    private final TextureRegion[][][] quadrosFantasmas = new TextureRegion[6][][];

    private float tempoAnimacaoPacman;
    private float tempoPulso;
    private int framePacman;

    public RenderizadorJogo() {
        texturaMapa = carregarTextura(
            "images/mapa_halloween_v5.png"
        );
        texturaCereja = carregarTextura("images/cereja.png");
        texturaPacman = carregarTextura("images/pacman_v5.png");
        texturaPacmanMonstro = carregarTextura("images/pacman_monstro_v55.png");
        texturaChapeu = carregarTextura("images/chapeu_caindo_v55.png");
        regiaoChapeu = new TextureRegion(texturaChapeu);
        texturaFantasmaAssustado = carregarTextura("images/fantasma_assustado_v55.png");
        quadrosAssustados=TextureRegion.split(texturaFantasmaAssustado,32,32);

        quadrosPacman = TextureRegion.split(texturaPacman, TAMANHO_FRAME, TAMANHO_FRAME);
        quadrosPacmanMonstro = TextureRegion.split(
            texturaPacmanMonstro, TAMANHO_FRAME, TAMANHO_FRAME
        );

        carregarFantasmas();
        fonte.setColor(Color.WHITE);
    }

    private Texture carregarTextura(String caminho) {
        Texture textura = new Texture(Gdx.files.internal(caminho));
        textura.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        return textura;
    }

    private void carregarFantasmas() {
        String[] caminhos = {
            "sprites/fantasmas/pacman_halloween_ghost_pack/fantasma_bruxa_spritesheet.png",
            "images/fantasma_bruxa_laranja.png",
            "images/fantasma_bruxa_verde.png",
            "images/fantasma_bruxa_azul.png",
            "images/fantasma_bruxa_rosa_v55.png",
            "images/fantasma_bruxa_dourado_v55.png"
        };

        for (int i = 0; i < caminhos.length; i++) {
            texturasFantasmas[i] = carregarTextura(caminhos[i]);
            quadrosFantasmas[i] = TextureRegion.split(
                texturasFantasmas[i], 32, 32
            );
        }
    }

    public void desenhar(
        MapaJogo mapa,
        Pacman pacman,
        Pontuacao pontuacao,
        Array<Fantasma> fantasmas,
        int vidas,
        boolean gameOver,
        boolean venceu,
        float tempoProtecaoPacman,
        String nomeJogador,
        float tempoPartida,
        boolean pausado
    ) {
        atualizarCameraELayout(mapa);
        float delta = pausado ? 0f : Math.min(Gdx.graphics.getDeltaTime(), 0.05f);
        atualizarAnimacaoPacman(delta);
        tempoPulso += delta;

        // 1. A imagem do labirinto e todo o seu desenho decorativo.
        batch.begin();
        batch.draw(
            texturaMapa,
            deslocamentoX,
            deslocamentoY,
            mapa.getColunas() * tamanhoCelula,
            mapa.getLinhas() * tamanhoCelula
        );
        batch.end();

        // 2. Transparencia real para os halos (ShapeRenderer nao a ativa sozinho).
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        // Barras da interface + coletaveis (na mesma grade que o movimento).
        formas.begin(ShapeRenderer.ShapeType.Filled);
        formas.setColor(.075f,.045f,.12f,1f);
        formas.rect(0,Gdx.graphics.getHeight()-58,Gdx.graphics.getWidth(),58);
        formas.rect(0,0,Gdx.graphics.getWidth(),48);
        desenharBolinhas(mapa);
        desenharAuraMonstro(mapa, pacman);
        desenharEstrelasFantasmas(mapa, fantasmas);
        formas.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // 3. Personagens, fruta e interface.
        batch.begin();
        desenharCerejas(mapa);
        if(tempoProtecaoPacman<=0f || (int)(tempoPulso*9f)%2==0)
            desenharPacman(mapa, pacman);
        desenharChapeuCaindo(mapa,pacman);
        desenharFantasmas(mapa, fantasmas);
        desenharInterface(pontuacao, vidas, pacman, gameOver, venceu, mapa, nomeJogador, tempoPartida);
        batch.end();
    }

    private void atualizarCameraELayout(MapaJogo mapa) {
        int larguraTela = Gdx.graphics.getWidth();
        int alturaTela = Gdx.graphics.getHeight();

        if (larguraTela != ultimaLargura || alturaTela != ultimaAltura) {
            camera.setToOrtho(false, larguraTela, alturaTela);
            camera.update();
            batch.setProjectionMatrix(camera.combined);
            formas.setProjectionMatrix(camera.combined);
            ultimaLargura = larguraTela;
            ultimaAltura = alturaTela;
        }

        tamanhoCelula = Math.min(
            (larguraTela - 52f) / mapa.getColunas(),
            (alturaTela - 124f) / mapa.getLinhas()
        );

        float larguraMapa = mapa.getColunas() * tamanhoCelula;
        float alturaMapa = mapa.getLinhas() * tamanhoCelula;
        deslocamentoX = (larguraTela - larguraMapa) / 2f;
        deslocamentoY = (alturaTela - alturaMapa) / 2f - 2f;
    }

    private void desenharBolinhas(MapaJogo mapa) {
        float raioNormal = tamanhoCelula * 0.14f;
        float raioEspecial = tamanhoCelula * 0.34f;
        float pulso = 0.92f + 0.08f * MathUtils.sin(tempoPulso * 5f);

        for (int linha = 0; linha < mapa.getLinhas(); linha++) {
            for (int coluna = 0; coluna < mapa.getColunas(); coluna++) {
                if (!mapa.temPonto(linha, coluna) || mapa.temFruta(linha, coluna)) {
                    continue;
                }

                float x = xDaColuna(coluna) + tamanhoCelula / 2f;
                float y = yDaLinha(linha, mapa.getLinhas()) + tamanhoCelula / 2f;
                char item = mapa.getCelula(linha, coluna);

                if (item == 'p') {
                    // Bolinha vermelha especial, com halo de brilho.
                    formas.setColor(1f, 0.04f, 0.12f, 0.20f);
                    formas.circle(x, y, raioEspecial * 1.65f * pulso);
                    formas.setColor(1f, 0.06f, 0.14f, 1f);
                    formas.circle(x, y, raioEspecial * pulso);
                    formas.setColor(1f, 0.58f, 0.62f, 1f);
                    formas.circle(x - raioEspecial * 0.22f,
                        y + raioEspecial * 0.22f, raioEspecial * 0.24f);
                } else if (item == 'o') {
                    formas.setColor(1f, 0.78f, 0.18f, 1f);
                    formas.circle(x, y, raioEspecial * pulso);
                } else {
                    formas.setColor(1f, 0.91f, 0.52f, 1f);
                    formas.circle(x, y, raioNormal);
                }
            }
        }
    }

    private void desenharAuraMonstro(MapaJogo mapa, Pacman pacman) {
        if (pacman == null || !pacman.isVivo() || !pacman.isModoMonstro()) {
            return;
        }
        float x = deslocamentoX + pacman.getPosicaoXEmCelulas() * tamanhoCelula;
        float y = deslocamentoY
            + (mapa.getLinhas() - pacman.getPosicaoYEmCelulas()) * tamanhoCelula;
        float pulsacao = 1f + 0.10f * MathUtils.sin(tempoPulso * 10f);
        formas.setColor(1f, 0.02f, 0.10f, 0.18f);
        formas.circle(x, y, tamanhoCelula * 1.8f * pulsacao);
        formas.setColor(1f, 0.18f, 0.12f, 0.12f);
        formas.circle(x, y, tamanhoCelula * 1.35f * pulsacao);
    }

    private void desenharEstrelasFantasmas(MapaJogo mapa, Array<Fantasma> fantasmas) {
        if (fantasmas == null) return;
        for (Fantasma fantasma : fantasmas) {
            if (!fantasma.isAtivo() || !fantasma.isAtordoado()) continue;
            float x = xDaColuna(fantasma.getColuna()) + tamanhoCelula / 2f;
            float y = yDaLinha(fantasma.getLinha(), mapa.getLinhas())
                + tamanhoCelula / 2f;
            float onda = MathUtils.sin(tempoPulso * 15f) * tamanhoCelula * 0.12f;
            formas.setColor(1f, 0.92f, 0.30f, 1f);
            formas.circle(x - tamanhoCelula * 0.90f, y + tamanhoCelula * 0.90f + onda,
                tamanhoCelula * 0.10f);
            formas.circle(x + tamanhoCelula * 0.90f, y + tamanhoCelula * 0.90f - onda,
                tamanhoCelula * 0.10f);
            formas.setColor(0.65f, 0.90f, 1f, 1f);
            formas.circle(x, y + tamanhoCelula * 1.10f, tamanhoCelula * 0.08f);
        }
    }

    private void desenharCerejas(MapaJogo mapa) {
        for (int linha = 0; linha < mapa.getLinhas(); linha++) {
            for (int coluna = 0; coluna < mapa.getColunas(); coluna++) {
                if (!mapa.temFruta(linha, coluna)) continue;

                float tamanho = tamanhoCelula * 1.40f;
                float centroX = xDaColuna(coluna) + tamanhoCelula / 2f;
                float centroY = yDaLinha(linha, mapa.getLinhas()) + tamanhoCelula / 2f;
                batch.draw(texturaCereja,
                    centroX - tamanho / 2f, centroY - tamanho / 2f,
                    tamanho, tamanho);
            }
        }
    }

    private void desenharPacman(MapaJogo mapa, Pacman pacman) {
        if (pacman == null || !pacman.isVivo()) return;

        float centroX = deslocamentoX
            + pacman.getPosicaoXEmCelulas() * tamanhoCelula;
        float centroY = deslocamentoY
            + (mapa.getLinhas() - pacman.getPosicaoYEmCelulas()) * tamanhoCelula;
        float tamanho = tamanhoCelula * ESCALA_PACMAN;
        int direcao=indiceDirecao(pacman.getOrientacao());
        TextureRegion quadro = pacman.isModoMonstro()
            ? quadrosPacmanMonstro[direcao][framePacman]
            : quadrosPacman[direcao][framePacman];

        batch.setColor(Color.WHITE);
        batch.draw(
            quadro,
            centroX - tamanho / 2f,
            centroY - tamanho / 2f,
            tamanho / 2f,
            tamanho / 2f,
            tamanho,
            tamanho,
            1f,
            1f,
            0f
        );
    }


    /** Chapéu se desprende da posicao ONDE a bolinha vermelha foi comida.
     * Nao acompanha o Pac-Man enquanto ele foge ou corre no modo monstro. */
    private void desenharChapeuCaindo(MapaJogo mapa, Pacman pacman) {
        float t=pacman.getProgressoQuedaChapeu();
        if(t<0f || !pacman.isModoMonstro() || !pacman.isVivo())return;
        float centroX=deslocamentoX+pacman.getChapeuXEmCelulas()*tamanhoCelula;
        float centroY=deslocamentoY+(mapa.getLinhas()-pacman.getChapeuYEmCelulas())*tamanhoCelula;
        float tam=tamanhoCelula*1.14f;
        float x=centroX-tam*.45f+t*tamanhoCelula*.85f;
        float y=centroY+tam*.32f-t*tamanhoCelula*1.55f+MathUtils.sin(t*MathUtils.PI)*tam*.45f;
        batch.setColor(1f,1f,1f,1f-MathUtils.clamp((t-.70f)/.30f,0f,1f));
        batch.draw(regiaoChapeu,x,y,tam/2f,tam/2f,tam,tam,1f,1f,-t*275f);
        batch.setColor(Color.WHITE);
    }

    private int indiceDirecao(Direcao d) {
        if(d==Direcao.ESQUERDA)return 1;
        if(d==Direcao.CIMA)return 2;
        if(d==Direcao.BAIXO)return 3;
        return 0;
    }

    private void atualizarAnimacaoPacman(float delta) {
        tempoAnimacaoPacman += delta;
        float intervalo = 0.085f;
        while (tempoAnimacaoPacman >= intervalo) {
            tempoAnimacaoPacman -= intervalo;
            framePacman = (framePacman + 1) % 4;
        }
    }

    private void desenharFantasmas(MapaJogo mapa, Array<Fantasma> fantasmas) {
        if (fantasmas == null) return;

        for (Fantasma fantasma : fantasmas) {
            if (!fantasma.isAtivo()) continue;
            int indice = fantasma.getIdentificadorCor();
            if (indice < 0 || indice >= quadrosFantasmas.length
                || quadrosFantasmas[indice] == null
                || quadrosFantasmas[indice].length == 0) {
                continue;
            }

            int frame = MathUtils.clamp(fantasma.getFrameAnimacao(), 0, 1);
            TextureRegion quadro = fantasma.isAssustado()
                ? quadrosAssustados[0][frame] : quadrosFantasmas[indice][0][frame];
            float centroX = deslocamentoX + fantasma.getPosicaoXEmCelulas()*tamanhoCelula;
            float centroY = deslocamentoY + (mapa.getLinhas()-fantasma.getPosicaoYEmCelulas())*tamanhoCelula;
            float tamanho = tamanhoCelula * ESCALA_FANTASMA;
            float rotacao = fantasma.isAtordoado()
                ? (3f - fantasma.getTempoAtordoado()) * 420f : 0f;

            if (fantasma.isAtordoado()) {
                batch.setColor(0.72f, 0.88f, 1f, 1f);
            } else if(fantasma.isAssustado()) {
                batch.setColor(.76f,.96f,1f,1f);
            } else {
                batch.setColor(Color.WHITE);
            }

            batch.draw(
                quadro,
                centroX - tamanho / 2f,
                centroY - tamanho / 2f,
                tamanho / 2f,
                tamanho / 2f,
                tamanho,
                tamanho,
                1f,
                1f,
                rotacao
            );
        }
        batch.setColor(Color.WHITE);
    }

    private void desenharInterface(Pontuacao pontos, int vidas,
                                   Pacman pacman, boolean gameOver,
                                   boolean venceu, MapaJogo mapa, String nomeJogador, float tempoPartida) {
        int largura=Gdx.graphics.getWidth();
        int altura=Gdx.graphics.getHeight();
        fonte.setColor(new Color(.98f,.66f,.26f,1f));
        fonte.getData().setScale(1.23f);
        fonte.draw(batch,"PAC-MAN  HALLOWEEN  V5.5",20,altura-19);
        fonte.getData().setScale(1.02f);
        fonte.setColor(Color.WHITE);
        fonte.draw(batch,"PONTOS  "+pontos.getPontos(),20,altura-43);
        fonte.draw(batch,"RECORDE  "+pontos.getRecorde(),220,altura-43);
        fonte.draw(batch,"VIDAS  "+vidas,largura-115,altura-27);
        fonte.setColor(new Color(.83f,.73f,.99f,1f));
        fonte.draw(batch,"JOGADOR  "+nomeJogador+"    TEMPO  "+formatarTempo(tempoPartida),390,altura-43);
        fonte.getData().setScale(.98f);
        fonte.setColor(new Color(.84f,.75f,1f,1f));
        fonte.draw(batch,"SETAS / WASD  |  P: PAUSA  |  BOLINHAS: "+mapa.getBolinhasRestantes(),20,31);
        if(pacman.isModoMonstro()) {
            fonte.setColor(new Color(1f,.38f,.26f,1f));
            fonte.getData().setScale(1.20f);
            fonte.draw(batch,"MODO MONSTRO  "+String.format(java.util.Locale.US,"%.1f",pacman.getTempoModoMonstro())+"s",
                largura-240,31);
        }
        fonte.getData().setScale(1f);
        fonte.setColor(Color.WHITE);
    }

    public static final int TELA_NOME=0;
    public static final int TELA_PAUSA=1;
    public static final int TELA_VITORIA=2;
    public static final int TELA_DERROTA=3;

    private static String formatarTempo(float totalSegundos) {
        int segundos=(int)Math.max(0,totalSegundos);
        return String.format(Locale.US,"%02d:%02d",segundos/60,segundos%60);
    }

    /** UI de menu, pausa e resultado, desenhada dentro do jogo sem dependencias de UI. */
    public void desenharPainel(int tela, String nome, int pontuacao,
                              float tempoSegundos, RankingPartidas ranking) {
        float w=Gdx.graphics.getWidth(),h=Gdx.graphics.getHeight();
        float panelW=Math.min(w-32f,720f), panelH=Math.min(h-96f,735f);
        float x=(w-panelW)/2f, y=(h-panelH)/2f;
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA,GL20.GL_ONE_MINUS_SRC_ALPHA);
        formas.begin(ShapeRenderer.ShapeType.Filled);
        formas.setColor(.035f,.01f,.07f,.87f);
        formas.rect(0,0,w,h);
        formas.setColor(.12f,.07f,.20f,.97f);
        formas.rect(x,y,panelW,panelH);
        formas.setColor(.54f,.23f,.72f,1f);
        formas.rect(x,y+panelH-6f,panelW,6f);
        formas.setColor(1f,.53f,.19f,1f);
        formas.rect(x+22f,y+panelH-13f,panelW-44f,3f);
        formas.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        String titulo=tela==TELA_NOME?"BEM-VINDO AO HALLOWEEN":
            tela==TELA_PAUSA?"JOGO PAUSADO":tela==TELA_VITORIA?"VITORIA!":"FIM DE JOGO";
        batch.begin();
        textoCentral(titulo,w/2f,y+panelH-64f,2.0f,new Color(1f,.68f,.27f,1f));
        if(tela==TELA_NOME) {
            textoCentral("DIGITE SEU NOME PARA COMECAR",w/2f,y+panelH-112f,1.12f,Color.WHITE);
            textoCentral(nome.isEmpty()?"[ SEU NOME ]":"[ "+nome+"_ ]",w/2f,y+panelH-152f,1.65f,
                new Color(.92f,.82f,1f,1f));
            textoCentral("ENTER  -  COMECAR",w/2f,y+panelH-194f,1.15f,
                new Color(.99f,.74f,.36f,1f));
        } else if(tela==TELA_PAUSA) {
            textoCentral("P  -  CONTINUAR",w/2f,y+panelH-116f,1.33f,Color.WHITE);
            textoCentral("O TEMPO E OS FANTASMAS ESTAO PARADOS",w/2f,y+panelH-153f,1.0f,
                new Color(.82f,.75f,.98f,1f));
        } else {
            String resumo="JOGADOR: "+nome+"    PONTOS: "+pontuacao+"    TEMPO: "+formatarTempo(tempoSegundos);
            textoCentral(resumo,w/2f,y+panelH-112f,1.05f,Color.WHITE);
            if(tela==TELA_VITORIA) {
                textoCentral(String.format(Locale.US,"RITMO: %.1f PONTOS / MIN",pontuacao*60f/Math.max(1f,tempoSegundos)),
                    w/2f,y+panelH-150f,1.10f,new Color(.98f,.75f,.34f,1f));
            } else {
                textoCentral("SO VITORIAS ENTRAM NO RANKING",w/2f,y+panelH-150f,1.10f,
                    new Color(.95f,.51f,.56f,1f));
            }
            textoCentral("R  -  NOVA PARTIDA (NOVO NOME)",w/2f,y+panelH-185f,1.0f,Color.WHITE);
        }
        float topoRanking=y+panelH-(tela==TELA_NOME?258f:235f);
        textoCentral("RANKING  -  PARTIDAS VENCIDAS",w/2f,topoRanking,1.38f,
            new Color(1f,.65f,.28f,1f));
        fonte.getData().setScale(.90f);
        fonte.setColor(new Color(.78f,.69f,.88f,1f));
        fonte.draw(batch,"POS    NOME",x+42f,topoRanking-36f);
        fonte.draw(batch,"PONTOS",x+333f,topoRanking-36f);
        fonte.draw(batch,"TEMPO",x+434f,topoRanking-36f);
        fonte.draw(batch,"P/MIN",x+530f,topoRanking-36f);
        List<RankingPartidas.Resultado> top=ranking.melhores(RankingPartidas.LIMITE_VISIVEL);
        if(top.isEmpty()) {
            textoCentral("NINGUEM VENCEU AINDA. SEJA O PRIMEIRO!",w/2f,topoRanking-93f,
                1.0f,new Color(.90f,.84f,1f,1f));
        }
        for(int i=0;i<top.size();i++) {
            RankingPartidas.Resultado r=top.get(i);
            float yy=topoRanking-76f-i*37f;
            fonte.setColor(i==0?new Color(1f,.82f,.29f,1f):
                i==1?new Color(.85f,.88f,.98f,1f):i==2?new Color(.96f,.67f,.49f,1f):Color.WHITE);
            fonte.getData().setScale(1f);
            fonte.draw(batch,String.format(Locale.US,"%02d   %s",i+1,r.nome),x+42f,yy);
            fonte.draw(batch,String.valueOf(r.pontos),x+333f,yy);
            fonte.draw(batch,r.tempoFormatado(),x+434f,yy);
            fonte.draw(batch,String.format(Locale.US,"%.1f",r.pontosPorMinuto()),x+530f,yy);
        }
        fonte.setColor(Color.WHITE);fonte.getData().setScale(1f);
        batch.end();
    }

    private void textoCentral(String texto,float cx,float y,float escala,Color cor) {
        fonte.setColor(cor);
        fonte.getData().setScale(escala);
        fonte.draw(batch,texto,cx-440f,y,880f,Align.center,false);
    }

    private float xDaColuna(int coluna) {
        return deslocamentoX + coluna * tamanhoCelula;
    }

    private float yDaLinha(int linha, int totalLinhas) {
        return deslocamentoY + (totalLinhas - 1 - linha) * tamanhoCelula;
    }

    public void dispose() {
        formas.dispose();
        batch.dispose();
        fonte.dispose();
        if (texturaMapa != null) texturaMapa.dispose();
        if (texturaCereja != null) texturaCereja.dispose();
        if (texturaPacman != null) texturaPacman.dispose();
        if (texturaPacmanMonstro != null) texturaPacmanMonstro.dispose();
        if (texturaChapeu != null) texturaChapeu.dispose();
        if (texturaFantasmaAssustado != null) texturaFantasmaAssustado.dispose();
        for (Texture textura : texturasFantasmas) {
            if (textura != null) textura.dispose();
        }
    }
}

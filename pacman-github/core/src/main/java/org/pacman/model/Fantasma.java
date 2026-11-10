package org.pacman.model;

import org.pacman.ai.EstrategiaPerseguicao;
import java.util.ArrayDeque;

/**
 * V5.5: seis patrulheiros com campo de visao em pixels do mapa e reserva de celula.
 * Fantasmas nao podem ocupar o mesmo bloco nem trocar posicoes no mesmo passo.
 * Quando um esta na frente, o outro aguarda ou contorna em vez de vibrar/colidir.
 */
public class Fantasma {
    public static final int TAMANHO_CELULA_MAPA_PX = 32;
    public static final int RAIO_VISAO_PIXELS = 224; // 7 celulas de 32px
    public static final float TEMPO_RENASCIMENTO_SEGUNDOS = 8f;
    public static final float TEMPO_ATORDOADO_SEGUNDOS = 1f;
    private static final float[] TEMPO_ENTRE_PASSOS = {.205f,.238f,.263f,.195f,.224f,.245f};
    private static final Direcao[] ORDEM = {
        Direcao.CIMA, Direcao.DIREITA, Direcao.BAIXO, Direcao.ESQUERDA
    };

    private final int id;
    private final int inicioLinha;
    private final int inicioColuna;
    private final float intervalo;
    private int linha,coluna;
    private int origemLinha, origemColuna;
    private Direcao direcaoAtual=Direcao.NENHUMA;
    private float tempoMovimento;
    private float tempoAnimacao;
    private int frameAnimacao;
    private int passos;
    private int passosDesvio;
    private float esperaBloqueado;
    private boolean ativo=true;
    private boolean assustado;
    private float tempoAtordoado;
    private float tempoRespawn;

    public Fantasma(int id,int linha,int coluna,float atrasoInicial) {
        this.id=id;
        this.inicioLinha=linha;this.inicioColuna=coluna;
        this.linha=linha;this.coluna=coluna;
        origemLinha=linha;origemColuna=coluna;
        intervalo=TEMPO_ENTRE_PASSOS[Math.floorMod(id,TEMPO_ENTRE_PASSOS.length)];
        // Atrasos nao devem fazer o fantasma saltar varios quadrados logo no inicio.
        tempoMovimento=-Math.max(0f,atrasoInicial);
    }
    public int getLinha(){return linha;}
    public int getColuna(){return coluna;}
    public int getIdentificadorCor(){return id;}
    public int getFrameAnimacao(){return frameAnimacao;}
    public Direcao getDirecaoAtual(){return direcaoAtual;}
    public boolean isAtivo(){return ativo;}
    public boolean isAtordoado(){return tempoAtordoado>0f;}
    public boolean isAssustado(){return ativo && assustado;}
    public float getTempoAtordoado(){return Math.max(0f,tempoAtordoado);}
    public float getTempoRespawn(){return Math.max(0f,tempoRespawn);}

    public float getPosicaoXEmCelulas() {
        float a=Math.max(0f,Math.min(1f,tempoMovimento/intervaloEfetivo()));
        return origemColuna+.5f+(coluna-origemColuna)*a;
    }
    public float getPosicaoYEmCelulas() {
        float a=Math.max(0f,Math.min(1f,tempoMovimento/intervaloEfetivo()));
        return origemLinha+.5f+(linha-origemLinha)*a;
    }

    private float intervaloEfetivo() { return assustado ? intervalo * .77f : intervalo; }

    public void atualizar(float delta,MapaJogo mapa,Pacman pacman,Fantasma[] grupo) {
        if(delta<=0 || mapa==null || pacman==null)return;
        assustado=pacman.isModoMonstro() && pacman.isVivo();
        tempoAnimacao+=delta;
        if(tempoAnimacao>=.14f){ tempoAnimacao%=.14f; frameAnimacao=1-frameAnimacao; }
        if(!ativo) {
            tempoRespawn-=delta;
            if(tempoRespawn<=0) renascer();
            return;
        }
        if(tempoAtordoado>0f) {
            tempoAtordoado=Math.max(0f,tempoAtordoado-delta);
            return;
        }
        tempoMovimento+=delta;
        float passoAtual=intervaloEfetivo();
        if(tempoMovimento < passoAtual)return;
        tempoMovimento %= passoAtual;
        // O proximo bloco so pode ser escolhido quando o anterior terminou.
        origemLinha=linha;origemColuna=coluna;
        Direcao escolhida=escolher(mapa,pacman,grupo);
        if(escolhida!=Direcao.NENHUMA) {
            linha+=escolhida.getDeltaLinha();
            coluna+=escolhida.getDeltaColuna();
            direcaoAtual=escolhida;
            passos++;
            esperaBloqueado=0f;
        } else {
            esperaBloqueado+=intervalo;
        }
    }

    private Direcao escolher(MapaJogo mapa,Pacman pacman,Fantasma[] grupo) {
        // Modo monstro: todos fogem pelo grafo do labirinto, inclusive os que
        // estavam longe demais para enxergar o Pac-Man quando patrulhavam.
        if (assustado) return escolherFuga(mapa,pacman,grupo);
        // Um fantasma que desviou de outro termina seu desvio antes de
        // recalcular a perseguicao, evitando pingue-pongue no mesmo corredor.
        if(passosDesvio>0) {
            passosDesvio--;
            if(disponivel(mapa,grupo,direcaoAtual))return direcaoAtual;
            Direcao desvio=escolherDesvio(mapa,grupo);
            if(desvio!=Direcao.NENHUMA)return desvio;
        }
        Direcao perseguir=Direcao.NENHUMA;
        if(pacman.isVivo()) {
            float dx=(pacman.getPosicaoXEmCelulas()-(coluna+.5f))*TAMANHO_CELULA_MAPA_PX;
            float dy=(pacman.getPosicaoYEmCelulas()-(linha+.5f))*TAMANHO_CELULA_MAPA_PX;
            if(dx*dx+dy*dy <= RAIO_VISAO_PIXELS*RAIO_VISAO_PIXELS) {
                perseguir=EstrategiaPerseguicao.proximoPassoLimitado(
                    mapa,linha,coluna,pacman.getLinha(),pacman.getColuna(),id,
                    RAIO_VISAO_PIXELS/TAMANHO_CELULA_MAPA_PX
                );
            }
        }
        if(perseguir!=Direcao.NENHUMA) {
            if(disponivel(mapa,grupo,perseguir))return perseguir;
            // Nao faz uma troca abrupta de direcao repetidamente quando ha fila.
            // Depois de 2 intervalos bloqueado, busca passagem lateral antes de recuar.
            if(esperaBloqueado < .35f) return Direcao.NENHUMA;
            Direcao desvio=escolherDesvio(mapa,grupo);
            if(desvio!=Direcao.NENHUMA) {
                passosDesvio=4;
                return desvio;
            }
            return Direcao.NENHUMA;
        }
        // Patrulha estavel: continua em frente, vira em cruzamento ou da meia-volta
        // somente no beco sem saida. A ordem de curvas e diferente para cada cor.
        if (disponivel(mapa,grupo,direcaoAtual) &&
            (passos%5!=id%5 || perseguir!=Direcao.NENHUMA))return direcaoAtual;
        Direcao oposta=oposta(direcaoAtual);
        for(int i=0;i<4;i++) {
            Direcao d=ORDEM[(i+id+passos/4)%4];
            if(d!=oposta && disponivel(mapa,grupo,d))return d;
        }
        if(disponivel(mapa,grupo,oposta))return oposta;
        return Direcao.NENHUMA;
    }

    /** Fuga: prefere aumentar a distancia real por corredores, sem atravessar paredes.
     * Reserva as celulas como na patrulha e evita meia-volta se houver alternativa. */
    private Direcao escolherFuga(MapaJogo mapa,Pacman pacman,Fantasma[] grupo) {
        int h=mapa.getLinhas(), w=mapa.getColunas();
        int[][] distancia=new int[h][w];
        for(int r=0;r<h;r++)java.util.Arrays.fill(distancia[r],-1);
        int origemR=pacman.getLinha(),origemC=pacman.getColuna();
        ArrayDeque<Integer> fila=new ArrayDeque<Integer>();
        if(!mapa.ehParede(origemR,origemC)) {
            distancia[origemR][origemC]=0;
            fila.add(origemR*w+origemC);
        }
        while(!fila.isEmpty()) {
            int k=fila.removeFirst(),r=k/w,c=k%w;
            for(Direcao d:ORDEM)if(mapa.podeMover(r,c,d)) {
                int nr=r+d.getDeltaLinha(),nc=c+d.getDeltaColuna();
                if(distancia[nr][nc]<0){distancia[nr][nc]=distancia[r][c]+1;fila.add(nr*w+nc);}
            }
        }
        Direcao melhor=Direcao.NENHUMA;
        float pontuacaoMelhor=Float.NEGATIVE_INFINITY;
        for(int i=0;i<4;i++){
            Direcao d=ORDEM[(i+id+passos/7)%4];
            if(!disponivel(mapa,grupo,d))continue;
            int nr=linha+d.getDeltaLinha(),nc=coluna+d.getDeltaColuna();
            float valor=distancia[nr][nc]*10f;
            if(d==direcaoAtual)valor+=2.5f;
            if(d==oposta(direcaoAtual))valor-=2f;
            int saidas=0;
            for(Direcao s:ORDEM)if(mapa.podeMover(nr,nc,s))saidas++;
            if(saidas==1)valor-=1f;
            if(valor>pontuacaoMelhor){pontuacaoMelhor=valor;melhor=d;}
        }
        return melhor;
    }

    private Direcao escolherDesvio(MapaJogo mapa,Fantasma[] grupo) {
        Direcao contrario=oposta(direcaoAtual);
        for(int i=0;i<4;i++) {
            Direcao d=ORDEM[(i+id+passos/3)%4];
            if(d!=contrario && d!=direcaoAtual && disponivel(mapa,grupo,d))return d;
        }
        if(disponivel(mapa,grupo,contrario))return contrario;
        return Direcao.NENHUMA;
    }

    private boolean disponivel(MapaJogo mapa,Fantasma[] grupo,Direcao d) {
        if(d==Direcao.NENHUMA || !mapa.podeMover(linha,coluna,d)) return false;
        int nr=linha+d.getDeltaLinha(),nc=coluna+d.getDeltaColuna();
        if(grupo!=null)for(Fantasma outro:grupo) {
            if(outro!=null && outro!=this && outro.ativo &&
               ((outro.linha==nr && outro.coluna==nc) ||
                (outro.origemLinha==nr && outro.origemColuna==nc &&
                 (outro.linha!=outro.origemLinha || outro.coluna!=outro.origemColuna) &&
                 outro.tempoMovimento<outro.intervaloEfetivo()*.85f))) return false;
        }
        return true;
    }
    private static Direcao oposta(Direcao d) {
        if(d==Direcao.CIMA)return Direcao.BAIXO;
        if(d==Direcao.BAIXO)return Direcao.CIMA;
        if(d==Direcao.ESQUERDA)return Direcao.DIREITA;
        if(d==Direcao.DIREITA)return Direcao.ESQUERDA;
        return Direcao.NENHUMA;
    }
    public void atordoar() { if(ativo)tempoAtordoado=TEMPO_ATORDOADO_SEGUNDOS; }
    public void comer() {
        if(!ativo)return;
        ativo=false; tempoAtordoado=0; tempoRespawn=TEMPO_RENASCIMENTO_SEGUNDOS;
        tempoMovimento=0f;
    }
    public void setPosicao(int novaLinha,int novaColuna) {
        linha=novaLinha;coluna=novaColuna;
        origemLinha=linha;origemColuna=coluna;
        tempoMovimento=0f;
        assustado=false;
        esperaBloqueado=0f;
        passosDesvio=0;
    }
    private void renascer() {
        ativo=true;
        linha=inicioLinha;coluna=inicioColuna;
        origemLinha=linha;origemColuna=coluna;
        direcaoAtual=Direcao.NENHUMA;
        tempoRespawn=0f;
        tempoMovimento=0f;
        tempoAtordoado=0f;
        assustado=false;
        esperaBloqueado=0f;
        passosDesvio=0;
    }
}

package org.pacman.model;

/**
 * Movimento V5: posicao exata por celula + interpolacao entre dois centros.
 * NUNCA corta uma curva no meio do corredor; a curva acontece no centro.
 * Nao depende de teste de cor de pixel nem do tamanho visual do sprite.
 */
public class Pacman {
    private static final float VELOCIDADE_CELULAS_SEGUNDO = 6.35f;
    private static final float EPS = 0.00001f;

    private final int linhaInicial;
    private final int colunaInicial;
    private int linha;
    private int coluna;
    private float progresso;
    private Direcao atual = Direcao.NENHUMA;
    private Direcao desejada = Direcao.NENHUMA;
    private Direcao orientacao = Direcao.DIREITA;
    private boolean vivo = true;
    private float tempoMonstro;
    private float tempoChapeu;
    private float chapeuX, chapeuY;
    public static final float DURACAO_QUEDA_CHAPEU = 0.95f;

    public Pacman(int linha, int coluna) {
        linhaInicial = linha;
        colunaInicial = coluna;
        this.linha = linha;
        this.coluna = coluna;
    }

    public int getLinha() {
        return progresso >= .5f ? linha + atual.getDeltaLinha() : linha;
    }
    public int getColuna() {
        return progresso >= .5f ? coluna + atual.getDeltaColuna() : coluna;
    }
    public float getPosicaoXEmCelulas() {
        return coluna + .5f + atual.getDeltaColuna() * progresso;
    }
    public float getPosicaoYEmCelulas() {
        return linha + .5f + atual.getDeltaLinha() * progresso;
    }
    public Direcao getDirecaoAtual() { return atual; }
    public Direcao getOrientacao() { return orientacao; }
    public boolean isVivo() { return vivo; }
    public boolean isModoMonstro() { return tempoMonstro > 0; }
    public float getTempoModoMonstro() { return Math.max(0f,tempoMonstro); }
    public void ativarModoMonstro(float segundos) {
        if (tempoMonstro <= 0f) {
            chapeuX = getPosicaoXEmCelulas();
            chapeuY = getPosicaoYEmCelulas();
            tempoChapeu = DURACAO_QUEDA_CHAPEU;
        }
        tempoMonstro = Math.max(tempoMonstro, segundos);
    }
    public float getProgressoQuedaChapeu() {
        return tempoChapeu > 0f ? 1f - tempoChapeu/DURACAO_QUEDA_CHAPEU : -1f;
    }
    public float getChapeuXEmCelulas() { return chapeuX; }
    public float getChapeuYEmCelulas() { return chapeuY; }
    public void desativarModoMonstro() { tempoMonstro=0f; }

    public void setDirecaoDesejada(Direcao direcao) {
        if(direcao != null && direcao != Direcao.NENHUMA) desejada=direcao;
    }

    public void atualizar(float delta, MapaJogo mapa) {
        if(!vivo || mapa == null || delta<=0f) return;
        tempoMonstro = Math.max(0f,tempoMonstro-delta);
        tempoChapeu = Math.max(0f,tempoChapeu-delta);
        float restante = Math.min(delta,.12f)*VELOCIDADE_CELULAS_SEGUNDO;
        int protecao=0;
        while(restante>EPS && protecao++<16) {
            if(progresso <= EPS) {
                progresso=0f;
                // Buffer de curva: aplica somente se houver realmente ligacao.
                if (mapa.podeMover(linha,coluna,desejada)) atual=desejada;
                if(!mapa.podeMover(linha,coluna,atual)) {
                    atual=Direcao.NENHUMA;
                    break; // centro exato da ultima celula valida: sem travar
                }
            } else if (desejada == oposta(atual)) {
                // Inversao exata com manutencao da coordenada interpolada.
                linha += atual.getDeltaLinha();
                coluna += atual.getDeltaColuna();
                progresso = 1f-progresso;
                atual=desejada;
            }
            orientacao=atual;
            float avancar=Math.min(restante,1f-progresso);
            progresso+=avancar;
            restante-=avancar;
            if (progresso >= 1f-EPS) {
                linha += atual.getDeltaLinha();
                coluna += atual.getDeltaColuna();
                progresso=0f;
            } else break;
        }
    }

    private static Direcao oposta(Direcao d) {
        if(d==Direcao.CIMA)return Direcao.BAIXO;
        if(d==Direcao.BAIXO)return Direcao.CIMA;
        if(d==Direcao.ESQUERDA)return Direcao.DIREITA;
        if(d==Direcao.DIREITA)return Direcao.ESQUERDA;
        return Direcao.NENHUMA;
    }

    public void morrer() {
        vivo=false;
        tempoMonstro=0f;
        tempoChapeu=0f;
        atual=Direcao.NENHUMA;
        desejada=Direcao.NENHUMA;
        progresso=0f;
    }
    public void renascer() {
        linha=linhaInicial;
        coluna=colunaInicial;
        atual=Direcao.NENHUMA;
        desejada=Direcao.NENHUMA;
        orientacao=Direcao.DIREITA;
        progresso=0f;
        vivo=true;
        tempoMonstro=0f;
        tempoChapeu=0f;
    }
}

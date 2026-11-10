package org.pacman.ai;

import org.pacman.model.Direcao;
import org.pacman.model.MapaJogo;

import java.util.ArrayDeque;

/**
 * Busca em largura (BFS) pela menor rota entre um fantasma e o Pac-Man.
 * O caminho respeita as conexoes do mapa, portanto os fantasmas nao
 * atravessam paredes nem usam areas decorativas como atalhos.
 */
public final class EstrategiaPerseguicao {

    private static final Direcao[] ORDEM_BASE = {
        Direcao.CIMA,
        Direcao.ESQUERDA,
        Direcao.BAIXO,
        Direcao.DIREITA
    };

    private EstrategiaPerseguicao() {
    }

    /**
     * Retorna o primeiro movimento do menor caminho ate o alvo.
     * Cada fantasma recebe uma prioridade de direcoes diferente para que
     * caminhos de mesmo tamanho nao produzam exatamente o mesmo percurso.
     */
    public static Direcao proximoPasso(
        MapaJogo mapa,
        int linhaOrigem,
        int colunaOrigem,
        int linhaAlvo,
        int colunaAlvo,
        int identificadorFantasma
    ) {
        return proximoPassoLimitado(mapa,linhaOrigem,colunaOrigem,linhaAlvo,colunaAlvo,identificadorFantasma,Integer.MAX_VALUE);
    }

    public static Direcao proximoPassoLimitado(
        MapaJogo mapa,int linhaOrigem,int colunaOrigem,int linhaAlvo,int colunaAlvo,
        int identificadorFantasma,int maxPassos
    ) {
        if (mapa == null
            || linhaOrigem == linhaAlvo && colunaOrigem == colunaAlvo
            || mapa.ehParede(linhaOrigem, colunaOrigem)
            || mapa.ehParede(linhaAlvo, colunaAlvo)) {
            return Direcao.NENHUMA;
        }

        int linhas = mapa.getLinhas();
        int colunas = mapa.getColunas();

        boolean[][] visitado = new boolean[linhas][colunas];
        Direcao[][] primeiroPasso = new Direcao[linhas][colunas];
        int[][] distancia = new int[linhas][colunas];
        ArrayDeque<Integer> fila = new ArrayDeque<Integer>();

        fila.add(Integer.valueOf(linhaOrigem * colunas + colunaOrigem));
        visitado[linhaOrigem][colunaOrigem] = true;

        int prioridade = Math.floorMod(identificadorFantasma, ORDEM_BASE.length);

        while (!fila.isEmpty()) {
            int indice = fila.removeFirst().intValue();
            int linha = indice / colunas;
            int coluna = indice % colunas;
            if(distancia[linha][coluna]>=maxPassos)continue;

            for (int i = 0; i < ORDEM_BASE.length; i++) {
                Direcao direcao = ORDEM_BASE[(i + prioridade) % ORDEM_BASE.length];

                if (!mapa.podeMover(linha, coluna, direcao)) {
                    continue;
                }

                int novaLinha = linha + direcao.getDeltaLinha();
                int novaColuna = coluna + direcao.getDeltaColuna();

                if (visitado[novaLinha][novaColuna]) {
                    continue;
                }

                visitado[novaLinha][novaColuna] = true;
                distancia[novaLinha][novaColuna]=distancia[linha][coluna]+1;
                primeiroPasso[novaLinha][novaColuna] =
                    linha == linhaOrigem && coluna == colunaOrigem
                        ? direcao
                        : primeiroPasso[linha][coluna];

                if (novaLinha == linhaAlvo && novaColuna == colunaAlvo) {
                    return primeiroPasso[novaLinha][novaColuna];
                }

                fila.add(Integer.valueOf(novaLinha * colunas + novaColuna));
            }
        }

        return Direcao.NENHUMA;
    }
}

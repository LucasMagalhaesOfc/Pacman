package org.pacman.model;

/**
 * Mapa logico do jogo. As celulas representam o piso percorrivel e a matriz
 * de ligacoes representa as passagens reais entre celulas vizinhas.
 */
public class MapaJogo {

    private final char[][] celulas;
    private final int linhaInicialPacman;
    private final int colunaInicialPacman;
    private final int linhaInicialFantasma;
    private final int colunaInicialFantasma;
    private final boolean[][][] movimentosPermitidos;
    private final int[][] posicoesFantasmas;

    public MapaJogo(char[][] celulas, int linhaInicialPacman,
                    int colunaInicialPacman) {
        this(celulas, linhaInicialPacman, colunaInicialPacman, -1, -1, null);
    }

    public MapaJogo(char[][] celulas,
                    int linhaInicialPacman,
                    int colunaInicialPacman,
                    int linhaInicialFantasma,
                    int colunaInicialFantasma) {
        this(celulas, linhaInicialPacman, colunaInicialPacman,
            linhaInicialFantasma, colunaInicialFantasma, null);
    }

    public MapaJogo(char[][] celulas,
                    int linhaInicialPacman,
                    int colunaInicialPacman,
                    int linhaInicialFantasma,
                    int colunaInicialFantasma,
                    boolean[][][] movimentosPermitidos) {
        if (celulas == null || celulas.length == 0 || celulas[0].length == 0) {
            throw new IllegalArgumentException("O mapa nao pode estar vazio.");
        }
        this.celulas = celulas;
        this.linhaInicialPacman = linhaInicialPacman;
        this.colunaInicialPacman = colunaInicialPacman;
        this.linhaInicialFantasma = linhaInicialFantasma;
        this.colunaInicialFantasma = colunaInicialFantasma;
        this.movimentosPermitidos = movimentosPermitidos;
        this.posicoesFantasmas = null;
    }

    /** Construtor da V5: o mapa de texto especifica os quatro spawns. */
    public MapaJogo(char[][] celulas, int inicioLinha, int inicioColuna,
                    int[][] posicoesFantasmas) {
        if (celulas == null || celulas.length == 0 || celulas[0].length == 0) {
            throw new IllegalArgumentException("Mapa V5 vazio");
        }
        this.celulas = celulas;
        this.linhaInicialPacman = inicioLinha;
        this.colunaInicialPacman = inicioColuna;
        this.posicoesFantasmas = posicoesFantasmas;
        this.linhaInicialFantasma = posicoesFantasmas[0][0];
        this.colunaInicialFantasma = posicoesFantasmas[0][1];
        this.movimentosPermitidos = null; // vizinhos adjacentes na mesma grade
    }

    public int getQuantidadeFantasmas() {
        return posicoesFantasmas == null ? 1 : posicoesFantasmas.length;
    }

    public int[] getSpawnFantasma(int indice) {
        if (posicoesFantasmas != null && indice >= 0 && indice < posicoesFantasmas.length) {
            return new int[]{posicoesFantasmas[indice][0], posicoesFantasmas[indice][1]};
        }
        return new int[]{linhaInicialFantasma, colunaInicialFantasma};
    }

    /** Apenas bolinhas (normais, grandes e vermelhas): cereja e bonus opcional. */
    public int getBolinhasRestantes() {
        int total=0;
        for(int r=0;r<getLinhas();r++)for(int c=0;c<getColunas();c++) {
            char item=celulas[r][c];
            if(item=='.'||item=='o'||item=='p') total++;
        }
        return total;
    }

    public int getColetaveisRestantes() {
        int total = 0;
        for(int r=0; r<getLinhas();r++)
            for(int c=0;c<getColunas();c++)
                if(temPonto(r,c)) total++;
        return total;
    }

    public int getLinhas() {
        return celulas.length;
    }

    public int getColunas() {
        return celulas[0].length;
    }

    public int getLinhaInicialPacman() {
        return linhaInicialPacman;
    }

    public int getColunaInicialPacman() {
        return colunaInicialPacman;
    }

    public int getLinhaInicialFantasma() {
        return linhaInicialFantasma;
    }

    public int getColunaInicialFantasma() {
        return colunaInicialFantasma;
    }

    private boolean foraDoMapa(int linha, int coluna) {
        return linha < 0 || linha >= getLinhas()
            || coluna < 0 || coluna >= getColunas();
    }

    public boolean ehParede(int linha, int coluna) {
        return foraDoMapa(linha, coluna) || celulas[linha][coluna] == '#';
    }

    /** Confere a ligacao entre duas celulas; nao permite atalhos diagonais. */
    public boolean podeMover(int linha, int coluna, Direcao direcao) {
        if (direcao == null || direcao == Direcao.NENHUMA
            || ehParede(linha, coluna)) {
            return false;
        }

        int novaLinha = linha + direcao.getDeltaLinha();
        int novaColuna = coluna + direcao.getDeltaColuna();
        if (ehParede(novaLinha, novaColuna)) {
            return false;
        }

        if (movimentosPermitidos == null) {
            return true;
        }

        int indice;
        switch (direcao) {
            case CIMA: indice = 0; break;
            case BAIXO: indice = 1; break;
            case ESQUERDA: indice = 2; break;
            case DIREITA: indice = 3; break;
            default: return false;
        }

        return movimentosPermitidos[linha][coluna][indice];
    }

    public char getCelula(int linha, int coluna) {
        return foraDoMapa(linha, coluna) ? '#' : celulas[linha][coluna];
    }

    public boolean temPonto(int linha, int coluna) {
        if (foraDoMapa(linha, coluna)) {
            return false;
        }
        char c = celulas[linha][coluna];
        return c == '.' || c == 'o' || c == 'F' || c == 'p';
    }

    public boolean temFruta(int linha, int coluna) {
        return !foraDoMapa(linha, coluna) && celulas[linha][coluna] == 'F';
    }

    public boolean temPoder(int linha, int coluna) {
        return !foraDoMapa(linha, coluna) && celulas[linha][coluna] == 'p';
    }

    public void removerPonto(int linha, int coluna) {
        if (temPonto(linha, coluna)) {
            celulas[linha][coluna] = ' ';
        }
    }

    public int pontosDoItem(int linha, int coluna) {
        if (foraDoMapa(linha, coluna)) {
            return 0;
        }
        switch (celulas[linha][coluna]) {
            case '.': return 10;
            case 'o': return 50;
            case 'p': return 100;
            case 'F': return 100;
            default: return 0;
        }
    }
}

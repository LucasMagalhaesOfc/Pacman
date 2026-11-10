package org.pacman.util;

import java.util.ArrayDeque;
import org.pacman.model.Direcao;
import org.pacman.model.MapaJogo;

/**
 * V5: sem leitura ambigua de PNG/pixel e sem esqueleto Zhang-Suen.
 * Geometria, coletaveis e spawns sao definidos somente no arquivo TXT.
 * O PNG e decorativo e nao consegue bloquear o personagem.
 */
public final class CarregadorMapa {
    private CarregadorMapa() {}

    /** P=jogador; R/L/V/A/S/D=seis fantasmas; #=parede, .=bolinha, p=poder, F=fruta. */
    public static MapaJogo carregarV5(String texto) {
        if(texto==null)throw new IllegalArgumentException("Mapa V5 nulo");
        String[] linhas=texto.replace("\r","").trim().split("\n");
        if(linhas.length<5)throw new IllegalArgumentException("Mapa V5 pequeno");
        int largura=linhas[0].length();
        char[][] celulas=new char[linhas.length][largura];
        int linhaPac=-1,colPac=-1;
        int[][] spawns={{-1,-1},{-1,-1},{-1,-1},{-1,-1},{-1,-1},{-1,-1}};
        for(int r=0;r<linhas.length;r++) {
            if(linhas[r].length()!=largura)
                throw new IllegalArgumentException("Largura diferente na linha "+r);
            for(int c=0;c<largura;c++) {
                char ch=linhas[r].charAt(c);
                if(ch=='P') {
                    if(linhaPac>=0)throw new IllegalArgumentException("Dois Pac-Man no mapa");
                    linhaPac=r;colPac=c;ch=' ';
                }
                int id=ch=='R'?0:ch=='L'?1:ch=='V'?2:ch=='A'?3:ch=='S'?4:ch=='D'?5:-1;
                if(id>=0) {
                    if(spawns[id][0]>=0)throw new IllegalArgumentException("Fantasma repetido: "+id);
                    spawns[id][0]=r;spawns[id][1]=c;ch=' ';
                }
                if("#.pFo ".indexOf(ch)<0)
                    throw new IllegalArgumentException("Simbolo invalido no mapa: '"+ch+"'");
                celulas[r][c]=ch;
            }
        }
        if(linhaPac<0)throw new IllegalArgumentException("Falta P no mapa");
        for(int i=0;i<6;i++)
            if(spawns[i][0]<0)throw new IllegalArgumentException("Falta fantasma "+i);
        MapaJogo mapa=new MapaJogo(celulas,linhaPac,colPac,spawns);

        // Garantia de um unico corredor de largura, sem multiplas fileiras.
        for(int r=0;r<mapa.getLinhas()-1;r++)
            for(int c=0;c<mapa.getColunas()-1;c++)
                if(!mapa.ehParede(r,c) && !mapa.ehParede(r+1,c)
                    && !mapa.ehParede(r,c+1) && !mapa.ehParede(r+1,c+1))
                    throw new IllegalArgumentException("Corredor com largura dupla em "+r+","+c);

        // Garante que cada ponto, power-up e spawn seja alcancavel do Pac-Man.
        boolean[][] visitado=new boolean[mapa.getLinhas()][mapa.getColunas()];
        ArrayDeque<Integer> fila=new ArrayDeque<Integer>();
        fila.add(linhaPac*largura+colPac);visitado[linhaPac][colPac]=true;
        Direcao[] direcoes={Direcao.CIMA,Direcao.BAIXO,Direcao.ESQUERDA,Direcao.DIREITA};
        while(!fila.isEmpty()) {
            int n=fila.removeFirst(),r=n/largura,c=n%largura;
            for(Direcao d:direcoes)if(mapa.podeMover(r,c,d)) {
                int nr=r+d.getDeltaLinha(),nc=c+d.getDeltaColuna();
                if(!visitado[nr][nc]) {
                    visitado[nr][nc]=true;
                    fila.addLast(nr*largura+nc);
                }
            }
        }
        for(int r=0;r<mapa.getLinhas();r++)
            for(int c=0;c<mapa.getColunas();c++)
                if(!mapa.ehParede(r,c) && !visitado[r][c])
                    throw new IllegalArgumentException("Celula isolada em "+r+","+c);
        return mapa;
    }

    /** Compatibilidade com um TXT antigo contendo P e G. */
    public static MapaJogo carregar(String texto) {
        String[] linhas=texto.trim().split("\\R");
        char[][] celulas=new char[linhas.length][linhas[0].length()];
        int pr=0,pc=0,gr=-1,gc=-1;
        for(int r=0;r<linhas.length;r++) {
            if(linhas[r].length()!=celulas[r].length)
                throw new IllegalArgumentException("Linha irregular: "+r);
            for(int c=0;c<linhas[r].length();c++) {
                char ch=linhas[r].charAt(c);
                if(ch=='P'){pr=r;pc=c;ch=' ';}
                if(ch=='G'){gr=r;gc=c;ch=' ';}
                celulas[r][c]=ch;
            }
        }
        return new MapaJogo(celulas,pr,pc,gr,gc);
    }
}

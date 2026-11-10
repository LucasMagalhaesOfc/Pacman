import java.io.*;
import java.nio.file.*;
import java.util.*;
import org.pacman.model.*;
import org.pacman.ai.*;
import org.pacman.util.CarregadorMapa;

/** Sem LibGDX: testes de regressao para geometria, movimento e fantasmas. */
public class V5SmokeTests {
    static Direcao[] dirs={Direcao.CIMA, Direcao.BAIXO, Direcao.ESQUERDA, Direcao.DIREITA};
    static void ok(boolean condition,String message) { if(!condition)throw new AssertionError(message); }
    static MapaJogo mapa;
    static int[][] spawns=new int[4][2];
    static int tests=0;
    public static void main(String[] args)throws Exception {
        String raw=new String(Files.readAllBytes(Paths.get(args[0])),"UTF-8").trim();
        String[] lines=raw.split("\\R");
        int h=lines.length,w=lines[0].length();char[][] cells=new char[h][w];
        int rp=-1,cp=-1,pellets=0;
        for(int r=0;r<h;r++){
            ok(lines[r].length()==w,"linha irregular "+r);
            for(int c=0;c<w;c++){
                char ch=lines[r].charAt(c);
                if(ch=='P'){rp=r;cp=c;ch=' ';}
                int id= ch=='R'?0:ch=='L'?1:ch=='V'?2:ch=='A'?3:-1;
                if(id>=0){spawns[id]=new int[]{r,c};ch=' ';}
                if(ch=='.'||ch=='p'||ch=='F')pellets++;
                cells[r][c]=ch;
            }
        }
        mapa=CarregadorMapa.carregarV5(raw);
        ok(mapa.getLinhaInicialPacman()==rp && mapa.getColunaInicialPacman()==cp,
            "parser V5 alterou spawn do Pac-Man");
        for(int i=0;i<4;i++)ok(Arrays.equals(mapa.getSpawnFantasma(i),spawns[i]),
            "parser V5 alterou spawn do fantasma "+i);
        ok(w==25&&h==29,"dimensoes incorretas");
        ok(pellets>=120,"poucos coletaveis");
        tests++;
        // Em nenhum lugar pode existir piso 2x2: uma unica fileira por corredor.
        for(int r=0;r<h-1;r++)for(int c=0;c<w-1;c++){
            int livres=0;
            for(int dy=0;dy<2;dy++)for(int dx=0;dx<2;dx++)
                if(!mapa.ehParede(r+dy,c+dx))livres++;
            ok(livres!=4,"encontrada via larga 2x2 em "+r+","+c);
        }
        tests++;
        // O inicio deve estar ligado a TODAS as casas livres (inclusive os itens).
        Set<Integer> visto=new HashSet<Integer>(); ArrayDeque<Integer> q=new ArrayDeque<Integer>();
        q.add(rp*w+cp);visto.add(rp*w+cp);
        while(!q.isEmpty()){
            int idx=q.remove(),r=idx/w,c=idx%w;
            for(Direcao d:dirs) if(mapa.podeMover(r,c,d)){
                int nr=r+d.getDeltaLinha(),nc=c+d.getDeltaColuna(),k=nr*w+nc;
                if(visto.add(k))q.add(k);
            }
        }
        int livres=0;
        for(int r=0;r<h;r++)for(int c=0;c<w;c++) if(!mapa.ehParede(r,c)){
            livres++;
            ok(visto.contains(r*w+c),"piso desconectado "+r+","+c);
        }
        tests++;
        // Cada parede detem movimento, nova direcao valida inicia na mesma celula.
        int paredesTestadas=0,curvasTestadas=0;
        for(int r=0;r<h;r++)for(int c=0;c<w;c++){
            if(mapa.ehParede(r,c))continue;
            for(Direcao d:dirs){
                if(mapa.podeMover(r,c,d))continue;
                Pacman p=new Pacman(r,c);
                p.setDirecaoDesejada(d); p.atualizar(.12f,mapa);
                ok(Math.abs(p.getPosicaoXEmCelulas()-c-.5f)<.0001f &&
                   Math.abs(p.getPosicaoYEmCelulas()-r-.5f)<.0001f,"penetrou parede");
                for(Direcao valid:dirs)if(mapa.podeMover(r,c,valid)) {
                    p.setDirecaoDesejada(valid);p.atualizar(.03f,mapa);
                    ok(p.getDirecaoAtual()==valid,"travou depois da parede");
                    paredesTestadas++;break;
                }
            }
            // Buffer de curva: solicita curva antes de cruzar o centro, aplica ao chegar.
            for(Direcao entrada:dirs)for(Direcao saida:dirs){
                if(!mapa.podeMover(r,c,entrada)||!mapa.podeMover(r,c,saida))continue;
                if(entrada.getDeltaLinha()*saida.getDeltaLinha()+
                   entrada.getDeltaColuna()*saida.getDeltaColuna()!=0)continue;
                int prevR=r+entrada.getDeltaLinha(),prevC=c+entrada.getDeltaColuna();
                Pacman p=new Pacman(prevR,prevC);
                p.setDirecaoDesejada(oposta(entrada));
                p.atualizar(.115f,mapa); // avanca parte da celula
                p.setDirecaoDesejada(saida);
                for(int i=0;i<20;i++)p.atualizar(.01f,mapa);
                ok(!mapa.ehParede(p.getLinha(),p.getColuna()),"curva gerou parede");
                // se chegou ao centro, virou no sentido solicitado
                ok(p.getOrientacao()==saida,"buffer de curva nao funcionou");
                curvasTestadas++;
            }
        }
        tests++;
        // Reversao exata no meio de corredor: sem teleporte.
        int reversoes=0;
        for(int r=1;r<h-1;r++)for(int c=1;c<w-1;c++)if(!mapa.ehParede(r,c)){
            for(Direcao d:dirs)if(mapa.podeMover(r,c,d)){
                Pacman p=new Pacman(r,c);
                p.setDirecaoDesejada(d);p.atualizar(.065f,mapa);
                float x=p.getPosicaoXEmCelulas(),y=p.getPosicaoYEmCelulas();
                p.setDirecaoDesejada(oposta(d));p.atualizar(.008f,mapa);
                float diff=(float)Math.hypot(x-p.getPosicaoXEmCelulas(),y-p.getPosicaoYEmCelulas());
                ok(diff<.065f,"teleporte na reversao: "+diff);
                reversoes++;
            }
        }
        tests++;
        // Exploracao aleatoria por muitas atualizacoes. NUNCA entra na parede.
        Pacman p=new Pacman(rp,cp);Random rand=new Random(5);
        for(int i=0;i<20000;i++){
            if(i%13==0)p.setDirecaoDesejada(dirs[rand.nextInt(4)]);
            p.atualizar(.016f,mapa);
            ok(!mapa.ehParede(p.getLinha(),p.getColuna()),"Pac-Man preso na parede, frame "+i);
        }
        tests++;
        Fantasma[] ghosts=new Fantasma[4];
        for(int i=0;i<4;i++)ghosts[i]=new Fantasma(i,spawns[i][0],spawns[i][1],i*.10f);
        int[] changes=new int[4];int[] oldR=new int[4],oldC=new int[4];
        for(int i=0;i<4;i++){oldR[i]=ghosts[i].getLinha();oldC[i]=ghosts[i].getColuna();}
        Pacman alvo=new Pacman(rp,cp);
        for(int t=0;t<10000;t++){
            if(t%200==0)alvo.setDirecaoDesejada(dirs[rand.nextInt(4)]);
            alvo.atualizar(.016f,mapa);
            for(Fantasma f:ghosts)f.atualizar(.016f,mapa,alvo,ghosts);
            for(int i=0;i<4;i++){
                Fantasma f=ghosts[i];
                ok(!mapa.ehParede(f.getLinha(),f.getColuna()),"fantasma entrou parede");
                int dist=Math.abs(f.getLinha()-oldR[i])+Math.abs(f.getColuna()-oldC[i]);
                ok(dist<=1,"fantasma teleportou "+dist);
                if(dist>0)changes[i]++;
                oldR[i]=f.getLinha();oldC[i]=f.getColuna();
                for(int j=i+1;j<4;j++)ok(f.getLinha()!=ghosts[j].getLinha()||
                    f.getColuna()!=ghosts[j].getColuna(),"fantasmas se sobrepuseram");
            }
        }
        for(int i=0;i<4;i++)ok(changes[i]>20,"fantasma nao patrulhou: "+i);
        tests++;
        System.out.println("PASSOU: "+tests+" grupos de testes de regressao | "+livres+
            " celulas navegaveis, "+pellets+" itens, "+paredesTestadas+" paredes de teste, "+
            curvasTestadas+" curvas, "+reversoes+" reversoes, 20k frames Pac-Man, "+
            "10k frames de 4 fantasmas (sem sobreposicoes).");
    }
    static Direcao oposta(Direcao d){
        if(d==Direcao.CIMA)return Direcao.BAIXO;
        if(d==Direcao.BAIXO)return Direcao.CIMA;
        if(d==Direcao.ESQUERDA)return Direcao.DIREITA;
        if(d==Direcao.DIREITA)return Direcao.ESQUERDA;
        return Direcao.NENHUMA;
    }
}

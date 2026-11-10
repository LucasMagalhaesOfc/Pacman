import java.io.*;
import java.nio.file.*;
import java.util.*;
import org.pacman.model.*;
import org.pacman.ai.*;
import org.pacman.util.CarregadorMapa;

/** Regressao 5.5: mapa, 6 fantasmas, fuga, transformacao e ranking. */
public class V55SmokeTests {
    static int grupos=0;
    static final Direcao[] DIR={Direcao.CIMA,Direcao.BAIXO,Direcao.ESQUERDA,Direcao.DIREITA};
    static void verificar(boolean b,String nome){if(!b)throw new AssertionError(nome);}
    public static void main(String[] args)throws Exception {
        String texto=new String(Files.readAllBytes(Paths.get(args[0])),"UTF-8");
        MapaJogo mapa=CarregadorMapa.carregarV5(texto);
        verificar(mapa.getQuantidadeFantasmas()==6,"Nao existem seis fantasmas");
        int[][] spawn=new int[6][2];
        for(int i=0;i<6;i++) {
            spawn[i]=mapa.getSpawnFantasma(i);
            verificar(!mapa.ehParede(spawn[i][0],spawn[i][1]),"Fantasma nasce em parede: "+i);
            for(int j=0;j<i;j++)verificar(!Arrays.equals(spawn[i],spawn[j]),"Spawns repetidos");
        }
        grupos++;
        // A mesma geometria V5 deve continuar: corredores de uma fileira.
        for(int r=0;r<mapa.getLinhas()-1;r++)for(int c=0;c<mapa.getColunas()-1;c++)
            verificar(mapa.ehParede(r,c)||mapa.ehParede(r+1,c)||
                mapa.ehParede(r,c+1)||mapa.ehParede(r+1,c+1),"corredor largo");
        grupos++;
        // Todas as celulas e seus pontos precisam ser conectadas.
        Set<Integer> vistos=new HashSet<Integer>();ArrayDeque<Integer> fila=new ArrayDeque<Integer>();
        int w=mapa.getColunas(),pr=mapa.getLinhaInicialPacman(),pc=mapa.getColunaInicialPacman();
        fila.add(pr*w+pc);vistos.add(pr*w+pc);
        while(!fila.isEmpty()){
            int pos=fila.removeFirst(),r=pos/w,c=pos%w;
            for(Direcao d:DIR)if(mapa.podeMover(r,c,d)){
                int p=(r+d.getDeltaLinha())*w+c+d.getDeltaColuna();
                if(vistos.add(p))fila.add(p);
            }
        }
        for(int r=0;r<mapa.getLinhas();r++)for(int c=0;c<w;c++)
            verificar(mapa.ehParede(r,c)||vistos.contains(r*w+c),"area desconectada");
        verificar(mapa.getColetaveisRestantes()>100,"poucos coletaveis");
        grupos++;
        // A queda do chapeu inicia na primeira ativacao; segundo poder prolonga
        // o monstro, mas nao provoca outra queda no meio da transformacao.
        Pacman pacman=new Pacman(pr,pc);
        pacman.ativarModoMonstro(8f);
        verificar(pacman.isModoMonstro(),"nao ativou monstro");
        verificar(Math.abs(pacman.getProgressoQuedaChapeu())<.0001f,"chapeu nao iniciou");
        pacman.atualizar(.11f,mapa);
        verificar(pacman.getProgressoQuedaChapeu()>0,"chapeu nao cai");
        pacman.ativarModoMonstro(8f);
        verificar(pacman.getProgressoQuedaChapeu()>0,"chapeu reiniciou indevidamente");
        for(int i=0;i<80;i++)pacman.atualizar(.02f,mapa);
        verificar(pacman.getProgressoQuedaChapeu()<0,"chapeu nao concluiu a animacao");
        grupos++;
        // Fuga por corredores: um fantasma nao recua em direcao ao monstro
        // se existir fuga livre de alcance maior.
        char[][] via={"#########".toCharArray(),"#       #".toCharArray(),"#########".toCharArray()};
        MapaJogo corredor=new MapaJogo(via,1,1,new int[][]{{1,4}});
        Pacman assustador=new Pacman(1,1);assustador.ativarModoMonstro(8f);
        Fantasma fugitivo=new Fantasma(0,1,4,0);
        Fantasma[] sozinho={fugitivo};
        for(int i=0;i<20;i++)fugitivo.atualizar(.016f,corredor,assustador,sozinho);
        verificar(fugitivo.isAssustado(),"fantasma nao fica com medo");
        verificar(fugitivo.getColuna()>4,"fantasma nao foge do monstro");
        grupos++;
        // Nao sobrepoe os SEIS fantasmas, nao atravessa paredes, nem da saltos.
        Fantasma[] grupo=new Fantasma[6];
        for(int i=0;i<6;i++)grupo[i]=new Fantasma(i,spawn[i][0],spawn[i][1],i*.1f);
        Pacman alvo=new Pacman(pr,pc);
        Random rand=new Random(55); int[] movimentos=new int[6];
        for(int passo=0;passo<12000;passo++){
            if(passo==1700||passo==4500||passo==8000)alvo.ativarModoMonstro(8f);
            if(passo%71==0)alvo.setDirecaoDesejada(DIR[rand.nextInt(4)]);
            alvo.atualizar(.016f,mapa);
            verificar(!mapa.ehParede(alvo.getLinha(),alvo.getColuna()),"Pacman entrou parede");
            int[] olds=new int[6];
            for(int i=0;i<6;i++)olds[i]=grupo[i].getLinha()*w+grupo[i].getColuna();
            for(Fantasma f:grupo)f.atualizar(.016f,mapa,alvo,grupo);
            for(int i=0;i<6;i++){
                Fantasma f=grupo[i];
                verificar(!mapa.ehParede(f.getLinha(),f.getColuna()),"fantasma atravessou parede");
                int antR=olds[i]/w,antC=olds[i]%w;
                int dist=Math.abs(antR-f.getLinha())+Math.abs(antC-f.getColuna());
                verificar(dist<=1,"fantasma teleportou");
                if(dist==1)movimentos[i]++;
                for(int j=i+1;j<6;j++)
                    verificar(f.getLinha()!=grupo[j].getLinha()||f.getColuna()!=grupo[j].getColuna(),"fantasmas se chocaram");
            }
        }
        for(int m:movimentos)verificar(m>20,"fantasma travou completamente");
        grupos++;
        // Ranking: mais pontos por minuto; empate desempata por pontos e tempo.
        RankingPartidas ranking=new RankingPartidas();
        ranking.registrarVitoria("Ana",1500,150f); // 600/min
        ranking.registrarVitoria("Beto",900,60f);  // 900/min, deve ficar em primeiro
        ranking.registrarVitoria("Ceci",2400,300f);// 480/min
        List<RankingPartidas.Resultado> top=ranking.melhores(8);
        verificar("BETO".equals(top.get(0).nome),"ranking deveria priorizar pontos por minuto");
        verificar("ANA".equals(top.get(1).nome),"ranking em ordem incorreta");
        verificar(ranking.quantidade()==3,"deveria registrar tres vitorias");
        String salvo=ranking.serializar();
        RankingPartidas restaurado=RankingPartidas.carregar(salvo+"INCOMPATIVEL\nA|x|123\n");
        verificar(restaurado.quantidade()==3,"persistencia perdeu ranking");
        verificar(restaurado.melhores(1).get(0).nome.equals("BETO"),"persistencia mudou rank");
        verificar(RankingPartidas.normalizarNome("João| 01 ").equals("JOAO 01"),"nome nao sanitizado");
        grupos++;
        System.out.println("PASSOU: "+grupos+" grupos V5.5 | 6 fantasmas | 12.000 quadros | fuga, mapa, rank e transformacao.");
    }
}

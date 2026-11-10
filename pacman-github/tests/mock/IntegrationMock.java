import org.pacman.Main;
import org.pacman.model.*;
import org.pacman.controller.*;
import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.*;
import com.badlogic.gdx.files.*;
import com.badlogic.gdx.graphics.*;
import java.nio.file.*;
import java.lang.reflect.*;
import java.util.*;

public class IntegrationMock {
    static class Pref implements Preferences {
        Map<String,String> vals=new HashMap<String,String>();
        public int getInteger(String k,int d){return Integer.parseInt(vals.getOrDefault(k,""+d));}
        public Preferences putInteger(String k,int v){vals.put(k,""+v);return this;}
        public String getString(String k,String d){return vals.getOrDefault(k,d);}
        public Preferences putString(String k,String v){vals.put(k,v);return this;}
        public void flush(){}
    }
    static class App implements Application {Map<String,Pref> ps=new HashMap<>();public Preferences getPreferences(String n){return ps.computeIfAbsent(n,x->new Pref());}}
    static class FH extends FileHandle {String path;FH(String p){path=p;}public String readString(String charset){try{return new String(java.nio.file.Files.readAllBytes(Paths.get("assets/"+path)),charset);}catch(Exception e){throw new RuntimeException(e);}}}
    static class Assets implements com.badlogic.gdx.Files {public FileHandle internal(String p){return new FH(p);}}
    static class Song implements Music {boolean on;public void setLooping(boolean b){}public void setVolume(float a){}public void play(){on=true;}public void pause(){on=false;}public void dispose(){}}
    static class Snd implements Sound {public long play(){return 0;}public void dispose(){}}
    static class AudioStub implements Audio{public Music newMusic(FileHandle h){return new Song();}public Sound newSound(FileHandle h){return new Snd();}}
    static class Graf implements Graphics {public int getWidth(){return 920;}public int getHeight(){return 980;}public float getDeltaTime(){return .016f;}}
    static class Keys implements Input {InputAdapter listener;public boolean isKeyPressed(int k){return false;}public boolean isKeyJustPressed(int k){return false;}public void setInputProcessor(InputAdapter a){listener=a;}void type(String s){for(char c:s.toCharArray())listener.keyTyped(c);} void press(int key){listener.keyDown(key);listener.keyUp(key);}}
    static class GL implements GL20 {public void glEnable(int i){}public void glDisable(int i){}public void glBlendFunc(int a,int b){}}
    static Object get(Object obj,String name)throws Exception {Field field=obj.getClass().getDeclaredField(name);field.setAccessible(true);return field.get(obj);}
    static void ok(boolean cond,String msg){if(!cond)throw new AssertionError(msg);}
    public static void main(String[] argv)throws Exception {
        App app=new App();Keys keys=new Keys();
        Gdx.app=app;Gdx.input=keys;Gdx.graphics=new Graf();Gdx.files=new Assets();Gdx.audio=new AudioStub();Gdx.gl=new GL();
        Main jogo=new Main();jogo.create();jogo.render();
        ok(get(jogo,"tela").toString().equals("NOME"),"sem tela nome no inicio");
        keys.type("Brunó");keys.press(Input.Keys.ENTER);
        ok(get(jogo,"tela").toString().equals("JOGO"),"ENTER nao inicia");
        jogo.render();jogo.render();
        float tempo=(Float)get(jogo,"tempoPartida");ok(tempo>0,"tempo nao conta");
        Pacman pac=(Pacman)get(jogo,"pacman");pac.ativarModoMonstro(8f);
        float antes=pac.getTempoModoMonstro();
        keys.press(Input.Keys.P);jogo.render();for(int n=0;n<300;n++)jogo.render();
        ok(get(jogo,"tela").toString().equals("PAUSA"),"P nao pausa");
        ok((Float)get(jogo,"tempoPartida")==tempo,"tempo corre na pausa");
        ok(pac.getTempoModoMonstro()==antes,"poder corre na pausa");
        keys.press(Input.Keys.P);jogo.render();
        ok(get(jogo,"tela").toString().equals("JOGO"),"P nao retoma");
        ControladorJogo ctrl=(ControladorJogo)get(jogo,"controlador");
        ctrl.getPontuacao().adicionar(1550);
        MapaJogo m=(MapaJogo)get(jogo,"mapa");
        for(int r=0;r<m.getLinhas();r++)for(int c=0;c<m.getColunas();c++)m.removerPonto(r,c);
        jogo.render();
        ok(get(jogo,"tela").toString().equals("VITORIA"),"sem tela VITORIA");
        RankingPartidas r=(RankingPartidas)get(jogo,"ranking");
        ok(r.quantidade()==1,"deve gravar 1 vitoria");
        for(int n=0;n<100;n++)jogo.render();
        ok(r.quantidade()==1,"rank duplicou vitoria");
        String salvo=app.ps.get("pacman-halloween-v55").getString("ranking-vitorias","");
        ok(salvo.contains("BRUNO|"),"nome nao persistiu");
        keys.press(Input.Keys.R);
        ok(get(jogo,"tela").toString().equals("NOME"),"R nao exige novo nome");
        ok(get(jogo,"nomeDigitado").equals(""),"nome nao resetou");
        // Nova partida com derrota nunca deve registrar ranking.
        keys.type("BIA");keys.press(Input.Keys.ENTER);
        ctrl=(ControladorJogo)get(jogo,"controlador");
        Method perder=ControladorJogo.class.getDeclaredMethod("perderVida");perder.setAccessible(true);
        for(int i=0;i<3;i++)perder.invoke(ctrl);
        jogo.render();
        ok(get(jogo,"tela").toString().equals("DERROTA"),"sem tela derrota");
        ok(r.quantidade()==1,"derrota entrou no ranking");
        jogo.dispose();
        Main reiniciado=new Main();reiniciado.create();reiniciado.render();
        ok(((RankingPartidas)get(reiniciado,"ranking")).quantidade()==1,"rank sumiu ao reabrir");
        reiniciado.dispose();
        System.out.println("PASSOU: integracao mock V5.5: menu, nome, pausa, timer, transformacao, vitoria, rank persistente, R, derrota.");
    }
}

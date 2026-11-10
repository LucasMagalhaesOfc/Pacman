package org.pacman;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import java.text.Normalizer;
import java.util.Locale;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.utils.ScreenUtils;

import org.pacman.controller.ControladorJogo;
import org.pacman.model.MapaJogo;
import org.pacman.model.Pacman;
import org.pacman.model.RankingPartidas;
import org.pacman.util.CarregadorMapa;
import org.pacman.view.RenderizadorJogo;

/** Pac-Man Halloween 5.5: entrada por nome, seis fantasmas, pausa e ranking local. */
public class Main extends ApplicationAdapter {
    private enum Tela { NOME, JOGO, PAUSA, VITORIA, DERROTA }

    private MapaJogo mapa;
    private Pacman pacman;
    private ControladorJogo controlador;
    private RenderizadorJogo renderizador;
    private Music musicaFundo;
    private Preferences armazenamento;
    private RankingPartidas ranking;
    private Tela tela=Tela.NOME;
    private String nomeDigitado="";
    private String nomeJogador="";
    private float tempoPartida;
    private boolean teclaPSegurada;

    @Override
    public void create() {
        armazenamento=Gdx.app.getPreferences("pacman-halloween-v55");
        ranking=RankingPartidas.carregar(armazenamento.getString("ranking-vitorias",""));
        musicaFundo=Gdx.audio.newMusic(Gdx.files.internal("sounds/fundo.mp3"));
        musicaFundo.setLooping(true);
        musicaFundo.setVolume(.55f);
        renderizador=new RenderizadorJogo();
        prepararNovaPartida();

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override public boolean keyTyped(char caractere) {
                if(tela!=Tela.NOME)return false;
                if(caractere>=32 && !Character.isISOControl(caractere) && nomeDigitado.length()<16) {
                    String simplificado=Normalizer.normalize(String.valueOf(caractere),Normalizer.Form.NFD)
                        .replaceAll("\\p{M}+","").toUpperCase(Locale.ROOT);
                    if(simplificado.length()==1) {
                        char c=simplificado.charAt(0);
                        if((c>='A'&&c<='Z') || (c>='0'&&c<='9') || c==' ' || c=='-' || c=='_')
                            nomeDigitado+=c;
                    }
                }
                return true;
            }
            @Override public boolean keyDown(int codigo) {
                if(tela==Tela.NOME) {
                    if(codigo==Input.Keys.BACKSPACE && !nomeDigitado.isEmpty())
                        nomeDigitado=nomeDigitado.substring(0,nomeDigitado.length()-1);
                    if(codigo==Input.Keys.ENTER || codigo==Input.Keys.NUMPAD_ENTER)
                        comecarSeNomeValido();
                } else if(codigo==Input.Keys.P && (tela==Tela.JOGO || tela==Tela.PAUSA)) {
                    // Ignora auto-repeat do teclado: uma transicao por pressionamento.
                    if(teclaPSegurada)return true;
                    teclaPSegurada=true;
                    if(tela==Tela.JOGO) {
                        tela=Tela.PAUSA;
                        if(musicaFundo!=null)musicaFundo.pause();
                    } else {
                        tela=Tela.JOGO;
                        if(musicaFundo!=null)musicaFundo.play();
                    }
                } else if((tela==Tela.VITORIA || tela==Tela.DERROTA) && codigo==Input.Keys.R) {
                    prepararNovaPartida();
                }
                return false; // controles direcionais continuam sendo lidos por polling
            }
            @Override public boolean keyUp(int codigo) {
                if(codigo==Input.Keys.P)teclaPSegurada=false;
                return false;
            }
        });
    }
    private void comecarSeNomeValido() {
        String nome=RankingPartidas.normalizarNome(nomeDigitado);
        if(nome.length()==0)return;
        nomeJogador=nome;
        tempoPartida=0f;
        tela=Tela.JOGO;
        musicaFundo.play();
    }

    private void prepararNovaPartida() {
        if(controlador!=null)controlador.dispose();
        mapa=CarregadorMapa.carregarV5(Gdx.files.internal("maps/mapa_v5.txt").readString("UTF-8"));
        pacman=new Pacman(mapa.getLinhaInicialPacman(),mapa.getColunaInicialPacman());
        controlador=new ControladorJogo(mapa,pacman);
        nomeDigitado="";
        teclaPSegurada=false;
        nomeJogador="";
        tempoPartida=0f;
        tela=Tela.NOME;
        if(musicaFundo!=null)musicaFundo.pause();
    }

    @Override public void render() {
        float delta=Math.min(Gdx.graphics.getDeltaTime(),.05f);
        if(tela==Tela.JOGO) {
            tempoPartida+=delta;
            controlador.atualizar(delta);
            if(controlador.isVenceu()) {
                // Somente uma gravacao por partida, na transicao de JOGO para VITORIA.
                ranking.registrarVitoria(nomeJogador,controlador.getPontuacao().getPontos(),tempoPartida);
                armazenamento.putString("ranking-vitorias",ranking.serializar());
                armazenamento.flush();
                tela=Tela.VITORIA;
                musicaFundo.pause();
            } else if(controlador.isGameOver()) {
                tela=Tela.DERROTA;
                musicaFundo.pause();
            }
        }
        ScreenUtils.clear(.02f,.01f,.04f,1f);
        renderizador.desenhar(mapa,pacman,controlador.getPontuacao(),controlador.getFantasmas(),
            controlador.getVidas(),controlador.isGameOver(),controlador.isVenceu(),
            controlador.getTempoProtecaoPacman(),nomeJogador,tempoPartida,tela!=Tela.JOGO);
        if(tela!=Tela.JOGO) {
            int modo=tela==Tela.NOME?RenderizadorJogo.TELA_NOME:
                tela==Tela.PAUSA?RenderizadorJogo.TELA_PAUSA:
                tela==Tela.VITORIA?RenderizadorJogo.TELA_VITORIA:RenderizadorJogo.TELA_DERROTA;
            renderizador.desenharPainel(modo,tela==Tela.NOME?nomeDigitado:nomeJogador,
                controlador.getPontuacao().getPontos(),tempoPartida,ranking);
        }
    }

    @Override public void pause() {
        if(tela==Tela.JOGO) {
            tela=Tela.PAUSA;
            if(musicaFundo!=null)musicaFundo.pause();
        }
    }

    @Override public void dispose() {
        if(armazenamento!=null)armazenamento.flush();
        if(renderizador!=null)renderizador.dispose();
        if(controlador!=null)controlador.dispose();
        if(musicaFundo!=null)musicaFundo.dispose();
    }
}

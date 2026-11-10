package org.pacman.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/** V5: pontuacao em memoria; recorde salvo ao fim da partida/fechamento. */
public class Pontuacao {
    private final Preferences preferencias;
    private int pontos;
    private int recorde;
    private int fantasmasComidos;

    public Pontuacao() {
        preferencias=Gdx.app.getPreferences("pacman");
        recorde=preferencias.getInteger("recorde",0);
    }
    public void adicionar(int n) {
        pontos+=n;
        if(pontos>recorde) {
            recorde=pontos;
            preferencias.putInteger("recorde",recorde);
        }
    }
    public int getPontos(){return pontos;}
    public int getRecorde(){return recorde;}
    public void zerar(){pontos=0;reiniciarComboFantasmas();}
    public int comerFantasma() {
        int[] valores={200,400,800,1600};
        int valor=valores[Math.min(fantasmasComidos,3)];
        adicionar(valor);
        fantasmasComidos++;
        return valor;
    }
    public void reiniciarComboFantasmas(){fantasmasComidos=0;}
    public void salvarRecorde(){preferencias.flush();}
}

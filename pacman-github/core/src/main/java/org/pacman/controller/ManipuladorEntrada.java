package org.pacman.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import org.pacman.model.Direcao;

/**
 * V5: a tecla pressionada MAIS RECENTEMENTE ganha prioridade.
 * V4 lia cima primeiro e ignorava direita/esquerda quando duas teclas ficavam
 * pressionadas. Isso podia parecer uma parede travando o jogador.
 */
public class ManipuladorEntrada {
    private Direcao ultima=Direcao.NENHUMA;

    public Direcao lerDirecao() {
        if(nova(Input.Keys.UP,Input.Keys.W))return ultima=Direcao.CIMA;
        if(nova(Input.Keys.DOWN,Input.Keys.S))return ultima=Direcao.BAIXO;
        if(nova(Input.Keys.LEFT,Input.Keys.A))return ultima=Direcao.ESQUERDA;
        if(nova(Input.Keys.RIGHT,Input.Keys.D))return ultima=Direcao.DIREITA;

        if(pressionada(ultima))return ultima;
        if(pressionada(Direcao.CIMA))return ultima=Direcao.CIMA;
        if(pressionada(Direcao.BAIXO))return ultima=Direcao.BAIXO;
        if(pressionada(Direcao.ESQUERDA))return ultima=Direcao.ESQUERDA;
        if(pressionada(Direcao.DIREITA))return ultima=Direcao.DIREITA;
        return Direcao.NENHUMA;
    }

    private boolean nova(int seta,int letra) {
        return Gdx.input.isKeyJustPressed(seta)||Gdx.input.isKeyJustPressed(letra);
    }
    private boolean pressionada(Direcao d) {
        switch(d) {
            case CIMA:return Gdx.input.isKeyPressed(Input.Keys.UP)||Gdx.input.isKeyPressed(Input.Keys.W);
            case BAIXO:return Gdx.input.isKeyPressed(Input.Keys.DOWN)||Gdx.input.isKeyPressed(Input.Keys.S);
            case ESQUERDA:return Gdx.input.isKeyPressed(Input.Keys.LEFT)||Gdx.input.isKeyPressed(Input.Keys.A);
            case DIREITA:return Gdx.input.isKeyPressed(Input.Keys.RIGHT)||Gdx.input.isKeyPressed(Input.Keys.D);
            default:return false;
        }
    }
}

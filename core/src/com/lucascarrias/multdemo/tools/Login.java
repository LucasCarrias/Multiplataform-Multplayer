package com.lucascarrias.multdemo.tools;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class Login implements Input.TextInputListener, Screen {
    private String playerName;
    private String serverPort;
    private ApplicationAdapter app;

    private Input.TextInputListener serverIp = new Input.TextInputListener() {
        private String value;
        private boolean onScreen;

        public void getInput(){
            if (value == null && !onScreen)
                Gdx.input.getTextInput(this, "Server IP", "Default", "IP");
        }

        @Override
        public void input(String text) {
            serverIp.value = text;
        }

        @Override
        public void canceled() {

        }
    };

    private Stage stage;


    public Login(ApplicationAdapter app){
        this.app = app;

        stage = new Stage();
        Gdx.input.setInputProcessor(stage);

    }


    @Override
    public void input(String text) {

    }

    @Override
    public void canceled() {

    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.7f, 0.7f, 0.7f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}

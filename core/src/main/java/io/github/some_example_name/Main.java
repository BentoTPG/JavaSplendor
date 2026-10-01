package io.github.some_example_name;

import com.badlogic.gdx.Game;

import io.github.some_example_name.model.GameState;
import io.github.some_example_name.screens.GameScreen;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {
    @Override
    public void create() {
        setScreen(new GameScreen(GameState.newGame()));
        
        //System.out.println("1");
    }
}
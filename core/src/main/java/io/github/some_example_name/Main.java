package io.github.some_example_name;

import com.badlogic.gdx.Game;

import io.github.some_example_name.data.CardCatalog;
import io.github.some_example_name.screens.GameScreen;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {
    @Override
    public void create() {
        // Read the four CSV files (Lv1Card, Lv2Card, Lv3Card, Nobles) once, as the game starts.
        setScreen(new GameScreen(CardCatalog.load()));
    }
}

package io.github.some_example_name;

import com.badlogic.gdx.Game;

import io.github.some_example_name.data.CardCatalog;
import io.github.some_example_name.model.GameManager;
import io.github.some_example_name.model.GameState;
import io.github.some_example_name.screens.GameScreen;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {
    /** Number of players in a new game (2 to 4). */
    private static final int PLAYER_COUNT = 4;

    @Override
    public void create() {
        // Read the four CSV files (Lv1Card, Lv2Card, Lv3Card, Nobles) once, as the game starts.
        CardCatalog catalog = CardCatalog.load();
        // Deal the board (face-up cards, nobles, bank) and start the rules for turn 1.
        GameState state = GameState.newGame(PLAYER_COUNT, catalog);
        setScreen(new GameScreen(new GameManager(state)));
    }
}

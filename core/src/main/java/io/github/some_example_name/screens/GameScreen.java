package io.github.some_example_name.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import io.github.some_example_name.model.Card;
import io.github.some_example_name.model.GameManager;
import io.github.some_example_name.model.GameState;
import io.github.some_example_name.ui.DeckView;

import io.github.some_example_name.ui.CardView;
import io.github.some_example_name.ui.Gfx;

public class GameScreen extends ScreenAdapter {
    private final Stage stage = new Stage(new FitViewport(1920, 1080));
    private final BitmapFont bigFont = new BitmapFont(), smallFont = new BitmapFont();
    private final GameState state;
    private final GameManager manager;
    private final Table board = new Table();
    private final Label.LabelStyle big, small;

    public GameScreen(GameState state) {
        this.state = state;
        this.manager = new GameManager(state);
        Gdx.input.setInputProcessor(stage);
        bigFont.getData().setScale(3.5f);
        smallFont.getData().setScale(2f);
        big = new Label.LabelStyle(bigFont, Color.WHITE);
        small = new Label.LabelStyle(smallFont, Color.WHITE);

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        root.add(board).expand();
        buildBoard();

        stage.setDebugAll(true);          // remove when the layout looks right
    }

    /** (Re)creates the deck and card views from the current game state. */
    private void buildBoard() {
        board.clear();
        for (int tier = 3; tier >= 1; tier--) {
            board.add(new DeckView(tier, state.decks.get(tier - 1).size, big))
                 .size(175, 265).pad(10, 6, 10, 6);
            for (Card c : state.market.get(tier - 1)) {
                CardView view = new CardView(c, big, small);
                view.onClick = () -> tryBuy(c);
                board.add(view).size(175, 265).pad(10, 6, 10, 6);
            }
            board.row();
        }
    }

    /** Click on a card = buy it for the current player, then end the turn. */
    private void tryBuy(Card c) {
        if (!manager.buyCard(c)) return;
        manager.endTurn();
        buildBoard();
    }

    @Override public void render(float delta) {
        ScreenUtils.clear(0.45f, 0.30f, 0.18f, 1f);   // wood-brown placeholder
        stage.act(delta);
        stage.draw();
    }
    @Override public void resize(int w, int h) { stage.getViewport().update(w, h, true); }
    @Override public void dispose() {
        stage.dispose(); bigFont.dispose(); smallFont.dispose(); Gfx.disposeAll();
    }
}
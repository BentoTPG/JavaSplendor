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
import io.github.some_example_name.model.GameState;
import io.github.some_example_name.ui.DeckView;

import io.github.some_example_name.ui.CardView;
import io.github.some_example_name.ui.Gfx;

public class GameScreen extends ScreenAdapter {
    private final Stage stage = new Stage(new FitViewport(1920, 1080));
    private final BitmapFont bigFont = new BitmapFont(), smallFont = new BitmapFont();

    public GameScreen(GameState state) {
        Gdx.input.setInputProcessor(stage);
        bigFont.getData().setScale(3.5f);
        smallFont.getData().setScale(2f);
        Label.LabelStyle big = new Label.LabelStyle(bigFont, Color.WHITE);
        Label.LabelStyle small = new Label.LabelStyle(smallFont, Color.WHITE);

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        Table board = new Table();
        for (int tier = 3; tier >= 1; tier--) {
            board.add(new DeckView(tier, state.decks.get(tier - 1).size, big))
                 .size(175, 265).pad(10, 6, 10, 6);
            for (Card c : state.market.get(tier - 1))
                board.add(new CardView(c, big, small)).size(175, 265).pad(10, 6, 10, 6);
            board.row();
        }
        root.add(board).expand();

        stage.setDebugAll(true);          // remove when the layout looks right
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
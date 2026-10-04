package io.github.some_example_name.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import io.github.some_example_name.data.CardCatalog;
import io.github.some_example_name.data.CardInfo;
import io.github.some_example_name.data.DrawPile;
import io.github.some_example_name.data.NobleInfo;
import io.github.some_example_name.ui.CardView;
import io.github.some_example_name.ui.DeckView;
import io.github.some_example_name.ui.Gfx;
import io.github.some_example_name.ui.NobleView;

/**
 * Show-only board: three rows of cards (level 3, 2, 1) and a column of nobles on their right.
 * The cards on screen are drawn at random from the CSV piles and removed from them, so none repeats.
 */
public class GameScreen extends ScreenAdapter {
    /** Face-up cards in each row. The first column of the row is the "cards left" tile. */
    public static final int FACE_UP_PER_LEVEL = 3;
    /** Nobles shown in the noble column. The last tile of the column is the "nobles left" tile. */
    public static final int NOBLES_SHOWN = 3;

    private static final boolean DEBUG_LAYOUT = false;      // true draws the outline of every cell

    private final Stage stage = new Stage(new FitViewport(1920, 1080));
    private final BitmapFont bigFont = new BitmapFont(), smallFont = new BitmapFont();
    private final Label.LabelStyle big, small;

    public GameScreen(CardCatalog catalog) {
        Gdx.input.setInputProcessor(stage);
        bigFont.getData().setScale(3.5f);
        smallFont.getData().setScale(2f);
        bigFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        smallFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        big = new Label.LabelStyle(bigFont, Color.WHITE);
        small = new Label.LabelStyle(smallFont, Color.WHITE);

        Table content = new Table();
        content.add(buildMarket(catalog));
        content.add(buildNobles(catalog)).padLeft(60);

        Table root = new Table();
        root.setFillParent(true);
        root.add(content).expand();
        stage.addActor(root);

        stage.setDebugAll(DEBUG_LAYOUT);
    }

    /** Three rows, level 3 on top. Column 1 = cards left in the pile, the other columns = cards drawn from it. */
    private Table buildMarket(CardCatalog catalog) {
        Table market = new Table();
        for (int tier = 3; tier >= 1; tier--) {
            DrawPile<CardInfo> pile = catalog.level(tier);
            Array<CardInfo> faceUp = pile.draw(FACE_UP_PER_LEVEL);          // drawn cards leave the pile
            market.add(new DeckView("Lv " + tier, pile.size(), big, small))   // so size() is what is left
                  .size(CardView.W, CardView.H).pad(10, 6, 10, 6);
            for (CardInfo card : faceUp) {
                market.add(new CardView(card, big, small)).size(CardView.W, CardView.H).pad(10, 6, 10, 6);
            }
            market.row();
        }
        return market;
    }

    /** Rows 1 to 3 = nobles drawn from the pile, last row = nobles left. */
    private Table buildNobles(CardCatalog catalog) {
        Table nobles = new Table();
        Array<NobleInfo> shown = catalog.nobles.draw(NOBLES_SHOWN);
        for (NobleInfo noble : shown) {
            nobles.add(new NobleView(noble, big, small)).size(NobleView.SIZE).pad(8).row();
        }
        nobles.add(new DeckView("Nobles", catalog.nobles.size(), big, small)).size(NobleView.SIZE).pad(8);
        return nobles;
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

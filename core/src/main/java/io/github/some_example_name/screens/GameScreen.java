package io.github.some_example_name.screens;

import java.util.EnumMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import io.github.some_example_name.data.CardCatalog;
import io.github.some_example_name.data.CardInfo;
import io.github.some_example_name.data.DrawPile;
import io.github.some_example_name.data.NobleInfo;
import io.github.some_example_name.model.Gem;
import io.github.some_example_name.ui.CardDetailOverlay;
import io.github.some_example_name.ui.CardView;
import io.github.some_example_name.ui.CurrentPlayerBar;
import io.github.some_example_name.ui.DeckView;
import io.github.some_example_name.ui.Fonts;
import io.github.some_example_name.ui.GemBankView;
import io.github.some_example_name.ui.Gfx;
import io.github.some_example_name.ui.NobleView;
import io.github.some_example_name.ui.OtherPlayerCard;
import io.github.some_example_name.ui.OutlinedLabel;
import io.github.some_example_name.ui.PlayerParts;
import io.github.some_example_name.ui.PlayerSnapshot;
import io.github.some_example_name.ui.UiStyles;

/**
 * Show-only board.
 * <pre>
 *  +--------------+---------------------------------------------+
 *  | other player |  Lv3 row | gem bank | nobles left: N          |
 *  | other player |  Lv2 row |          | 4 nobles                |
 *  | other player |  Lv1 row |          |                         |
 *  |              |  current player bar (whose turn it is)      |
 *  +--------------+---------------------------------------------+
 * </pre>
 * The cards on screen are drawn at random from the CSV piles and removed from them, so none repeats.
 * Press N (or SPACE) to pass the turn to the next player and watch the panels swap places.
 */
public class GameScreen extends ScreenAdapter {
    /** Face-up cards in each row. The first column of the row is the "cards left" tile. */
    public static final int FACE_UP_PER_LEVEL = 3;
    /** Nobles shown in the noble column, under the "nobles left" label. */
    public static final int NOBLES_SHOWN = 4;

    /** Gems in the bank at the start of a 4-player game: 7 of each colour, 5 gold. Placeholder until the backend owns it. */
    public static final int BANK_PER_COLOUR = 7, BANK_GOLD = 5;

    /** true: fill the player panels with made-up numbers so the layout can be checked. The backend sets this to false. */
    private static final boolean DEMO_PLAYERS = true;
    private static final boolean DEBUG_LAYOUT = false;      // true draws the outline of every cell

    private final Stage stage = new Stage(new FitViewport(1920, 1080));
    private final BitmapFont bigFont = Fonts.createBigNumberFont();
    private final BitmapFont smallFont = Fonts.createSmallNumberFont();
    private final BitmapFont captionFont = Fonts.createCaptionFont();
    private final Label.LabelStyle big, small;
    private final UiStyles styles;

    private final PlayerSnapshot[] players;
    private int current = 0;

    private final Table othersColumn = new Table();                         // left side: everyone but the current player
    private final Container<CurrentPlayerBar> currentSlot = new Container<CurrentPlayerBar>();

    /** Opens the large view of a reserved card. */
    private final PlayerParts.CardClick showCard = new PlayerParts.CardClick() {
        @Override public void open(CardInfo card) {
            stage.addActor(new CardDetailOverlay(card, big, small, styles));
        }
    };

    public GameScreen(CardCatalog catalog) {
        Gdx.input.setInputProcessor(stage);
        big = new Label.LabelStyle(bigFont, Color.WHITE);
        small = new Label.LabelStyle(smallFont, Color.WHITE);
        styles = new UiStyles(big, small, new Label.LabelStyle(captionFont, new Color(0.92f, 0.92f, 0.92f, 1f)));

        // Reserved cards come out of the piles before the market is dealt, as in a real game,
        // so the "cards left" numbers below already count them.
        players = DEMO_PLAYERS ? demoPlayers(catalog) : emptyPlayers();

        Table board = new Table();
        board.add(buildMarket(catalog)).top();
        board.add(buildGemBank()).top().padLeft(30);
        board.add(buildNobles(catalog)).top().padLeft(30);

        Table rightSide = new Table();
        rightSide.add(board).row();
        currentSlot.fill();
        rightSide.add(currentSlot).growX().height(CurrentPlayerBar.HEIGHT).padTop(12);

        Table root = new Table();
        root.setFillParent(true);
        root.top().pad(16);
        root.add(othersColumn).top().padRight(20);
        root.add(rightSide).top();
        stage.addActor(root);

        showTurn(0);

        stage.addListener(new InputListener() {
            @Override public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.N || keycode == Input.Keys.SPACE) {
                    showTurn((current + 1) % players.length);
                    return true;
                }
                return false;
            }
        });

        stage.setDebugAll(DEBUG_LAYOUT);
    }

    /**
     * Puts player `index` in the bottom bar and the others, in turn order after them, on the left.
     * The backend calls this when the turn passes (after updating the snapshots).
     */
    public void showTurn(int index) {
        current = index;
        currentSlot.setActor(new CurrentPlayerBar(players[index], styles, showCard));

        othersColumn.clearChildren();
        for (int step = 1; step < players.length; step++) {
            PlayerSnapshot p = players[(index + step) % players.length];
            othersColumn.add(new OtherPlayerCard(p, styles, showCard))
                        .size(OtherPlayerCard.W, OtherPlayerCard.H).pad(5).row();
        }
    }

    /** Three rows, level 3 on top. Column 1 = cards left in the pile, the other columns = cards drawn from it. */
    private Table buildMarket(CardCatalog catalog) {
        Table market = new Table();
        for (int tier = 3; tier >= 1; tier--) {
            DrawPile<CardInfo> pile = catalog.level(tier);
            Array<CardInfo> faceUp = pile.draw(FACE_UP_PER_LEVEL);          // drawn cards leave the pile
            market.add(new DeckView("Lv " + tier, pile.size(), big, small))   // so size() is what is left
                  .size(CardView.W, CardView.H).pad(6);
            for (CardInfo card : faceUp) {
                market.add(new CardView(card, big, small)).size(CardView.W, CardView.H).pad(6);
            }
            market.row();
        }
        return market;
    }

    /** The gem deck between the card rows and the nobles. It shows the starting gems minus what players hold. */
    private GemBankView buildGemBank() {
        Map<Gem, Integer> bank = new EnumMap<Gem, Integer>(Gem.class);
        for (Gem gem : Gem.values()) {
            int left = gem == Gem.GOLD ? BANK_GOLD : BANK_PER_COLOUR;
            for (PlayerSnapshot p : players) left -= p.gems(gem);
            bank.put(gem, Math.max(0, left));
        }
        return new GemBankView(bank, big);
    }

    /** "Nobles left" label on top, then the nobles drawn from the pile. */
    private Table buildNobles(CardCatalog catalog) {
        Array<NobleInfo> shown = catalog.nobles.draw(NOBLES_SHOWN);         // drawn first, so the count is what is left
        Table nobles = new Table();
        nobles.add(new OutlinedLabel("Nobles left: " + catalog.nobles.size(), small, 1f)).left().padBottom(4).row();
        for (NobleInfo noble : shown) {
            nobles.add(new NobleView(noble, big, small)).size(NobleView.SIZE).pad(4).row();
        }
        return nobles;
    }

    private static PlayerSnapshot[] emptyPlayers() {
        PlayerSnapshot[] ps = new PlayerSnapshot[4];
        for (int i = 0; i < ps.length; i++) ps[i] = new PlayerSnapshot("P" + (i + 1));
        return ps;
    }

    /** Made-up player states for checking the layout. Gems: white, blue, green, red, black, gold. */
    private static PlayerSnapshot[] demoPlayers(CardCatalog c) {
        PlayerSnapshot[] ps = emptyPlayers();
        ps[0].withPoints(6).withGems(2, 0, 1, 3, 1, 1).withBonuses(1, 2, 0, 1, 3);
        ps[1].withPoints(9).withGems(1, 3, 0, 1, 2, 1).withBonuses(2, 1, 3, 0, 1);
        ps[2].withPoints(3).withGems(0, 2, 2, 0, 1, 0).withBonuses(0, 1, 1, 2, 0);
        ps[3].withPoints(12).withGems(3, 1, 2, 1, 0, 1).withBonuses(3, 2, 1, 2, 2);
        reserve(ps[0], c.level2);
        reserve(ps[1], c.level1); reserve(ps[1], c.level3);
        reserve(ps[3], c.level1); reserve(ps[3], c.level2); reserve(ps[3], c.level3);
        return ps;
    }

    private static void reserve(PlayerSnapshot p, DrawPile<CardInfo> pile) {
        CardInfo card = pile.draw();
        if (card != null) p.reserved.add(card);
    }

    @Override public void render(float delta) {
        ScreenUtils.clear(0.45f, 0.30f, 0.18f, 1f);   // wood-brown placeholder
        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int w, int h) { stage.getViewport().update(w, h, true); }

    @Override public void dispose() {
        stage.dispose(); bigFont.dispose(); smallFont.dispose(); captionFont.dispose(); Gfx.disposeAll();
    }
}

package io.github.some_example_name.screens;

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
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import io.github.some_example_name.data.CardInfo;
import io.github.some_example_name.data.NobleInfo;
import io.github.some_example_name.model.GameManager;
import io.github.some_example_name.model.GameState;
import io.github.some_example_name.model.Gem;
import io.github.some_example_name.model.Player;
import io.github.some_example_name.model.PlayerSnapshots;
import io.github.some_example_name.ui.CardActionOverlay;
import io.github.some_example_name.ui.CardDetailOverlay;
import io.github.some_example_name.ui.CardView;
import io.github.some_example_name.ui.CurrentPlayerBar;
import io.github.some_example_name.ui.DeckView;
import io.github.some_example_name.ui.Fonts;
import io.github.some_example_name.ui.GemBankView;
import io.github.some_example_name.ui.Gfx;
import io.github.some_example_name.ui.MiniCardView;
import io.github.some_example_name.ui.NobleActionOverlay;
import io.github.some_example_name.ui.NobleView;
import io.github.some_example_name.ui.OtherPlayerCard;
import io.github.some_example_name.ui.OutlinedLabel;
import io.github.some_example_name.ui.PlayerParts;
import io.github.some_example_name.ui.PlayerSnapshot;
import io.github.some_example_name.ui.UiButtons;
import io.github.some_example_name.ui.UiStyles;

/**
 * Board connected to the game rules.
 * Everything on screen is read from GameState; every action goes through GameManager.
 * After each action call refresh() so the screen is rebuilt from the new state.
 *
 * Controls:
 *   click a face-up card           = window with [Buy] [Reserve] [Cancel]
 *   click gems in the bank         = add one gem to the pick (right-click a gem = take it back out),
 *                                    then press [Pick]
 *   click a deck tile              = reserve the top card of that deck (face down)
 *   click your own reserved card   = window with [Buy] [Cancel]
 *   click your own gems            = give a token back (only while tokens are owed)
 *   click a noble                  = window with [Buy] [Cancel] (buying a noble is the action of the turn)
 *   [End turn] / [Pass] buttons    = gm.endTurn() / gm.pass() (Pass skips the turn, any time before acting)
 *   ESC                            = clear the gems picked so far
 */
public class GameScreen extends ScreenAdapter {
    public static final int FACE_UP_PER_LEVEL = GameState.FACE_UP_PER_TIER;
    public static final int NOBLES_SHOWN = GameState.MAX_NOBLES;

    private static final boolean DEBUG_LAYOUT = false;

    private final GameManager game;
    private final GameState state;

    /** The board was laid out for 1920 x 1080; 70 more pixels of height make room for the status line and buttons. */
    private final Stage stage = new Stage(new FitViewport(1920, 1150));
    private final BitmapFont bigFont = Fonts.createBigNumberFont();
    private final BitmapFont smallFont = Fonts.createSmallNumberFont();
    private final BitmapFont captionFont = Fonts.createCaptionFont();
    private final Label.LabelStyle big, small;
    private final UiStyles styles;

    private PlayerSnapshot[] players;

    private final Table othersColumn = new Table();
    private final Container<CurrentPlayerBar> currentSlot = new Container<CurrentPlayerBar>();
    private final Table marketTable = new Table();
    private final Table bankTable = new Table();
    private final Table noblesTable = new Table();
    private final Label statusLabel;
    private final TextButton pickButton, endTurnButton, passButton;

    /** Gems picked so far for a "take tokens" action (not yet sent to the game). */
    private final Array<Gem> pending = new Array<Gem>();

    private final PlayerParts.CardClick showCard = new PlayerParts.CardClick() {
        @Override public void open(CardInfo card) { onReservedClicked(card); }
    };

    /** Clicking one of your own gems gives it back (only while tokens are owed). */
    private final PlayerParts.GemClick ownGemClick = new PlayerParts.GemClick() {
        @Override public void click(Gem gem) { onOwnGemClicked(gem); }
    };

    public GameScreen(final GameManager game) {
        this.game = game;
        this.state = game.state;

        Gdx.input.setInputProcessor(stage);
        big = new Label.LabelStyle(bigFont, Color.WHITE);
        small = new Label.LabelStyle(smallFont, Color.WHITE);
        styles = new UiStyles(big, small, new Label.LabelStyle(captionFont, new Color(0.92f, 0.92f, 0.92f, 1f)));

        statusLabel = new Label("", small);
        statusLabel.setWrap(true);

        pickButton = UiButtons.create("Pick", small, new Runnable() {
            @Override public void run() { commitPending(); }
        });
        endTurnButton = UiButtons.create("End turn", small, new Runnable() {
            @Override public void run() {
                if (!game.endTurn()) Gdx.app.log("Game", "cannot end turn yet");
                refresh();
            }
        });
        passButton = UiButtons.create("Pass", small, new Runnable() {
            @Override public void run() {
                if (!game.pass()) Gdx.app.log("Game", "cannot pass");
                refresh();
            }
        });

        Table board = new Table();
        board.add(marketTable).top();
        board.add(bankTable).top().padLeft(30);
        board.add(noblesTable).top().padLeft(30);

        Table controls = new Table();
        controls.add(statusLabel).left().width(900).padRight(16);
        controls.add(pickButton).padRight(10);
        controls.add(endTurnButton).padRight(10);
        controls.add(passButton);

        Table rightSide = new Table();
        rightSide.add(board).row();
        currentSlot.fill();
        rightSide.add(currentSlot).growX().height(CurrentPlayerBar.HEIGHT).padTop(12).row();
        rightSide.add(controls).left().padTop(8);

        Table root = new Table();
        root.setFillParent(true);
        root.top().pad(16);
        root.add(othersColumn).top().padRight(20);
        root.add(rightSide).top();
        stage.addActor(root);

        refresh();

        stage.addListener(new InputListener() {
            @Override public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ESCAPE && pending.size > 0) {
                    pending.clear();
                    refresh();
                    return true;
                }
                return false;
            }
        });

        stage.setDebugAll(DEBUG_LAYOUT);
    }

    // ---- reserved cards and own gems (panels) ----

    private void onReservedClicked(CardInfo card) {
        Player owner = ownerOfReserved(card);
        if (owner != null && owner != state.current() && owner.isHidden(card)) return;   // face-down card of another player
        if (owner == state.current()) {                                 // your own reserved card: [Buy] [Cancel]
            openReservedCardWindow(card);
            return;
        }
        stage.addActor(new CardDetailOverlay(card, big, small, styles));
    }

    /** The window with [Buy] [Cancel] for one of your own reserved cards (Buy is grey when you cannot afford it). */
    private void openReservedCardWindow(final CardInfo card) {
        stage.addActor(new CardActionOverlay(card, game.canBuyCard(card), false, big, small,
                new Runnable() {
                    @Override public void run() {
                        if (!game.buyCard(card)) Gdx.app.log("Game", "cannot buy card " + card.id);
                        refresh();
                    }
                },
                null));                                                 // null = no Reserve button
    }

    private void onOwnGemClicked(Gem gem) {
        if (game.hasActed() && game.tokensToDiscard() > 0 && !game.returnToken(gem)) {
            Gdx.app.log("Game", "cannot return " + gem);
        }
        refresh();
    }

    /** The player who has reserved this card, or null. */
    private Player ownerOfReserved(CardInfo card) {
        for (Player p : state.players) {
            if (p.reserved.contains(card, true)) return p;
        }
        return null;
    }

    // ---- drawing ----

    /** Rebuilds everything on screen from the current GameState. Call after every action. */
    public void refresh() {
        if (game.hasActed() || state.gameOver) pending.clear();
        players = PlayerSnapshots.fromGame(state);
        buildMarket();
        buildGemBank();
        buildNobles();
        showTurn(state.currentPlayer);
        updateStatus();
    }

    /** Player `index` in the bottom bar, the others in turn order on the left. */
    public void showTurn(int index) {
        for (int i = 0; i < players.length; i++) players[i].maskHidden = (i != index);   // only the owner sees face-down cards
        currentSlot.setActor(new CurrentPlayerBar(players[index], styles, showCard, ownGemClick));

        othersColumn.clearChildren();
        for (int step = 1; step < players.length; step++) {
            PlayerSnapshot p = players[(index + step) % players.length];
            othersColumn.add(new OtherPlayerCard(p, styles, showCard))
                        .size(OtherPlayerCard.W, OtherPlayerCard.H).pad(5).row();
        }
    }

    /** Level 3 on top. Deck tile first, then the face-up cards (a row can be shorter when its deck runs out). */
    private void buildMarket() {
        marketTable.clearChildren();
        for (int tier = GameState.TIERS; tier >= 1; tier--) {
            final int t = tier;
            DeckView deck = new DeckView("Lv " + tier, state.deck(tier).size(), big, small);
            deck.addListener(new ClickListener() {
                @Override public void clicked(InputEvent event, float x, float y) {
                    if (!game.reserveFromDeck(t)) Gdx.app.log("Game", "cannot reserve from deck " + t);
                    refresh();
                }
            });
            marketTable.add(deck).size(CardView.W, CardView.H).pad(6);

            for (final CardInfo card : state.row(tier)) {
                CardView view = new CardView(card, big, small);
                view.onClick = new Runnable() {         // ClickableStack's own left-click hook
                    @Override public void run() { openCardWindow(card); }
                };
                marketTable.add(view).size(CardView.W, CardView.H).pad(6);
            }
            // When a deck runs out its row gets shorter; empty boxes keep the row's width, so the board does not move.
            for (int i = state.row(tier).size; i < FACE_UP_PER_LEVEL; i++) {
                marketTable.add(MiniCardView.emptySlot()).size(CardView.W, CardView.H).pad(6);
            }
            marketTable.row();
        }
    }

    /** The window with [Buy] [Reserve] [Cancel] for a face-up card. */
    private void openCardWindow(final CardInfo card) {
        stage.addActor(new CardActionOverlay(card, game.canBuyCard(card), game.canReserveCard(), big, small,
                new Runnable() {
                    @Override public void run() {
                        if (!game.buyCard(card)) Gdx.app.log("Game", "cannot buy card " + card.id);
                        refresh();
                    }
                },
                new Runnable() {
                    @Override public void run() {
                        if (!game.reserveCard(card)) Gdx.app.log("Game", "cannot reserve card " + card.id);
                        refresh();
                    }
                }));
    }

    /** The bank shows the real bank from GameState. */
    private void buildGemBank() {
        bankTable.clearChildren();
        GemBankView bank = new GemBankView(state.bank, big);
        for (final Gem gem : Gem.BASIC) {
            bank.slot(gem).onClick = new Runnable() {       // GemSlot is a ClickableStack
                @Override public void run() { onBankGemClicked(gem); }
            };
            bank.slot(gem).addListener(new ClickListener(Input.Buttons.RIGHT) {     // right-click: take one back out
                @Override public void clicked(InputEvent event, float x, float y) { onBankGemRemoved(gem); }
            });
        }
        bankTable.add(bank);
    }

    private void onBankGemClicked(Gem gem) {
        if (game.hasActed() || state.gameOver) return;          // after the action, tokens go back by clicking your own gems
        // Only picks that can still become a legal take: up to 3 different colours, or 2 of one colour.
        boolean twoSame = pending.size == 2 && pending.get(0) == pending.get(1);
        if (state.bank.get(gem) <= countPending(gem)) {
            hint = "The bank has no more " + gem + ".";
        } else if (pending.contains(gem, true)) {
            if (pending.size != 1) {
                hint = "Take 2 of one colour, or up to 3 different colours.";
            } else if (state.bank.get(gem) < GameManager.MIN_FOR_TWO_SAME) {
                // Splendor rule: 2 of the same colour only when the bank has 4 or more of it
                hint = "2 " + gem + " needs " + GameManager.MIN_FOR_TWO_SAME + "+ in the bank (only "
                        + state.bank.get(gem) + " left).";
            } else {
                pending.add(gem);                               // second gem of the same colour
            }
        } else if (pending.size < 3 && !twoSame) {
            pending.add(gem);
        } else {
            hint = "Take 2 of one colour, or up to 3 different colours.";
        }
        refresh();
    }

    /** One-time message for the status line, explaining why a click did nothing. Cleared after it is shown. */
    private String hint;

    private int countPending(Gem gem) {
        int n = 0;
        for (Gem g : pending) if (g == gem) n++;
        return n;
    }
    

    private void onBankGemRemoved(Gem gem) {
        if (game.hasActed() || state.gameOver) return;
        pending.removeValue(gem, true);                         // removes one gem of this colour
        refresh();
    }

    /** The picked gems as a plain array, for GameManager.takeTokens / canTakeTokens. */
    private Gem[] pendingArray() {
        Gem[] out = new Gem[pending.size];
        for (int i = 0; i < pending.size; i++) out[i] = pending.get(i);
        return out;
    }

    private void commitPending() {
        Gem[] picked = pendingArray();
        pending.clear();
        if (!game.takeTokens(picked)) Gdx.app.log("Game", "cannot take those tokens");
        refresh();
    }

    /** The window with [Buy] [Cancel] for a noble on the board. */
    private void openNobleWindow(final NobleInfo noble) {
        stage.addActor(new NobleActionOverlay(noble, game.canBuyNoble(noble), big, small,
                new Runnable() {
                    @Override public void run() {
                        if (!game.buyNoble(noble)) Gdx.app.log("Game", "cannot buy noble " + noble.id);
                        refresh();
                    }
                }));
    }

    /** Nobles still on the board; the label counts the nobles left in the pile. */
    private void buildNobles() {
        noblesTable.clearChildren();
        noblesTable.add(new OutlinedLabel("Nobles left: " + state.catalog.nobles.size(), small, 1f))
                   .left().padBottom(4).row();
        for (final NobleInfo noble : state.nobles) {
            NobleView view = new NobleView(noble, big, small);
            view.onClick = new Runnable() {
                @Override public void run() { openNobleWindow(noble); }
            };
            noblesTable.add(view).size(NobleView.SIZE).pad(4).row();
        }
        // Keep the column as tall as at the start of the game (players + 1 nobles, at most 4).
        int slots = Math.min(state.players.size + 1, NOBLES_SHOWN);
        for (int i = state.nobles.size; i < slots; i++) {
            noblesTable.add(MiniCardView.emptySlot()).size(NobleView.SIZE).pad(4).row();
        }
    }

    // ---- status line and buttons ----

    private void updateStatus() {
        boolean over = state.gameOver;
        boolean canTake = pending.size > 0 && game.canTakeTokens(pendingArray());
        pickButton.setDisabled(over || game.hasActed() || !canTake);
        endTurnButton.setDisabled(over || !game.canEndTurn());
        passButton.setDisabled(!game.canPass());                // house rule: skip the turn at any time

        if (over) {
            StringBuilder sb = new StringBuilder("Game over - winner: ");
            Array<Player> winners = state.winners();
            for (int i = 0; i < winners.size; i++) {
                if (i > 0) sb.append(", ");
                sb.append(winners.get(i).name);
            }
            statusLabel.setText(sb.toString());
            return;
        }

        String text = state.current().name + "'s turn. ";
        if (!game.hasActed()) {
            if (pending.size > 0) {
                StringBuilder sb = new StringBuilder("Picked: ");
                for (int i = 0; i < pending.size; i++) sb.append(i > 0 ? ", " : "").append(pending.get(i));
                text += sb + (canTake ? " - press Pick." : " - keep picking (right-click a gem to remove it).");
            } else if (!game.canAct()) {
                text += "No move possible: press Pass.";
            } else {
                text += "Choose an action.";
            }
        } else if (game.tokensToDiscard() > 0) {
            text += "Return " + game.tokensToDiscard() + " token(s): click your own gems.";
        } else if (game.canEndTurn()) {
            text += "Press End turn.";
        }
        if (state.finalRound) {
            text = "Last round: every card is gone.  " + text;
        }
        if (hint != null) {
            text = hint + "  " + text;
            hint = null;
        }
        statusLabel.setText(text);
    }

    @Override public void render(float delta) {
        ScreenUtils.clear(0.45f, 0.30f, 0.18f, 1f);
        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int w, int h) { stage.getViewport().update(w, h, true); }

    @Override public void dispose() {
        stage.dispose(); bigFont.dispose(); smallFont.dispose(); captionFont.dispose(); Gfx.disposeAll();
    }
}

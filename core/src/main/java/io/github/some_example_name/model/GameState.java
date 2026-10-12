package io.github.some_example_name.model;

import java.util.EnumMap;

import com.badlogic.gdx.utils.Array;

import io.github.some_example_name.data.CardCatalog;
import io.github.some_example_name.data.CardInfo;
import io.github.some_example_name.data.DrawPile;
import io.github.some_example_name.data.NobleInfo;

public class GameState {
    public static final int WIN_POINTS = 15;
    public static final int TIERS = 3;                  // card levels 1, 2, 3
    public static final int FACE_UP_PER_TIER = 3;       // cards shown per level (same as GameScreen.FACE_UP_PER_LEVEL)
    public static final int MAX_NOBLES = 4;             // the screen has room for 4 nobles (GameScreen.NOBLES_SHOWN)
    public static final int GOLD_TOKENS = 5;

    // The decks are the catalog's piles: catalog.level(tier) for cards, catalog.nobles for the nobles not in play.
    // Drawing takes a random card out of a pile, so pile.size() is always "cards left".
    public final CardCatalog catalog;
    // Face-up cards: one row per level. Use row(tier) instead of market.get(tier - 1).
    public final Array<Array<CardInfo>> market = new Array<>();
    public final Array<NobleInfo> nobles = new Array<>();         // nobles on the board (not the pile in catalog)
    public final Array<Player> players = new Array<>();
    public final EnumMap<Gem, Integer> bank = new EnumMap<>(Gem.class);

    public int currentPlayer;
    public boolean finalRound;      // someone reached WIN_POINTS; finish the round
    public boolean gameOver;

    // private: make a game with GameState.newGame(...) below, not with "new GameState".
    private GameState(CardCatalog catalog) { this.catalog = catalog; }

    // New game with the cards and nobles read from the CSV files (CardCatalog.load()).
    // The face-up cards and the nobles are drawn from the catalog's piles, so the catalog changes.
    public static GameState newGame(int playerCount, CardCatalog catalog) {
        if (playerCount < 2 || playerCount > 4) {
            throw new IllegalArgumentException("2-4 players");
        }
        int nobleCount = Math.min(playerCount + 1, MAX_NOBLES);
        if (catalog.nobles.size() < nobleCount) {
            throw new IllegalArgumentException("need " + nobleCount + " nobles, got " + catalog.nobles.size());
        }

        GameState g = new GameState(catalog);
        for (int i = 1; i <= playerCount; i++) {
            g.players.add(new Player("P" + i));         // same names as the screen's panels
        }
        g.setupBank(playerCount);
        g.setupMarket();
        Array<NobleInfo> drawn = catalog.nobles.draw(nobleCount);   // taken out of the pile...
        g.nobles.addAll(drawn);                                     // ...and put on the board
        return g;
    }

    // Tokens per colour depend on the number of players: 2 -> 4, 3 -> 5, 4 -> 7. Plus 5 gold.
    private void setupBank(int playerCount) {
        int perColour;
        if (playerCount == 2) {
            perColour = 4;
        } else if (playerCount == 3) {
            perColour = 5;
        } else {
            perColour = 7;
        }
        for (Gem gem : Gem.BASIC) {
            bank.put(gem, perColour);
        }
        bank.put(Gem.GOLD, GOLD_TOKENS);
    }

    // Draw FACE_UP_PER_TIER cards from each level's pile (fewer if the pile is small).
    private void setupMarket() {
        for (int tier = 1; tier <= TIERS; tier++) {
            market.add(deck(tier).draw(FACE_UP_PER_TIER));
        }
    }

    public Player current() { return players.get(currentPlayer); }

    // The face-up cards of a level (1, 2 or 3). The list starts at 0, so level 1 is market.get(0).
    public Array<CardInfo> row(int tier) { return market.get(tier - 1); }

    public DrawPile<CardInfo> deck(int tier) { return catalog.level(tier); }

    // Take a random card from a deck. Returns null if the deck is empty.
    public CardInfo draw(int tier) { return deck(tier).draw(); }

    // Put a new card from the deck into the empty spot on the board.
    // If the deck is empty, the spot is removed.
    public void refill(int tier, int index) {
        CardInfo next = draw(tier);
        if (next != null) {
            row(tier).set(index, next);
        } else {
            row(tier).removeIndex(index);
        }
    }

    // Which level (1-3) shows this card on the board, or 0 if it is not on the board.
    // Looks at the rows, not card.tier, so a CSV row with a wrong tier cannot break the board.
    public int tierOnBoard(CardInfo card) {
        for (int tier = 1; tier <= TIERS; tier++) {
            if (row(tier).contains(card, true)) {       // true = this exact card object
                return tier;
            }
        }
        return 0;
    }

    public boolean isOnBoard(CardInfo card) { return tierOnBoard(card) != 0; }

    // Who won: the most points wins.
    // Same points -> the player with fewer cards wins.
    // Still the same -> they all win together.
    public Array<Player> winners() {
        // 1. the highest score
        int bestScore = 0;
        for (Player p : players) {
            bestScore = Math.max(bestScore, p.score());
        }

        // 2. among the players with that score, the fewest cards
        int fewestCards = Integer.MAX_VALUE;
        for (Player p : players) {
            if (p.score() == bestScore) {
                fewestCards = Math.min(fewestCards, p.cards.size);
            }
        }

        // 3. everyone with that score and that number of cards wins
        Array<Player> result = new Array<>();
        for (Player p : players) {
            if (p.score() == bestScore && p.cards.size == fewestCards) {
                result.add(p);
            }
        }
        return result;
    }

    // Just one winner. Use winners() if more than one player can win.
    public Player winner() { return winners().first(); }
}

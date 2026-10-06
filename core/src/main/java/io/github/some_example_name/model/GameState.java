package io.github.some_example_name.model;

import java.util.EnumMap;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public class GameState {
    public static final int WIN_POINTS = 15;
    public static final int TIERS = 3;                  // card levels 1, 2, 3
    public static final int FACE_UP_PER_TIER = 4;       // cards shown on the board per level
    public static final int GOLD_TOKENS = 5;

    public final Array<Array<Card>> decks = new Array<>();    // index 0 = tier 1
    public final Array<Array<Card>> market = new Array<>();   // face-up cards
    public final Array<Noble> nobles = new Array<>();
    public final Array<Player> players = new Array<>();
    public final EnumMap<Gem, Integer> bank = new EnumMap<>(Gem.class);

    public int currentPlayer;
    public boolean finalRound;      // someone reached WIN_POINTS; finish the round
    public boolean gameOver;

    public static GameState newGame() { return newGame(4); }

    // New game with random test cards (used until the real cards are passed in).
    public static GameState newGame(int playerCount) {
        int[] deckSizes = {40, 30, 20};                 // official number of cards per tier
        Array<Card> cards = new Array<>();
        int id = 0;
        for (int tier = 1; tier <= TIERS; tier++) {
            for (int i = 0; i < deckSizes[tier - 1]; i++) {
                id++;
                cards.add(randomCard(id, tier));
            }
        }
        return newGame(playerCount, cards, allNobles());
    }

    // New game using the cards and nobles given from outside (e.g. read from CSV).
    // Cards can be in any order; they are put into decks by their tier.
    // The given lists are copied, so they are not changed.
    public static GameState newGame(int playerCount, Array<Card> cards, Array<Noble> nobles) {
        if (playerCount < 2 || playerCount > 4) {
            throw new IllegalArgumentException("2-4 players");
        }
        int nobleCount = playerCount + 1;
        if (nobles.size < nobleCount) {
            throw new IllegalArgumentException("need " + nobleCount + " nobles, got " + nobles.size);
        }

        GameState g = new GameState();
        for (int i = 1; i <= playerCount; i++) {
            g.players.add(new Player("Player " + i));
        }
        g.setupBank(playerCount);
        g.setupDecks(cards);
        g.setupNobles(nobles, nobleCount);
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

    // Put every card into the deck of its tier, shuffle each deck, then show 4 cards per tier.
    private void setupDecks(Array<Card> cards) {
        for (int tier = 1; tier <= TIERS; tier++) {
            decks.add(new Array<Card>());
            market.add(new Array<Card>());
        }
        for (Card c : cards) {
            if (c.tier < 1 || c.tier > TIERS) {
                throw new IllegalArgumentException("card " + c.id + " has tier " + c.tier);
            }
            decks.get(c.tier - 1).add(c);
        }
        for (int tier = 1; tier <= TIERS; tier++) {
            Array<Card> deck = decks.get(tier - 1);
            Array<Card> row = market.get(tier - 1);
            deck.shuffle();
            while (row.size < FACE_UP_PER_TIER && deck.size > 0) {   // fewer if the deck is small
                row.add(deck.pop());
            }
        }
    }

    // Shuffle the nobles and put `count` of them on the board.
    private void setupNobles(Array<Noble> allNobles, int count) {
        Array<Noble> pool = new Array<>(allNobles);     // copy, so the given list is not shuffled
        pool.shuffle();
        for (int i = 0; i < count; i++) {
            nobles.add(pool.pop());
        }
    }

    public Player current() { return players.get(currentPlayer); }

    // Take the top card of a deck. Returns null if the deck is empty.
    public Card draw(int tier) {
        Array<Card> deck = decks.get(tier - 1);
        if (deck.size == 0) return null;
        return deck.pop();
    }

    // Put a new card from the deck into the empty spot on the board.
    // If the deck is empty, the spot is removed.
    public void refill(int tier, int index) {
        Card next = draw(tier);
        Array<Card> row = market.get(tier - 1);
        if (next != null) {
            row.set(index, next);
        } else {
            row.removeIndex(index);
        }
    }

    // Who won: the most points wins.
    // Same points -> the player with fewer cards wins.
    // Still the same -> they all win together.
    public Array<Player> winners() {
        Array<Player> best = new Array<>();
        for (Player p : players) {
            if (best.size == 0 || ranksAbove(p, best.first())) {
                best.clear();
                best.add(p);
            } else if (!ranksAbove(best.first(), p)) {
                best.add(p);                            // exactly tied
            }
        }
        return best;
    }

    // Just one winner. Use winners() if more than one player can win.
    public Player winner() { return winners().first(); }

    // True if player a is better than b: more points, or same points with fewer cards.
    private static boolean ranksAbove(Player a, Player b) {
        if (a.score() != b.score()) return a.score() > b.score();
        return a.cards.size < b.cards.size;
    }

    // ---- placeholder data (used by newGame(playerCount) until the real cards are passed in) ----

    private static Card randomCard(int id, int tier) {
        Gem bonus = randomColour();
        int points;
        if (tier == 1) {
            points = MathUtils.random(0, 1);
        } else if (tier == 2) {
            points = MathUtils.random(1, 3);
        } else {
            points = MathUtils.random(3, 5);
        }
        Card c = new Card(id, tier, points, bonus);

        // price: pick a random colour 2-3 times (never the card's own colour)
        int picks = MathUtils.random(2, 3);
        for (int i = 0; i < picks; i++) {
            Gem gem = randomColour();
            if (gem != bonus) {
                int amount = MathUtils.random(1, tier + 2);
                c.cost.put(gem, c.cost.getOrDefault(gem, 0) + amount);
            }
        }
        if (c.cost.isEmpty()) {                         // every card must cost something
            Gem other = Gem.WHITE;
            if (bonus == Gem.WHITE) other = Gem.BLUE;
            c.cost.put(other, tier + 1);
        }
        return c;
    }

    private static Gem randomColour() {
        return Gem.BASIC[MathUtils.random(0, Gem.BASIC.length - 1)];
    }

    // The 10 official nobles (same as Nobles.csv). Each row = cards needed per colour.
    private static Array<Noble> allNobles() {
        int[][] needs = {
            // white, blue, green, red, black   (same order as Gem.BASIC)
            {0, 3, 3, 3, 0},
            {4, 0, 0, 0, 4},
            {0, 0, 0, 4, 4},
            {4, 4, 0, 0, 0},
            {0, 0, 4, 4, 0},
            {3, 0, 0, 3, 3},
            {3, 3, 0, 0, 3},
            {0, 4, 4, 0, 0},
            {3, 3, 3, 0, 0},
            {0, 0, 3, 3, 3},
        };
        Array<Noble> list = new Array<>();
        for (int i = 0; i < needs.length; i++) {
            Noble noble = new Noble(i + 1);
            for (int colour = 0; colour < Gem.BASIC.length; colour++) {
                if (needs[i][colour] > 0) {
                    noble.cost.put(Gem.BASIC[colour], needs[i][colour]);
                }
            }
            list.add(noble);
        }
        return list;
    }
}

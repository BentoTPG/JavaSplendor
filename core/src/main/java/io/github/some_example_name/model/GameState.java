package io.github.some_example_name.model;

import java.util.EnumMap;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public class GameState {
    public static final int WIN_POINTS = 15;

    public final Array<Array<Card>> decks = new Array<>();    // index 0 = tier 1
    public final Array<Array<Card>> market = new Array<>();   // face-up cards
    public final Array<Noble> nobles = new Array<>();
    public final Array<Player> players = new Array<>();
    public final EnumMap<Gem, Integer> bank = new EnumMap<>(Gem.class);

    public int currentPlayer;
    public boolean finalRound;      // someone reached WIN_POINTS; finish the round
    public boolean gameOver;

    public static GameState newGame() { return newGame(4); }

    public static GameState newGame(int playerCount) {
        if (playerCount < 2 || playerCount > 4) throw new IllegalArgumentException("2-4 players");
        GameState g = new GameState();
        for (int i = 1; i <= playerCount; i++) g.players.add(new Player("Player " + i));

        int each = playerCount == 2 ? 4 : playerCount == 3 ? 5 : 7;
        for (Gem gem : Gem.BASIC) g.bank.put(gem, each);
        g.bank.put(Gem.GOLD, 5);

        int[] sizes = {40, 30, 20};                           // official deck sizes
        for (int t = 1; t <= 3; t++) {
            Array<Card> deck = new Array<>();
            for (int i = 0; i < sizes[t - 1]; i++) deck.add(randomCard(t));
            deck.shuffle();
            Array<Card> row = new Array<>();
            for (int i = 0; i < 4; i++) row.add(deck.pop());  // deal 4 face-up
            g.decks.add(deck);
            g.market.add(row);
        }

        Array<Noble> all = allNobles();
        all.shuffle();
        for (int i = 0; i < playerCount + 1; i++) g.nobles.add(all.pop());
        return g;
    }

    public Player current() { return players.get(currentPlayer); }

    /** Draw the top card of a tier's deck, or null if empty. */
    public Card draw(int tier) {
        Array<Card> deck = decks.get(tier - 1);
        return deck.size == 0 ? null : deck.pop();
    }

    /** Fill the slot at index with the next card of the tier (or drop the slot if the deck is empty). */
    public void refill(int tier, int index) {
        Card next = draw(tier);
        Array<Card> row = market.get(tier - 1);
        if (next != null) row.set(index, next); else row.removeIndex(index);
    }

    public Player winner() {
        Player best = null;
        for (Player p : players) {
            if (best == null || p.score() > best.score()
                || (p.score() == best.score() && p.cards.size < best.cards.size)) best = p;
        }
        return best;
    }

    // Placeholder card data until the real 90-card list is added.
    private static Card randomCard(int tier) {
        Gem[] gems = Gem.BASIC;
        Gem bonus = gems[MathUtils.random(0, 4)];
        int pts = tier == 1 ? MathUtils.random(0, 1)
                : tier == 2 ? MathUtils.random(1, 3) : MathUtils.random(3, 5);
        Card c = new Card(tier, pts, bonus);
        for (int i = 0; i < MathUtils.random(2, 3); i++) {
            Gem g = gems[MathUtils.random(0, 4)];
            if (g != bonus) c.cost.merge(g, MathUtils.random(1, tier + 2), Integer::sum);
        }
        if (c.cost.isEmpty()) c.cost.put(bonus == Gem.WHITE ? Gem.BLUE : Gem.WHITE, tier + 1);
        return c;
    }

    // Five 4+4 nobles and five 3+3+3 nobles (each worth 3 points).
    private static Array<Noble> allNobles() {
        Array<Noble> list = new Array<>();
        Gem[] b = Gem.BASIC;
        int id = 0;
        for (int i = 0; i < 5; i++) {
            Noble n = new Noble(id++);
            n.cost.put(b[i], 4); n.cost.put(b[(i + 1) % 5], 4);
            list.add(n);
        }
        for (int i = 0; i < 5; i++) {
            Noble n = new Noble(id++);
            n.cost.put(b[i], 3); n.cost.put(b[(i + 2) % 5], 3); n.cost.put(b[(i + 3) % 5], 3);
            list.add(n);
        }
        return list;
    }
}

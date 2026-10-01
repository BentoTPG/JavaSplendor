package io.github.some_example_name.model;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public class GameState {
    public final Array<Array<Card>> decks = new Array<>();    // index 0 = tier 1
    public final Array<Array<Card>> market = new Array<>();   // face-up cards

    public static GameState newGame() {
        GameState g = new GameState();
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
        return g;
    }

    private static Card randomCard(int tier) {
        Gem[] gems = Gem.values();
        Gem bonus = gems[MathUtils.random(0, 4)];             // 0..4 = no gold
        int pts = tier == 1 ? MathUtils.random(0, 1)
                : tier == 2 ? MathUtils.random(1, 3) : MathUtils.random(3, 5);
        Card c = new Card(tier, pts, bonus);
        for (int i = 0; i < MathUtils.random(2, 3); i++) {
            Gem g = gems[MathUtils.random(0, 4)];
            if (g != bonus) c.cost.merge(g, MathUtils.random(1, tier + 2), Integer::sum);
        }
        return c;
    }
}

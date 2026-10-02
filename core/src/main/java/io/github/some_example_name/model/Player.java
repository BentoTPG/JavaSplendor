package io.github.some_example_name.model;

import java.util.EnumMap;

import com.badlogic.gdx.utils.Array;

public class Player {
    public final String name;
    public final EnumMap<Gem, Integer> tokens = new EnumMap<>(Gem.class);
    public final Array<Card> cards = new Array<>();
    public final Array<Card> reserved = new Array<>();
    public final Array<Noble> nobles = new Array<>();

    public Player(String name) {
        this.name = name;
        for (Gem g : Gem.values()) tokens.put(g, 0);
    }

    public int tokenCount(Gem g) { return tokens.get(g); }

    public int totalTokens() {
        int sum = 0;
        for (int n : tokens.values()) sum += n;
        return sum;
    }

    public void addToken(Gem g, int n) { tokens.put(g, tokens.get(g) + n); }

    public void removeToken(Gem g, int n) {
        if (tokens.get(g) < n) throw new IllegalStateException("Not enough " + g + " tokens");
        tokens.put(g, tokens.get(g) - n);
    }

    /** Permanent discount: number of owned cards that give this gem. */
    public int bonus(Gem g) {
        int n = 0;
        for (Card c : cards) if (c.bonus == g) n++;
        return n;
    }

    public void addCard(Card c) { cards.add(c); }
    public void addReservedCard(Card c) { reserved.add(c); }
    public void addNoble(Noble n) { nobles.add(n); }

    public int score() {
        int s = 0;
        for (Card c : cards) s += c.points;
        for (Noble n : nobles) s += n.points;
        return s;
    }
}

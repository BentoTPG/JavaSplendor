package io.github.some_example_name.model;

import java.util.EnumMap;

import com.badlogic.gdx.utils.Array;

public class Player {
    public final String name;
    public final EnumMap<Gem, Integer> tokens = new EnumMap<>(Gem.class);
    public final Array<Card> cards = new Array<>();
    public final Array<Card> reserved = new Array<>();
    public final Array<Noble> nobles = new Array<>();
    private final Array<Card> hiddenReserved = new Array<>();   // reserved face down from a deck

    public Player(String name) {
        this.name = name;
        for (Gem g : Gem.values()) {
            tokens.put(g, 0);
        }
    }

    public int tokenCount(Gem g) { return tokens.get(g); }

    public int totalTokens() {
        int sum = 0;
        for (int n : tokens.values()) {
            sum += n;
        }
        return sum;
    }

    public void addToken(Gem g, int n) { tokens.put(g, tokens.get(g) + n); }

    public void removeToken(Gem g, int n) {
        if (tokens.get(g) < n) {
            throw new IllegalStateException("Not enough " + g + " tokens");
        }
        tokens.put(g, tokens.get(g) - n);
    }

    // Discount for this colour = how many bought cards give this colour.
    public int bonus(Gem g) {
        int n = 0;
        for (Card c : cards) {
            if (c.bonus == g) {
                n++;
            }
        }
        return n;
    }

    public void addCard(Card c) { cards.add(c); }

    // hidden = true when the card came face down from a deck (only this player can see it).
    public void addReservedCard(Card c, boolean hidden) {
        reserved.add(c);
        if (hidden) {
            hiddenReserved.add(c);
        }
    }

    // Remove a reserved card (when it is bought). Returns false if the player did not reserve it.
    public boolean removeReservedCard(Card c) {
        hiddenReserved.removeValue(c, true);
        return reserved.removeValue(c, true);
    }

    // True if other players must see this reserved card face down.
    public boolean isHidden(Card c) { return hiddenReserved.contains(c, true); }

    public void addNoble(Noble n) { nobles.add(n); }

    public int score() {
        int s = 0;
        for (Card c : cards) {
            s += c.points;
        }
        for (Noble n : nobles) {
            s += n.points;
        }
        return s;
    }
}

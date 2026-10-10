package io.github.some_example_name.model;

import java.util.EnumMap;
import java.util.Map;

import com.badlogic.gdx.utils.Array;

import io.github.some_example_name.data.CardInfo;
import io.github.some_example_name.data.NobleInfo;

// Cards and nobles are the CSV rows (CardInfo, NobleInfo), the same objects the screen draws.
public class Player {
    public final String name;
    public final EnumMap<Gem, Integer> tokens = new EnumMap<>(Gem.class);
    public final Array<CardInfo> cards = new Array<>();
    public final Array<CardInfo> reserved = new Array<>();
    public final Array<NobleInfo> nobles = new Array<>();
    private final Array<CardInfo> hiddenReserved = new Array<>();   // reserved face down from a deck

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
        for (CardInfo c : cards) {
            if (c.bonus == g) {
                n++;
            }
        }
        return n;
    }

    // How many tokens of each colour this player must pay for the card.
    // Card bonuses lower the price first, then gold covers any missing colour.
    // Returns null if the player cannot afford it.
    public EnumMap<Gem, Integer> paymentFor(CardInfo card) {
        EnumMap<Gem, Integer> payment = new EnumMap<>(Gem.class);
        int goldNeeded = 0;

        for (Map.Entry<Gem, Integer> entry : card.cost.entrySet()) {
            Gem gem = entry.getKey();
            int price = entry.getValue();

            // 1. bonuses from bought cards lower the price (never below 0)
            int stillNeed = Math.max(0, price - bonus(gem));

            // 2. pay with tokens of this colour, as many as the player has
            int payWithColour = Math.min(stillNeed, tokenCount(gem));
            if (payWithColour > 0) {
                payment.put(gem, payWithColour);
            }

            // 3. whatever is still missing has to be paid with gold
            goldNeeded += stillNeed - payWithColour;
        }

        if (goldNeeded > tokenCount(Gem.GOLD)) {
            return null;                // not enough gold: cannot buy
        }
        if (goldNeeded > 0) {
            payment.put(Gem.GOLD, goldNeeded);
        }
        return payment;
    }

    public boolean canAfford(CardInfo card) { return paymentFor(card) != null; }

    // True if the player has enough cards of every colour this noble needs.
    public boolean qualifiesFor(NobleInfo noble) {
        for (Map.Entry<Gem, Integer> need : noble.requires.entrySet()) {
            if (bonus(need.getKey()) < need.getValue()) {
                return false;
            }
        }
        return true;
    }

    public void addCard(CardInfo c) { cards.add(c); }

    // hidden = true when the card came face down from a deck (only this player can see it).
    public void addReservedCard(CardInfo c, boolean hidden) {
        reserved.add(c);
        if (hidden) {
            hiddenReserved.add(c);
        }
    }

    // Remove a reserved card (when it is bought). Returns false if the player did not reserve it.
    public boolean removeReservedCard(CardInfo c) {
        hiddenReserved.removeValue(c, true);
        return reserved.removeValue(c, true);
    }

    // True if other players must see this reserved card face down.
    public boolean isHidden(CardInfo c) { return hiddenReserved.contains(c, true); }

    public void addNoble(NobleInfo n) { nobles.add(n); }

    public int score() {
        int s = 0;
        for (CardInfo c : cards) {
            s += c.points;
        }
        for (NobleInfo n : nobles) {
            s += n.points;
        }
        return s;
    }
}

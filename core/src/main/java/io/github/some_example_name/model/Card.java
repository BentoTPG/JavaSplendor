package io.github.some_example_name.model;

import java.util.EnumMap;
import java.util.Map;

public class Card {
    public final int id;
    public final int tier;              // 1, 2 or 3
    public final int points;
    public final Gem bonus;             // colour discount this card gives once it is bought
    public final EnumMap<Gem, Integer> cost = new EnumMap<>(Gem.class);

    public Card(int id, int tier, int points, Gem bonus) {
        this.id = id;
        this.tier = tier;
        this.points = points;
        this.bonus = bonus;
    }

    // How many tokens of each colour the player must pay for this card.
    // Card bonuses lower the price first, then gold covers any missing colour.
    // Returns null if the player cannot afford it.
    public EnumMap<Gem, Integer> paymentFor(Player p) {
        EnumMap<Gem, Integer> payment = new EnumMap<>(Gem.class);
        int goldNeeded = 0;

        for (Map.Entry<Gem, Integer> entry : cost.entrySet()) {
            Gem gem = entry.getKey();
            int price = entry.getValue();

            // 1. bonuses from bought cards lower the price (never below 0)
            int stillNeed = Math.max(0, price - p.bonus(gem));

            // 2. pay with tokens of this colour, as many as the player has
            int payWithColour = Math.min(stillNeed, p.tokenCount(gem));
            if (payWithColour > 0) {
                payment.put(gem, payWithColour);
            }

            // 3. whatever is still missing has to be paid with gold
            goldNeeded += stillNeed - payWithColour;
        }

        if (goldNeeded > p.tokenCount(Gem.GOLD)) {
            return null;                // not enough gold: cannot buy
        }
        if (goldNeeded > 0) {
            payment.put(Gem.GOLD, goldNeeded);
        }
        return payment;
    }

    public boolean isBuyable(Player p) { return paymentFor(p) != null; }
}

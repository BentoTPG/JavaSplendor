package io.github.some_example_name.model;

import java.util.EnumMap;
import java.util.Map;

public class Card {
    public final int tier, points;
    public final Gem bonus;
    public final EnumMap<Gem, Integer> cost = new EnumMap<>(Gem.class);

    public Card(int tier, int points, Gem bonus) {
        this.tier = tier; this.points = points; this.bonus = bonus;
    }

    /**
     * Tokens the player would have to pay (bonuses applied, gold covering any shortfall),
     * or null if the player cannot afford this card.
     */
    public EnumMap<Gem, Integer> paymentFor(Player p) {
        EnumMap<Gem, Integer> pay = new EnumMap<>(Gem.class);
        int goldNeeded = 0;
        for (Map.Entry<Gem, Integer> e : cost.entrySet()) {
            Gem g = e.getKey();
            int need = Math.max(0, e.getValue() - p.bonus(g));
            int use = Math.min(need, p.tokenCount(g));
            if (use > 0) pay.put(g, use);
            goldNeeded += need - use;
        }
        if (goldNeeded > p.tokenCount(Gem.GOLD)) return null;
        if (goldNeeded > 0) pay.put(Gem.GOLD, goldNeeded);
        return pay;
    }

    public boolean isBuyable(Player p) { return paymentFor(p) != null; }
}

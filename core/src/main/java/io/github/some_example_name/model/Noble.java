package io.github.some_example_name.model;

import java.util.EnumMap;

public class Noble {
    public final int id;
    public final int points = 3;
    public final EnumMap<Gem, Integer> cost = new EnumMap<>(Gem.class);   // required card bonuses

    public Noble(int id) { this.id = id; }

    /** True if the player's card bonuses meet every requirement. */
    public boolean isAvailable(Player p) {
        for (java.util.Map.Entry<Gem, Integer> e : cost.entrySet())
            if (p.bonus(e.getKey()) < e.getValue()) return false;
        return true;
    }
}

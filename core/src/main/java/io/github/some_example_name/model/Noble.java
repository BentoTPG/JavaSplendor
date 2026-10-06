package io.github.some_example_name.model;

import java.util.EnumMap;
import java.util.Map;

public class Noble {
    public final int id;
    public final int points = 3;                                         // every noble is worth 3 points
    public final EnumMap<Gem, Integer> cost = new EnumMap<>(Gem.class);   // cards needed per colour

    public Noble(int id) { this.id = id; }

    // True if the player has enough cards of every colour this noble needs.
    public boolean isAvailable(Player p) {
        for (Map.Entry<Gem, Integer> need : cost.entrySet()) {
            if (p.bonus(need.getKey()) < need.getValue()) {
                return false;
            }
        }
        return true;
    }
}

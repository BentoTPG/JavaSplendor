package io.github.some_example_name.model;

import java.util.EnumMap;

public class Card {
    public final int tier, points;
    public final Gem bonus;
    public final EnumMap<Gem, Integer> cost = new EnumMap<>(Gem.class);

    public Card(int tier, int points, Gem bonus) {
        this.tier = tier; this.points = points; this.bonus = bonus;
    }
}

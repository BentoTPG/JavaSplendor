package io.github.some_example_name.data;

import java.util.EnumMap;

import io.github.some_example_name.model.Gem;

/** One noble exactly as written in Nobles.csv. Display data only. */
public final class NobleInfo {
    public final int id;
    public final int points;
    /** Card bonuses the player needs. Only colours above 0 are present. */
    public final EnumMap<Gem, Integer> requires;
    /** Image path exactly as written in the CSV (for example "assets/noble_art1.png"), or null. */
    public final String art;

    public NobleInfo(int id, int points, EnumMap<Gem, Integer> requires, String art) {
        this.id = id;
        this.points = points;
        this.requires = requires;
        this.art = art;
    }
}

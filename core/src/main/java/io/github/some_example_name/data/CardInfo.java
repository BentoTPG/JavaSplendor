package io.github.some_example_name.data;

import java.util.EnumMap;

import io.github.some_example_name.model.Gem;

/** One development card exactly as written in a Lv?Card.csv row. Display data only. */
public final class CardInfo {
    public final int id;
    public final int tier;
    public final int points;
    public final Gem bonus;
    /** Price per gem colour. Only colours with a price above 0 are present. */
    public final EnumMap<Gem, Integer> cost;
    /** Image path exactly as written in the CSV (for example "assets/card_art1.png"), or null. */
    public final String art;

    public CardInfo(int id, int tier, int points, Gem bonus, EnumMap<Gem, Integer> cost, String art) {
        this.id = id;
        this.tier = tier;
        this.points = points;
        this.bonus = bonus;
        this.cost = cost;
        this.art = art;
    }
}

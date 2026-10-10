package io.github.some_example_name.ui;

import java.util.EnumMap;

import com.badlogic.gdx.utils.Array;

import io.github.some_example_name.data.CardInfo;
import io.github.some_example_name.model.Gem;

/**
 * What the player panels show about one player. Display data only: the backend fills it from the
 * real player state and asks the screen to redraw; nothing here changes the game.
 */
public class PlayerSnapshot {
    /** A player may hold at most three reserved cards. */
    public static final int MAX_RESERVED = 3;

    public final String name;
    public int points;
    /** Gems in hand, gold included. */
    public final EnumMap<Gem, Integer> gems = new EnumMap<Gem, Integer>(Gem.class);
    /** How many bought cards of each colour (the permanent discount). */
    public final EnumMap<Gem, Integer> bonuses = new EnumMap<Gem, Integer>(Gem.class);
    public final Array<CardInfo> reserved = new Array<CardInfo>();

    public PlayerSnapshot(String name) {
        this.name = name;
    }

    public int gems(Gem gem) {
        Integer n = gems.get(gem);
        return n == null ? 0 : n;
    }

    public int bonus(Gem gem) {
        Integer n = bonuses.get(gem);
        return n == null ? 0 : n;
    }

    /** Sets every gem count at once, in the order white, blue, green, red, black, gold. */
    public PlayerSnapshot withGems(int white, int blue, int green, int red, int black, int gold) {
        gems.put(Gem.WHITE, white); gems.put(Gem.BLUE, blue); gems.put(Gem.GREEN, green);
        gems.put(Gem.RED, red); gems.put(Gem.BLACK, black); gems.put(Gem.GOLD, gold);
        return this;
    }

    /** Sets every card bonus at once, in the order white, blue, green, red, black. */
    public PlayerSnapshot withBonuses(int white, int blue, int green, int red, int black) {
        bonuses.put(Gem.WHITE, white); bonuses.put(Gem.BLUE, blue); bonuses.put(Gem.GREEN, green);
        bonuses.put(Gem.RED, red); bonuses.put(Gem.BLACK, black);
        return this;
    }

    public PlayerSnapshot withPoints(int points) {
        this.points = points;
        return this;
    }
}

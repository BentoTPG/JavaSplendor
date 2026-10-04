package io.github.some_example_name.data;

import java.util.EnumMap;
import java.util.Locale;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.IntSet;
import com.badlogic.gdx.utils.ObjectIntMap;

import io.github.some_example_name.model.Gem;

/**
 * The four CSV files read at start-up and kept apart as four piles:
 * level 1, level 2, level 3 cards and nobles. Draw from a pile to pick random items;
 * drawn items leave the pile.
 */
public final class CardCatalog {
    public static final String LEVEL1_CSV = "data/Lv1Card.csv";
    public static final String LEVEL2_CSV = "data/Lv2Card.csv";
    public static final String LEVEL3_CSV = "data/Lv3Card.csv";
    public static final String NOBLES_CSV = "data/Nobles.csv";

    /** Official sizes of the three card decks and the noble pile. A different count only logs a warning. */
    private static final int[] EXPECTED_CARDS = {40, 30, 20};
    private static final int EXPECTED_NOBLES = 10;

    public final DrawPile<CardInfo> level1;
    public final DrawPile<CardInfo> level2;
    public final DrawPile<CardInfo> level3;
    public final DrawPile<NobleInfo> nobles;

    public CardCatalog(Array<CardInfo> level1, Array<CardInfo> level2, Array<CardInfo> level3,
                       Array<NobleInfo> nobles) {
        this.level1 = new DrawPile<CardInfo>(level1);
        this.level2 = new DrawPile<CardInfo>(level2);
        this.level3 = new DrawPile<CardInfo>(level3);
        this.nobles = new DrawPile<NobleInfo>(nobles);
    }

    /** Reads the four CSV files from the assets folder. Stops with a clear message on a bad row. */
    public static CardCatalog load() {
        return new CardCatalog(
                parseCards(read(LEVEL1_CSV), LEVEL1_CSV, 1),
                parseCards(read(LEVEL2_CSV), LEVEL2_CSV, 2),
                parseCards(read(LEVEL3_CSV), LEVEL3_CSV, 3),
                parseNobles(read(NOBLES_CSV), NOBLES_CSV));
    }

    /** The card pile of a level (1, 2 or 3). */
    public DrawPile<CardInfo> level(int tier) {
        switch (tier) {
            case 1: return level1;
            case 2: return level2;
            case 3: return level3;
            default: throw new IllegalArgumentException("tier must be 1, 2 or 3 but was " + tier);
        }
    }

    private static String read(String path) {
        return Gdx.files.internal(path).readString("UTF-8");
    }

    // ---- parsing (plain strings in, so it can be tested without starting libGDX) ----

    /**
     * Columns (any order, header names are not case sensitive):
     * id, tier, points, bonus, white, blue, green, red, black, art.
     */
    public static Array<CardInfo> parseCards(String csv, String fileName, int expectedTier) {
        CsvTable t = new CsvTable(csv, fileName,
                "id", "tier", "points", "bonus", "white", "blue", "green", "red", "black");
        Array<CardInfo> out = new Array<CardInfo>();
        IntSet ids = new IntSet();
        for (int r = 0; r < t.rows.size; r++) {
            String[] f = t.rows.get(r);
            try {
                int id = t.integer(f, "id");
                if (!ids.add(id)) throw new IllegalArgumentException("duplicate id " + id);
                int tier = t.integer(f, "tier");
                if (tier != expectedTier) {
                    warn(fileName + " line " + t.lineNumbers.get(r) + ": tier is " + tier
                            + " but this file is level " + expectedTier);
                }
                int points = t.integer(f, "points");
                Gem bonus = gem(t.cell(f, "bonus"));
                EnumMap<Gem, Integer> cost = readGems(t, f);
                out.add(new CardInfo(id, tier, points, bonus, cost, t.optional(f, "art")));
            } catch (RuntimeException e) {
                throw t.error(r, e);
            }
        }
        if (expectedTier >= 1 && expectedTier <= 3 && out.size != EXPECTED_CARDS[expectedTier - 1]) {
            warn(fileName + ": expected " + EXPECTED_CARDS[expectedTier - 1] + " cards but found " + out.size);
        }
        return out;
    }

    /** Columns (any order): id, points, white, blue, green, red, black, art. */
    public static Array<NobleInfo> parseNobles(String csv, String fileName) {
        CsvTable t = new CsvTable(csv, fileName, "id", "points", "white", "blue", "green", "red", "black");
        Array<NobleInfo> out = new Array<NobleInfo>();
        IntSet ids = new IntSet();
        for (int r = 0; r < t.rows.size; r++) {
            String[] f = t.rows.get(r);
            try {
                int id = t.integer(f, "id");
                if (!ids.add(id)) throw new IllegalArgumentException("duplicate id " + id);
                int points = t.integer(f, "points");
                out.add(new NobleInfo(id, points, readGems(t, f), t.optional(f, "art")));
            } catch (RuntimeException e) {
                throw t.error(r, e);
            }
        }
        if (out.size != EXPECTED_NOBLES) {
            warn(fileName + ": expected " + EXPECTED_NOBLES + " nobles but found " + out.size);
        }
        return out;
    }

    /** Reads the white, blue, green, red, black columns into a map; zero columns are left out. */
    private static EnumMap<Gem, Integer> readGems(CsvTable t, String[] f) {
        EnumMap<Gem, Integer> map = new EnumMap<Gem, Integer>(Gem.class);
        for (Gem g : Gem.BASIC) {
            String column = g.name().toLowerCase(Locale.ROOT);
            int n = t.integer(f, column);
            if (n < 0) throw new IllegalArgumentException("'" + column + "' cannot be negative");
            if (n > 0) map.put(g, n);
        }
        return map;
    }

    private static Gem gem(String name) {
        try {
            Gem g = Gem.valueOf(name.toUpperCase(Locale.ROOT));
            if (g == Gem.GOLD) throw new IllegalArgumentException();
            return g;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "unknown bonus '" + name + "' (use WHITE, BLUE, GREEN, RED or BLACK)");
        }
    }

    private static void warn(String message) {
        if (Gdx.app != null) Gdx.app.error("CardCatalog", message);
        else System.err.println("CardCatalog: " + message);
    }

    /** A header row plus data rows. Blank lines and lines starting with # are skipped. */
    private static final class CsvTable {
        final String file;
        final ObjectIntMap<String> columns = new ObjectIntMap<String>();   // lower-case header -> index
        final Array<String[]> rows = new Array<String[]>();
        final Array<String> rawLines = new Array<String>();
        final IntArray lineNumbers = new IntArray();

        CsvTable(String csv, String file, String... required) {
            this.file = file;
            if (csv.length() > 0 && csv.charAt(0) == '﻿') csv = csv.substring(1);   // Excel's UTF-8 marker
            String[] lines = csv.split("\\r?\\n");
            boolean headerSeen = false;
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] cells = line.split(",", -1);
                if (!headerSeen) {
                    for (int c = 0; c < cells.length; c++) {
                        columns.put(cells[c].trim().toLowerCase(Locale.ROOT), c);
                    }
                    headerSeen = true;
                    continue;
                }
                rows.add(cells);
                rawLines.add(line);
                lineNumbers.add(i + 1);
            }
            if (!headerSeen) throw new GdxRuntimeException(file + ": the file is empty");
            for (String name : required) {
                if (!columns.containsKey(name)) {
                    throw new GdxRuntimeException(file + ": missing column '" + name + "' in the header row");
                }
            }
        }

        String cell(String[] f, String name) {
            int index = columns.get(name, -1);
            if (index < 0 || index >= f.length) throw new IllegalArgumentException("no value for '" + name + "'");
            return f[index].trim();
        }

        int integer(String[] f, String name) {
            String v = cell(f, name);
            try {
                return Integer.parseInt(v);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("'" + name + "' must be a whole number but was '" + v + "'");
            }
        }

        /** The cell's text, or null when the column is missing or the cell is empty. */
        String optional(String[] f, String name) {
            int index = columns.get(name, -1);
            if (index < 0 || index >= f.length) return null;
            String v = f[index].trim();
            return v.isEmpty() ? null : v;
        }

        GdxRuntimeException error(int row, RuntimeException cause) {
            return new GdxRuntimeException(
                    file + " line " + lineNumbers.get(row) + ": " + rawLines.get(row) + " -> " + cause.getMessage(),
                    cause);
        }
    }
}

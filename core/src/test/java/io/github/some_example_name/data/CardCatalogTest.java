package io.github.some_example_name.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

import io.github.some_example_name.model.Gem;

public class CardCatalogTest {
    private static final String CARD_HEADER = "id,tier,points,bonus,white,blue,green,red,black,art\n";

    /** The tests run inside the core folder, so the real CSV files are one level up in assets/data. */
    private static String csv(String name) throws IOException {
        return new String(Files.readAllBytes(Paths.get("..", "assets", "data", name)), StandardCharsets.UTF_8);
    }

    private static Array<CardInfo> cards(String file, int tier) throws IOException {
        return CardCatalog.parseCards(csv(file), file, tier);
    }

    private static CardCatalog realCatalog() throws IOException {
        return new CardCatalog(cards("Lv1Card.csv", 1), cards("Lv2Card.csv", 2), cards("Lv3Card.csv", 3),
                CardCatalog.parseNobles(csv("Nobles.csv"), "Nobles.csv"));
    }

    @Test
    public void realFilesHaveTheOfficialSizes() throws IOException {
        CardCatalog c = realCatalog();
        assertEquals(40, c.level1.size());
        assertEquals(30, c.level2.size());
        assertEquals(20, c.level3.size());
        assertEquals(10, c.nobles.size());
    }

    @Test
    public void firstLevel1RowIsReadCorrectly() throws IOException {
        CardInfo c = cards("Lv1Card.csv", 1).first();
        assertEquals(1, c.id);
        assertEquals(1, c.tier);
        assertEquals(0, c.points);
        assertEquals(Gem.BLACK, c.bonus);
        assertEquals(4, c.cost.size());                     // black costs 0 and is left out
        assertEquals(1, (int) c.cost.get(Gem.WHITE));
        assertEquals(1, (int) c.cost.get(Gem.RED));
        assertEquals("assets/card_art1.png", c.art);
    }

    @Test
    public void firstNobleIsReadCorrectly() throws IOException {
        NobleInfo n = CardCatalog.parseNobles(csv("Nobles.csv"), "Nobles.csv").first();
        assertEquals(1, n.id);
        assertEquals(3, n.points);
        assertEquals(3, n.requires.size());
        assertEquals(3, (int) n.requires.get(Gem.BLUE));
        assertEquals(3, (int) n.requires.get(Gem.GREEN));
        assertEquals(3, (int) n.requires.get(Gem.RED));
        assertEquals("assets/noble_art1.png", n.art);
    }

    @Test
    public void aDrawnCardNeverComesBackOut() throws IOException {
        DrawPile<CardInfo> pile = realCatalog().level1;
        Set<Integer> seen = new HashSet<Integer>();

        for (CardInfo c : pile.draw(3)) assertTrue("repeated id " + c.id, seen.add(c.id));
        assertEquals(37, pile.size());                      // 40 - 3 drawn: what the "cards left" tile shows

        CardInfo next;
        while ((next = pile.draw()) != null) assertTrue("repeated id " + next.id, seen.add(next.id));
        assertEquals(40, seen.size());                      // every card came out exactly once
        assertEquals(0, pile.size());
        assertTrue(pile.isEmpty());
        assertNull(pile.draw());
    }

    @Test
    public void drawingMoreThanTheRestJustReturnsTheRest() throws IOException {
        DrawPile<NobleInfo> nobles = realCatalog().nobles;
        assertEquals(3, nobles.draw(3).size);
        assertEquals(7, nobles.draw(100).size);
        assertEquals(0, nobles.draw(5).size);
    }

    @Test
    public void everyRealCardAndNobleHasAnImageFile() throws IOException {
        CardCatalog c = realCatalog();
        for (int tier = 1; tier <= 3; tier++) {
            for (CardInfo card : c.level(tier).draw(1000)) assertImageExists(card.art);
        }
        for (NobleInfo noble : c.nobles.draw(1000)) assertImageExists(noble.art);
    }

    private static void assertImageExists(String art) {
        assertTrue("missing image path", art != null);
        // The CSV says "assets/x.png"; from the core folder the file is ../assets/x.png.
        String relative = art.startsWith("assets/") ? art.substring("assets/".length()) : art;
        assertTrue("image file not found: " + art, Files.exists(Paths.get("..", "assets", relative)));
    }

    @Test
    public void aBadRowNamesTheFileTheLineAndTheProblem() {
        String csv = CARD_HEADER
                + "1,1,0,BLACK,1,1,1,1,0,a.png\n"
                + "2,1,0,BLUEE,0,0,2,1,0,b.png\n";
        try {
            CardCatalog.parseCards(csv, "Lv1Card.csv", 1);
            fail("expected an error");
        } catch (GdxRuntimeException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("Lv1Card.csv line 3"));
            assertTrue(e.getMessage(), e.getMessage().contains("BLUEE"));
        }
    }

    @Test
    public void anEmptyPriceCellIsReported() {
        String csv = CARD_HEADER + "1,1,0,BLACK,1,,1,1,0,a.png\n";
        try {
            CardCatalog.parseCards(csv, "Lv1Card.csv", 1);
            fail("expected an error");
        } catch (GdxRuntimeException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("'blue' must be a whole number"));
        }
    }

    @Test
    public void aDuplicateIdIsReported() {
        String csv = CARD_HEADER
                + "1,1,0,BLACK,1,1,1,1,0,a.png\n"
                + "1,1,0,BLUE,0,0,2,1,0,b.png\n";
        try {
            CardCatalog.parseCards(csv, "Lv1Card.csv", 1);
            fail("expected an error");
        } catch (GdxRuntimeException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("duplicate id 1"));
        }
    }

    @Test
    public void aMissingColumnIsReported() {
        try {
            CardCatalog.parseCards("id,tier,points,bonus,white,blue,green,red\n1,1,0,BLACK,1,1,1,1\n",
                    "Lv1Card.csv", 1);
            fail("expected an error");
        } catch (GdxRuntimeException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("missing column 'black'"));
        }
    }

    @Test
    public void columnOrderCommentsAndExcelMarkerDoNotMatter() {
        String csv = "﻿art,black,red,green,blue,white,bonus,points,tier,id\r\n"
                + "# a comment line\r\n"
                + "x.png,0,1,2,0,3,green,2,2,7\r\n"
                + "\r\n";
        Array<CardInfo> out = CardCatalog.parseCards(csv, "Lv2Card.csv", 2);
        assertEquals(1, out.size);
        CardInfo c = out.first();
        assertEquals(7, c.id);
        assertEquals(2, c.points);
        assertEquals(Gem.GREEN, c.bonus);
        assertEquals(3, (int) c.cost.get(Gem.WHITE));
        assertEquals(1, (int) c.cost.get(Gem.RED));
        assertEquals("x.png", c.art);
    }
}

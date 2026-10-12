package io.github.some_example_name.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.EnumMap;

import org.junit.Test;

import com.badlogic.gdx.utils.Array;

import io.github.some_example_name.data.CardCatalog;
import io.github.some_example_name.data.CardInfo;
import io.github.some_example_name.data.NobleInfo;

public class GameManagerTest {

    // ---- setup ----

    @Test
    public void newGameMatchesTheScreen() throws IOException {
        GameState s = GameState.newGame(4, realCatalog());
        for (int tier = 1; tier <= GameState.TIERS; tier++) {
            assertEquals(3, s.row(tier).size);
        }
        assertEquals(37, s.deck(1).size());
        assertEquals(27, s.deck(2).size());
        assertEquals(17, s.deck(3).size());
        assertEquals(4, s.nobles.size);
        assertEquals(6, s.catalog.nobles.size());               // the screen's "Nobles left"
        for (Gem g : Gem.BASIC) {
            assertEquals(7, (int) s.bank.get(g));
        }
        assertEquals(5, (int) s.bank.get(Gem.GOLD));
        assertEquals("P1", s.players.first().name);
    }

    @Test
    public void twoPlayersGetThreeNoblesAndFourTokensPerColour() {
        GameState s = GameState.newGame(2, catalog(farNobles(5)));
        assertEquals(3, s.nobles.size);
        assertEquals(4, (int) s.bank.get(Gem.RED));
    }

    // ---- tokens ----

    @Test
    public void takeThreeColoursThenNoSecondAction() {
        GameManager gm = game();
        assertTrue(gm.takeTokens(Gem.WHITE, Gem.BLUE, Gem.GREEN));
        assertEquals(1, gm.state.current().tokenCount(Gem.BLUE));
        assertEquals(3, (int) gm.state.bank.get(Gem.BLUE));
        assertFalse(gm.takeTokens(Gem.RED, Gem.BLACK, Gem.WHITE));
        assertTrue(gm.endTurn());
        assertEquals(1, gm.state.currentPlayer);
    }

    @Test
    public void twoOfOneColourNeedsFourInTheBank() {
        GameManager gm = game();                                // 2 players: 4 of each colour
        assertTrue(gm.takeTokens(Gem.RED, Gem.RED));
        assertTrue(gm.endTurn());
        assertFalse(gm.canTakeTokens(Gem.RED, Gem.RED));        // only 2 red left
        assertFalse(gm.canTakeTokens(Gem.GOLD, Gem.WHITE, Gem.BLUE));
    }

    @Test
    public void moreThanTenTokensMustBeGivenBack() {
        GameManager gm = game();
        Player p = gm.state.current();
        p.addToken(Gem.BLACK, 9);
        assertTrue(gm.takeTokens(Gem.WHITE, Gem.BLUE, Gem.GREEN));
        assertEquals(2, gm.tokensToDiscard());
        assertFalse(gm.endTurn());
        assertTrue(gm.returnToken(Gem.BLACK));
        assertTrue(gm.returnToken(Gem.BLACK));
        assertFalse(gm.returnToken(Gem.BLACK));                 // nothing owed any more
        assertTrue(gm.endTurn());
    }

    @Test
    public void passWithSevenTokensHasNoPenalty() {
        GameState s = GameState.newGame(2, catalog(farNobles(5)));
        Player p = s.current();
        p.addToken(Gem.BLACK, 7);                               // 7+ tokens when the turn starts
        for (int i = 1; i <= GameManager.MAX_RESERVED; i++) {
            p.addReservedCard(card(i, 1, 0, Gem.RED, 9), false);  // too expensive, and no room to reserve more
        }
        for (Gem g : Gem.BASIC) {
            s.bank.put(g, 0);                                   // no tokens left to take
        }
        GameManager gm = new GameManager(s);

        assertFalse(gm.canAct());
        assertTrue(gm.pass());                                  // no 4-token penalty for a pass
        assertEquals(1, s.currentPlayer);
    }

    // ---- cards ----

    @Test
    public void buyingUsesBonusesThenGoldAndRefillsTheBoard() {
        GameManager gm = game();
        Player p = gm.state.current();
        CardInfo target = gm.state.row(1).first();      // every level-1 test card costs 2 blue
        p.addCard(card(1, 1, 0, Gem.BLUE));                     // 1 blue bonus, so 1 blue still needed
        p.addToken(Gem.GOLD, 1);
        int goldInBank = gm.state.bank.get(Gem.GOLD);

        assertTrue(gm.buyCard(target));
        assertEquals(0, p.tokenCount(Gem.GOLD));
        assertEquals(goldInBank + 1, (int) gm.state.bank.get(Gem.GOLD));
        assertTrue(p.cards.contains(target, true));
        assertFalse(gm.state.row(1).contains(target, true));
        assertEquals(3, gm.state.row(1).size);           // refilled from the deck
        assertEquals(1, gm.state.deck(1).size());               // 5 cards - 3 face up - 1 refill
    }

    @Test
    public void cannotBuyWithoutEnoughTokens() {
        GameManager gm = game();
        assertFalse(gm.buyCard(gm.state.row(1).first()));
        assertFalse(gm.hasActed());
    }

    @Test
    public void reserveGivesGoldAndRefillsTheBoard() {
        GameManager gm = game();
        Player p = gm.state.current();
        CardInfo target = gm.state.row(2).first();
        assertTrue(gm.reserveCard(target));
        assertSame(target, p.reserved.first());
        assertFalse(p.isHidden(target));
        assertEquals(1, p.tokenCount(Gem.GOLD));
        assertEquals(4, (int) gm.state.bank.get(Gem.GOLD));
        assertEquals(3, gm.state.row(2).size);
    }

    @Test
    public void fourthReserveIsRefused() {
        GameManager gm = game();
        Player p = gm.state.current();
        for (int i = 1; i <= GameManager.MAX_RESERVED; i++) {
            p.addReservedCard(card(i, 1, 0, Gem.RED), false);
        }
        assertFalse(gm.reserveCard(gm.state.row(1).first()));
        assertFalse(gm.reserveFromDeck(1));
    }

    @Test
    public void reserveFromDeckIsHiddenFromOthers() {
        GameManager gm = game();
        int before = gm.state.deck(3).size();
        assertTrue(gm.reserveFromDeck(3));
        CardInfo card = gm.state.current().reserved.first();
        assertTrue(gm.state.current().isHidden(card));
        assertEquals(before - 1, gm.state.deck(3).size());
    }

    // ---- nobles and end of game ----

    @Test
    public void nobleVisitsAtTheEndOfTheTurn() {
        Array<NobleInfo> nobles = farNobles(2);
        nobles.add(noble(50, 0, 0, 0, 2, 0));                   // needs 2 red cards
        GameManager gm = new GameManager(GameState.newGame(2, catalog(nobles)));   // 2 players: all 3 are drawn
        Player p = gm.state.current();
        p.addCard(card(1, 1, 1, Gem.RED));
        p.addCard(card(2, 1, 1, Gem.RED));

        assertTrue(gm.takeTokens(Gem.WHITE, Gem.BLUE, Gem.GREEN));
        assertTrue(gm.endTurn());
        assertEquals(1, p.nobles.size);
        assertEquals(5, p.score());                             // 1 + 1 + 3 for the noble
        assertEquals(2, gm.state.nobles.size);
    }

    @Test
    public void twoNoblesAtOnceNeedAChoice() {
        NobleInfo red = noble(50, 0, 0, 0, 2, 0);
        NobleInfo black = noble(51, 0, 0, 0, 0, 2);
        Array<NobleInfo> nobles = farNobles(1);
        nobles.add(red);
        nobles.add(black);
        GameManager gm = new GameManager(GameState.newGame(2, catalog(nobles)));
        Player p = gm.state.current();
        p.addCard(card(1, 1, 0, Gem.RED));
        p.addCard(card(2, 1, 0, Gem.RED));
        p.addCard(card(3, 1, 0, Gem.BLACK));
        p.addCard(card(4, 1, 0, Gem.BLACK));

        assertTrue(gm.takeTokens(Gem.WHITE, Gem.BLUE, Gem.GREEN));
        assertTrue(gm.needsNobleChoice());
        assertFalse(gm.endTurn());
        assertTrue(gm.claimNoble(black));
        assertFalse(gm.claimNoble(red));                        // one noble per turn
        assertTrue(gm.endTurn());
        assertSame(black, p.nobles.first());
    }

    @Test
    public void fifteenPointsEndsTheGameAfterTheRound() {
        GameManager gm = game();
        Player first = gm.state.current();
        for (int i = 1; i <= 3; i++) {
            first.addCard(card(i, 3, 5, Gem.BLACK));            // 15 points
        }
        assertTrue(gm.takeTokens(Gem.WHITE, Gem.BLUE, Gem.GREEN));
        assertTrue(gm.endTurn());
        assertTrue(gm.state.finalRound);
        assertFalse(gm.checkGameOver());                        // the second player still gets a turn

        assertTrue(gm.takeTokens(Gem.WHITE, Gem.BLUE, Gem.GREEN));
        assertTrue(gm.endTurn());
        assertTrue(gm.checkGameOver());
        assertSame(first, gm.state.winner());
    }

    // ---- test data ----

    // 2 players, 5 cards per level, nobody can reach a noble.
    private static GameManager game() {
        return new GameManager(GameState.newGame(2, catalog(farNobles(5))));
    }

    // Every card of a level is the same, so it does not matter which ones are drawn.
    private static CardCatalog catalog(Array<NobleInfo> nobles) {
        return new CardCatalog(
                sameCards(100, 1, 0, Gem.WHITE, 0, 2, 0, 0, 0),     // costs 2 blue
                sameCards(200, 2, 2, Gem.BLUE, 0, 0, 3, 0, 0),      // costs 3 green
                sameCards(300, 3, 4, Gem.GREEN, 0, 0, 0, 5, 0),     // costs 5 red
                nobles);
    }

    private static Array<CardInfo> sameCards(int firstId, int tier, int points, Gem bonus, int... cost) {
        Array<CardInfo> cards = new Array<>();
        for (int i = 0; i < 5; i++) {
            cards.add(card(firstId + i, tier, points, bonus, cost));
        }
        return cards;
    }

    // card(id, tier, points, bonus, white, blue, green, red, black).
    // "int... cost" means the prices can be left out: card(1, 1, 0, Gem.RED) is a free red card.
    private static CardInfo card(int id, int tier, int points, Gem bonus, int... cost) {
        return new CardInfo(id, tier, points, bonus, gems(cost), null);
    }

    // Card bonuses needed, in the order white, blue, green, red, black.
    private static NobleInfo noble(int id, int... requires) {
        return new NobleInfo(id, 3, gems(requires), null);
    }

    // Nobles that need 9 white cards, which no test reaches.
    private static Array<NobleInfo> farNobles(int count) {
        Array<NobleInfo> nobles = new Array<>();
        for (int i = 1; i <= count; i++) {
            nobles.add(noble(i, 9, 0, 0, 0, 0));
        }
        return nobles;
    }

    private static EnumMap<Gem, Integer> gems(int... counts) {
        EnumMap<Gem, Integer> map = new EnumMap<>(Gem.class);
        for (int i = 0; i < counts.length; i++) {
            if (counts[i] > 0) {
                map.put(Gem.BASIC[i], counts[i]);
            }
        }
        return map;
    }

    // The tests run inside the core folder, so the real CSV files are one level up in assets/data.
    private static CardCatalog realCatalog() throws IOException {
        return new CardCatalog(
                CardCatalog.parseCards(csv("Lv1Card.csv"), "Lv1Card.csv", 1),
                CardCatalog.parseCards(csv("Lv2Card.csv"), "Lv2Card.csv", 2),
                CardCatalog.parseCards(csv("Lv3Card.csv"), "Lv3Card.csv", 3),
                CardCatalog.parseNobles(csv("Nobles.csv"), "Nobles.csv"));
    }

    private static String csv(String name) throws IOException {
        return new String(Files.readAllBytes(Paths.get("..", "assets", "data", name)), StandardCharsets.UTF_8);
    }
}

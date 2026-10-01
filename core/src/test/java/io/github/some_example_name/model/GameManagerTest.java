package io.github.some_example_name.model;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class GameManagerTest {
    private GameState state;
    private GameManager manager;

    @Before
    public void setUp() {
        state = GameState.newGame(4);
        manager = new GameManager(state);
    }

    private Card cardCosting(int tier, int points, Gem bonus, Gem costGem, int amount) {
        Card c = new Card(tier, points, bonus);
        c.cost.put(costGem, amount);
        return c;
    }

    /** Put a known card in the first slot of a market row. */
    private Card placeInMarket(Card c) {
        state.market.get(c.tier - 1).set(0, c);
        return c;
    }

    @Test
    public void startGame_setsUpBoard() {
        assertEquals(4, state.players.size);
        assertEquals(7, (int) state.bank.get(Gem.RED));
        assertEquals(5, (int) state.bank.get(Gem.GOLD));
        assertEquals(5, state.nobles.size);
        for (int t = 0; t < 3; t++) assertEquals(4, state.market.get(t).size);
    }

    @Test
    public void startGame_twoPlayersUseFourTokens() {
        GameState two = GameState.newGame(2);
        assertEquals(4, (int) two.bank.get(Gem.WHITE));
        assertEquals(3, two.nobles.size);
    }

    @Test
    public void takeThreeDifferentTokens() {
        assertTrue(manager.takeTokens(Gem.RED, Gem.BLUE, Gem.GREEN));
        assertEquals(1, state.current().tokenCount(Gem.RED));
        assertEquals(6, (int) state.bank.get(Gem.RED));
    }

    @Test
    public void takeTwoSameRequiresFourInBank() {
        state.bank.put(Gem.RED, 3);
        assertFalse(manager.canTakeTokens(Gem.RED, Gem.RED));
        state.bank.put(Gem.RED, 4);
        assertTrue(manager.takeTokens(Gem.RED, Gem.RED));
        assertEquals(2, state.current().tokenCount(Gem.RED));
    }

    @Test
    public void cannotTakeGoldOrDuplicatesInThree() {
        assertFalse(manager.canTakeTokens(Gem.GOLD));
        assertFalse(manager.canTakeTokens(Gem.RED, Gem.RED, Gem.BLUE));
    }

    @Test
    public void onlyOneActionPerTurn() {
        assertTrue(manager.takeTokens(Gem.RED, Gem.BLUE, Gem.GREEN));
        assertFalse(manager.takeTokens(Gem.WHITE, Gem.BLACK, Gem.RED));
    }

    @Test
    public void buyCard_appliesBonusDiscountAndGold() {
        Player p = state.current();
        p.addCard(new Card(1, 0, Gem.RED));                       // bonus: 1 red discount
        p.addToken(Gem.RED, 1);
        p.addToken(Gem.GOLD, 1);
        Card target = placeInMarket(cardCosting(1, 1, Gem.WHITE, Gem.RED, 3));
        // needs 3 red - 1 bonus = 2; has 1 red + 1 gold -> affordable
        assertTrue(manager.buyCard(target));
        assertEquals(0, p.tokenCount(Gem.RED));
        assertEquals(0, p.tokenCount(Gem.GOLD));
        assertEquals(6, (int) state.bank.get(Gem.GOLD));          // gold went back to the bank (5 + 1)
        assertTrue(p.cards.contains(target, true));
    }

    @Test
    public void buyCard_failsWhenTooPoor() {
        Card target = placeInMarket(cardCosting(1, 1, Gem.WHITE, Gem.RED, 3));
        assertFalse(manager.buyCard(target));
        assertFalse(state.current().cards.contains(target, true));
    }

    @Test
    public void buyCard_refillsMarketSlot() {
        state.current().addToken(Gem.RED, 5);
        Card target = placeInMarket(cardCosting(1, 0, Gem.WHITE, Gem.RED, 1));
        int deckBefore = state.decks.get(0).size;
        assertTrue(manager.buyCard(target));
        assertEquals(4, state.market.get(0).size);
        assertEquals(deckBefore - 1, state.decks.get(0).size);
        assertNotSame(target, state.market.get(0).get(0));
    }

    @Test
    public void reserveCard_givesGoldAndLimitsToThree() {
        Card c = state.market.get(0).get(0);
        assertTrue(manager.reserveCard(c));
        assertEquals(1, state.current().tokenCount(Gem.GOLD));
        assertEquals(1, state.current().reserved.size);

        state.current().addReservedCard(new Card(1, 0, Gem.RED));
        state.current().addReservedCard(new Card(1, 0, Gem.RED));
        manager.endTurn();                                        // next player
        state.currentPlayer = 0;                                  // back to the full player
        assertFalse(manager.canReserveCard());
    }

    @Test
    public void endTurn_requiresAnActionAndTokenLimit() {
        assertFalse(manager.endTurn());
        manager.takeTokens(Gem.RED, Gem.BLUE, Gem.GREEN);
        state.current().addToken(Gem.WHITE, 8);                   // now 11 tokens
        assertFalse(manager.endTurn());
        manager.returnToken(Gem.WHITE);
        assertTrue(manager.endTurn());
        assertEquals(1, state.currentPlayer);
    }

    @Test
    public void endTurn_awardsNoble() {
        Noble noble = new Noble(99);
        noble.cost.put(Gem.RED, 1);
        state.nobles.clear();
        state.nobles.add(noble);

        manager.takeTokens(Gem.RED, Gem.BLUE, Gem.GREEN);
        state.current().addCard(new Card(1, 0, Gem.RED));
        Player p = state.current();
        assertTrue(manager.endTurn());
        assertTrue(p.nobles.contains(noble, true));
        assertEquals(3, p.score());
        assertEquals(0, state.nobles.size);
    }

    @Test
    public void gameEndsAfterFullRoundOnceSomeoneReaches15() {
        // player 1 (index 0) reaches 15 on the first turn; the round must still finish
        state.current().addCard(new Card(3, 15, Gem.RED));
        manager.takeTokens(Gem.RED, Gem.BLUE, Gem.GREEN);
        manager.endTurn();
        assertTrue(state.finalRound);
        assertFalse(state.gameOver);

        for (int i = 1; i < 4; i++) {
            manager.takeTokens(Gem.WHITE, Gem.BLACK, Gem.GREEN);
            manager.endTurn();
            state.bank.put(Gem.WHITE, 7); state.bank.put(Gem.BLACK, 7); state.bank.put(Gem.GREEN, 7);
            for (Player p : state.players) {                      // keep token counts legal
                for (Gem g : Gem.values()) p.tokens.put(g, 0);
            }
        }
        assertTrue(manager.checkGameOver());
        assertEquals(state.players.get(0), state.winner());
    }

    @Test
    public void winnerTieBreaksOnFewerCards() {
        state.players.get(0).addCard(new Card(3, 5, Gem.RED));
        state.players.get(0).addCard(new Card(1, 0, Gem.RED));
        state.players.get(1).addCard(new Card(3, 5, Gem.BLUE));
        assertEquals(state.players.get(1), state.winner());
    }
}

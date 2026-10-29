package io.github.some_example_name.model;

import java.util.EnumMap;

import com.badlogic.gdx.utils.Array;

import io.github.some_example_name.data.CardInfo;
import io.github.some_example_name.data.NobleInfo;

// Game rules for each turn.
// A turn = do ONE action (take tokens / buy a card / reserve / buy a noble), then call endTurn().
// House rules: nobles are bought as an action (no free visit), and the game ends after the round
// in which the last card is bought (no 15-point win).
// Every action returns false if the move is not allowed.
// In contains(x, true) / removeValue(x, true) on a libGDX Array, "true" means "this exact object" (==).
public class GameManager {
    public static final int MAX_TOKENS = 10;            // a player may hold at most 10 tokens
    public static final int MAX_RESERVED = 3;           // a player may hold at most 3 reserved cards
    public static final int MIN_FOR_TWO_SAME = 2;       // house rule: taking 2 of one colour needs 2+ of it in the bank

    // House rule: start a turn with 7+ tokens -> buy a card this turn, or give back 4 tokens.
    public static final int TOO_MANY_AT_START = 7;
    public static final int PENALTY_RETURN = 4;

    public final GameState state;

    // What happened in the current turn (cleared by startNewTurn)
    private boolean acted;                  // the player has done their action
    private boolean bought;                 // the action this turn was buying a card
    private boolean passed;                 // the turn was skipped with pass()
    private boolean startedWithTooMany;     // held TOO_MANY_AT_START+ tokens when the turn began
    private int tokensReturned;             // tokens given back this turn

    private int passesInRow;                // turns in a row ended by pass(); a full round of them ends the game

    public GameManager(GameState state) {
        this.state = state;
        startNewTurn();
    }

    // How many tokens the player must still give back before the turn can end.
    // Two reasons: holding more than 10, or the 7-token house rule (see TOO_MANY_AT_START).
    public int tokensToDiscard() {
        // Rule 1: started with 7+ tokens and did not buy a card -> give back 4 (minus those already given back).
        // A pass does not count: the player had no move at all.
        int owedByPenalty = 0;
        if (acted && startedWithTooMany && !bought && !passed) {
            owedByPenalty = PENALTY_RETURN - tokensReturned;
        }

        // Rule 2: holding more than 10 tokens -> give back the extra ones
        int owedByLimit = state.current().totalTokens() - MAX_TOKENS;

        // One returned token counts for both rules, so the player owes the bigger number
        int owed = Math.max(owedByPenalty, owedByLimit);
        if (owed < 0) {
            owed = 0;                               // negative = nothing owed
        }
        return owed;
    }

    public boolean hasActed() { return acted; }

    // True when endTurn() would succeed: action done and no tokens owed.
    public boolean canEndTurn() {
        return acted && !state.gameOver && tokensToDiscard() == 0;
    }

    // ---- tokens ----

    // Allowed: 3 different colours, or 2 of the same colour (only if the bank has 4+ of it).
    // Taking fewer than 3 different colours is allowed only when the bank has no other colours left.
    // "Gem... colors" lets you pass any number of colours, e.g. canTakeTokens(Gem.RED, Gem.BLUE, Gem.GREEN);
    // inside the method, colors is a normal array (Gem[]).
    public boolean canTakeTokens(Gem... colors) {
        if (acted || state.gameOver || colors == null) return false;
        if (colors.length < 1 || colors.length > 3) return false;

        for (Gem gem : colors) {
            if (gem == null || gem == Gem.GOLD) return false;      // gold cannot be taken
            if (state.bank.get(gem) < 1) return false;             // the bank has none of this colour
        }

        boolean twoOfSameColour = colors.length == 2 && colors[0] == colors[1];
        if (twoOfSameColour) {
            return state.bank.get(colors[0]) >= MIN_FOR_TWO_SAME;
        }
        return canTakeDifferent(colors);
    }

    public boolean takeTokens(Gem... colors) {
        if (!canTakeTokens(colors)) return false;
        for (Gem gem : colors) {
            takeFromBank(gem, 1);
        }
        acted = true;
        return true;
    }

    // Give one token back to the bank.
    // Only after the action, and only while the player still owes tokens (see tokensToDiscard),
    // so nobody can give tokens back early just to make a pile reach 4.
    public boolean returnToken(Gem gem) {
        if (gem == null || !acted || state.gameOver) return false;
        if (tokensToDiscard() < 1) return false;                   // nothing owed
        if (state.current().tokenCount(gem) < 1) return false;     // player has none of this colour

        payToBank(gem, 1);
        tokensReturned++;
        return true;
    }

    // ---- cards ----

    // The card can be one on the board or one the player has reserved.
    public boolean canBuyCard(CardInfo card) {
        if (card == null || acted || state.gameOver) return false;
        Player p = state.current();
        boolean reachable = inMarket(card) || p.reserved.contains(card, true);
        return reachable && p.canAfford(card);
    }

    public boolean buyCard(CardInfo card) {
        if (!canBuyCard(card)) return false;
        Player p = state.current();

        EnumMap<Gem, Integer> payment = p.paymentFor(card);     // tokens to pay, per colour
        for (Gem gem : payment.keySet()) {
            payToBank(gem, payment.get(gem));
        }

        boolean wasReserved = p.removeReservedCard(card);
        if (!wasReserved) {
            removeFromMarket(card);                 // bought from the board: a new card takes its place
        }
        p.addCard(card);
        bought = true;
        acted = true;
        return true;
    }

    public boolean canReserveCard() {
        return !acted && !state.gameOver && state.current().reserved.size < MAX_RESERVED;
    }

    // Reserve a card that is face up on the board.
    public boolean reserveCard(CardInfo card) {
        if (card == null || !canReserveCard() || !inMarket(card)) return false;
        removeFromMarket(card);
        takeReserved(card, false);
        return true;
    }

    // Reserve the top card of a deck without seeing it first.
    // Other players cannot see this card (see Player.isHidden).
    public boolean reserveFromDeck(int tier) {
        if (tier < 1 || tier > GameState.TIERS || !canReserveCard()) return false;
        CardInfo card = state.draw(tier);
        if (card == null) return false;                             // that deck is empty
        takeReserved(card, true);
        return true;
    }

    // ---- turn flow ----

    // Nobles the current player has enough card bonuses for right now.
    public Array<NobleInfo> getAvailableNobles() {
        Array<NobleInfo> result = new Array<>();
        for (NobleInfo n : state.nobles) {
            if (state.current().qualifiesFor(n)) {
                result.add(n);
            }
        }
        return result;
    }

    // House rule: buying a noble is the action of the turn, like buying a card (nobles cannot be reserved).
    // The price is the card bonuses the noble needs; nothing is paid, the bought cards stay.
    public boolean canBuyNoble(NobleInfo noble) {
        if (noble == null || acted || state.gameOver) return false;
        return state.nobles.contains(noble, true) && state.current().qualifiesFor(noble);
    }

    // The bought noble's place on the board is taken by the next noble from the pile (if any are left).
    public boolean buyNoble(NobleInfo noble) {
        if (!canBuyNoble(noble)) return false;
        int index = state.nobles.indexOf(noble, true);          // where the noble sits on the board
        NobleInfo next = state.catalog.nobles.draw();           // null when the pile is empty
        if (next != null) {
            state.nobles.set(index, next);                      // the new noble takes the same place
        } else {
            state.nobles.removeIndex(index);
        }
        state.current().addNoble(noble);
        acted = true;
        return true;
    }

    // True if the player can still do something: take tokens, reserve, buy a card, or buy a noble.
    public boolean canAct() {
        if (state.gameOver) return false;
        if (coloursInBank() > 0) return true;                       // can take tokens
        if (canReserveCard() && anyCardLeft()) return true;         // can reserve
        if (getAvailableNobles().size > 0) return true;             // can buy a noble
        return canBuyAnyCard();                                     // can buy a card
    }

    // House rule: a player may skip the turn at any time, before doing an action.
    public boolean canPass() {
        return !acted && !state.gameOver;
    }

    // Skip the turn.
    // Only a pass with no possible move ("forced") skips the 7-token penalty and counts toward
    // a full round of passes that ends the game; a pass by choice does neither.
    // If it returns false (tokens to give back): return them, then call endTurn.
    public boolean pass() {
        if (!canPass()) return false;
        boolean forced = !canAct();
        acted = true;
        passed = forced;
        return endTurn();
    }

    // End the turn: check if the game is over, then move to the next player.
    // Returns false if there was no action yet, or tokens still need to be returned.
    public boolean endTurn() {
        if (!canEndTurn()) return false;

        // 1. house rule: when no card is left on the board or in the decks, finish this round
        if (!anyCardLeft()) {
            state.finalRound = true;
        }

        // 2. count passes in a row (any real move starts the count again)
        if (passed) {
            passesInRow++;
        } else {
            passesInRow = 0;
        }

        // 3. end the game, or move on to the next player
        boolean lastInRound = state.currentPlayer == state.players.size - 1;
        if (state.finalRound && lastInRound) {
            state.gameOver = true;                  // everyone has had the same number of turns
        } else if (passesInRow == state.players.size) {
            state.gameOver = true;                  // a full round of passes: nobody can move any more
        } else {
            state.currentPlayer = (state.currentPlayer + 1) % state.players.size;   // after the last player, back to 0
        }

        startNewTurn();
        return true;
    }

    public boolean checkGameOver() { return state.gameOver; }

    // ---- helpers ----

    // Clear the turn flags for the player whose turn it is now.
    private void startNewTurn() {
        acted = false;
        bought = false;
        passed = false;
        tokensReturned = 0;
        startedWithTooMany = state.current().totalTokens() >= TOO_MANY_AT_START;
    }

    // Different colours: no colour twice, and 3 of them
    // (fewer only when the bank does not have 3 colours left).
    private boolean canTakeDifferent(Gem[] colors) {
        if (hasDuplicate(colors)) return false;
        if (colors.length == 3) return true;
        return colors.length == coloursInBank();
    }

    // True if the same colour appears twice: compare every pair once.
    private static boolean hasDuplicate(Gem[] colors) {
        for (int i = 0; i < colors.length; i++) {
            for (int j = i + 1; j < colors.length; j++) {
                if (colors[i] == colors[j]) {
                    return true;
                }
            }
        }
        return false;
    }

    // How many of the 5 normal colours the bank still has.
    private int coloursInBank() {
        int count = 0;
        for (Gem gem : Gem.BASIC) {
            if (state.bank.get(gem) > 0) {
                count++;
            }
        }
        return count;
    }

    // True if there is still a card on the board or in a deck that could be reserved.
    private boolean anyCardLeft() {
        for (int tier = 1; tier <= GameState.TIERS; tier++) {
            if (state.row(tier).size > 0 || state.deck(tier).size() > 0) {
                return true;
            }
        }
        return false;
    }

    // True if the player can afford a card on the board or one of their reserved cards.
    private boolean canBuyAnyCard() {
        Player p = state.current();
        for (Array<CardInfo> row : state.market) {
            for (CardInfo c : row) {
                if (p.canAfford(c)) return true;
            }
        }
        for (CardInfo c : p.reserved) {
            if (p.canAfford(c)) return true;
        }
        return false;
    }

    // Shared by both reserve actions: keep the card and get 1 gold if the bank has any.
    private void takeReserved(CardInfo card, boolean hidden) {
        state.current().addReservedCard(card, hidden);
        if (state.bank.get(Gem.GOLD) > 0) {
            takeFromBank(Gem.GOLD, 1);
        }
        acted = true;
    }

    // Move tokens from the bank to the current player.
    private void takeFromBank(Gem gem, int amount) {
        state.bank.put(gem, state.bank.get(gem) - amount);
        state.current().addToken(gem, amount);
    }

    // Move tokens from the current player back to the bank.
    private void payToBank(Gem gem, int amount) {
        state.current().removeToken(gem, amount);
        state.bank.put(gem, state.bank.get(gem) + amount);
    }

    private boolean inMarket(CardInfo card) {
        return state.isOnBoard(card);
    }

    // Take a card off the board and put the next card from the deck in its place.
    private void removeFromMarket(CardInfo card) {
        int tier = state.tierOnBoard(card);
        int index = state.row(tier).indexOf(card, true);       // where the card sits in its row
        state.refill(tier, index);
    }
}

package io.github.some_example_name.model;

import java.util.EnumMap;

import com.badlogic.gdx.utils.Array;

import io.github.some_example_name.data.CardInfo;
import io.github.some_example_name.data.NobleInfo;

// Game rules for each turn.
// A turn = do ONE action (take tokens / buy / reserve), then call endTurn().
// Every action returns false if the move is not allowed.
// In contains(x, true) / removeValue(x, true) on a libGDX Array, "true" means "this exact object" (==).
public class GameManager {
    public static final int MAX_TOKENS = 10;            // a player may hold at most 10 tokens
    public static final int MAX_RESERVED = 3;           // a player may hold at most 3 reserved cards
    public static final int MIN_FOR_TWO_SAME = 2;       // taking 2 of one colour needs 4+ of it in the bank

    // House rule: start a turn with 7+ tokens -> buy a card this turn, or give back 4 tokens.
    public static final int TOO_MANY_AT_START = 7;
    public static final int PENALTY_RETURN = 4;

    public final GameState state;

    // What happened in the current turn (cleared by startNewTurn)
    private boolean acted;                  // the player has done their action
    private boolean nobleClaimed;           // a noble was already taken this turn
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

    // True when the player qualifies for 2+ nobles and must call claimNoble before ending the turn.
    public boolean needsNobleChoice() {
        return acted && !nobleClaimed && getAvailableNobles().size > 1;
    }

    // True when endTurn() would succeed: action done, no tokens owed, no noble choice pending.
    public boolean canEndTurn() {
        return acted && !state.gameOver && tokensToDiscard() == 0 && !needsNobleChoice();
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

    // Pick a noble when the player can get more than one.
    // Only after the action is done, and only one noble per turn.
    public boolean claimNoble(NobleInfo noble) {
        if (!acted || state.gameOver || nobleClaimed) return false;
        if (!getAvailableNobles().contains(noble, true)) return false;

        state.nobles.removeValue(noble, true);
        state.current().addNoble(noble);
        nobleClaimed = true;
        return true;
    }

    // True if the player can still do something: take tokens, reserve, or buy.
    public boolean canAct() {
        if (state.gameOver) return false;
        if (coloursInBank() > 0) return true;                       // can take tokens
        if (canReserveCard() && anyCardLeft()) return true;         // can reserve
        return canBuyAnyCard();                                     // can buy
    }

    // Skip the turn. Only allowed when the player has no possible action.
    // If it returns false because a noble must be chosen: call claimNoble, then endTurn.
    public boolean pass() {
        if (acted || state.gameOver || canAct()) return false;
        acted = true;
        passed = true;
        return endTurn();
    }

    // End the turn: give a noble, check if the game is over, then move to the next player.
    // Returns false if: no action yet, tokens still need to be returned,
    // or the player must first choose between 2+ nobles.
    public boolean endTurn() {
        if (!canEndTurn()) return false;

        // 1. a noble visits if exactly one qualifies (with 2+, the player chose already with claimNoble)
        if (!nobleClaimed) {
            Array<NobleInfo> available = getAvailableNobles();
            if (available.size == 1) {
                claimNoble(available.first());
            }
        }

        // 2. reaching 15 points starts the final round
        if (state.current().score() >= GameState.WIN_POINTS) {
            state.finalRound = true;
        }

        // 3. count passes in a row (any real move starts the count again)
        if (passed) {
            passesInRow++;
        } else {
            passesInRow = 0;
        }

        // 4. end the game, or move on to the next player
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
        nobleClaimed = false;
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

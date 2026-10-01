package io.github.some_example_name.model;

import java.util.Map;

/** Turn rules. One action per turn, then endTurn(). All action methods return false if illegal. */
public class GameManager {
    public static final int MAX_TOKENS = 10;
    public static final int MAX_RESERVED = 3;

    public final GameState state;
    private boolean acted;

    public GameManager(GameState state) { this.state = state; }

    public boolean hasActed() { return acted; }

    // ---- tokens ----

    /** 3 different colours, or 2 of the same colour if the bank has 4+. Fewer than 3 only when no more colours are in stock. */
    public boolean canTakeTokens(Gem... colors) {
        if (acted || state.gameOver || colors.length < 1 || colors.length > 3) return false;
        for (Gem g : colors) if (g == Gem.GOLD || state.bank.get(g) < 1) return false;
        if (colors.length == 2 && colors[0] == colors[1]) return state.bank.get(colors[0]) >= 4;
        for (int i = 0; i < colors.length; i++)
            for (int j = i + 1; j < colors.length; j++) if (colors[i] == colors[j]) return false;
        if (colors.length == 3) return true;
        int inStock = 0;
        for (Gem g : Gem.BASIC) if (state.bank.get(g) > 0) inStock++;
        return colors.length == inStock;
    }

    public boolean takeTokens(Gem... colors) {
        if (!canTakeTokens(colors)) return false;
        for (Gem g : colors) move(g, 1);
        acted = true;
        return true;
    }

    /** Give a token back to the bank (needed when over MAX_TOKENS). */
    public boolean returnToken(Gem g) {
        Player p = state.current();
        if (p.tokenCount(g) < 1) return false;
        p.removeToken(g, 1);
        state.bank.put(g, state.bank.get(g) + 1);
        return true;
    }

    // ---- cards ----

    public boolean canBuyCard(Card card) {
        if (acted || state.gameOver) return false;
        Player p = state.current();
        return (inMarket(card) || p.reserved.contains(card, true)) && card.isBuyable(p);
    }

    public boolean buyCard(Card card) {
        if (!canBuyCard(card)) return false;
        Player p = state.current();
        for (Map.Entry<Gem, Integer> e : card.paymentFor(p).entrySet()) {
            p.removeToken(e.getKey(), e.getValue());
            state.bank.put(e.getKey(), state.bank.get(e.getKey()) + e.getValue());
        }
        if (!p.reserved.removeValue(card, true)) removeFromMarket(card);
        p.addCard(card);
        acted = true;
        return true;
    }

    public boolean canReserveCard() {
        return !acted && !state.gameOver && state.current().reserved.size < MAX_RESERVED;
    }

    /** Reserve a face-up card. */
    public boolean reserveCard(Card card) {
        if (!canReserveCard() || !inMarket(card)) return false;
        removeFromMarket(card);
        takeReserved(card);
        return true;
    }

    /** Reserve the top (hidden) card of a deck. */
    public boolean reserveFromDeck(int tier) {
        if (!canReserveCard()) return false;
        Card card = state.draw(tier);
        if (card == null) return false;
        takeReserved(card);
        return true;
    }

    // ---- turn flow ----

    /** Ends the turn: awards a noble, checks game over, advances. Fails if no action yet or >10 tokens held. */
    public boolean endTurn() {
        Player p = state.current();
        if (!acted || state.gameOver || p.totalTokens() > MAX_TOKENS) return false;

        for (Noble n : state.nobles) {
            if (n.isAvailable(p)) {
                state.nobles.removeValue(n, true);
                p.addNoble(n);
                break;                                    // one noble per turn
            }
        }
        if (p.score() >= GameState.WIN_POINTS) state.finalRound = true;
        if (state.currentPlayer == state.players.size - 1 && state.finalRound) {
            state.gameOver = true;                        // everyone has had the same number of turns
        } else {
            state.currentPlayer = (state.currentPlayer + 1) % state.players.size;
        }
        acted = false;
        return true;
    }

    public boolean checkGameOver() { return state.gameOver; }

    // ---- helpers ----

    private void takeReserved(Card card) {
        Player p = state.current();
        p.addReservedCard(card);
        if (state.bank.get(Gem.GOLD) > 0) move(Gem.GOLD, 1);
        acted = true;
    }

    private void move(Gem g, int n) {
        state.bank.put(g, state.bank.get(g) - n);
        state.current().addToken(g, n);
    }

    private boolean inMarket(Card card) {
        return state.market.get(card.tier - 1).contains(card, true);
    }

    private void removeFromMarket(Card card) {
        int idx = state.market.get(card.tier - 1).indexOf(card, true);
        state.refill(card.tier, idx);
    }
}

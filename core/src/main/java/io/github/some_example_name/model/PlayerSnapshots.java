package io.github.some_example_name.model;

import io.github.some_example_name.ui.PlayerSnapshot;

// Fills the player panels' display data (ui.PlayerSnapshot) from the real players.
// The screen only reads the snapshots; nothing here changes the game.
public final class PlayerSnapshots {
    private PlayerSnapshots() {}

    // One snapshot per player, in seat order, so index i matches currentPlayer == i (GameScreen.showTurn(i)).
    public static PlayerSnapshot[] of(GameState state) {
        PlayerSnapshot[] out = new PlayerSnapshot[state.players.size];
        for (int i = 0; i < out.length; i++) {
            out[i] = of(state.players.get(i));
        }
        return out;
    }

    public static PlayerSnapshot of(Player p) {
        PlayerSnapshot s = new PlayerSnapshot(p.name).withPoints(p.score());
        for (Gem gem : Gem.values()) {
            s.gems.put(gem, p.tokenCount(gem));
        }
        for (Gem gem : Gem.BASIC) {
            s.bonuses.put(gem, p.bonus(gem));
        }
        s.reserved.addAll(p.reserved);
        return s;
    }
}

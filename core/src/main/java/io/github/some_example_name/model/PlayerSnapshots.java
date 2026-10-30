package io.github.some_example_name.model;

import io.github.some_example_name.data.CardInfo;
import io.github.some_example_name.ui.PlayerSnapshot;

// Fills the player panels' display data (ui.PlayerSnapshot) from the real players.
// The screen only reads the snapshots; nothing here changes the game.
// Only static methods: call PlayerSnapshots.fromGame(state), no need for "new".
public class PlayerSnapshots {

    // One snapshot per player, in seat order, so index i matches currentPlayer == i (GameScreen.showTurn(i)).
    public static PlayerSnapshot[] fromGame(GameState state) {
        PlayerSnapshot[] out = new PlayerSnapshot[state.players.size];
        for (int i = 0; i < out.length; i++) {
            out[i] = fromPlayer(state.players.get(i));
        }
        return out;
    }

    public static PlayerSnapshot fromPlayer(Player p) {
        PlayerSnapshot s = new PlayerSnapshot(p.name);
        s.points = p.score();
        for (Gem gem : Gem.values()) {
            s.gems.put(gem, p.tokenCount(gem));
        }
        for (Gem gem : Gem.BASIC) {
            s.bonuses.put(gem, p.bonus(gem));
        }
        s.reserved.addAll(p.reserved);
        for (CardInfo c : p.reserved) {
            if (p.isHidden(c)) s.hidden.add(c);         // so the screen can draw a card back for other players
        }
        return s;
    }
}

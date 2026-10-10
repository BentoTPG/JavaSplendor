package io.github.some_example_name.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.EnumMap;

import org.junit.Test;

import io.github.some_example_name.data.CardInfo;
import io.github.some_example_name.ui.PlayerSnapshot;

public class PlayerSnapshotsTest {
    private static CardInfo card(int id, int points, Gem bonus) {
        return new CardInfo(id, 1, points, bonus, new EnumMap<Gem, Integer>(Gem.class), null);
    }

    @Test
    public void snapshotShowsTheRealPlayer() {
        Player p = new Player("P2");
        p.addToken(Gem.RED, 2);
        p.addToken(Gem.GOLD, 1);
        p.addCard(card(1, 1, Gem.BLUE));
        p.addCard(card(2, 2, Gem.BLUE));
        CardInfo kept = card(3, 4, Gem.RED);
        p.addReservedCard(kept, false);

        PlayerSnapshot s = PlayerSnapshots.of(p);
        assertEquals("P2", s.name);
        assertEquals(3, s.points);
        assertEquals(2, s.gems(Gem.RED));
        assertEquals(1, s.gems(Gem.GOLD));
        assertEquals(0, s.gems(Gem.WHITE));
        assertEquals(2, s.bonus(Gem.BLUE));
        assertEquals(0, s.bonus(Gem.RED));
        assertEquals(1, s.reserved.size);
        assertSame(kept, s.reserved.first());
    }
}

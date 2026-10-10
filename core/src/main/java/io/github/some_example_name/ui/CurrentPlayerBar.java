package io.github.some_example_name.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

/**
 * The bar along the bottom for the player whose turn it is: the same facts as {@link OtherPlayerCard},
 * laid out in one row because there is little height there.
 */
public class CurrentPlayerBar extends Table {
    public static final int HEIGHT = 190;
    private static final Color PANEL = new Color(0.20f, 0.16f, 0.10f, 0.95f);
    private static final Color TURN = new Color(1f, 0.85f, 0.35f, 1f);

    public final PlayerSnapshot player;

    public CurrentPlayerBar(PlayerSnapshot p, UiStyles s, PlayerParts.CardClick onReserved) {
        this.player = p;
        setBackground(Gfx.solid(PANEL));
        pad(10, 20, 10, 20);

        Table who = new Table();
        OutlinedLabel turn = new OutlinedLabel("Turn", s.caption, 1f);
        turn.setColor(TURN);
        who.add(turn).left().row();
        who.add(new OutlinedLabel(p.name, s.big, 1f)).left();
        add(who).width(110).left();

        Table reserved = PlayerParts.reservedGrid(p, 3, 86, 130, 1.6f, Align.bottom, s, onReserved);
        add(PlayerParts.section("Reserved", reserved, s)).padLeft(16);
        add(PlayerParts.section("Cards", PlayerParts.bonusGrid(p, 5, 60, s.small), s)).padLeft(24);
        add(PlayerParts.section("Gems", PlayerParts.gemGrid(p, 3, 60, s.small), s)).padLeft(24);

        Table total = new Table();
        total.add(PlayerParts.caption("Total", s)).right().row();
        total.add(new OutlinedLabel("" + p.points, s.big, 1f)).right();
        add(total).expandX().right();
    }
}

package io.github.some_example_name.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

/**
 * A player who is not taking their turn: name and total points on the left, gems and card colours in
 * the middle, reserved cards on the right (hover to enlarge, click to see the full card).
 */
public class OtherPlayerCard extends Table {
    public static final int W = 460, H = 330;
    private static final Color PANEL = new Color(0.13f, 0.15f, 0.20f, 0.92f);

    public final PlayerSnapshot player;

    public OtherPlayerCard(PlayerSnapshot p, UiStyles s, PlayerParts.CardClick onReserved) {
        this.player = p;
        setBackground(Gfx.solid(PANEL));
        pad(12);

        Table info = new Table();
        info.add(new OutlinedLabel(p.name, s.big, 1f)).left().row();
        info.add().expandY().row();
        info.add(PlayerParts.caption("Total", s)).left().row();
        info.add(new OutlinedLabel("" + p.points, s.big, 1f)).left();
        add(info).width(80).growY().top().left();

        Table middle = new Table();
        middle.add(PlayerParts.section("Gems", PlayerParts.gemGrid(p, 3, 50, s.small), s)).left().padBottom(8).row();
        middle.add(PlayerParts.section("Cards", PlayerParts.bonusGrid(p, 3, 46, s.small), s)).left();
        add(middle).top().left().padLeft(12);

        Table reserved = PlayerParts.reservedGrid(p, 2, 70, 106, 1.8f, Align.center, s, onReserved);
        add(PlayerParts.section("Reserved", reserved, s)).top().right().expandX().padLeft(12);
    }
}

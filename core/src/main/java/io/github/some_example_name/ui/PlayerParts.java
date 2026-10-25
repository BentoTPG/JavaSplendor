package io.github.some_example_name.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

import io.github.some_example_name.data.CardInfo;
import io.github.some_example_name.model.Gem;

/** Pieces shared by the other-player cards and the current-player bar. */
public final class PlayerParts {
    private PlayerParts() {}

    /** Called when a reserved card is clicked. */
    public interface CardClick {
        void open(CardInfo card);
    }

    /** Called when one of the player's own gems is clicked (used to give tokens back). */
    public interface GemClick {
        void click(Gem gem);
    }

    private static final Color TILE_EDGE = new Color(0f, 0f, 0f, 0.55f);
    /** How visible a gem or colour the player has none of is (0 = hidden, 1 = normal). */
    private static final float EMPTY_ALPHA = 0.30f;

    public static Label caption(String text, UiStyles s) {
        return new OutlinedLabel(text, s.caption, 1f);
    }

    /** All six gems (gold too) with how many the player holds, `columns` per row. */
    public static Table gemGrid(PlayerSnapshot p, int columns, int chip, Label.LabelStyle style) {
        return gemGrid(p, columns, chip, style, null);
    }

    /** Same grid, but clicking a gem calls onClick (null = not clickable). */
    public static Table gemGrid(PlayerSnapshot p, int columns, int chip, Label.LabelStyle style,
                                final GemClick onClick) {
        Table grid = new Table();
        int i = 0;
        for (final Gem gem : Gem.values()) {
            int n = p.gems(gem);
            Actor c = GemIcons.chip(gem, n, chip, style);
            if (n == 0) c.getColor().a = EMPTY_ALPHA;
            if (onClick != null) {
                c.addListener(new ClickListener() {
                    @Override public void clicked(InputEvent event, float x, float y) { onClick.click(gem); }
                });
            }
            grid.add(c).size(chip).pad(2);
            if (++i % columns == 0) grid.row();
        }
        return grid;
    }

    /** One coloured square per gem colour with the number of bought cards of that colour. */
    public static Table bonusGrid(PlayerSnapshot p, int columns, int tile, Label.LabelStyle style) {
        Table grid = new Table();
        int i = 0;
        for (Gem gem : Gem.BASIC) {
            grid.add(bonusTile(gem, p.bonus(gem), style)).size(tile).pad(2);
            if (++i % columns == 0) grid.row();
        }
        return grid;
    }

    public static Actor bonusTile(Gem gem, int count, Label.LabelStyle style) {
        Stack tile = new Stack();
        tile.add(new Image(Gfx.solid(TILE_EDGE)));
        Container<Image> face = new Container<Image>(new Image(Gfx.solid(gem.color))).fill().pad(3);
        tile.add(face);
        Label number = new OutlinedLabel("" + count, style, 1f);
        number.setAlignment(Align.center);
        tile.add(number);
        if (count == 0) tile.getColor().a = EMPTY_ALPHA;
        return tile;
    }

    /**
     * Up to three reserved cards, `columns` per row; the slots the player has not used are faint boxes.
     * growFrom says which edge stays put when a card grows under the mouse (Align.bottom near the screen bottom).
     */
    public static Table reservedGrid(PlayerSnapshot p, int columns, int w, int h, float hoverScale, int growFrom,
                                     UiStyles s, final CardClick onClick) {
        Table grid = new Table();
        for (int i = 0; i < PlayerSnapshot.MAX_RESERVED; i++) {
            if (i < p.reserved.size && p.maskHidden && p.isHidden(p.reserved.get(i))) {
                grid.add(cardBack()).size(w, h).pad(3);                     // someone else's face-down card
            } else if (i < p.reserved.size) {
                final MiniCardView mini = new MiniCardView(p.reserved.get(i), w, h, hoverScale, s.caption);
                mini.hoverAlign = growFrom;
                mini.onClick = new Runnable() {
                    @Override public void run() { if (onClick != null) onClick.open(mini.card); }
                };
                grid.add(mini).size(w, h).pad(3);
            } else {
                grid.add(MiniCardView.emptySlot()).size(w, h).pad(3);
            }
            if ((i + 1) % columns == 0) grid.row();
        }
        return grid;
    }

    /** The back of a card, shown instead of a face-down reserved card the viewer may not see. */
    private static Image cardBack() {
        return new Image(Gfx.solid(new Color(0.28f, 0.20f, 0.36f, 1f)));
    }

    /** A caption with its content underneath. */
    public static Table section(String title, Actor content, UiStyles s) {
        Table t = new Table();
        t.add(caption(title, s)).left().padBottom(2).row();
        t.add(content).left();
        return t;
    }
}
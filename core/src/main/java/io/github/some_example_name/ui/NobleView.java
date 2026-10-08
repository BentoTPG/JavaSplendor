package io.github.some_example_name.ui;

import java.util.Map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import io.github.some_example_name.data.NobleInfo;
import io.github.some_example_name.model.Gem;

/** A noble: the portrait, points at the top left, required card bonuses along the bottom. */
public class NobleView extends ClickableStack {
    public static final int SIZE = 200;

    /** Shown instead of the portrait when the image file is missing. */
    private static final Color FALLBACK_BG = new Color(0.30f, 0.26f, 0.38f, 1);
    public final NobleInfo noble;

    public NobleView(NobleInfo noble, Label.LabelStyle big, Label.LabelStyle small) {
        super(1.08f);
        this.noble = noble;
        add(new Image(Gfx.cover(noble.art, SIZE, SIZE, FALLBACK_BG)));          // layer 0: the portrait

        Table ui = new Table();                                                 // layer 1: points and requirements
        ui.pad(8);
        ui.add(new OutlinedLabel("" + noble.points, big, 3f)).expand().top().left().row();

        Table requires = new Table();
        for (Map.Entry<Gem, Integer> e : noble.requires.entrySet()) {
            requires.add(GemIcons.chip(e.getKey(), e.getValue(), 48, small)).size(46).pad(2);
        }
        ui.add(requires).bottom().left();
        add(ui);
    }
}

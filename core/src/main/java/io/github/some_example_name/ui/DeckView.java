package io.github.some_example_name.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

/** A tile that shows how many cards are left in a pile (first column of each card row, last tile of the nobles). */
public class DeckView extends Stack {
    public DeckView(String caption, int remaining, Label.LabelStyle big, Label.LabelStyle small) {
        add(new Image(Gfx.solid(new Color(0.16f, 0.18f, 0.24f, 1))));

        Table ui = new Table();
        ui.pad(8);
        ui.add(new OutlinedLabel(caption, small, 2f)).top().left().row();
        ui.add(new OutlinedLabel("" + remaining, big, 3f)).expand().center();
        add(ui);
    }
}

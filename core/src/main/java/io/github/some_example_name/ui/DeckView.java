package io.github.some_example_name.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.utils.Align;

public class DeckView extends Stack {
    public DeckView(int tier, int remaining, Label.LabelStyle big) {
        add(new Image(Gfx.solid(new Color(0.16f, 0.18f, 0.24f, 1))));
        Label l = new Label("" + remaining, big);
        l.setAlignment(Align.center);
        add(l);
    }
}

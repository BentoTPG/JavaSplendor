package io.github.some_example_name.ui;

import java.util.EnumMap;
import java.util.Map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import io.github.some_example_name.model.Gem;

/**
 * The deck players take gems from: one slot per gem (five colours plus gold), each showing how many are left.
 * It sits between the card rows and the nobles. Show-only: the numbers are given by the screen.
 */
public class GemBankView extends Stack {
    public static final int WIDTH = 190, SLOT_HEIGHT = 128;

    private final Map<Gem, GemSlot> slots = new EnumMap<Gem, GemSlot>(Gem.class);

    public GemBankView(Map<Gem, Integer> remaining, Label.LabelStyle big) {
        add(new Image(Gfx.solid(new Color(0.16f, 0.18f, 0.24f, 1))));

        Table column = new Table();
        column.pad(10);
        for (Gem gem : Gem.values()) {                       // white, blue, green, red, black, gold
            Integer n = remaining.get(gem);
            GemSlot slot = new GemSlot(gem, n == null ? 0 : n, big);
            slots.put(gem, slot);
            column.add(slot).size(WIDTH - 20, SLOT_HEIGHT).pad(3).row();
        }
        add(column);
    }

    public GemSlot slot(Gem gem) { return slots.get(gem); }
}

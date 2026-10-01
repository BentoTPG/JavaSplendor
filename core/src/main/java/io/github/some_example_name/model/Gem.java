package io.github.some_example_name.model;

import com.badlogic.gdx.graphics.Color;

public enum Gem {
    WHITE(0.92f, 0.92f, 0.95f), BLUE(0.20f, 0.45f, 0.90f), GREEN(0.15f, 0.70f, 0.35f),
    RED(0.85f, 0.20f, 0.20f),   BLACK(0.15f, 0.15f, 0.18f), GOLD(0.95f, 0.78f, 0.15f);

    public final Color color;
    Gem(float r, float g, float b) { color = new Color(r, g, b, 1f); }
}
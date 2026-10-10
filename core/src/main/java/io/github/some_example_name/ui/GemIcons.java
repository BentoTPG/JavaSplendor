package io.github.some_example_name.ui;

import java.util.Locale;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;

import io.github.some_example_name.model.Gem;

/**
 * Gem pictures made by the art team.
 *
 * <p>One image per gem sits in <code>assets/</code>: gem_white.png, gem_blue.png, gem_green.png,
 * gem_red.png, gem_black.png and gem_gold.png. To swap the art, replace the file with the same name.
 * A gem without an image is drawn as a plain coloured disc, so the game still runs.
 */
public final class GemIcons {
    private GemIcons() {}

    public static String path(Gem gem) {
        return "gem_" + gem.name().toLowerCase(Locale.ROOT) + ".png";
    }

    /** The picture for a gem, or a coloured disc of fallbackSize pixels when there is no picture yet. */
    public static Drawable of(Gem gem, int fallbackSize) {
        Texture texture = Gfx.texture(path(gem), false);
        if (texture != null) return new TextureRegionDrawable(new TextureRegion(texture));
        return Gfx.circle(gem.color, fallbackSize);
    }

    /** A gem picture with a number printed over it (card prices, noble requirements). Give its cell a square size. */
    public static Actor chip(Gem gem, int amount, int iconSize, Label.LabelStyle style) {
        Image icon = new Image(of(gem, iconSize));
        icon.setScaling(Scaling.fit);
        Label number = new OutlinedLabel("" + amount, style, 1f);
        number.setAlignment(Align.center);
        return new Stack(icon, number);
    }
}

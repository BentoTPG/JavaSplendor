package io.github.some_example_name.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

/**
 * A Label with a black outline, so white numbers stay readable on top of card photos.
 * It draws the same text eight times in black, shifted around the real position, then the normal text on top.
 */
public class OutlinedLabel extends Label {
    private static final float[][] DIRECTIONS = {
        {-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}
    };
    private final Color outline = new Color(0f, 0f, 0f, 1f);
    private final Color tint = new Color();
    private final float width;

    public OutlinedLabel(CharSequence text, LabelStyle style, float outlineWidth) {
        super(text, style);
        this.width = outlineWidth;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        validate();
        BitmapFontCache cache = getBitmapFontCache();
        tint.set(outline);
        tint.a *= getColor().a * parentAlpha;
        for (float[] d : DIRECTIONS) {
            cache.tint(tint);
            cache.setPosition(getX() + d[0] * width, getY() + d[1] * width);
            cache.draw(batch);
        }
        super.draw(batch, parentAlpha);      // resets the position and colour, then draws the real text
    }
}

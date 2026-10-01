package io.github.some_example_name.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;

public final class Gfx {
    private static final Array<Texture> owned = new Array<>();

    public static Drawable solid(Color c) {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(c); pm.fill();
        Texture t = new Texture(pm); pm.dispose(); owned.add(t);
        return new TextureRegionDrawable(new TextureRegion(t));
    }

    public static Drawable circle(Color c, int size) {
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pm.setColor(c); pm.fillCircle(size / 2, size / 2, size / 2 - 1);
        Texture t = new Texture(pm); pm.dispose(); owned.add(t);
        t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return new TextureRegionDrawable(new TextureRegion(t));
    }

    public static void disposeAll() { for (Texture t : owned) t.dispose(); owned.clear(); }
}
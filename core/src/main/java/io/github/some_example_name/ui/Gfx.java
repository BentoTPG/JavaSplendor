package io.github.some_example_name.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

/** Small helpers that make (and remember) textures. Call {@link #disposeAll()} when the screen closes. */
public final class Gfx {
    private static final Array<Texture> owned = new Array<>();
    private static final ObjectMap<String, Texture> images = new ObjectMap<>();      // path -> texture (null = not found)
    private static final ObjectMap<String, Drawable> covers = new ObjectMap<>();
    private static final ObjectMap<String, Drawable> circles = new ObjectMap<>();
    private static final ObjectMap<String, Drawable> solids = new ObjectMap<>();

    private Gfx() {}

    public static Drawable solid(Color c) {
        String key = "" + c.toIntBits();
        Drawable cached = solids.get(key);
        if (cached != null) return cached;
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(c); pm.fill();
        Texture t = new Texture(pm); pm.dispose(); owned.add(t);
        Drawable d = new TextureRegionDrawable(new TextureRegion(t));
        solids.put(key, d);
        return d;
    }

    public static Drawable circle(Color c, int size) {
        String key = c.toIntBits() + ":" + size;
        Drawable cached = circles.get(key);
        if (cached != null) return cached;
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pm.setColor(c); pm.fillCircle(size / 2, size / 2, size / 2 - 1);
        Texture t = new Texture(pm); pm.dispose(); owned.add(t);
        t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        Drawable d = new TextureRegionDrawable(new TextureRegion(t));
        circles.put(key, d);
        return d;
    }

    /**
     * Loads an image from the assets folder once and keeps it. Returns null when the file does not exist.
     * A path that starts with "assets/" (as written in the CSV files) also works, because the
     * assets folder is already the root that internal files are read from.
     */
    public static Texture texture(String path, boolean warnIfMissing) {
        if (path == null || path.isEmpty()) return null;
        if (images.containsKey(path)) return images.get(path);
        FileHandle file = Gdx.files.internal(path);
        if (!file.exists() && path.startsWith("assets/")) file = Gdx.files.internal(path.substring("assets/".length()));
        Texture t = null;
        if (file.exists()) {
            t = new Texture(file, true);                                   // mipmaps keep shrunk photos smooth
            t.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
            owned.add(t);
        } else if (warnIfMissing) {
            Gdx.app.error("Gfx", "image not found: " + path);
        }
        images.put(path, t);
        return t;
    }

    /**
     * The image at path, cropped from the middle to the shape w x h so it fills the area without stretching.
     * Uses a plain fallback colour when the image is missing.
     */
    public static Drawable cover(String path, float w, float h, Color fallback) {
        Texture t = texture(path, true);
        if (t == null) return solid(fallback);
        String key = path + "|" + w + "x" + h;
        Drawable cached = covers.get(key);
        if (cached != null) return cached;
        float target = w / h;
        int cw = t.getWidth(), ch = t.getHeight();
        if ((float) cw / ch > target) cw = Math.round(ch * target);       // picture too wide: trim the sides
        else ch = Math.round(cw / target);                                 // picture too tall: trim top and bottom
        int x = (t.getWidth() - cw) / 2, y = (t.getHeight() - ch) / 2;
        Drawable d = new TextureRegionDrawable(new TextureRegion(t, x, y, cw, ch));
        covers.put(key, d);
        return d;
    }

    public static void disposeAll() {
        for (Texture t : owned) t.dispose();
        owned.clear();
        images.clear();
        covers.clear();
        circles.clear();
        solids.clear();
    }
}

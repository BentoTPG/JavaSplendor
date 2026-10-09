package io.github.some_example_name.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

/** Builds the big number font from a TTF file at the size it is shown, so it stays sharp. */
public final class Fonts {
    private Fonts() {}

    public static BitmapFont createBigNumberFont() {
        FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal("fonts/NewRocker-Regular.ttf"));
        try {
            FreeTypeFontParameter p = new FreeTypeFontParameter();
            p.size = 52;
            p.color = Color.WHITE;
            p.borderWidth = 3;
            p.borderColor = Color.BLACK;
            p.minFilter = Texture.TextureFilter.Linear;
            p.magFilter = Texture.TextureFilter.Linear;

            StringBuilder chars = new StringBuilder(FreeTypeFontGenerator.DEFAULT_CHARS);
            for (char c = '\u0E01'; c <= '\u0E5B'; c++) chars.append(c);   // every Thai character
            p.characters = chars.toString();

            return gen.generateFont(p);
        } finally {
            gen.dispose();
        }
    }
    
    public static BitmapFont createSmallNumberFont() {
        FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal("fonts/JockeyOne-Regular.ttf"));
        try {
            FreeTypeFontParameter p = new FreeTypeFontParameter();
            p.size = 32;
            p.color = Color.WHITE;
            p.borderWidth = 1;
            p.borderColor = Color.BLACK;
            p.minFilter = Texture.TextureFilter.Linear;
            p.magFilter = Texture.TextureFilter.Linear;

            StringBuilder chars = new StringBuilder(FreeTypeFontGenerator.DEFAULT_CHARS);
            for (char c = '\u0E01'; c <= '\u0E5B'; c++) chars.append(c);   // every Thai character
            p.characters = chars.toString();

            return gen.generateFont(p);
        } finally {
            gen.dispose();
        }
    }
}
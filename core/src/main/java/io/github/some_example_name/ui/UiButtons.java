package io.github.some_example_name.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

/** Makes the plain text buttons used on the board (Pick, End turn, Pass, Buy, Reserve, Cancel). */
public final class UiButtons {
    private UiButtons() {}

    private static final Color UP = new Color(0.20f, 0.45f, 0.30f, 1f);
    private static final Color DOWN = new Color(0.14f, 0.32f, 0.21f, 1f);
    private static final Color OFF = new Color(0.25f, 0.25f, 0.27f, 1f);
    private static final Color OFF_TEXT = new Color(0.55f, 0.55f, 0.55f, 1f);

    /** A button with the given text. The action runs when it is clicked while it is enabled. */
    public static TextButton create(String text, Label.LabelStyle labelStyle, final Runnable action) {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font = labelStyle.font;
        style.fontColor = Color.WHITE;
        style.disabledFontColor = OFF_TEXT;
        style.up = Gfx.solid(UP);
        style.down = Gfx.solid(DOWN);
        style.disabled = Gfx.solid(OFF);

        final TextButton button = new TextButton(text, style);
        button.pad(6, 22, 6, 22);
        button.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                if (!button.isDisabled()) action.run();
            }
        });
        return button;
    }
}

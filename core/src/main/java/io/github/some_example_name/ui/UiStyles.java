package io.github.some_example_name.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Label;

/** The three text styles the board uses: big numbers, small numbers and captions such as "Gems" or "Total". */
public class UiStyles {
    public final Label.LabelStyle big, small, caption;

    public UiStyles(Label.LabelStyle big, Label.LabelStyle small, Label.LabelStyle caption) {
        this.big = big;
        this.small = small;
        this.caption = caption;
    }
}

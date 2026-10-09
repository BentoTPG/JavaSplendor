package io.github.some_example_name.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Scaling;

import io.github.some_example_name.model.Gem;

/** One row of the gem bank: the gem picture and how many of that gem are left. Show-only for now. */
public class GemSlot extends ClickableStack {
    public final Gem gem;
    private final Label count;

    public GemSlot(Gem gem, int remaining, Label.LabelStyle big) {
        super(1.08f);
        this.gem = gem;

        Image icon = new Image(GemIcons.of(gem, 96));
        icon.setScaling(Scaling.fit);
        count = new OutlinedLabel("" + remaining, big, 3f);

        Table row = new Table();
        row.add(icon).size(92).padRight(10);
        row.add(count).width(50).left();
        add(row);
    }

    /** For the backend: call this whenever the number of gems left in the bank changes. */
    public void setRemaining(int remaining) {
        count.setText("" + remaining);
    }
}

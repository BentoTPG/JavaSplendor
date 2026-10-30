package io.github.some_example_name.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

import io.github.some_example_name.data.NobleInfo;

/**
 * Darkens the screen and shows a noble large with two buttons: Buy, Cancel.
 * Buy is greyed out when the player does not have the card bonuses the noble needs.
 * Clicking the dark area does nothing.
 */
public class NobleActionOverlay extends Table {
    private static final float SCALE = 1.8f;

    public NobleActionOverlay(NobleInfo noble, boolean canBuy, Label.LabelStyle big, Label.LabelStyle small,
                              final Runnable onBuy) {
        setFillParent(true);
        setTouchable(Touchable.enabled);                 // catch every click, so the board underneath is not used
        setBackground(Gfx.solid(new Color(0f, 0f, 0f, 0.65f)));

        Container<NobleView> zoom = new Container<NobleView>(new NobleView(noble, big, small));
        zoom.size(NobleView.SIZE, NobleView.SIZE);
        zoom.setTransform(true);
        zoom.setOrigin(NobleView.SIZE / 2f, NobleView.SIZE / 2f);
        zoom.setScale(SCALE);
        float grow = (SCALE - 1f) * NobleView.SIZE / 2f;  // room for the part drawn outside the cell
        add(zoom).size(NobleView.SIZE, NobleView.SIZE).padTop(grow).padBottom(grow + 24).row();

        TextButton buy = UiButtons.create("Buy", small, new Runnable() {
            @Override public void run() { NobleActionOverlay.this.remove(); onBuy.run(); }
        });
        buy.setDisabled(!canBuy);

        TextButton cancel = UiButtons.create("Cancel", small, new Runnable() {
            @Override public void run() { NobleActionOverlay.this.remove(); }
        });

        Table buttons = new Table();
        buttons.add(buy).pad(8);
        buttons.add(cancel).pad(8);
        add(buttons);
    }
}

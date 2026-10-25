package io.github.some_example_name.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

import io.github.some_example_name.data.CardInfo;

/**
 * Darkens the screen and shows a face-up card large with three buttons: Buy, Reserve, Cancel.
 * A button that is not allowed right now is greyed out. Clicking the dark area does nothing.
 */
public class CardActionOverlay extends Table {
    private static final float SCALE = 1.8f;

    public CardActionOverlay(CardInfo card, boolean canBuy, boolean canReserve,
                             Label.LabelStyle big, Label.LabelStyle small,
                             final Runnable onBuy, final Runnable onReserve) {
        setFillParent(true);
        setTouchable(Touchable.enabled);                 // catch every click, so the board underneath is not used
        setBackground(Gfx.solid(new Color(0f, 0f, 0f, 0.65f)));

        Container<CardView> zoom = new Container<CardView>(new CardView(card, big, small));
        zoom.size(CardView.W, CardView.H);
        zoom.setTransform(true);
        zoom.setOrigin(CardView.W / 2f, CardView.H / 2f);
        zoom.setScale(SCALE);
        float grow = (SCALE - 1f) * CardView.H / 2f;     // room for the part drawn outside the cell
        add(zoom).size(CardView.W, CardView.H).padTop(grow).padBottom(grow + 24).row();

        TextButton buy = UiButtons.create("Buy", small, new Runnable() {
            @Override public void run() { CardActionOverlay.this.remove(); onBuy.run(); }
        });
        buy.setDisabled(!canBuy);

        TextButton reserve = UiButtons.create("Reserve", small, new Runnable() {
            @Override public void run() { CardActionOverlay.this.remove(); onReserve.run(); }
        });
        reserve.setDisabled(!canReserve);

        TextButton cancel = UiButtons.create("Cancel", small, new Runnable() {
            @Override public void run() { CardActionOverlay.this.remove(); }
        });

        Table buttons = new Table();
        buttons.add(buy).pad(8);
        buttons.add(reserve).pad(8);
        buttons.add(cancel).pad(8);
        add(buttons);
    }
}

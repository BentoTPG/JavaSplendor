package io.github.some_example_name.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import io.github.some_example_name.data.CardInfo;

/** Darkens the screen and shows one card large. A click anywhere closes it. */
public class CardDetailOverlay extends Table {
    private static final float SCALE = 1.8f;

    public CardDetailOverlay(CardInfo card, Label.LabelStyle big, Label.LabelStyle small, UiStyles s) {
        setFillParent(true);
        setTouchable(Touchable.enabled);                 // catch every click, so the board underneath is not used
        setBackground(Gfx.solid(new Color(0f, 0f, 0f, 0.65f)));

        // The card keeps its normal layout and is drawn larger around its centre.
        Container<CardView> zoom = new Container<CardView>(new CardView(card, big, small));
        zoom.size(CardView.W, CardView.H);
        zoom.setTransform(true);
        zoom.setOrigin(CardView.W / 2f, CardView.H / 2f);
        zoom.setScale(SCALE);
        float grow = (SCALE - 1f) * CardView.H / 2f;     // room for the part drawn outside the cell
        add(zoom).size(CardView.W, CardView.H).padTop(grow).padBottom(grow + 20).row();
        add(PlayerParts.caption("Level " + card.tier + " card  -  click anywhere to close", s));

        addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) { remove(); }
        });
    }
}

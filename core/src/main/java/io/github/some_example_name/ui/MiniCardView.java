package io.github.some_example_name.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Scaling;

import io.github.some_example_name.data.CardInfo;

/**
 * A small reserved card: the picture, points and bonus gem. It grows when the mouse is over it,
 * and clicking it runs {@link #onClick} (the screen uses that to open the full card).
 */
public class MiniCardView extends ClickableStack {
    private static final Color[] TIER_BG = {
        new Color(0.30f, 0.38f, 0.30f, 1), new Color(0.38f, 0.34f, 0.24f, 1), new Color(0.28f, 0.30f, 0.42f, 1)
    };
    public final CardInfo card;

    public MiniCardView(CardInfo card, int w, int h, float hoverScale, Label.LabelStyle caption) {
        super(hoverScale);
        this.card = card;
        add(new Image(Gfx.cover(card.art, w, h, TIER_BG[card.tier - 1])));

        Table ui = new Table();
        ui.pad(3);
        ui.add(new OutlinedLabel(card.points > 0 ? "" + card.points : "", caption, 1f)).expandX().top().left();
        Image bonus = new Image(GemIcons.of(card.bonus, 32));
        bonus.setScaling(Scaling.fit);
        int gem = Math.round(w * 0.36f);
        ui.add(bonus).size(gem).top().right().row();
        ui.add().colspan(2).expand();
        add(ui);
    }

    /** A faint box that keeps the place of a reserved card the player does not have. */
    public static Image emptySlot() {
        return new Image(Gfx.solid(new Color(1f, 1f, 1f, 0.10f)));
    }
}

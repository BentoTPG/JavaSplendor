package io.github.some_example_name.ui;

import java.util.Map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Scaling;

import io.github.some_example_name.data.CardInfo;
import io.github.some_example_name.model.Gem;

/** A development card: the picture, points at the top left, bonus gem at the top right, prices at the bottom left. */
public class CardView extends ClickableStack {
    public static final int W = 175, H = 265;

    /** Shown instead of the picture when the image file is missing. */
    private static final Color[] TIER_BG = {
        new Color(0.30f, 0.38f, 0.30f, 1), new Color(0.38f, 0.34f, 0.24f, 1), new Color(0.28f, 0.30f, 0.42f, 1)
    };
    public final CardInfo card;

    public CardView(CardInfo card, Label.LabelStyle big, Label.LabelStyle small) {
        super(1.08f);
        this.card = card;
        add(new Image(Gfx.cover(card.art, W, H, TIER_BG[card.tier - 1])));     // layer 0: the picture

        Table ui = new Table();                                                 // layer 1: points, bonus and prices
        ui.pad(8);
        ui.add(new OutlinedLabel(card.points > 0 ? "" + card.points : "", big, 3f))
          .expandX().top().left();
        Image bonus = new Image(GemIcons.of(card.bonus, 64));
        bonus.setScaling(Scaling.fit);
        ui.add(bonus).size(56).top().right().row();

        Table costs = new Table();
        int i = 0;
        for (Map.Entry<Gem, Integer> e : card.cost.entrySet()) {
            costs.add(GemIcons.chip(e.getKey(), e.getValue(), 48, small)).size(44).pad(2);
            if (++i % 2 == 0) costs.row();                                      // two chips per row
        }
        ui.add(costs).colspan(2).expand().bottom().left();
        add(ui);
    }
}

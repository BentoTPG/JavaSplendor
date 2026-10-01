package io.github.some_example_name.ui;

import java.util.Map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import io.github.some_example_name.model.Card;
import io.github.some_example_name.model.Gem;

public class CardView extends Stack {
    private static final Color[] TIER_BG = {
        new Color(0.30f, 0.38f, 0.30f, 1), new Color(0.38f, 0.34f, 0.24f, 1), new Color(0.28f, 0.30f, 0.42f, 1)
    };
    public final Card card;

    public CardView(Card card, Label.LabelStyle big, Label.LabelStyle small) {
        this.card = card;
        add(new Image(Gfx.solid(TIER_BG[card.tier - 1])));         // layer 0: background (photo later)

        Table ui = new Table();                                    // layer 1: overlays
        ui.pad(8);
        ui.add(new Label(card.points > 0 ? "" + card.points : "", big))
          .expandX().top().left();
        ui.add(new Image(Gfx.circle(card.bonus.color, 64))).size(56).top().right().row();

        Table costs = new Table();
        int i = 0;
        for (Map.Entry<Gem, Integer> e : card.cost.entrySet()) {
            Label n = new Label("" + e.getValue(), small);
            n.setAlignment(Align.center);
            costs.add(new Stack(new Image(Gfx.circle(e.getKey().color, 48)), n)).size(44).pad(2);
            if (++i % 2 == 0) costs.row();                         // two chips per row
        }
        ui.add(costs).colspan(2).expand().bottom().left();
        add(ui);

        setTransform(true);                                        // needed for the hover scale
        addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                System.out.println("clicked tier " + card.tier + " bonus " + card.bonus);
            }
            @Override public void enter(InputEvent e, float x, float y, int p, Actor from) {
                if (p != -1) return;
                setOrigin(Align.center);
                toFront();                                         // draw above neighbours
                addAction(Actions.scaleTo(1.08f, 1.08f, 0.1f, Interpolation.smooth));
            }
            @Override public void exit(InputEvent e, float x, float y, int p, Actor to) {
                if (p != -1) return;
                addAction(Actions.scaleTo(1f, 1f, 0.1f, Interpolation.smooth));
            }
        });
    }
}

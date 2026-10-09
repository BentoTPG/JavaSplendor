package io.github.some_example_name.ui;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

/** A Stack that grows a little when the mouse is over it and runs {@link #onClick} when clicked. */
public abstract class ClickableStack extends Stack {
    /** Set by the screen. Runs when the view is clicked. */
    public Runnable onClick;
    /** The point that stays still while the view grows, e.g. Align.bottom for views at the bottom of the screen. */
    public int hoverAlign = Align.center;

    protected ClickableStack(final float hoverScale) {
        setTransform(true);                                        // needed for scaling
        addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                if (onClick != null) onClick.run();
            }

            @Override public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer != -1) return;
                // Moving between the children of this stack also fires enter/exit; ignore those.
                if (fromActor != null && fromActor.isDescendantOf(ClickableStack.this)) return;
                setOrigin(hoverAlign);
                // Draw above the neighbours: this view inside its parent, and each parent inside its own,
                // so a growing card is not hidden by the panel next to it. Tables lay out by cells, so the order is safe to change.
                for (Actor a = ClickableStack.this; a.getParent() != null && a.getParent().getParent() != null; a = a.getParent()) {
                    a.toFront();
                }
                clearActions();
                addAction(Actions.scaleTo(hoverScale, hoverScale, 0.1f, Interpolation.smooth));
            }

            @Override public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer != -1) return;
                if (toActor != null && toActor.isDescendantOf(ClickableStack.this)) return;
                clearActions();
                addAction(Actions.scaleTo(1f, 1f, 0.1f, Interpolation.smooth));
            }
        });
    }
}

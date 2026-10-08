package io.github.some_example_name.data;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

/**
 * A pile of items that are drawn at random. Every drawn item is removed from the pile,
 * so the same item can never come out twice.
 */
public final class DrawPile<T> {
    private final Array<T> items;

    public DrawPile(Array<T> items) {
        this.items = new Array<T>(items);      // copy, so the caller's array is never changed
    }

    /** How many items are still in the pile. */
    public int size() { return items.size; }

    public boolean isEmpty() { return items.size == 0; }

    /** Removes one random item from the pile and returns it, or null when the pile is empty. */
    public T draw() {
        if (items.size == 0) return null;
        return items.removeIndex(MathUtils.random(items.size - 1));
    }

    /** Removes up to count random items from the pile (fewer if the pile runs out). */
    public Array<T> draw(int count) {
        Array<T> drawn = new Array<T>(true, Math.max(count, 1));
        for (int i = 0; i < count && items.size > 0; i++) drawn.add(draw());
        return drawn;
    }
}

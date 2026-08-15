package org.maz;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class GameControl {
    public static final float BURST_SPEED_NORMAL = 0.2f; // Spawn an item every X sec.
    public static final float BURST_SPEED_HIGH = 0.2f;
    public static final float PPM = 100f; // pixels per meter (to convert between box2d-meters and pixels)

    /** Scale factor to scale textures, body-fixtures, etc. according to (resized) window size. */
    public static float scale = 1f;

    private static GameControl INSTANCE;
    private static int itemIndex = 0;

    public static ScreenViewport viewport;
    public static Engine ashleyEngine;

    private GameControl() {}

    public static enum Item {
        ROCK,
        PAPER,
        SCISSORS
    }

    public static Item getNextItem() {
        itemIndex++;
        if (itemIndex > Item.values().length - 1) {
            itemIndex = 0;
        }
        return Item.values()[itemIndex];
    }

    public static Item getCurrentItem() {
        return Item.values()[itemIndex];
    }

    public static void setCurrentItem(Item item) {
        itemIndex = item.ordinal();
    }

}

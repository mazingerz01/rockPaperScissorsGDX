package org.maz;

import com.badlogic.ashley.core.Component;

public class ItemComponent implements Component {
    GameControl.Item item;

    ItemComponent(GameControl.Item item) {
        this.item = item;
    }
}

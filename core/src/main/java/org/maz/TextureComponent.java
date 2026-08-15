package org.maz;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class TextureComponent implements Component {
    public TextureRegion textureRegion;
    public float originX;
    public float originY;

    public TextureComponent(TextureRegion textureRegion) {
        this.textureRegion = textureRegion;
        originX = textureRegion.getRegionWidth() / 2f;
        originY = textureRegion.getRegionHeight() / 2f;
    }
}

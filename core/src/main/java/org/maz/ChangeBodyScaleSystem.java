package org.maz;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.physics.box2d.Body;

/** Change the size of a body fixture to fit the fixture to the scaled rendered image. */
public class ChangeBodyScaleSystem extends IteratingSystem {

    public ChangeBodyScaleSystem() {
        super(Family.all(TextureComponent.class, Box2DBodyComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        float width = ComponentMapper.getFor(TextureComponent.class).get(entity).textureRegion.getRegionWidth();
        Body body = ComponentMapper.getFor(Box2DBodyComponent.class).get(entity).body;
        body.destroyFixture(body.getFixtureList().get(0));

        // Apply new fixture which was created according to scale.
        GameControl.addItemFixture(body, width);
    }
}

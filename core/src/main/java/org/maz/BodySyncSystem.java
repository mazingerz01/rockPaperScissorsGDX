package org.maz;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.physics.box2d.Body;

public class BodySyncSystem extends IteratingSystem {
    private final ComponentMapper<PositionComponent> positionComponentMapper;
    private final ComponentMapper<Box2DBodyComponent> box2DBodyComponentMapper;

    /** Sync box2d-body with the position component, so that the former can be used for collision detection. */
    public BodySyncSystem() {
        super(Family.all(PositionComponent.class, Box2DBodyComponent.class).get());
        positionComponentMapper = ComponentMapper.getFor(PositionComponent.class);
        box2DBodyComponentMapper = ComponentMapper.getFor(Box2DBodyComponent.class);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        PositionComponent positionComponent = positionComponentMapper.get(entity);

        Body body = box2DBodyComponentMapper.get(entity).body;
        body.setTransform(positionComponent.pos.x, positionComponent.pos.y, 0);
    }
}

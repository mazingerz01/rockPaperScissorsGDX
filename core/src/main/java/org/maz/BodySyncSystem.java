package org.maz;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Body;

public class BodySyncSystem extends IteratingSystem {
    private final ComponentMapper<PositionComponent> positionComponentMapper;
    private final ComponentMapper<RotationComponent> rotationComponentMapper;
    private final ComponentMapper<Box2DBodyComponent> box2DBodyComponentMapper;

    /** Sync box2d-body with the position component, so that the former can be used for collision detection. */
    public BodySyncSystem() {
        super(Family.all(PositionComponent.class, RotationComponent.class, Box2DBodyComponent.class).get());
        positionComponentMapper = ComponentMapper.getFor(PositionComponent.class);
        rotationComponentMapper = ComponentMapper.getFor(RotationComponent.class);
        box2DBodyComponentMapper = ComponentMapper.getFor(Box2DBodyComponent.class);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        PositionComponent positionComponent = positionComponentMapper.get(entity);
        RotationComponent rotationComponent = rotationComponentMapper.get(entity);

        Body body = box2DBodyComponentMapper.get(entity).body;
        body.setTransform(positionComponent.pos.x, positionComponent.pos.y,
            rotationComponent.angle * MathUtils.degreesToRadians);
    }
}

package org.maz;

import java.util.Random;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Bezier;
import com.badlogic.gdx.math.Vector2;

public class MoveSystem extends IteratingSystem {
    ComponentMapper<PositionComponent> positionComponentComponentMapper;
    ComponentMapper<RotationComponent> rotationComponentComponentMapper;

    static final Random random = new Random();

    float speed = 0.2f;
    Vector2 out = new Vector2();

    public MoveSystem() {
        super(Family.all(PositionComponent.class, RotationComponent.class).get());
        positionComponentComponentMapper = ComponentMapper.getFor(PositionComponent.class);
        rotationComponentComponentMapper = ComponentMapper.getFor(RotationComponent.class);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        PositionComponent positionComponent = positionComponentComponentMapper.get(entity);
        RotationComponent rotationComponent = rotationComponentComponentMapper.get(entity);

        positionComponent.t += deltaTime * speed;

        positionComponent.bezier.valueAt(out, positionComponent.t);
        positionComponent.pos.set(out.x, out.y);
        if (positionComponent.t >= 1f) {
            positionComponent.t = 0;
            positionComponent.bezier = getBezier(new Vector2(out.x, out.y));
        }

        positionComponent.bezier.derivativeAt(out, positionComponent.t);
        rotationComponent.angle = out.angleDeg() - 90;
    }

    public static Bezier<Vector2> getBezier(Vector2 startPoint) {
        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();
        return new Bezier<>(startPoint,
            new Vector2(width * random.nextFloat(), height * random.nextFloat()),
            new Vector2(width * random.nextFloat(), height * random.nextFloat()));
    }
}

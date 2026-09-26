package org.maz;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

public class PhysicsSystem extends EntitySystem {
    private static final float FIXED_TIME_STEP = 1f / 60f;
    private static final float MAX_FRAME_TIME = 0.25f;

    public static World world;
    private float accumulator;

    public PhysicsSystem() {
        world = new World(new Vector2(0, 0), true);
    }

    @Override
    public void update(float deltaTime) {
        accumulator += Math.min(deltaTime, MAX_FRAME_TIME);
        while (accumulator >= FIXED_TIME_STEP) {
            world.step(FIXED_TIME_STEP, 6, 2);
            accumulator -= FIXED_TIME_STEP;
        }
    }

}

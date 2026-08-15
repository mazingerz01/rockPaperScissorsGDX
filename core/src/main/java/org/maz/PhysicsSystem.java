package org.maz;

import static org.maz.GameControl.viewport;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;

public class PhysicsSystem extends EntitySystem {
    public static World world;
    private final Box2DDebugRenderer debugRenderer;

    public PhysicsSystem() {
        debugRenderer = new Box2DDebugRenderer();
        debugRenderer.setDrawBodies(true);

        world = new World(new Vector2(0, 0), true);
    }

    @Override
    public void update(float deltaTime) {
        world.step(1 / 30f, 6, 2);
        debugRenderer.render(world, viewport.getCamera().combined);
        // do normal rendering
    }

}

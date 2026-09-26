package org.maz;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;

public class ExplosionSystem extends EntitySystem {

    private static final float DURATION = 2f;
    private static final float START_RADIUS = 4f;
    private static final float END_RADIUS = 32f;
    private static final float MAX_ALPHA = 0.5f;

    private final Array<Explosion> explosions = new Array<>();
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();

    void addExplosion(float x, float y) {
        explosions.add(new Explosion(x, y));
    }

    @Override
    public void update(float deltaTime) {
        for (int i = explosions.size - 1; i >= 0; i--) {
            Explosion explosion = explosions.get(i);
            explosion.age += deltaTime;
            if (explosion.age >= DURATION) {
                explosions.removeIndex(i);
            }
        }

        if (explosions.isEmpty()) {
            return;
        }

        Matrix4 cameraMatrix = GameControl.viewport.getCamera().combined;
        shapeRenderer.setProjectionMatrix(cameraMatrix);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Explosion explosion : explosions) {
            float progress = explosion.age / DURATION;
            float growthProgress = Math.min(progress * 2f, 1f);
            float radius = (START_RADIUS + (END_RADIUS - START_RADIUS) * growthProgress) * GameControl.scale;
            float alpha = MAX_ALPHA * (1f - progress);
            shapeRenderer.setColor(1f, 0f, 0f, alpha);
            shapeRenderer.circle(explosion.x, explosion.y, radius, 24);
        }
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    public void dispose() {
        shapeRenderer.dispose();
    }

    private static class Explosion {
        final float x;
        final float y;
        float age;

        Explosion(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
}

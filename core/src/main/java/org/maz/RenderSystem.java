package org.maz;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.Body;

public class RenderSystem extends IteratingSystem {

    private final SpriteBatch batch;
    private final ComponentMapper<TextureComponent> textureMapper;
    private final ComponentMapper<RotationComponent> rotationComponentComponentMapper;

    public RenderSystem(SpriteBatch batch) {
        super(Family.all(
            RotationComponent.class,
            TextureComponent.class,
            Box2DBodyComponent.class).get());
        this.batch = batch;

        rotationComponentComponentMapper = ComponentMapper.getFor(RotationComponent.class);
        textureMapper = ComponentMapper.getFor(TextureComponent.class);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        TextureComponent texture = textureMapper.get(entity);
        RotationComponent rotationComponent = rotationComponentComponentMapper.get(entity);
        Body body = ComponentMapper.getFor(Box2DBodyComponent.class).get(entity).body;

        float textureWidth = texture.textureRegion.getRegionWidth();
        float textureHeight = texture.textureRegion.getRegionHeight();

        // It's important to render the texture to the body position (not the other way around) to avoid stepping/delta mismatches.
        // All textures in the atlas have he same dimensions, scale textures according to screen size.
        batch.begin();
        batch.draw(texture.textureRegion, body.getPosition().x - texture.originX, body.getPosition().y - texture.originY,
            texture.originX, texture.originY, textureWidth, textureHeight,
            GameControl.scale, GameControl.scale, rotationComponent.angle);
        batch.end();
    }

}

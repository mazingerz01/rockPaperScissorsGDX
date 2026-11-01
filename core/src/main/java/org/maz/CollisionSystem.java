package org.maz;

import static org.maz.GameControl.Item.PAPER;
import static org.maz.GameControl.Item.ROCK;
import static org.maz.GameControl.Item.SCISSORS;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.WorldManifold;
import com.badlogic.gdx.utils.Array;
import org.maz.GameControl.Item;

public class CollisionSystem extends EntitySystem implements ContactListener {

    private final Array<CollisionEvent> contacts = new Array<>();
    private final Array<CollisionEvent> currentContacts = new Array<>();
    ComponentMapper<ItemComponent> itemComponentMapper = ComponentMapper.getFor(ItemComponent.class);
    ComponentMapper<TextureComponent> textureComponentMapper = ComponentMapper.getFor(TextureComponent.class);
    ComponentMapper<Box2DBodyComponent> box2DBodyComponentMapper = ComponentMapper.getFor(Box2DBodyComponent.class);

    CollisionSystem() {
        PhysicsSystem.world.setContactListener(this);
    }

    @Override
    public void beginContact(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();
        if (fixtureA == null || fixtureB == null) {
            return;
        }

        Body bodyA = fixtureA.getBody();
        Body bodyB = fixtureB.getBody();
        Object userDataA = bodyA.getUserData();
        Object userDataB = bodyB.getUserData();
        if (!(userDataA instanceof Entity entityA) || !(userDataB instanceof Entity entityB)) {
            return;
        }

        ItemComponent componentA = itemComponentMapper.get(entityA);
        ItemComponent componentB = itemComponentMapper.get(entityB);
        if (componentA == null || componentB == null || componentA.item == componentB.item) {
            return;
        }

        WorldManifold worldManifold = contact.getWorldManifold();
        Vector2[] points = worldManifold.getPoints();
        int pointCount = worldManifold.getNumberOfContactPoints();
        float x = 0;
        float y = 0;
        for (int point = 0; point < pointCount; point++) {
            x += points[point].x;
            y += points[point].y;
        }
        if (pointCount > 0) {
            x /= pointCount;
            y /= pointCount;
        }

        contacts.add(new CollisionEvent(entityA, componentA.item, bodyA,
            entityB, componentB.item, bodyB, x, y, pointCount > 0));
    }

    @Override
    public void endContact(Contact contact) {}

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {}

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {}

    @Override
    public void update(float deltaTime) {
        currentContacts.clear();
        currentContacts.addAll(contacts);
        contacts.clear();

        for (int i = currentContacts.size - 1; i >= 0; i--) {
            CollisionEvent event = currentContacts.get(i);
            ItemComponent componentA = itemComponentMapper.get(event.entityA);
            ItemComponent componentB = itemComponentMapper.get(event.entityB);
            Box2DBodyComponent bodyComponentA = box2DBodyComponentMapper.get(event.entityA);
            Box2DBodyComponent bodyComponentB = box2DBodyComponentMapper.get(event.entityB);
            if (componentA == null || componentB == null || bodyComponentA == null || bodyComponentB == null
                || componentA.item != event.itemA || componentB.item != event.itemB
                || bodyComponentA.body != event.bodyA || bodyComponentB.body != event.bodyB) {
                continue;
            }

            if (event.hasContactPoint) {
                GameControl.ashleyEngine.getSystem(ExplosionSystem.class)
                    .addExplosion(event.x, event.y);
            }

            if (beats(event.itemA, event.itemB)) {
                handleLoser(event.entityB, event.itemA);
            } else if (beats(event.itemB, event.itemA)) {
                handleLoser(event.entityA, event.itemB);
            }
        }
    }

    private boolean beats(Item itemA, Item itemB) {
        return (itemA == ROCK && itemB == SCISSORS)
            || (itemA == PAPER && itemB == ROCK)
            || (itemA == SCISSORS && itemB == PAPER);
    }

    private void handleLoser(Entity loser, Item winnerItem) {
        if (GameControl.isKillMode()) {
            GameControl.ashleyEngine.removeEntity(loser);
            Box2DBodyComponent bodyComponent = box2DBodyComponentMapper.get(loser);
            if (bodyComponent != null && bodyComponent.body != null) {
                PhysicsSystem.world.destroyBody(bodyComponent.body);
                bodyComponent.body = null;
            }
            return;
        }

        ItemComponent itemComponent = itemComponentMapper.get(loser);
        if (itemComponent == null) {
            return;
        }
        itemComponent.item = winnerItem;

        TextureComponent tc = textureComponentMapper.get(loser);
        if (tc != null) {
            tc.textureRegion = AssetManager.getInstance().getAtlasRegion(winnerItem);
            tc.originX = tc.textureRegion.getRegionWidth() / 2f;
            tc.originY = tc.textureRegion.getRegionHeight() / 2f;
        }

        Box2DBodyComponent bodyComponent = box2DBodyComponentMapper.get(loser);
        if (bodyComponent != null && bodyComponent.body != null) {
            PhysicsSystem.world.destroyBody(bodyComponent.body);
            float width = tc != null ? tc.textureRegion.getRegionWidth() : 16f;
            bodyComponent.body = GameControl.createBody(width, loser);
        }
    }

    private static class CollisionEvent {
        private final Entity entityA;
        private final Item itemA;
        private final Body bodyA;
        private final Entity entityB;
        private final Item itemB;
        private final Body bodyB;
        private final float x;
        private final float y;
        private final boolean hasContactPoint;

        private CollisionEvent(Entity entityA, Item itemA, Body bodyA, Entity entityB, Item itemB, Body bodyB,
                               float x, float y, boolean hasContactPoint) {
            this.entityA = entityA;
            this.itemA = itemA;
            this.bodyA = bodyA;
            this.entityB = entityB;
            this.itemB = itemB;
            this.bodyB = bodyB;
            this.x = x;
            this.y = y;
            this.hasContactPoint = hasContactPoint;
        }
    }

}

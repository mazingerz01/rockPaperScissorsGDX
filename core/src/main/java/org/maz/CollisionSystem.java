package org.maz;

import static org.maz.GameControl.Item.PAPER;
import static org.maz.GameControl.Item.ROCK;
import static org.maz.GameControl.Item.SCISSORS;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.WorldManifold;
import com.badlogic.gdx.utils.Array;
import org.maz.GameControl.Item;

public class CollisionSystem extends EntitySystem implements ContactListener {

    private final Array<Contact> contacts = new Array<>();
    private final Array<Contact> currentContacts = new Array<>();
    ComponentMapper<ItemComponent> itemComponentMapper = ComponentMapper.getFor(ItemComponent.class);
    ComponentMapper<TextureComponent> textureComponentMapper = ComponentMapper.getFor(TextureComponent.class);
    ComponentMapper<Box2DBodyComponent> box2DBodyComponentMapper = ComponentMapper.getFor(Box2DBodyComponent.class);

    CollisionSystem() {
        PhysicsSystem.world.setContactListener(this);
    }

    @Override
    public void beginContact(Contact contact) {
        // Do not destroy bodies during a step of world. Instead, note it for later.
        contacts.add(contact);
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
            Contact contact = currentContacts.get(i);
            if (contact == null || !contact.isEnabled()) {
                continue;
            }

            Fixture fixtureA = contact.getFixtureA();
            Fixture fixtureB = contact.getFixtureB();
            if (fixtureA == null || fixtureB == null) {
                continue;
            }

            Object userDataA = fixtureA.getBody().getUserData();
            Object userDataB = fixtureB.getBody().getUserData();
            if (!(userDataA instanceof Entity entityA) || !(userDataB instanceof Entity entityB)) {
                continue;
            }

            if (!entityA.equals(entityB)) {
                Item itemA = itemComponentMapper.get(entityA) != null ? itemComponentMapper.get(entityA).item : null;
                Item itemB = itemComponentMapper.get(entityB) != null ? itemComponentMapper.get(entityB).item : null;
                if (itemA == null || itemB == null) {
                    continue;
                }

                WorldManifold worldManifold = contact.getWorldManifold();
                Vector2[] points = worldManifold.getPoints();
                int pointCount = worldManifold.getNumberOfContactPoints();
                if (pointCount > 0) {
                    float x = 0;
                    float y = 0;
                    for (int point = 0; point < pointCount; point++) {
                        x += points[point].x;
                        y += points[point].y;
                    }
                    GameControl.ashleyEngine.getSystem(ExplosionSystem.class)
                        .addExplosion(x / pointCount, y / pointCount);
                }

                if (itemA == ROCK) {
                    switch (itemB) {
                        case SCISSORS:
                            handleB(entityB, contact);
                            break;
                        case PAPER:
                            handleA(entityA, contact);
                            break;
                    }
                }
                else if (itemA == PAPER) {
                    switch (itemB) {
                        case ROCK:
                            handleB(entityB, contact);
                            break;
                        case SCISSORS:
                            handleA(entityA, contact);
                            break;
                    }
                }
                else if (itemA == SCISSORS) {
                    switch (itemB) {
                        case PAPER:
                            handleB(entityB, contact);
                            break;
                        case ROCK:
                            handleA(entityA, contact);
                            break;
                    }
                }
            }
        }
    }

    private void handleA(Entity entityA, Contact contact) {
        // If kill mode is enabled, remove the losing entity as before.
        if (GameControl.isKillMode()) {
            GameControl.ashleyEngine.removeEntity(entityA);
            PhysicsSystem.world.destroyBody(contact.getFixtureA().getBody());
            return;
        }

        // In non-kill mode: transform the loser (entityA) into the winner's item instead of removing it.
        Fixture fixtureB = contact.getFixtureB();
        if (fixtureB == null || fixtureB.getBody() == null) {
            return;
        }
        Object ud = fixtureB.getBody().getUserData();
        if (!(ud instanceof Entity winnerEntity)) {
            return;
        }

        Item winnerItem = itemComponentMapper.get(winnerEntity) != null
                          ? itemComponentMapper.get(winnerEntity).item : null;
        if (winnerItem == null) {
            return;
        }

        // Update item component
        ItemComponent ic = itemComponentMapper.get(entityA);
        if (ic != null) {
            ic.item = winnerItem;
        }

        // Update texture component
        TextureComponent tc = textureComponentMapper.get(entityA);
        if (tc != null) {
            tc.textureRegion = AssetManager.getInstance().getAtlasRegion(winnerItem);
            tc.originX = tc.textureRegion.getRegionWidth() / 2f;
            tc.originY = tc.textureRegion.getRegionHeight() / 2f;
        }

        // Recreate physics body to match new texture size
        Box2DBodyComponent bc = box2DBodyComponentMapper.get(entityA);
        if (bc != null && bc.body != null) {
            // Destroy old body
            PhysicsSystem.world.destroyBody(bc.body);
            // Create a new body using the updated texture width
            float width = tc != null ? tc.textureRegion.getRegionWidth() : 16f;
            bc.body = GameControl.createBody(width, entityA);
        }
    }

    private void handleB(Entity entityB, Contact contact) {
        // If kill mode is enabled, remove the losing entity as before.
        if (GameControl.isKillMode()) {
            GameControl.ashleyEngine.removeEntity(entityB);
            PhysicsSystem.world.destroyBody(contact.getFixtureB().getBody());
            return;
        }

        // Non-kill mode: transform entityB into the winner's item (entityA)
        Fixture fixtureA = contact.getFixtureA();
        if (fixtureA == null || fixtureA.getBody() == null) {
            return;
        }
        Object ud = fixtureA.getBody().getUserData();
        if (!(ud instanceof Entity winnerEntity)) {
            return;
        }

        Item winnerItem = itemComponentMapper.get(winnerEntity) != null ? itemComponentMapper.get(winnerEntity).item : null;
        if (winnerItem == null) {
            return;
        }

        // Update item component
        ItemComponent ic = itemComponentMapper.get(entityB);
        if (ic != null) {
            ic.item = winnerItem;
        }

        // Update texture component
        TextureComponent tc = textureComponentMapper.get(entityB);
        if (tc != null) {
            tc.textureRegion = AssetManager.getInstance().getAtlasRegion(winnerItem);
            tc.originX = tc.textureRegion.getRegionWidth() / 2f;
            tc.originY = tc.textureRegion.getRegionHeight() / 2f;
        }

        // Recreate physics body to match new texture size
        Box2DBodyComponent bc = box2DBodyComponentMapper.get(entityB);
        if (bc != null && bc.body != null) {
            PhysicsSystem.world.destroyBody(bc.body);
            float width = tc != null ? tc.textureRegion.getRegionWidth() : 16f;
            bc.body = GameControl.createBody(width, entityB);
        }
    }

}

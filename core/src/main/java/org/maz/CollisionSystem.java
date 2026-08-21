package org.maz;

import static org.maz.GameControl.Item.PAPER;
import static org.maz.GameControl.Item.ROCK;
import static org.maz.GameControl.Item.SCISSORS;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.utils.Array;
import org.maz.GameControl.Item;

public class CollisionSystem extends EntitySystem implements ContactListener {

    private final Array<Contact> contacts = new Array<>();
    private final Array<Contact> currentContacts = new Array<>();
    ComponentMapper<ItemComponent> itemComponentMapper = ComponentMapper.getFor(ItemComponent.class);

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
        GameControl.ashleyEngine.removeEntity(entityA);
        PhysicsSystem.world.destroyBody(contact.getFixtureA().getBody());
        if (!GameControl.isKillMode()) {

        }
    }

    private void handleB(Entity entityB, Contact contact) {
        GameControl.ashleyEngine.removeEntity(entityB);
        PhysicsSystem.world.destroyBody(contact.getFixtureB().getBody());
    }

}

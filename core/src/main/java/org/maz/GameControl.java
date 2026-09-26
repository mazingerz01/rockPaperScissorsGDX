package org.maz;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class GameControl {
    private static GameControl INSTANCE;

    public static final float BURST_SPEED_NORMAL = 0.12f; // Spawn an item every X sec.
    public static final float BURST_SPEED_HIGH = 0.04f;
    public static final float PPM = 100f; // pixels per meter (to convert between box2d-meters and pixels)

    /** Scale factor to scale textures, body-fixtures, etc. according to (resized) window size. */
    public static float scale = 1f;
    private static int itemIndex = 0;
    private static boolean killMode = false;

    public static ScreenViewport viewport;
    public static Engine ashleyEngine;

    private GameControl() {
        System.out.println("GameControl instance created"); //xxxm
    }

    public enum Item {
        ROCK,
        PAPER,
        SCISSORS
    }

    public static Item getNextItem() {
        itemIndex++;
        if (itemIndex > Item.values().length - 1) {
            itemIndex = 0;
        }
        return Item.values()[itemIndex];
    }

    public static Item getCurrentItem() {
        return Item.values()[itemIndex];
    }

    public static void setCurrentItem(Item item) {
        itemIndex = item.ordinal();
    }

    public static void toggleKillMode() {
        killMode = !killMode;
    }

    public static boolean isKillMode() {return killMode;}

    public static void init() {
        ashleyEngine = new Engine();
        ashleyEngine.addSystem(new MoveSystem());
        ashleyEngine.addSystem(new BodySyncSystem());
        ashleyEngine.addSystem(new PhysicsSystem());
        ashleyEngine.addSystem(new CollisionSystem());
        ashleyEngine.addSystem(new RenderSystem(GameScreen.getInstance().getSpriteBatch()));
        ashleyEngine.addSystem(new ExplosionSystem());

        ChangeBodyScaleSystem changeBodyScaleSystem = new ChangeBodyScaleSystem();
        changeBodyScaleSystem.setProcessing(false); // // Only needed on resizing the window
        ashleyEngine.addSystem(changeBodyScaleSystem);

        GameControl.createEntity(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f, GameControl.Item.ROCK);

    }

    static void createEntity(float x, float y, GameControl.Item item) {
        Entity entity = new Entity();

        PositionComponent positionComponent = new PositionComponent();
        positionComponent.pos.set(x, y);
        positionComponent.bezier = MoveSystem.getBezier(new Vector2(x, y));
        entity.add(positionComponent);

        RotationComponent rot = new RotationComponent();
        rot.angle = 0;
        entity.add(rot);

        TextureComponent textureComponent = new TextureComponent(AssetManager.getInstance().getAtlasRegion(item));
        entity.add(textureComponent);

        ItemComponent itemComponent = new ItemComponent(item);
        entity.add(itemComponent);

        Box2DBodyComponent box2DBodyComponent = new Box2DBodyComponent();
        box2DBodyComponent.body = GameControl.createBody(textureComponent.textureRegion.getRegionWidth(), entity);
        entity.add(box2DBodyComponent);

        ashleyEngine.addEntity(entity);
    }

    static Body createBody(float width, Entity entity) {
        BodyDef bd = new BodyDef();
        bd.type = BodyDef.BodyType.DynamicBody;
        Body body = PhysicsSystem.world.createBody(bd);
        ComponentMapper<ItemComponent> componentMapper = ComponentMapper.getFor(ItemComponent.class);
        body.setUserData(componentMapper.get(entity).item);
        addItemFixture(body, width);
        body.setUserData(entity);
        return body;
    }

    static void addItemFixture(Body body, float width) {
        CircleShape shape = new CircleShape();
        float radius = width / 2 * GameControl.scale;
        shape.setRadius(radius);
        FixtureDef fd = new FixtureDef();
        fd.shape = shape;
        try {
            body.createFixture(fd);
        }
        finally {
            shape.dispose();
        }
    }

}

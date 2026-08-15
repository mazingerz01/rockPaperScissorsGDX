package org.maz;

import static org.maz.GameControl.ashleyEngine;
import static org.maz.GameControl.getCurrentItem;
import static org.maz.GameControl.viewport;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/** First screen of the application. Displayed after the application is created. */
public class GameScreen implements Screen {
    private SpriteBatch spriteBatch;
    private InputProcessor inputProcessor;
    private Stage uiStage;

    private float burstHelper;

    public GameScreen() {
    }

    @Override
    public void show() {
        spriteBatch = new SpriteBatch(); // This should be used in all screens
        viewport = new ScreenViewport();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);

        uiStage = new UIStage();

        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(uiStage);
        inputProcessor = new InputProcessor();
        inputMultiplexer.addProcessor(inputProcessor);
        Gdx.input.setInputProcessor(inputMultiplexer);

        ashleyEngine = new Engine();
        ashleyEngine.addSystem(new MoveSystem());
        ashleyEngine.addSystem(new RenderSystem(spriteBatch));

        // Box2D/Physics
        ashleyEngine.addSystem(new PhysicsSystem());
        ashleyEngine.addSystem(new BodySyncSystem());
        ChangeBodyScaleSystem changeBodyScaleSystem = new ChangeBodyScaleSystem();
        changeBodyScaleSystem.setProcessing(false); // // Only needed on resizing the window
        ashleyEngine.addSystem(changeBodyScaleSystem);
        ashleyEngine.addSystem(new CollisionSystem());

        determineScale();

        Gdx.graphics.setCursor(AssetManager.getInstance().getCursor(getCurrentItem()));
        createEntity(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f, GameControl.Item.ROCK);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.925f, 0.925f, 0.94f, 1f);

        burstHelper += delta;
        if (inputProcessor.touchDown && burstHelper > GameControl.BURST_SPEED_NORMAL) {
            Vector2 startPos = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
            createEntity(startPos.x, startPos.y, getCurrentItem());
            burstHelper = 0;
        }
        ashleyEngine.update(delta);

        uiStage.act(delta);
        uiStage.draw();
    }

    @Override
    public void resize(int width, int height) {
        // If the window is minimized on a desktop (LWJGL3) platform, width and height are 0, which causes problems.
        if (width <= 0 || height <= 0) {
            return;
        }

        viewport.update(width, height, true);
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);

        determineScale();

        // We don't need this system processed every time, just once on resizing.
        ashleyEngine.getSystem(ChangeBodyScaleSystem.class).setProcessing(true);
        ashleyEngine.update(0.5f);
        ashleyEngine.getSystem(ChangeBodyScaleSystem.class).setProcessing(false);

        uiStage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
        // Invoked when your application is paused.
    }

    @Override
    public void resume() {
        // Invoked when your application is resumed after pause.
    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        // Destroy screen's assets here.
    }

    void createEntity(float x, float y, GameControl.Item item) {
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
        box2DBodyComponent.body = createBody(textureComponent.textureRegion.getRegionWidth(), entity);
        entity.add(box2DBodyComponent);

        ashleyEngine.addEntity(entity);
    }

    private Body createBody(float width, Entity entity) {
        BodyDef bd = new BodyDef();
        bd.type = BodyDef.BodyType.DynamicBody;
        Body body = PhysicsSystem.world.createBody(bd);
        ComponentMapper<ItemComponent> componentMapper = ComponentMapper.getFor(ItemComponent.class);
        body.setUserData(componentMapper.get(entity).item);
        body.createFixture(createFixtureDefForItem(width));
        body.setUserData(entity);
        return body;
    }

    static FixtureDef createFixtureDefForItem(float width) {
        CircleShape shape = new CircleShape();
        float radius = width / 2 * GameControl.scale;
        shape.setRadius(radius);
        FixtureDef fd = new FixtureDef();
        fd.shape = shape;
        shape.dispose();
        return fd;
    }

    /** Determine general scale factor according to window size.*/
    private void determineScale() {
        GameControl.scale = Math.min(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()) * 0.0005f;
    }

}

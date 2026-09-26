package org.maz;

import static org.maz.GameControl.ashleyEngine;
import static org.maz.GameControl.getCurrentItem;
import static org.maz.GameControl.viewport;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/** First screen of the application. Displayed after the application is created. */
public class GameScreen implements Screen {
    private static final float DEBUG_ITEM_SCALE = 4f;
    private static GameScreen INSTANCE;
    private final SpriteBatch spriteBatch = new SpriteBatch();
    private InputProcessor inputProcessor;
    private Stage uiStage;

    private float burstHelper;

    private GameScreen() {
    }

    public static GameScreen getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new GameScreen();
        }
        return INSTANCE;
    }

    @Override
    public void show() {
        viewport = new ScreenViewport();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);

        uiStage = new UIStage();

        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(uiStage);
        inputProcessor = new InputProcessor();
        inputMultiplexer.addProcessor(inputProcessor);
        Gdx.input.setInputProcessor(inputMultiplexer);

        determineScale();

        Gdx.graphics.setCursor(AssetManager.getInstance().getCursor(getCurrentItem()));
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.925f, 0.925f, 0.94f, 1f);

        burstHelper += delta;
        if (inputProcessor.touchDown && burstHelper > GameControl.BURST_SPEED_NORMAL) {
            Vector2 startPos = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
            GameControl.createEntity(startPos.x, startPos.y, GameControl.getCurrentItem());
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
        ChangeBodyScaleSystem changeBodyScaleSystem =
            GameControl.ashleyEngine.getSystem(ChangeBodyScaleSystem.class);
        changeBodyScaleSystem.setProcessing(true);
        changeBodyScaleSystem.update(0f);
        changeBodyScaleSystem.setProcessing(false);

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
        GameControl.ashleyEngine.getSystem(ExplosionSystem.class).dispose();
        if (uiStage != null) {
            uiStage.dispose();
        }
    }

    /** Determine general scale factor according to window size.*/
    private void determineScale() {
        GameControl.scale = Math.min(Gdx.graphics.getWidth(), Gdx.graphics.getHeight())
                            * 0.0017f;
        //* DEBUG_ITEM_SCALE;
    }

    public SpriteBatch getSpriteBatch() {
        return spriteBatch;
    }
}

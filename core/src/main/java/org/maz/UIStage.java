package org.maz;

import static org.maz.GameControl.getCurrentItem;
import static org.maz.GameControl.setCurrentItem;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class UIStage extends Stage {
    private static final float WIDTH = 800; // Virtual size of the GUI
    private static final float HEIGHT = 400;
    private final float buttonSize;
    private final Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
    private Drawable buttonUp = null;
    private Drawable buttonDown = null;
    private Drawable buttonMouseOver = null;

    public UIStage() {
        super(new ExtendViewport(WIDTH, HEIGHT)); //Virtual size of the GUI

        float scaleFactor = (Gdx.app.getType() == Application.ApplicationType.Android) ? 0.06f : 0.035f;
        buttonSize = WIDTH * scaleFactor;

        initButtonBGs();

        Table rootTable = new Table();
        rootTable.setFillParent(true);
        this.addActor(rootTable);

        Table leftPanel = new Table();
        rootTable.add(leftPanel).width(buttonSize).top().left().expand().fill();

        Table buttonContainer = new Table(); // Primary to keep the cursor as an arrow as long as it hoovers over the buttons.
        buttonContainer.defaults().pad(1f);
        buttonContainer.add(createIconButton(GameControl.Item.ROCK)).size(buttonSize).row();
        buttonContainer.add(createIconButton(GameControl.Item.PAPER)).size(buttonSize).row();
        buttonContainer.add(createIconButton(GameControl.Item.SCISSORS)).size(buttonSize).row();
        buttonContainer.add(createIconButton(new TextureRegionDrawable(AssetManager.getInstance().getSprite(AssetManager.Sprite.SKULL)),
            GameControl::toggleKillMode)).size(buttonSize).row();

        // ButtonContainer-table must be touchable to become fully "hittable" (=be detected as an actor) so enter/exit works
        buttonContainer.setTouchable(Touchable.enabled);
        buttonContainer.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (fromActor == null) { // Only when entering from outside
                    Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (toActor == null) {                 // Only when leaving the container completely
                    Gdx.graphics.setCursor(AssetManager.getInstance().getCursor(getCurrentItem())
                    );
                }
            }
        });

        leftPanel.add(buttonContainer).width(buttonSize).top().left();
        leftPanel.add().expandY();
    }

    private ImageButton createIconButton(GameControl.Item item) {
        TextureRegionDrawable textureRegionDrawable = new TextureRegionDrawable(AssetManager.getInstance().getAtlasRegion(item));
        return createIconButton(textureRegionDrawable, () -> {
            setCurrentItem(item);
            Gdx.graphics.setCursor(AssetManager.getInstance().getCursor(item));
        });
    }

    private ImageButton createIconButton(TextureRegionDrawable textureRegionDrawable, Runnable onClickAction) {
        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();

        style.imageUp = textureRegionDrawable;
        style.imageDown = textureRegionDrawable;
        style.up = buttonUp;
        style.down = buttonDown;

        ImageButton button = new ImageButton(style);
        button.getImageCell().pad(3f);

        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onClickAction.run();
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                button.getStyle().up = buttonMouseOver;
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                button.getStyle().up = buttonUp;
            }
        });

        return button;
    }

    private void initButtonBGs() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0.4f, 0.4f, 0.4f, 0.3f);
        pixmap.fill();
        buttonUp = new TextureRegionDrawable(new Texture(pixmap));
        pixmap.dispose();

        pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0f, 0f, 0.7f, 0.6f);
        pixmap.fill();
        buttonDown = new TextureRegionDrawable(new Texture(pixmap));
        pixmap.dispose();

        pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(1f, 1f, 1f, 0.3f);
        pixmap.fill();
        buttonMouseOver = new TextureRegionDrawable(new Texture(pixmap));
        pixmap.dispose();
    }

}

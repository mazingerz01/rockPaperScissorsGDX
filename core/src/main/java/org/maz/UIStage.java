package org.maz;

import static org.maz.GameControl.getCurrentItem;
import static org.maz.GameControl.setCurrentItem;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
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
    private final FractionBar[] itemBars = new FractionBar[GameControl.Item.values().length];
    private final ComponentMapper<ItemComponent> itemMapper = ComponentMapper.getFor(ItemComponent.class);
    private final ImmutableArray<Entity> items =
        GameControl.ashleyEngine.getEntitiesFor(Family.all(ItemComponent.class).get());
    private final Texture barTexture;
    private final Texture settingsGearTexture;
    private Drawable buttonUp = null;
    private Drawable buttonDown = null;
    private Drawable buttonMouseOver = null;

    public UIStage() {
        super(new ExtendViewport(WIDTH, HEIGHT)); //Virtual size of the GUI

        float scaleFactor = (Gdx.app.getType() == Application.ApplicationType.Android) ? 0.06f : 0.035f;
        buttonSize = WIDTH * scaleFactor;

        initButtonBGs();
        barTexture = createBarTexture();
        settingsGearTexture = createSettingsGearTexture();

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
        ImageButton skullButton = createIconButton(new TextureRegionDrawable(AssetManager.getInstance().getSprite(AssetManager.Sprite.SKULL)),
            GameControl::toggleKillMode);
        // initial color depends on mode
        skullButton.setColor(GameControl.isKillMode() ? Color.RED : Color.WHITE);
        // update color after the toggle action runs
        skullButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                skullButton.setColor(GameControl.isKillMode() ? Color.RED : Color.WHITE);
            }
        });
        buttonContainer.add(skullButton).size(buttonSize).row();

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

        addItemBars();
        addSettingsButton();
    }

    @Override
    public void act(float delta) {
        updateItemBars();
        super.act(delta);
    }

    private ImageButton createIconButton(GameControl.Item item) {
        TextureRegionDrawable textureRegionDrawable = new TextureRegionDrawable(AssetManager.getInstance().getAtlasRegion(item));
        return createIconButton(textureRegionDrawable, () -> {
            setCurrentItem(item);
            Gdx.graphics.setCursor(AssetManager.getInstance().getCursor(item));
        });
    }

    private ImageButton createIconButton(TextureRegionDrawable textureRegionDrawable, Runnable onClickAction) {
        ImageButton button = createIconButton(textureRegionDrawable);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onClickAction.run();
            }
        });
        return button;
    }

    private ImageButton createIconButton(TextureRegionDrawable textureRegionDrawable) {
        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();

        style.imageUp = textureRegionDrawable;
        style.imageDown = textureRegionDrawable;
        style.up = buttonUp;
        style.down = buttonDown;

        ImageButton button = new ImageButton(style);
        button.getImageCell().pad(3f);

        button.addListener(new ClickListener() {
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
        pixmap.setColor(0.4f, 0.4f, 0.4f, 0.4f);
        pixmap.fill();
        buttonUp = new TextureRegionDrawable(new Texture(pixmap));
        pixmap.dispose();

        pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0f, 0f, 0.7f, 0.7f);
        pixmap.fill();
        buttonDown = new TextureRegionDrawable(new Texture(pixmap));
        pixmap.dispose();

        pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(1f, 1f, 1f, 0.4f);
        pixmap.fill();
        buttonMouseOver = new TextureRegionDrawable(new Texture(pixmap));
        pixmap.dispose();
    }

    private Texture createBarTexture() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private Texture createSettingsGearTexture() {
        int size = 50;
        float center = (size - 1) / 2f;
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = x - center;
                float dy = y - center;
                float radius = (float) Math.sqrt(dx * dx + dy * dy);
                float angle = (float) Math.atan2(dy, dx);
                float toothPosition = (angle + (float) Math.PI) * 8f / (float) (Math.PI * 2);
                float toothPhase = toothPosition - (float) Math.floor(toothPosition);
                boolean tooth = toothPhase > 0.2f && toothPhase < 0.8f;
                if (radius >= 7f && (radius <= 16f || (tooth && radius <= 22f))) {
                    pixmap.drawPixel(x, y);
                }
            }
        }

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private void addSettingsButton() {
        ImageButton settingsButton = createIconButton(
            new TextureRegionDrawable(new TextureRegion(settingsGearTexture)));
        settingsButton.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (fromActor == null) {
                    Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (toActor == null) {
                    Gdx.graphics.setCursor(AssetManager.getInstance().getCursor(getCurrentItem()));
                }
            }
        });

        Table settingsTable = new Table();
        settingsTable.setFillParent(true);
        settingsTable.bottom().left().pad(12f);
        settingsTable.add(settingsButton).size(buttonSize);
        addActor(settingsTable);
    }

    private void addItemBars() {
        TextureRegionDrawable barBase = new TextureRegionDrawable(new TextureRegion(barTexture));
        Color cyan = new Color(0f, 0.55f, 0.55f, 0.8f);

        Table barsTable = new Table();
        barsTable.setFillParent(true);
        barsTable.top().right().pad(12f);
        for (GameControl.Item item : GameControl.Item.values()) {
            FractionBar bar = new FractionBar(barBase.tint(cyan));
            itemBars[item.ordinal()] = bar;

            Image icon = new Image(new TextureRegionDrawable(AssetManager.getInstance().getAtlasRegion(item)));
            barsTable.add(bar).width(135f).height(9f).padRight(6f).padBottom(5f);
            barsTable.add(icon).size(21.12f).row();
        }
        addActor(barsTable);
    }

    private void updateItemBars() {
        int[] counts = new int[itemBars.length];
        int total = 0;
        for (int i = 0; i < items.size(); i++) {
            int itemIndex = itemMapper.get(items.get(i)).item.ordinal();
            counts[itemIndex]++;
            total++;
        }

        for (int i = 0; i < itemBars.length; i++) {
            itemBars[i].setFraction(total == 0 ? 0f : counts[i] / (float) total);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        barTexture.dispose();
        settingsGearTexture.dispose();
        skin.dispose();
    }

    private static class FractionBar extends Actor {
        private final Drawable fill;
        private float fraction;

        FractionBar(Drawable fill) {
            this.fill = fill;
            setTouchable(Touchable.disabled);
        }

        void setFraction(float fraction) {
            this.fraction = fraction;
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            if (fraction <= 0f) {
                return;
            }
            Color color = getColor();
            batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
            float fillWidth = getWidth() * fraction;
            fill.draw(batch, getX() + getWidth() - fillWidth, getY(), fillWidth, getHeight());
            batch.setColor(Color.WHITE);
        }
    }
}

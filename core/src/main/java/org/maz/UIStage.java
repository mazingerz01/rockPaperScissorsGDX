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
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
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
    private final Texture backArrowTexture;
    private final Texture flashTexture;
    private final Texture settingsDialogBackgroundTexture;
    private final Color barColor = new Color(0f, 0.55f, 0.55f, 0.8f);
    private Table settingsDialog;
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
        backArrowTexture = createBackArrowTexture();
        flashTexture = createFlashTexture();
        settingsDialogBackgroundTexture = createWindowBackgroundTexture();

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
        boolean[][] gearPixels = new boolean[size][size];

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = x - center;
                float dy = y - center;
                float radius = (float) Math.sqrt(dx * dx + dy * dy);
                float angle = (float) Math.atan2(dy, dx);
                float toothPosition = (angle + (float) Math.PI) * 8f / (float) (Math.PI * 2);
                float toothPhase = toothPosition - (float) Math.floor(toothPosition);
                boolean tooth = toothPhase > 0.2f && toothPhase < 0.8f;
                gearPixels[y][x] = radius >= 7f && (radius <= 16f || (tooth && radius <= 22f));
            }
        }

        // Add a black outline around the gear by checking for neighboring gear pixels
        pixmap.setColor(Color.BLACK);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                if (!gearPixels[y][x] && hasIconNeighbor(gearPixels, x, y)) {
                    pixmap.drawPixel(x, y);
                }
            }
        }

        pixmap.setColor(Color.SKY);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                if (gearPixels[y][x]) {
                    pixmap.drawPixel(x, y);
                }
            }
        }

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private boolean hasIconNeighbor(boolean[][] iconPixels, int x, int y) {
        for (int neighborY = Math.max(0, y - 1); neighborY <= Math.min(iconPixels.length - 1, y + 1); neighborY++) {
            for (int neighborX = Math.max(0, x - 1); neighborX <= Math.min(iconPixels[neighborY].length - 1, x + 1); neighborX++) {
                if (iconPixels[neighborY][neighborX]) {
                    return true;
                }
            }
        }
        return false;
    }

    private Texture createFlashTexture() {
        int size = 50;
        int[] polygonX = {30, 14, 23, 19, 38, 27};
        int[] polygonY = {4, 27, 27, 46, 20, 20};
        boolean[][] flashPixels = new boolean[size][size];
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                flashPixels[y][x] = isInsidePolygon(x, y, polygonX, polygonY);
            }
        }

        pixmap.setColor(Color.BLACK);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                if (!flashPixels[y][x] && hasIconNeighbor(flashPixels, x, y)) {
                    pixmap.drawPixel(x, y);
                }
            }
        }

        pixmap.setColor(Color.SKY);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                if (flashPixels[y][x]) {
                    pixmap.drawPixel(x, y);
                }
            }
        }

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private Texture createBackArrowTexture() {
        int size = 50;
        int[] polygonX = {6, 22, 22, 42, 42, 22, 22};
        int[] polygonY = {25, 9, 18, 18, 32, 32, 41};
        boolean[][] arrowPixels = new boolean[size][size];
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                arrowPixels[y][x] = isInsidePolygon(x, y, polygonX, polygonY);
            }
        }

        pixmap.setColor(Color.BLACK);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                if (!arrowPixels[y][x] && hasIconNeighbor(arrowPixels, x, y)) {
                    pixmap.drawPixel(x, y);
                }
            }
        }

        pixmap.setColor(Color.SKY);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                if (arrowPixels[y][x]) {
                    pixmap.drawPixel(x, y);
                }
            }
        }

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private boolean isInsidePolygon(int x, int y, int[] polygonX, int[] polygonY) {
        boolean inside = false;
        for (int i = 0, j = polygonX.length - 1; i < polygonX.length; j = i++) {
            if ((polygonY[i] > y) != (polygonY[j] > y)
                && x < (polygonX[j] - polygonX[i]) * (y - polygonY[i])
                    / (float) (polygonY[j] - polygonY[i]) + polygonX[i]) {
                inside = !inside;
            }
        }
        return inside;
    }

    private void addSettingsButton() {
        ImageButton flashButton = createIconButton(
            new TextureRegionDrawable(new TextureRegion(flashTexture)));
        ImageButton settingsButton = createIconButton(
            new TextureRegionDrawable(new TextureRegion(settingsGearTexture)));
        addCursorListeners(flashButton);
        addCursorListeners(settingsButton);
        settingsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showSettingsDialog();
            }
        });

        Table settingsTable = new Table();
        settingsTable.setFillParent(true);
        settingsTable.bottom().left().pad(12f);
        settingsTable.add(flashButton).size(buttonSize).row();
        settingsTable.add(settingsButton).size(buttonSize);
        addActor(settingsTable);
    }

    private void addCursorListeners(ImageButton button) {
        button.addListener(new InputListener() {
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
    }

    private void showSettingsDialog() {
        if (settingsDialog != null) {
            return;
        }
        settingsDialog = new Table();
        settingsDialog.setFillParent(true);
        settingsDialog.setTouchable(Touchable.enabled);
        settingsDialog.top().center();
        settingsDialog.setBackground(new TextureRegionDrawable(new TextureRegion(settingsDialogBackgroundTexture)));

        Label title = new Label("Settings", skin);
        title.setColor(barColor);
        settingsDialog.add(title).top().center().padTop(10f).row();

        Slider itemScaleSlider = new Slider(0.5f, 2f, 0.1f, false, skin);
        itemScaleSlider.setValue(GameControl.itemScaleMultiplier);
        ItemScalePreview preview = new ItemScalePreview(
            AssetManager.getInstance().getAtlasRegion(GameControl.Item.ROCK));
        preview.setScaleMultiplier(GameControl.itemScaleMultiplier);
        itemScaleSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameControl.itemScaleMultiplier = itemScaleSlider.getValue();
                preview.setScaleMultiplier(GameControl.itemScaleMultiplier);

                ChangeBodyScaleSystem scaleSystem =
                    GameControl.ashleyEngine.getSystem(ChangeBodyScaleSystem.class);
                scaleSystem.setProcessing(true);
                scaleSystem.update(0f);
                scaleSystem.setProcessing(false);
            }
        });

        Table scaleControls = new Table();
        scaleControls.add(itemScaleSlider).width(240f).height(32f).padRight(12f);
        scaleControls.add(preview).size(72f);
        settingsDialog.add(scaleControls).center().padTop(20f).row();

        ImageButton backButton = createIconButton(
            new TextureRegionDrawable(new TextureRegion(backArrowTexture)));
        addCursorListeners(backButton);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                settingsDialog.remove();
                settingsDialog = null;
            }
        });
        settingsDialog.add(backButton).size(buttonSize).expand().left().bottom().pad(12f);
        addActor(settingsDialog);
    }

    private Texture createWindowBackgroundTexture() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0.925f, 0.925f, 0.94f, 1f);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
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
            barsTable.add(icon).size(21.12f).padBottom(2f).row();
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
        backArrowTexture.dispose();
        flashTexture.dispose();
        settingsDialogBackgroundTexture.dispose();
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

    private static class ItemScalePreview extends Actor {
        private static final float PREVIEW_SIZE = 36f;
        private final TextureRegion itemTexture;
        private float scaleMultiplier = 1f;

        ItemScalePreview(TextureRegion itemTexture) {
            this.itemTexture = itemTexture;
            setTouchable(Touchable.disabled);
        }

        void setScaleMultiplier(float scaleMultiplier) {
            this.scaleMultiplier = scaleMultiplier;
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            float width = PREVIEW_SIZE * scaleMultiplier;
            float height = width * itemTexture.getRegionHeight() / itemTexture.getRegionWidth();
            float x = getX() + (getWidth() - width) / 2f;
            float y = getY() + (getHeight() - height) / 2f;
            Color color = getColor();
            batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
            batch.draw(itemTexture, x, y, width, height);
            batch.setColor(Color.WHITE);
        }
    }
}

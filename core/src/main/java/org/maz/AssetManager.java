package org.maz;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public class AssetManager {
    private static AssetManager INSTANCE;

    Cursor cursorRock;
    Cursor cursorPaper;
    Cursor cursorScissors;
    TextureAtlas.AtlasRegion rock;
    TextureAtlas.AtlasRegion paper;
    TextureAtlas.AtlasRegion scissors;
    TextureAtlas.AtlasRegion skull;

    private AssetManager() {
    }

    public static AssetManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AssetManager();
        }
        return INSTANCE;
    }

    public static enum Sprite {
        SKULL
    }

    void load() {
        Pixmap pixmap = new Pixmap(Gdx.files.internal("cursor/rock.png"));
        cursorRock = Gdx.graphics.newCursor(pixmap, 0, 0);
        pixmap = new Pixmap(Gdx.files.internal("cursor/paper.png"));
        cursorPaper = Gdx.graphics.newCursor(pixmap, 0, 0);
        pixmap = new Pixmap(Gdx.files.internal("cursor/scissors.png"));
        cursorScissors = Gdx.graphics.newCursor(pixmap, 0, 0);

        TextureAtlas textureAtlas = new TextureAtlas("rps.atlas");
        rock = textureAtlas.findRegion("rock");
        scissors = textureAtlas.findRegion("scissors");
        paper = textureAtlas.findRegion("paper");
        skull = textureAtlas.findRegion("skull");
    }

    public TextureAtlas.AtlasRegion getAtlasRegion(GameControl.Item item) {
        return switch (item) {
            case ROCK -> this.rock;
            case PAPER -> this.paper;
            case SCISSORS -> this.scissors;
        };
    }

    public TextureAtlas.AtlasRegion getSprite(Sprite sprite) {
        return switch (sprite) {
            case SKULL -> this.skull;
        };
    }

    public Cursor getCursor(GameControl.Item item) {
        return switch (item) {
            case ROCK -> this.cursorRock;
            case PAPER -> this.cursorPaper;
            case SCISSORS -> this.cursorScissors;
        };
    }
}

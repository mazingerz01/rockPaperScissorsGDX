package org.maz;

import com.badlogic.gdx.Game;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class RPS extends Game {

    @Override
    public void create() {
        AssetManager.getInstance().load();
        GameControl.init();
        setScreen(GameScreen.getInstance());
    }
}

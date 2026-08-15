package org.maz;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Bezier;
import com.badlogic.gdx.math.Vector2;

public class PositionComponent implements Component {
    public Vector2 pos = new Vector2(0f, 0f);
    public Bezier<Vector2> bezier = new Bezier<>();
    public float t;
}

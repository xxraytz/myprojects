package com.mobimore.graphicsObjects;

import com.mobimore.camera.Camera;
import com.mobimore.graphicsObjects.waveFront.Vect3D;

import java.awt.*;

/**
 * Created by mobimore on 10/9/16.
 */
public abstract class Drawable {
    private boolean visible = true;
    private String name;

    public abstract void paint(final Graphics graphics, final Camera camera, final int[][] zBuffer);
    public final boolean isVisible() {
        return visible;
    }
    public final void setVisible(boolean visible) {
        this.visible = visible;
    }
    public abstract Vect3D getCenter();
    public void setColor(Color color){}
    public Color getColor(){
        return null;
    }
    public final String getName() {
        return name;
    }
    public final void setName(String name) {
        this.name = name;
    }

    public void onPanelSizeChanged(Dimension dimension) {}
}

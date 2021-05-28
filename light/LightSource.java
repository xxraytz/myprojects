package com.mobimore.light;

import com.mobimore.graphicsObjects.waveFront.Vect3D;

/**
 * Created by mobimore on 1/5/17.
 */
public class LightSource {
    protected Vect3D position;
    protected float strength;

    public LightSource(Vect3D position, float strength) {
        this.position = position;
        this.strength = strength;
    }

    public Vect3D getPosition() {
        return new Vect3D(position);
    }

    public void setPosition(Vect3D position) {
        this.position = new Vect3D(position);
    }

    public float getStrength() {
        return strength;
    }

    public void setStrength(float strength) {
        this.strength = strength;
    }

    public void translate(double dx, double dy, double dz){
        position.x += dx;
        position.y += dy;
        position.z += dz;
    }
}

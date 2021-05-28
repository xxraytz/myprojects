package com.mobimore.camera;

import com.mobimore.graphicsObjects.waveFront.Vect3D;

/**
 * Created by mobimore on 10/12/16.
 */
public class Camera {
    private Vect3D position;
    private double[][] perspectiveArr; //full Camera transformation array

    //eX, eY, eZ - viewers position
    //dx, dy, dz - moving object when applying perspective transformation
    public Camera(double eX, double eY, double eZ, double dx, double dy, double dz) {
        position = new Vect3D(eX, eY, eZ);
        this.perspectiveArr = new double[][]{
                {1.0, 0.0, -eX/eZ,  dx},
                {0.0, 1.0, -eY/eZ,  dy},
                {0.0, 0.0,    1.0,  dz},
                {0.0, 0.0,  -1/eZ, 1.0}
        };
    }

    public Camera(Vect3D position, Vect3D delta) {
        this.position = new Vect3D(position);
        double eX = position.x;
        double eY = position.y;
        double eZ = position.z;
        this.perspectiveArr = new double[][]{
                {1.0, 0.0, -eX/eZ,  delta.x},
                {0.0, 1.0, -eY/eZ,  delta.y},
                {0.0, 0.0,    1.0,  delta.z},
                {0.0, 0.0,  -1/eZ,      1.0}
        };
    }

    public double[][] getPerspectiveArr() {
        return perspectiveArr;
    }

    public void setPerspectiveArr(double eX, double eY, double eZ) {
        this.perspectiveArr = new double[][]{
                {1.0, 0.0, -eX/eZ,  0},
                {0.0, 1.0, -eY/eZ,  0},
                {0.0, 0.0,   1.0,   0},
                {0.0, 0.0, -1/eZ, 1.0}
        };
    }

    public void setEx(double eX) {
        double eZ = -1 / perspectiveArr[3][2];
        this.perspectiveArr[1][2] = -eX/eZ;
        position.x = eX;
    }

    public void setEy(double eY) {
        double eZ = -1 / perspectiveArr[3][2];
        this.perspectiveArr[1][2] = -eY/eZ;
        position.y = eY;
    }

    public void setEz(double eZ) {
        this.perspectiveArr[3][2] = -1/eZ;
        position.z = eZ;
    }

    public void setDx(double dx){
        this.perspectiveArr[0][3] = dx;
    }
    public void setDy(double dy){
        this.perspectiveArr[1][3] = dy;
    }
    public void setDz(double dz){
        this.perspectiveArr[2][3] = dz;
    }

    public Vect3D getPosition(){
        return new Vect3D(position);
    }
}

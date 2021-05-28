package com.mobimore.graphicsObjects.waveFront;

/**
 * Created by mobimore on 10/9/16.
 */
public class Vect3D {
    public double x ,y, z;

    public Vect3D(){
        x = 0;
        y = 0;
        z = 0;
    }

    public Vect3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vect3D(double[] coords) {
        x = coords[0];
        y = coords[1];
        z = coords[2];
    }

    public Vect3D(int[] coords){
        x = coords[0];
        y = coords[1];
        z = coords[2];
    }
    public Vect3D(Vect3D vect3D) {
        x = vect3D.getX();
        y = vect3D.getY();
        z = vect3D.getZ();
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public void setLocation(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vect3D plus(Vect3D vect3D) {
        return new Vect3D(x + vect3D.x, y + vect3D.y, z + vect3D.z);
    }

    public Vect3D minus(Vect3D vect3D) {
        return new Vect3D(x - vect3D.x, y - vect3D.y, z - vect3D.z);
    }

    public double dot(Vect3D vect3D) {
        return x * vect3D.x + y * vect3D.y + z * vect3D.z;
    }

    public Vect3D div(double num){
        return new Vect3D(x / num, y / num, z / num);
    }

    public Vect3D mult(double num){
        return new Vect3D(x * num, y * num, z * num);
    }

    public Vect3D cross(Vect3D vect3D) {
        return new Vect3D(y * vect3D.z - vect3D.y * z, z * vect3D.x - vect3D.z * x, x * vect3D.y - vect3D.x * y);
    }

    public void normalize(){
        double sqrtLength = Math.sqrt(x*x+y*y+z*z);
        if (sqrtLength == 0) {
            sqrtLength = 1;
        }
        x /= sqrtLength;
        y /= sqrtLength;
        z /= sqrtLength;
    }

    public Vect3D normal(){
        double x = this.x, y = this.y, z = this.z;
        double sqrtLength = Math.sqrt(x*x+y*y+z*z);
        if (sqrtLength == 0) {
            sqrtLength = 1;
        }
        x /= sqrtLength;
        y /= sqrtLength;
        z /= sqrtLength;
        return new Vect3D(x, y, z);
    }

    @Override
    public String toString() {
        return "x: " + x + " y: " + y + " z: " + z;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() == Vect3D.class){
            Vect3D objV = (Vect3D) obj;
            //if ((Double.compare(x, objV.x))==0 && (Double.compare(y, objV.y)==0) && (Double.compare(z, objV.z)==0)) return true;
            return (x == objV.x) && (y == objV.y) && (z == objV.z);
        }
        return super.equals(obj);
    }

    public double length(){
        return Math.sqrt(x * x + y * y + z * z);
    }
}

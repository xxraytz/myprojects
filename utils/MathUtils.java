package com.mobimore.utils;

import Jama.Matrix;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Created by mobimore on 11/15/16.
 */
public class MathUtils {

    public static double[][] multiplyMatrix(double[][] transformationArr, double[][] originalArr) {
        Matrix originalMatrix = new Matrix(originalArr).transpose();
        Matrix transformationMatrix = new Matrix(transformationArr);
        Matrix result = transformationMatrix.times(originalMatrix);
        return result.transpose().getArray();
    }

    public static double[][] multiplyMatrix(Matrix transformationMatrix, double[][] originalArr) {
        Matrix originalMatrix = new Matrix(originalArr).transpose();
        Matrix result = transformationMatrix.times(originalMatrix);
        return result.transpose().getArray();
    }

    public static Double[] substractMatrix(Double[] arrA, Double[] arrB) {
        Double[] result = new Double[arrA.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = arrA[i] - arrB[i];
        }
        return result;
    }

    public static double lerp(double input, double sourceRangeLow, double sourceRangeHigh, double destRangeLow, double destRangeHigh) {
        input = min(sourceRangeLow,max(sourceRangeHigh, input));
        return ((input - sourceRangeLow) / (sourceRangeHigh - sourceRangeLow)) * (destRangeHigh - destRangeLow) + destRangeLow;
    }

    /*public static float inverseLerp(float minVal, float maxVal, float lerpValue) {
        return (lerpValue - minVal) / (maxVal - minVal);
    }*/
}

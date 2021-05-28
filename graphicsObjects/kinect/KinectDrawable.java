package com.mobimore.graphicsObjects.kinect;

import com.mobimore.GTest.FallDetectionListener;
import com.mobimore.GTest.SkeletonDrawableObserver;
import com.mobimore.camera.Camera;
import com.mobimore.graphicsObjects.Controllable;
import com.mobimore.graphicsObjects.Drawable;
import com.mobimore.graphicsObjects.waveFront.Vect3D;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class KinectDrawable extends Drawable implements Controllable {
    private DepthDrawable depthDrawable;
    private SkeletonDrawable skeletonDrawable;
    private SkeletonDrawable skeletonDrawableSkSpace;
    private int fallStartedFrame = -1;
    private int fallEndedFrame = -1;
    private double maxHeight = -1;

    public String getFolderWithFiles() {
        return folderWithFiles;
    }

    private String folderWithFiles;

    public void setFallDetectionListener(FallDetectionListener fallDetectionListener) {
        this.skeletonDrawableSkSpace.setFallDetectionListener(fallDetectionListener);
    }

    public void setSkeletonDrawableObserver(SkeletonDrawableObserver observer){
        this.skeletonDrawableSkSpace.setObserver(observer);
    }

    public static KinectDrawable fromFolder(File folder, String name){
        String folderWithFiles = folder.getAbsolutePath() + File.separatorChar;
        DepthDrawable depthDrawable = DepthDrawable.fromFolder(new File(folderWithFiles + "Depth"), name + "_depth");
        SkeletonDrawable skeletonDrawable = null;
        SkeletonDrawable skeletonDrawableSkSpace = null;
        File[] files = folder.listFiles();
        assert files != null;
        for (File fi : files) {
            if (fi.getName().contains(".skeleton")) {
                skeletonDrawable = SkeletonDrawable.fromCSVFile(new File(folderWithFiles + fi.getName()), name + "skeleton", false);
                skeletonDrawableSkSpace = SkeletonDrawable.fromCSVFile(new File(folderWithFiles + fi.getName()), name + "skeleton", true);
            }
        }
        if (skeletonDrawable == null)
            skeletonDrawable = SkeletonDrawable.fromCSVFile(new File(folderWithFiles + "Body" + File.separatorChar + "Fileskeleton.csv"), name + "skeleton", false);
        if (skeletonDrawableSkSpace == null)
            skeletonDrawableSkSpace = SkeletonDrawable.fromCSVFile(new File(folderWithFiles + "Body" + File.separatorChar + "FileskeletonSkSpace.csv"), name + "skeletonSkSpace", true);
        KinectDrawable kinectDrawable = new KinectDrawable(name, folderWithFiles);
        kinectDrawable.depthDrawable = depthDrawable;
        kinectDrawable.skeletonDrawable = skeletonDrawable;
        kinectDrawable.skeletonDrawableSkSpace = skeletonDrawableSkSpace;
        kinectDrawable.skeletonDrawable.setFallStartFrame(kinectDrawable.fallStartedFrame);
        kinectDrawable.skeletonDrawable.setFallEndFrame(kinectDrawable.fallEndedFrame);
        kinectDrawable.skeletonDrawableSkSpace.setFallStartFrame(kinectDrawable.fallStartedFrame);
        kinectDrawable.skeletonDrawableSkSpace.setFallEndFrame(kinectDrawable.fallEndedFrame);
        kinectDrawable.maxHeight = skeletonDrawableSkSpace.getMaxHeight();

        return kinectDrawable;
    }

    private void readFallMarks(File file) throws IOException {
        List<String> lines = Files.readAllLines(file.toPath());
        String[] line = lines.get(1).split(", ");
        fallStartedFrame = Integer.parseInt(line[0]);
        fallEndedFrame = Integer.parseInt(line[1]);
    }

    private KinectDrawable(String name, String folderWithFiles) {
        setName(name);
        this.folderWithFiles = folderWithFiles;
        File fallMarksFile = new File(folderWithFiles + "fallMarks.csv");
        if(fallMarksFile.exists()){
            try {
                readFallMarks(fallMarksFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void paint(Graphics graphics, Camera camera, int[][] zBuffer) {
        if (depthDrawable != null) {
            depthDrawable.paint(graphics, camera, zBuffer);
        }
        skeletonDrawableSkSpace.paint(graphics, camera, zBuffer);
        skeletonDrawable.isOutlier = skeletonDrawableSkSpace.isOutlier;
        skeletonDrawable.paint(graphics, camera, zBuffer);

    }

    @Override
    public Vect3D getCenter() {
        return null;
    }

    @Override
    public void setColor(Color color) {
        skeletonDrawable.setColor(color);
    }

    @Override
    public Color getColor() {
        return null;
    }

    @Override
    public void pause() {
        if(depthDrawable!=null){
            depthDrawable.pause();
        }

        if (skeletonDrawable != null) {
            skeletonDrawable.pause();
        }

        if (skeletonDrawableSkSpace != null) {
            skeletonDrawableSkSpace.pause();
        }
    }

    @Override
    public void resume() {
        if(depthDrawable!=null){
            depthDrawable.resume();
        }

        if (skeletonDrawable != null) {
            skeletonDrawable.resume();
        }

        if (skeletonDrawableSkSpace != null) {
            skeletonDrawableSkSpace.resume();
        }
    }

    @Override
    public void restart() {
        if(depthDrawable!=null){
            depthDrawable.restart();
        }

        if (skeletonDrawable != null) {
            skeletonDrawable.restart();
        }

        if (skeletonDrawableSkSpace != null) {
            skeletonDrawableSkSpace.restart();
        }
    }

    @Override
    public void stepBack() {
        if(depthDrawable!=null){
            depthDrawable.stepBack();
        }

        if (skeletonDrawable != null) {
            skeletonDrawable.stepBack();
        }

        if (skeletonDrawableSkSpace != null) {
            skeletonDrawableSkSpace.stepBack();
        }
    }

    @Override
    public void stepFW() {
        if(depthDrawable!=null){
            depthDrawable.stepFW();
        }

        if (skeletonDrawable != null) {
            skeletonDrawable.stepFW();
        }

        if (skeletonDrawableSkSpace != null) {
            skeletonDrawableSkSpace.stepFW();
        }
    }

    @Override
    public int getCurrentFrameNumber() {
        if (skeletonDrawable != null) {
            return skeletonDrawable.getCurrentFrameNumber();
        }
        return -1;
    }

    @Override
    public boolean isPaused() {
        return depthDrawable.isPaused() && skeletonDrawable.isPaused();
    }

    public int getFallStartedFrame() {
        return fallStartedFrame;
    }

    public void setFallStartedFrame(int fallStartedFrame) {
        this.fallStartedFrame = fallStartedFrame;
        this.skeletonDrawable.setFallStartFrame(fallStartedFrame);
    }

    public int getFallEndedFrame() {
        return fallEndedFrame;
    }

    public void setFallEndedFrame(int fallEndedFrame) {
        this.fallEndedFrame = fallEndedFrame;
        this.skeletonDrawable.setFallEndFrame(fallEndedFrame);
    }

    public double getMaxHeight() {
        return maxHeight;
    }

    public void saveDataToFolder() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("fallStartedFrame, ").append("fallEndedFrame, ").append("height\n");
        stringBuilder.append(fallStartedFrame).append(", ").append(fallEndedFrame).append(", ").append(String.format("%.3f", maxHeight));
        //System.out.println(stringBuilder);
        File file = new File(folderWithFiles + "fallMarks.csv");
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(stringBuilder.toString());
            fileWriter.flush();
        }
    }

    public double getLowerBound() {
        if (skeletonDrawableSkSpace != null) {
            return skeletonDrawableSkSpace.getLowerBound();
        } else {
            return 1;
        }
    }

    public void setLowerBound(double lowerBound) {
        if(skeletonDrawableSkSpace!=null){
            skeletonDrawableSkSpace.setLowerBound(lowerBound);
        }
    }

    public double getUpperBound() {
        if (skeletonDrawableSkSpace != null) {
            return skeletonDrawableSkSpace.getUpperBound();
        }else{
            return -1;
        }
    }

    public void setUpperBound(double upperBound) {
        if (skeletonDrawableSkSpace != null) {
            skeletonDrawableSkSpace.setUpperBound(upperBound);
        }
    }

    public int getFramesCount(){
        if (skeletonDrawableSkSpace != null) {
            return skeletonDrawableSkSpace.getFramesCount();
        }else{
            return 0;
        }
    }
}

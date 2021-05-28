package com.mobimore.graphicsObjects.kinect;

import com.mobimore.GTest.FallDetectionListener;
import com.mobimore.GTest.SkeletonDrawableObserver;
import com.mobimore.*;
import com.mobimore.camera.Camera;
import com.mobimore.graphicsObjects.Controllable;
import com.mobimore.graphicsObjects.Drawable;
import com.mobimore.graphicsObjects.waveFront.Vect3D;
import com.mobimore.utils.LibSVM;
import com.mobimore.utils.MathUtils;
import com.mobimore.utils.Pair;

import libsvm.svm_node;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mobimore.utils.MathUtils.multiplyMatrix;

public class SkeletonDrawable extends Drawable implements Controllable {
    private final double HEIGHT_CORRECTION = 1500;
    private int currentFrameNumber = 0;
    private int prevFrameNumber = 0;

    private SkeletonFrame[] skeletonFrames;
    private ArrayList<Pair<JointType, JointType>> bones;
    private ArrayList<Double[]> distances;
    private ArrayList<Double[]> heights;
    private ArrayList<Double[]> distancesSpeed;
    private ArrayList<Double[]> heightsSpeed;
    private ArrayList<Double[]> distancesAcceleration;
    private ArrayList<Double[]> heightsAcceleration;
    private double[][] coordinates;
    private boolean skeletonSpace;
    private int label;
    private Camera camera;
    private boolean pause = false;
    private int fallStartFrame = -1;
    private int fallEndFrame = -1;
    private Color currentColor = Color.WHITE;
    private Color mainColor = Color.WHITE;
    private LibSVM svm = LibSVM.getInstance();
    private FallDetectionListener fallDetectionListener;
    private SkeletonDrawableObserver observer;

    private double fallDetectionL = 0;
    private double lowerBound = -3;
    private double upperBound = 3;
    private boolean fall = false;
    //TODO temp material public variables going to be deleted
    public boolean isOutlier = false;
    private static String nametext;

    private final double RATE = 0.46;

    public double getLowerBound() {
        return lowerBound;
    }

    public void setLowerBound(double lowerBound) {
        this.lowerBound = lowerBound;
    }

    public double getUpperBound() {
        return upperBound;
    }

    public void setUpperBound(double upperBound) {
        this.upperBound = upperBound;
    }

    public void setObserver(SkeletonDrawableObserver observer) {
        this.observer = observer;
    }

    public void setFallDetectionListener(FallDetectionListener fallDetectionListener) {
        this.fallDetectionListener = fallDetectionListener;
    }

    private SkeletonDrawable(SkeletonFrame[] skeletonFrames, String name, boolean skeletonSpace) {
        setName(name);
        this.skeletonSpace = skeletonSpace;
        label = -1;
        this.skeletonFrames = new SkeletonFrame[skeletonFrames.length];
        this.distances = new ArrayList<>();
        this.heights = new ArrayList<>();
        this.distancesSpeed = new ArrayList<>();
        this.heightsSpeed = new ArrayList<>();
        this.distancesAcceleration = new ArrayList<>();
        this.heightsAcceleration = new ArrayList<>();
        for (int i = 0; i < this.skeletonFrames.length; i++) {
            this.skeletonFrames[i] = new SkeletonFrame(skeletonFrames[i]);
        }

        // a bone defined as a line between two joints
        this.bones = new ArrayList<>();

        // Torso
        this.bones.add(new Pair<>(JointType.Head, JointType.Neck));
        this.bones.add(new Pair<>(JointType.Neck, JointType.SpineShoulder));
        this.bones.add(new Pair<>(JointType.SpineShoulder, JointType.SpineMid));
        this.bones.add(new Pair<>(JointType.SpineMid, JointType.SpineBase));
        this.bones.add(new Pair<>(JointType.SpineShoulder, JointType.ShoulderRight));
        this.bones.add(new Pair<>(JointType.SpineShoulder, JointType.ShoulderLeft));
        this.bones.add(new Pair<>(JointType.SpineBase, JointType.HipRight));
        this.bones.add(new Pair<>(JointType.SpineBase, JointType.HipLeft));

        // Right Arm
        this.bones.add(new Pair<>(JointType.ShoulderRight, JointType.ElbowRight));
        this.bones.add(new Pair<>(JointType.ElbowRight, JointType.WristRight));
        this.bones.add(new Pair<>(JointType.WristRight, JointType.HandRight));
        this.bones.add(new Pair<>(JointType.HandRight, JointType.HandTipRight));
        this.bones.add(new Pair<>(JointType.WristRight, JointType.ThumbRight));

        // Left Arm
        this.bones.add(new Pair<>(JointType.ShoulderLeft, JointType.ElbowLeft));
        this.bones.add(new Pair<>(JointType.ElbowLeft, JointType.WristLeft));
        this.bones.add(new Pair<>(JointType.WristLeft, JointType.HandLeft));
        this.bones.add(new Pair<>(JointType.HandLeft, JointType.HandTipLeft));
        this.bones.add(new Pair<>(JointType.WristLeft, JointType.ThumbLeft));

        // Right Leg
        this.bones.add(new Pair<>(JointType.HipRight, JointType.KneeRight));
        this.bones.add(new Pair<>(JointType.KneeRight, JointType.AnkleRight));
        this.bones.add(new Pair<>(JointType.AnkleRight, JointType.FootRight));

        // Left Leg
        this.bones.add(new Pair<>(JointType.HipLeft, JointType.KneeLeft));
        this.bones.add(new Pair<>(JointType.KneeLeft, JointType.AnkleLeft));
        this.bones.add(new Pair<>(JointType.AnkleLeft, JointType.FootLeft));
    }

    public static String getStringName(File file)
    {
        String[] strs = new String[4];
        File temp = new File(file.getParent());
        for (int i = 0; i < 4; i ++)
        {
            temp = new File(temp.getParent());
            strs[i] = temp.getName();
        }
        StringBuilder res = new StringBuilder();
        for (int i = 3; i >= 0; i--)
            res.append(strs[i]).append("_");
        return (String) (res.toString()).subSequence(0, res.length()-1);
    }

    public static SkeletonDrawable fromCSVFile(File file, String name, boolean skeletonSpace) {
        nametext = getStringName(file);
        SkeletonCSVParser parser = new SkeletonCSVParser();
        try {

            SkeletonObject skeletonObject = file.getName().contains(".skeleton") ?
                    parserFile_dotskeleton(file, skeletonSpace)
                        : parser.parse(file);

            SkeletonFrame[] skeletonFrames = skeletonObject.getSkeletonFrames();
            return new SkeletonDrawable(skeletonFrames, name, skeletonSpace);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Parser for new database
    private static SkeletonObject parserFile_dotskeleton(File file, boolean skeletonSpace) throws IOException {
        nametext = file.getName();
        if (!file.exists()) {
            throw new FileNotFoundException();
        } else {

            List<String> lines = Files.readAllLines(file.toPath(), Charset.defaultCharset());
            String[] linesArray = new String[lines.size()];
            linesArray = lines.toArray(linesArray);
            lines.clear();

            ArrayList<SkeletonFrame> frames = new ArrayList<>();
            ArrayList<List<Joint>> playerCoordinates = new ArrayList();
            ArrayList<Joint> joints = new ArrayList<>();

            int n_frames = Integer.parseInt(linesArray[0]); System.out.println(n_frames);
            byte startline = 1;
            byte box = 28;
            byte start_joints = 4;

            for (int i = 0; i < n_frames; i++)
            {
                int cur_block = startline + (i * box);
                byte n_coords = Byte.parseByte(linesArray[cur_block + 2]);
                for (int j = 0; j < n_coords; j++) {
                    String line = linesArray[cur_block + start_joints-1 + j];
                    double[] crds = Arrays.stream(line.split(" ")).mapToDouble(Double::parseDouble).toArray();
                    if (skeletonSpace) {
                        double x = crds[0] * 1000;
                        double y = crds[1] * 1000;
                        double z = crds[2] * 1000;
                        int state = (int) crds[crds.length-1];
                        Joint joint = new Joint(x, y, z, state);
                        joints.add(joint);
                    }
                    else {
                        double x = Double.isNaN(crds[3]) ? 9999 : crds[3];
                        double y = Double.isNaN(crds[4]) ? 9999 : crds[4];
                        double z = x == 9999 || y == 9999 ? 9999 : crds[2] * 1000;
                        if (frames.size() == 71)
                            System.out.println("stop");
                        int state = (int) crds[crds.length-1];
                        Joint joint = new Joint(x, y, z, state);
                        joints.add(joint);
                    }
                }
                playerCoordinates.add(new ArrayList<>(joints));
                joints.clear();
                frames.add(new SkeletonFrame(playerCoordinates));
                playerCoordinates.clear();
            }
            SkeletonFrame[] skeletonFrames = new SkeletonFrame[frames.size()];
            SkeletonObject skeletonObject = new SkeletonObject(frames.toArray(skeletonFrames));
            return skeletonObject;
        }
    }
    //--------------------------------------------------------------------
    public double getMaxHeight(){
        double max = skeletonFrames[0].getPlayerCoordinates().get(0).get(JointType.Head.ordinal()).getY()+HEIGHT_CORRECTION;
        for (SkeletonFrame frame :
                skeletonFrames) {
            List<Joint> j = frame.getPlayerCoordinates().get(0);
            double current = j.get(JointType.Head.ordinal()).getY()+HEIGHT_CORRECTION;
            if (current > max) {
                max = current;
            }
        }
        return max;
    }

    private void clearLists(){
        distances.clear();
        heights.clear();
        distancesSpeed.clear();
        heightsSpeed.clear();
        distancesAcceleration.clear();
        heightsAcceleration.clear();
    }

    private boolean prevFallState = false;
    @Override
    public void paint(Graphics graphics, Camera camera, int[][] zBuffer) {
        if (currentFrameNumber == 0) {
            fall = false;
            fallDetectionL = 0;
            clearLists();
            if (observer != null) {
                observer.onSceneStarted(this);
            }
        }

        ArrayList<Joint> currentFrameCoordsArrList = (ArrayList<Joint>) skeletonFrames[currentFrameNumber].getPlayerCoordinates().get(0);

        if (!skeletonSpace) {
            coordinates = jointArrListToArray(currentFrameCoordsArrList);
            this.camera = camera;
            if (currentFrameNumber >= fallStartFrame && currentFrameNumber <= fallEndFrame) {
                this.currentColor = Color.red;
            } else {
                this.currentColor = mainColor;
            }
            BufferedImage renderedImage = rasterize(zBuffer);
/*
//TODO убрать проверку и загрузить. Посмотреть что в этой проге с 9999 при импорте скелетов. Сделать обрывы такие же
            if (true) {
                try {
                    ImageIO.write(renderedImage, "jpg", new File("D:\\Images\\11\\" + this.nametext + "_image" + currentFrameNumber + ".jpg"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
*/

            graphics.drawImage(renderedImage, 0, 0, null);
            renderedImage.flush();
        } else if (prevFrameNumber != currentFrameNumber || prevFrameNumber == 0) {
            //long startTime = System.currentTimeMillis();
            currentFrameCoordsArrList.remove(24);
            currentFrameCoordsArrList.remove(23);
            currentFrameCoordsArrList.remove(22);
            currentFrameCoordsArrList.remove(21);
            currentFrameCoordsArrList.remove(19);
            currentFrameCoordsArrList.remove(15);
            currentFrameCoordsArrList.remove(11);
            currentFrameCoordsArrList.remove(7);
            coordinates = jointArrListToArray(currentFrameCoordsArrList);

            fillMatrix();
            svm_node[] nodes = generateLibSVMVector();

            if (nodes != null) {
                LibSVM.SVMResult result = svm.classify(nodes);
                if (result.isOutlier) {
                    isOutlier = true;
                    result.svmVal = getOutlierValue();
                }
                else isOutlier = false;
                boolean fallDetected = fallDetectPagesAlgo(result.svmVal);
                if (fallDetectionListener != null) {
                    fallDetectionListener.onNewFrameProcessed(getExpertValue(currentFrameNumber), result, fallDetected, currentFrameNumber);
                }
                if (prevFallState != fallDetected) {
                    if (fallDetected) {
                        if (fallDetectionListener != null) {
                            fallDetectionListener.onFallDetected(currentFrameNumber);
                        }
                    } else {
                        if (fallDetectionListener != null) {
                            fallDetectionListener.onFallUndetected(currentFrameNumber);
                        }
                    }
                }
                prevFallState = fallDetected;
            } else {
                if (fallDetectionListener != null) {
                    fallDetectionListener.onNewFrameProcessed(getExpertValue(currentFrameNumber), new LibSVM.SVMResult(false, 0), false, currentFrameNumber);
                }
                /*if(prevFallState) {
                    if (fallDetectionListener != null) {
                        fallDetectionListener.onFallUndetected();
                    }
                }*/
                prevFallState = false;
            }
        }

        prevFrameNumber = currentFrameNumber;

        if (!pause) {
            if (currentFrameNumber + 1 == skeletonFrames.length) {
                pause();
                if (observer != null) {
                    observer.onSceneEnded(this);
                }
            } else {
                currentFrameNumber++;
            }
        }

    }

    private double getOutlierValue(){
        //return lowerBound + RATE * (upperBound - lowerBound);
        return 0;
    }

    private boolean fallDetectPagesAlgo(double val){
        if (val >= 0.00) {
            if (fallDetectionL + val >= upperBound) {
                fallDetectionL = upperBound;
            } else {
                fallDetectionL += val;
            }
            if(fallDetectionL == upperBound){
                fall = true;
                return true;
            }else{
                return fall;
            }
        } else {
            if (fallDetectionL + val <= lowerBound) {
                fallDetectionL = lowerBound;
            } else {
                fallDetectionL += val;
            }
            if(fallDetectionL == lowerBound) {
                fall = false;
                return false;
            }else{
                return fall;
            }
        }
    }

    private boolean getExpertValue(int currentFrameNumber) {
        if (fallStartFrame == -1 && fallEndFrame == -1) {
            return false;
        }
        if (currentFrameNumber < fallStartFrame) {
            return false;
        }
        return currentFrameNumber <= fallEndFrame;
    }

    @Override
    public Vect3D getCenter() {
        Joint joint = skeletonFrames[currentFrameNumber].getPlayerCoordinates().get(2).get(JointType.SpineBase.ordinal());
        return new Vect3D(joint.getX(), joint.getY(), joint.getZ());
    }

    @Override
    public void setColor(Color color) {
        this.mainColor = color;
    }

    @Override
    public Color getColor() {
        return mainColor;
    }

    private static int[][] toScreenCoordinates(double[][] coordinates) {
        int[][] screenCoordinates = new int[coordinates.length][coordinates[0].length];
        for (int i = 0; i < coordinates.length; i++) {
            for (int j = 0; j < coordinates[0].length; j++) {
                screenCoordinates[i][j] = (int) (coordinates[i][j] / coordinates[i][3]);
            }
        }
        return screenCoordinates;
    }

    private double[][] perspectiveCoords(final Camera camera) {
        return multiplyMatrix(camera.getPerspectiveArr(), coordinates);
    }

    private BufferedImage rasterize(int[][] zBuffer) {
        int[][] coordinatesT;
        if(skeletonSpace) {
            coordinatesT = toScreenCoordinates(perspectiveCoords(camera));
        }else{
            coordinatesT = toScreenCoordinates(coordinates);
        }

        BufferedImage image = new BufferedImage(zBuffer.length, zBuffer[0].length, BufferedImage.TYPE_4BYTE_ABGR);

        Graphics graphics = image.getGraphics();
        Color prevColor = graphics.getColor();
        graphics.setColor(currentColor);
        for (Pair<JointType, JointType> jointPair : bones) {
            int x = coordinatesT[jointPair.getKey().ordinal()][0];
            int y = coordinatesT[jointPair.getKey().ordinal()][1];
            int x2 = coordinatesT[jointPair.getValue().ordinal()][0];
            int y2 = coordinatesT[jointPair.getValue().ordinal()][1];
            if (x != 9999 && y != 9999 && x2 != 9999 && y2 != 9999) {
                graphics.drawLine(x, y, x2, y2);
            }
        }
        graphics.setColor(prevColor);
        graphics.drawString("Frame number: " + currentFrameNumber, 10, 15);
        return image;
    }

    private static double[][] jointArrListToArray(ArrayList<Joint> jointArrayList) {
        double[][] result = new double[jointArrayList.size()][4];
        for (int i = 0; i < result.length; i++) {
            Joint joint = jointArrayList.get(i);
            result[i][0] = joint.getX();
            result[i][1] = joint.getY();
            result[i][2] = joint.getZ();
            result[i][3] = 1;
        }
        return result;
    }

    private svm_node[] generateLibSVMVector(){
        Double[] currentDistanceAcceleration = distancesAcceleration.get(currentFrameNumber);
        Double[] currentHeightAcceleration = heightsAcceleration.get(currentFrameNumber);

        if (currentDistanceAcceleration == null || currentHeightAcceleration == null) {
            return null;
        }
        Double[] currentDistances = distances.get(currentFrameNumber);
        Double[] currentHeights = heights.get(currentFrameNumber);
        Double[] currentDistanceSpeed = distancesSpeed.get(currentFrameNumber);
        Double[] currentHeightSpeed = heightsSpeed.get(currentFrameNumber);

        int maxDim = currentDistanceAcceleration.length + currentHeightAcceleration.length + currentDistances.length +
                currentHeights.length + currentDistanceSpeed.length + currentHeightSpeed.length;

        double height = getMaxHeight();
        assert height != 0;

        svm_node[] nodes = new svm_node[maxDim];
        int i = 0;

        //distances
        for (Double currentDistance : currentDistances) {
            nodes[i] = new svm_node();
            nodes[i].index = i+1;
            nodes[i].value = currentDistance / height;
            i++;
        }

        //height
        for (Double currentHeight : currentHeights) {
            nodes[i] = new svm_node();
            nodes[i].index = i+1;
            nodes[i].value = currentHeight / height;
            i++;
        }

        //distances speed
        for (Double aCurrentDistanceSpeed : currentDistanceSpeed) {
            nodes[i] = new svm_node();
            nodes[i].index = i+1;
            nodes[i].value = aCurrentDistanceSpeed / height;
            i++;
        }

        //height speed
        for (Double aCurrentHeightSpeed : currentHeightSpeed) {
            nodes[i] = new svm_node();
            nodes[i].index = i+1;
            nodes[i].value = aCurrentHeightSpeed / height;
            i++;
        }

        //distances acceleration
        for (Double aCurrentDistanceAcceleration : currentDistanceAcceleration) {
            nodes[i] = new svm_node();
            nodes[i].index = i+1;
            nodes[i].value = aCurrentDistanceAcceleration / height;
            i++;
        }
        //height acceleration
        for (Double aCurrentHeightAcceleration : currentHeightAcceleration) {
            nodes[i] = new svm_node();
            nodes[i].index = i+1;
            nodes[i].value = aCurrentHeightAcceleration / height;
            i++;
        }

        return nodes;
    }

    private void fillMatrix() {
        generateDistancesMatrix();
        generateHeightsMatrix();
        generateDistancesSpeedMatrix();
        generateHeightsSpeedMatrix();
        generateDistancesAccelerationMatrix();
        generateHeightsAccelerationMatrix();
    }

    private void generateDistancesMatrix() {            //TODO
        double[][] coordinates = new double[this.coordinates.length][this.coordinates[0].length];
        for (int i = 0; i < this.coordinates.length; i++) {
            System.arraycopy(this.coordinates[i], 0, coordinates[i], 0, this.coordinates[0].length);
            coordinates[i][1] += HEIGHT_CORRECTION;
        }
        Double[] result = new Double[(coordinates.length * (coordinates.length - 1)) / 2-8-6];
        for (int i = 0; i < result.length; i++) {
            //for (int j = 0; j < coordinates.length; j++) {
            result[i] = 0.;
            //}
        }

        int resultIndex = 0;
        for (int i = 0; i < coordinates.length; i++) {
            for (int j = i + 1; j < coordinates.length; j++) {
                if (!(((i == 4) && (j == 5)) || ((i == 5) && (j == 6))
                        || ((i == 7) && (j == 8)) || ((i == 8) && (j == 9))
                        || ((i == 10) && (j == 11)) || ((i == 11) && (j == 12))
                        || ((i == 2) && (j == 3)) || ((i == 2) && (j == 16))
                        || ((i == 3) && (j == 16)) || ((i == 0) && (j == 10))
                        || ((i == 0) && (j == 13)) || ((i == 10) && (j == 13))
                        || ((i == 13) && (j == 14)) || ((i == 14) && (j == 15)))) {
                    result[resultIndex] = Math.sqrt(Math.pow(coordinates[i][0] - coordinates[j][0], 2) + Math.pow(coordinates[i][1] - coordinates[j][1], 2) + Math.pow(coordinates[i][2] - coordinates[j][2], 2));
                    resultIndex++;
                }
            }
        }
        distances.add(result);
    }

    private void generateDistancesSpeedMatrix() {
        if (currentFrameNumber < 1) {
            distancesSpeed.add(null);
            return;
        }
        Double[] prevDistances = distances.get(currentFrameNumber - 1);
        Double[] currentDistances = distances.get(currentFrameNumber);

        Double[] result = MathUtils.substractMatrix(currentDistances, prevDistances);
        distancesSpeed.add(result);
    }

    private void generateDistancesAccelerationMatrix() {
        if (currentFrameNumber < 2) {
            distancesAcceleration.add(null);
            return;
        }
        Double[] prevDistancesSpeed = distancesSpeed.get(currentFrameNumber - 1);
        Double[] currentDistancesSpeed = distancesSpeed.get(currentFrameNumber);

        Double[] result = MathUtils.substractMatrix(currentDistancesSpeed, prevDistancesSpeed);
        distancesAcceleration.add(result);
    }

    private void generateHeightsMatrix() {
        double[][] coordinates = new double[this.coordinates.length][this.coordinates[0].length];
        for (int i = 0; i < this.coordinates.length; i++) {
            System.arraycopy(this.coordinates[i], 0, coordinates[i], 0, this.coordinates[0].length);
            coordinates[i][1] += HEIGHT_CORRECTION;
        }
        Double[] result = new Double[coordinates.length];
        for (int i = 0; i < coordinates.length; i++) {
            result[i] = coordinates[i][1];
        }
        heights.add(result);
    }

    private void generateHeightsSpeedMatrix() {
        if (currentFrameNumber < 1) {
            heightsSpeed.add(null);
            return;
        }
        Double[] prevHeight = heights.get(currentFrameNumber - 1);
        Double[] currentHeight = heights.get(currentFrameNumber);

        Double[] result = MathUtils.substractMatrix(currentHeight, prevHeight);
        heightsSpeed.add(result);
    }

    private void generateHeightsAccelerationMatrix() {
        if (currentFrameNumber < 2) {
            heightsAcceleration.add(null);
            return;
        }
        Double[] prevHeightsSpeed = heightsSpeed.get(currentFrameNumber - 1);
        Double[] currentHeightsSpeed = heightsSpeed.get(currentFrameNumber);

        Double[] result = MathUtils.substractMatrix(currentHeightsSpeed, prevHeightsSpeed);
        heightsAcceleration.add(result);
    }

    public int getLabel() {
        return label;
    }

    public void setLabel(int label) {
        this.label = label;
    }

    @Override
    public void pause() {
        this.pause = true;
    }

    @Override
    public void resume() {
        if (currentFrameNumber + 1 == skeletonFrames.length) {
            restart();
        }
        this.pause = false;
    }

    @Override
    public void restart() {
        currentFrameNumber = 0;
        this.pause = false;
    }

    @Override
    public void stepBack() {
        prevFrameNumber = currentFrameNumber;
        if (currentFrameNumber - 1 >= 0) {
            currentFrameNumber--;
        }
    }

    @Override
    public void stepFW() {
        prevFrameNumber = currentFrameNumber;
        if (currentFrameNumber + 1 < skeletonFrames.length) {
            currentFrameNumber++;
        }
    }

    @Override
    public int getCurrentFrameNumber() {
        return currentFrameNumber;
    }

    @Override
    public boolean isPaused() {
        return pause;
    }

    public int getFallStartFrame() {
        return fallStartFrame;
    }

    public void setFallStartFrame(int fallStartFrame) {
        this.fallStartFrame = fallStartFrame;
    }

    public int getFallEndFrame() {
        return fallEndFrame;
    }

    public void setFallEndFrame(int fallEndFrame) {
        this.fallEndFrame = fallEndFrame;
    }

    public double[][] getCoordinates() {
        double[][] coords = new double[coordinates.length][coordinates[0].length];
        for (int i = 0; i < coords.length; i++) {
            System.arraycopy(coordinates[i], 0, coords[i], 0, coords[i].length);
        }
        return coords;
    }

    public int getFramesCount(){
        if (skeletonFrames != null) {
            return skeletonFrames.length;
        } else {
            return 0;
        }
    }
}

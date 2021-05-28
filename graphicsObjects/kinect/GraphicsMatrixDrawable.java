package com.mobimore.graphicsObjects.kinect;

import com.mobimore.GTest.MouseActionListener;
import com.mobimore.camera.Camera;
import com.mobimore.graphicsObjects.Drawable;
import com.mobimore.graphicsObjects.waveFront.Vect3D;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public abstract class GraphicsMatrixDrawable extends Drawable implements MouseActionListener {
    protected HashMap<String, FrameMark[]> scenesData = new LinkedHashMap<>();
    private ArrayList<ClickActionListener> clickListeners = new ArrayList<>();

    protected int rectSize = 3;
    private boolean shouldRedraw = true;
    private BufferedImage renderedImage = null;

    public HashMap<String, FrameMark[]> getScenesData() {
        return scenesData;
    }

    @Override
    public void paint(Graphics graphics, Camera camera, int[][] zBuffer) {
        graphics.drawImage(getRenderedImage(), 0, 0, null);
    }

    @Override
    public Vect3D getCenter() {
        return null;
    }

    public final int getRectSize() {
        return rectSize;
    }

    public final void setRectSize(int rectSize) {
        this.rectSize = rectSize;
        shouldRedraw = true;
    }

    protected static Color getColor(double power) {
        double H = power * 0.3; // Hue (note 0.4 = Green, see huge chart below)
        double S = 1; // Saturation
        double B = 1; // Brightness

        return Color.getHSBColor((float)H, (float)S, (float)B);
    }

    protected abstract BufferedImage rasterize();

    public final BufferedImage getRenderedImage(){
        if (renderedImage == null || shouldRedraw) {
            renderedImage = rasterize();
            shouldRedraw = false;
        }
        return renderedImage;
    }

    public final Dimension getSize(){
        BufferedImage bufferedImage = getRenderedImage();
        return new Dimension(bufferedImage.getWidth(), bufferedImage.getHeight());
    }

    public void invalidate(){
        this.shouldRedraw = true;
    }

    protected static String loadFile(File file) throws IOException {
        StringBuilder strB = new StringBuilder();
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            try (ProgressMonitorInputStream pmIS = new ProgressMonitorInputStream(null,
                    "Loading file", fileInputStream)) {
                pmIS.getProgressMonitor().setMillisToPopup(1);
                InputStreamReader reader = new InputStreamReader(pmIS);
                BufferedReader bufferedReader = new BufferedReader(reader);
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    strB.append(line).append('\n');
                }
            }
        }
        return strB.toString();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        int lineNumber = y / getRectSize();
        int columnNumber = x / getRectSize();

        ArrayList<String> keys = new ArrayList<>(scenesData.keySet());

        if (lineNumber >= keys.size()) {
            return;
        }
        String lineName = keys.get(lineNumber);
        FrameMark[] frameMarks = scenesData.get(lineName);
        if (columnNumber >= frameMarks.length) {
            return;
        }
        FrameMark mark = frameMarks[columnNumber];
        int frameNumber = mark.frameNumber;
        Object frameMark = mark.mark;
        //System.out.println("Clicked on: " + lineName + " frame number: " + frameNumber + " frameMark: " + frameMark);
        ClickAction action = new ClickAction(lineName, frameNumber, frameMark);
        for (ClickActionListener c : clickListeners) {
            c.onClicked(action);
        }
    }

    public void addOnClickListener(ClickActionListener listener) {
        clickListeners.add(listener);
    }

    public void removeOnClickListener(ClickActionListener listener) {
        clickListeners.remove(listener);
    }

    public abstract void setOriginalMarks(GraphicMatrixSolidDrawable comparisionGMD);

    public class ClickAction{
        private String lineName;
        private int frameNumber;
        private Object frameMark;

        public ClickAction(String lineName, int frameNumber, Object value) {
            this.lineName = lineName;
            this.frameNumber = frameNumber;
            this.frameMark = value;
        }

        public String getLineName() {
            return lineName;
        }

        public void setLineName(String lineName) {
            this.lineName = lineName;
        }

        public int getFrameNumber() {
            return frameNumber;
        }

        public void setFrameNumber(int frameNumber) {
            this.frameNumber = frameNumber;
        }

        public Object getFrameMark() {
            return frameMark;
        }

        public void setFrameMark(double frameMark) {
            this.frameMark = frameMark;
        }
    }
    public interface ClickActionListener {
        void onClicked(ClickAction action);
    }
}

package com.mobimore.GTest;

import com.mobimore.camera.Camera;
import com.mobimore.graphicsObjects.Drawable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mobimore on 10/9/16.
 */
class DrawingPanel extends JPanel {
    private final Map<String, Drawable> drawableMap = new HashMap<>();
    private final ArrayList<MouseActionListener> mouseActionListeners = new ArrayList<>();
    private DrawablesListener drawablesListener;
    private Camera camera;
    private int[][] zBuffer;
    private Timer timer;
    //private BufferedImage image;

    public DrawingPanel(Camera camera, DrawablesListener drawablesListener) {
        this.camera = camera;
        this.drawablesListener = drawablesListener;
        camera.setDx(getWidth() / 2);
        camera.setDy(getHeight() / 2);
    }
    public DrawingPanel(Camera camera) {
        this.camera = camera;
        camera.setDx(getWidth() / 2);
        camera.setDy(getHeight() / 2);
    }

    public DrawingPanel(DrawablesListener drawablesListener) {
        setupMouseListener();
        this.drawablesListener = drawablesListener;
        this.addComponentListener(this.componentListener);
    }
    public DrawingPanel() {
        setupMouseListener();
        this.addComponentListener(this.componentListener);

    }
    public void setDrawablesListener(DrawablesListener drawablesListener) {
        this.drawablesListener = drawablesListener;
        this.addComponentListener(this.componentListener);
    }

    private void setupMouseListener(){
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (!mouseActionListeners.isEmpty()) {
                    for (MouseActionListener m : mouseActionListeners) {
                        m.mouseClicked(e);
                    }
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        //super.paintComponent(g);
        int width = getWidth();
        int height = getHeight();
        initZBuffer(width, height);
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);

        for (Map.Entry<String, Drawable> entry : drawableMap.entrySet()) {
            Drawable drawable = entry.getValue();
            if (drawable.isVisible()) drawable.paint(g, camera, zBuffer);
        }
    }

    public Camera getCamera() {
        return camera;
    }
    public void setCamera(Camera camera) {
        this.camera = camera;
        camera.setDx(getWidth() / 2);
        camera.setDy(getHeight() / 2);
        repaint();
    }

    public boolean addDrawable(Drawable drawable) {
        String name = drawable.getName();
        if (drawableMap.containsKey(name)) {
            return false;
        }
        drawableMap.put(name, drawable);
        ////TODO CHECK!
        if(drawable instanceof MouseActionListener) mouseActionListeners.add((MouseActionListener) drawable);
        drawable.onPanelSizeChanged(getDimensions());
        if (drawablesListener != null) {
            SwingUtilities.invokeLater(() -> drawablesListener.onObjectAdded(name));
        }
        repaint();
        return true;
    }
    public Drawable getDrawable(String name){
        return drawableMap.get(name);
    }
    public boolean removeDrawable(String name){
        if (!drawableMap.containsKey(name)) {
            return false;
        }
        Drawable drawable = drawableMap.get(name);
        drawableMap.remove(name);
        if(drawable instanceof MouseActionListener) mouseActionListeners.remove(drawable);
        if (drawablesListener != null) {
            SwingUtilities.invokeLater(() -> drawablesListener.onObjectRemoved(name));
        }
        repaint();
        return true;
    }
    public void clearPanel() {
        //drawables.clear();
        drawableMap.clear();
        if (drawablesListener != null) {
            SwingUtilities.invokeLater(() -> drawablesListener.onPanelCleared());
        }
        repaint();
    }

    public String[] getDrawableNames(){
        return drawableMap.keySet().toArray(new String[0]);
    }

    public interface DrawablesListener{
        void onObjectAdded(final String objectName);
        void onObjectRemoved(final String objectName);
        void onPanelCleared();
    }

    private void initZBuffer(int width, int height){
        zBuffer = new int[width][height];
        for (int i = 0; i < zBuffer.length; i++) {
            for (int j = 0; j < zBuffer[0].length; j++) {
                zBuffer[i][j] = -200;
            }
        }
    }

    public void startUpdate(){
        timer = new Timer(33, e -> repaint());
        //timer = new Timer(0, e -> repaint());
        timer.start();
    }

    public void stopUpdate(){
        timer.stop();
    }

    public Dimension getDimensions(){
        return new Dimension(getWidth(), getHeight());
    }

    private ComponentListener componentListener = new ComponentListener() {
        @Override
        public void componentResized(ComponentEvent e) {
            for (Map.Entry<String, Drawable> entry : drawableMap.entrySet()) {
                entry.getValue().onPanelSizeChanged(getDimensions());
            }
        }

        @Override
        public void componentMoved(ComponentEvent e) {

        }

        @Override
        public void componentShown(ComponentEvent e) {

        }

        @Override
        public void componentHidden(ComponentEvent e) {

        }
    };

    public void update(){
        repaint();
    }
}

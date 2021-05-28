package com.mobimore.graphicsObjects.kinect;

import com.mobimore.camera.Camera;
import com.mobimore.graphicsObjects.Controllable;
import com.mobimore.graphicsObjects.Drawable;
import com.mobimore.graphicsObjects.waveFront.Vect3D;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferUShort;
import java.awt.image.RescaleOp;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;

public class DepthDrawable extends Drawable implements Controllable {

    private static class PathsComparator implements Comparator{
        @Override
        public int compare(Object f1, Object f2) {
            String fileName1 = ((Path) f1).getFileName().toString();
            String fileName2 = ((Path) f2).getFileName().toString();

            int fileId1 = Integer.parseInt(fileName1.split("_")[1].split("\\.")[0]);
            int fileId2 = Integer.parseInt(fileName2.split("_")[1].split("\\.")[0]);
            return fileId1 - fileId2;
        }
    }

    private ArrayList<short[]> framesData;
    private int currentFrameNumber = 0;
    private RescaleOp rescaleOp;
    private boolean pause = false;

    public BufferedImage globalImage;

    @Override
    public void pause() {
        this.pause = true;
    }

    @Override
    public void resume() {
        if (currentFrameNumber + 1 == framesData.size()) {
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
        if (currentFrameNumber - 1 >= 0) {
            currentFrameNumber--;
        }
    }

    @Override
    public void stepFW() {
        if (currentFrameNumber + 1 < framesData.size()) {
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

    public static DepthDrawable fromFolder(File folder, String name) {
        if (!folder.isDirectory()) {
            return null;
        }
        try {
            DepthDrawable depthDrawable = new DepthDrawable(name);
            PathsComparator comparator = new PathsComparator();
            Files.list(folder.toPath()).filter(Files::isRegularFile).filter(e -> e.getFileName().toString().toLowerCase().contains("filedepth_")).sorted(comparator::compare).forEachOrdered(depthDrawable::process);
            return depthDrawable;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void process(Path path) {
        short[] readback=new short[512*424];
        try{
            RandomAccessFile rFile = new RandomAccessFile(path.toFile(), "r");
            FileChannel inChannel = rFile.getChannel();
            ByteBuffer buf_in = ByteBuffer.allocate(512*424*2);
            buf_in.clear();

            inChannel.read(buf_in);

            buf_in.rewind();
            buf_in.order(ByteOrder.LITTLE_ENDIAN);
            buf_in.asShortBuffer().get(readback);

            inChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        framesData.add(readback);
    }

    private DepthDrawable(String name) {
        framesData = new ArrayList<>();
        RenderingHints hints = new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        rescaleOp = new RescaleOp(7f, 0, hints);
        setName(name);
    }

    @Override
    public void paint(Graphics graphics, Camera camera, int[][] zBuffer) {
        BufferedImage image = new BufferedImage(512, 424, BufferedImage.TYPE_USHORT_GRAY);

        short[] pixels = ((DataBufferUShort) image.getRaster().getDataBuffer()).getData();

        System.arraycopy(framesData.get(currentFrameNumber),0,pixels,0,pixels.length);
        rescaleOp.filter(image, image);

        graphics.drawImage(image, 0, 0, null);
        if (currentFrameNumber + 1 == framesData.size()) {
            pause();
        } else if (!pause) {
            currentFrameNumber++;
        }
    }

    @Override
    public Vect3D getCenter() {
        return null;
    }
}

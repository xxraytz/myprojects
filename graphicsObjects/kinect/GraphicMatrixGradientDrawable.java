package com.mobimore.graphicsObjects.kinect;

import com.mobimore.GTest.DrawableLoadCompletionListener;
import com.mobimore.utils.MathUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class GraphicMatrixGradientDrawable extends GraphicsMatrixDrawable{

    private GraphicMatrixSolidDrawable originalMarks = null;

    public void setOriginalMarks(GraphicMatrixSolidDrawable originalMarks) {
        this.originalMarks = originalMarks;
        invalidate();
    }

    private GraphicMatrixGradientDrawable(String name){
        setName(name);
    }

    public static void startLoadingFromCSVFile(File file, String name, DrawableLoadCompletionListener listener){
        if (listener == null) {
            throw new NullPointerException();
        }
        new Thread(){
            @Override
            public void run() {
                super.run();
                String fileData = null;
                try {
                    fileData = loadFile(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                GraphicMatrixGradientDrawable drawable = new GraphicMatrixGradientDrawable(name);
                drawable.initFromFiledata(fileData);
                if(listener!=null) listener.drawableLoaded(drawable);
            }
        }.start();
    }

    private void initFromFiledata(String fileData){
        String[] dataLines = fileData.split("\n");
        String prevName = "";
        ArrayList<FrameMark<Double>> arrayList = new ArrayList<>();
        for (int i = 1; i < dataLines.length; i++) {
            String line = dataLines[i];
            String[] dataOneLine = line.split(",");
            String currName = dataOneLine[0].substring(0, dataOneLine[0].lastIndexOf('_'));
            String frameNumberStr = dataOneLine[0].substring(dataOneLine[0].lastIndexOf("_Frame")+"_Frame".length(), dataOneLine[0].length());
            int frameNumber = Integer.parseInt(frameNumberStr);
            if (!prevName.equalsIgnoreCase(currName)) {
                if (prevName.equalsIgnoreCase("")) {
                    prevName = currName;
                } else {
                    FrameMark arr[] = new FrameMark[arrayList.size()];
                    arrayList.toArray(arr);
                    scenesData.put(prevName, arr);
                    arrayList.clear();
                    prevName = currName;
                }
            }
            double fall = Double.parseDouble(dataOneLine[1]);
            FrameMark<Double> mark = new FrameMark<>(frameNumber, fall);
            arrayList.add(mark);
        }
        if(arrayList.size()!=0) {
            FrameMark arr[] = new FrameMark[arrayList.size()];
            arrayList.toArray(arr);
            scenesData.put(prevName, arr);
            arrayList.clear();
        }
    }

    protected BufferedImage rasterize(){
        int height = scenesData.size() * rectSize;
        int width = 0;
        for (String key : scenesData.keySet()) {
            FrameMark[] vals = scenesData.get(key);
            int valsLength = vals.length;
            if (width < valsLength) {
                width = valsLength;
            }
        }
        width *= rectSize;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D graphics2D = (Graphics2D) image.getGraphics();
        graphics2D.setColor(Color.black);
        graphics2D.fillRect(0,0,width,height);
        int currentY = 0;
        for (String key : scenesData.keySet()) {
            FrameMark<Double>[] vals = scenesData.get(key);
            FrameMark<Integer>[] origVals = null;
            if (originalMarks!=null){
                origVals = originalMarks.getScenesData().getOrDefault(key, null);
            }

            int currentX = 0;
            for (int i = 0; i < vals.length; i++) {
                FrameMark<Double> oneVal = vals[i];
                Color color = getColor(MathUtils.lerp(oneVal.mark, 1, -1, 0, 1));
                graphics2D.setColor(color);
                graphics2D.fill3DRect(currentX, currentY, rectSize, rectSize, true);
                if (origVals != null) {
                    FrameMark<Integer> origMark = origVals[i];
                    if (origMark.mark <= 4) {
                        graphics2D.setColor(Color.BLACK);
                        graphics2D.drawLine(currentX, currentY, currentX + rectSize, currentY + rectSize);
                    }
                }
                currentX += rectSize;
            }
            currentY += rectSize;
        }

        return image;
    }
}

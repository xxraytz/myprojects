package com.mobimore.graphicsObjects.kinect;

import com.mobimore.GTest.DrawableLoadCompletionListener;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class GraphicMatrixSolidDrawable extends GraphicsMatrixDrawable {
    private static final Color FALLCOLOR = Color.red;
    private static final Color ADLCOLOR = Color.green;
    private static final Color MISMATCHONFALLCOLOR = Color.WHITE;
    private static final Color MISMATCHONADLCOLOR = Color.blue;
    private static final int MISMATCHONFALL = -1;
    private static final int MISMATCHONADL = -2;
    private CSVType type;

    private GraphicMatrixSolidDrawable(String name){
        setName(name);
    }

    public static void startLoadingFromCSVFile(File file, String name, CSVType type, DrawableLoadCompletionListener listener){
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
                GraphicMatrixSolidDrawable drawable = new GraphicMatrixSolidDrawable(name);
                drawable.initFromFiledata(fileData, type);
                if(listener!=null) listener.drawableLoaded(drawable);
            }
        }.start();
    }

    private void initFromFiledata(String fileData, CSVType type){
        this.type = type;
        String[] dataLines = fileData.split("\n");
        String prevName = "";
        ArrayList<FrameMark<Integer>> arrayList = new ArrayList<>();
        //Line 0 is header
        for (int i = 1; i < dataLines.length; i++) {
            String line = dataLines[i];
            String[] dataOneLine = line.split(",");
            String currName = dataOneLine[0].substring(0, dataOneLine[0].lastIndexOf('_'));
            String frameNumberStr = dataOneLine[0].substring(dataOneLine[0].lastIndexOf("_Frame")+"_Frame".length());
            int frameNumber = Integer.parseInt(frameNumberStr);
            if (!prevName.equalsIgnoreCase(currName)) {
                if (prevName.equalsIgnoreCase("")) {
                    prevName = currName;
                } else {
                    FrameMark<Integer>[] arr = new FrameMark[arrayList.size()];
                    arrayList.toArray(arr);
                    scenesData.put(prevName, arr);
                    arrayList.clear();
                    prevName = currName;
                }
            }
            int fall;
            if(type == CSVType.matrix) {
                fall = Integer.parseInt(dataOneLine[460]);
            }else{
                fall = Integer.parseInt(dataOneLine[1]);
            }
            FrameMark<Integer> mark = new FrameMark<>(frameNumber, fall);
            arrayList.add(mark);
        }
        if(arrayList.size()!=0) {
            FrameMark[] arr = new FrameMark[arrayList.size()];
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
        //System.out.println("Width: " + width + " height" + height);
        Graphics2D graphics2D = (Graphics2D) image.getGraphics();
        graphics2D.setColor(Color.black);
        graphics2D.fillRect(0,0,width,height);
        int currentY = 0;
        for (String key : scenesData.keySet()) {
            //System.out.println(key);
            FrameMark<Integer>[] vals = scenesData.get(key);
            int currentX = 0;
            for (FrameMark<Integer> oneVal : vals) {
                if(type == CSVType.matrix) {
                    switch (oneVal.mark) {
                        case -1:
                            graphics2D.setColor(MISMATCHONFALLCOLOR);
                            break;
                        case -2:
                            graphics2D.setColor(MISMATCHONADLCOLOR);
                            break;
                        case 1:
                        case 2:
                        case 3:
                        case 4:
                            graphics2D.setColor(FALLCOLOR);
                            break;
                        case 5:
                        case 6:
                        case 7:
                        case 8:
                        case 9:
                            graphics2D.setColor(ADLCOLOR);
                            break;
                        default:
                            System.out.println("Error!");
                    }
                }else{
                    switch (oneVal.mark) {
                        case -1:
                            graphics2D.setColor(MISMATCHONFALLCOLOR);
                            break;
                        case -2:
                            graphics2D.setColor(MISMATCHONADLCOLOR);
                            break;
                        case 1:
                            graphics2D.setColor(FALLCOLOR);
                            break;
                        case 2:
                            graphics2D.setColor(ADLCOLOR);
                            break;
                        default:
                            System.out.println("Error!");
                    }
                }
                graphics2D.fill3DRect(currentX, currentY, rectSize, rectSize, true);
                currentX += rectSize;
            }
            currentY += rectSize;
        }

        return image;
    }

    @Override
    public void setOriginalMarks(GraphicMatrixSolidDrawable comparisionGMD) {
        if (comparisionGMD == null) {
            return;
        }
        for (String key : scenesData.keySet()) {
            FrameMark<Integer>[] vals = scenesData.get(key);
            FrameMark<Integer>[] compVals = comparisionGMD.scenesData.get(key);
            /*if (vals.length != compVals.length) {
                System.out.println("Size mismatch in scene " + key + "!");
            }*/
            int cmpValIndex = 0;
            for (int i = 0; i < vals.length && cmpValIndex < compVals.length; i++) {
                if (vals[i].frameNumber == compVals[cmpValIndex].frameNumber) {
                    //System.out.println("drawing frame: "+ vals[i].frameNumber);
                    int oneVal = vals[i].mark;
                    int compVal = compVals[cmpValIndex].mark;
                    if(comparisionGMD.type == CSVType.svm) {
                        if (oneVal <= 4 && compVal == 2) {
                            vals[i].setMark(MISMATCHONFALL);
                        } else if (oneVal > 4 && compVal == 1) {
                            vals[i].setMark(MISMATCHONADL);
                        }
                    }else {
                        if (compVal <= 4 && oneVal == 2) {
                            vals[i].setMark(MISMATCHONFALL);
                        } else if (compVal > 4 && oneVal == 1) {
                            vals[i].setMark(MISMATCHONADL);
                        }
                    }
                    cmpValIndex++;
                }/* else {
                    System.out.println("Frame " + vals[i].frameNumber + " not found in compare csv!");
                }*/
            }
        }
        invalidate();
    }

    public enum CSVType{
        matrix, svm
    }
}

package com.mobimore.utils;

import com.mobimore.graphicsObjects.Drawable;
import com.mobimore.graphicsObjects.kinect.KinectDrawable;
import com.mobimore.graphicsObjects.kinect.SkeletonDrawable;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

/**
 * Created by mobimore on 11/6/16.
 */
public class FileUtils {
    public static Drawable loadWithFileChooser(Component parent){
        JFileChooser fileChooser = new JFileChooser(".");
        SkeletonDrawable drawable = null;
        int retValue = fileChooser.showOpenDialog(parent);
        if (retValue == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String fileExt = file.getName().substring(file.getName().lastIndexOf('.')+1);
            if (fileExt.equalsIgnoreCase("CSV")) {
                drawable = SkeletonDrawable.fromCSVFile(file, "loadedObject_" + new Random().nextInt(), false);
            }else {
                JOptionPane.showMessageDialog(null, "Error parsing file", "Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }

            String objName = (String)JOptionPane.showInputDialog(
                    parent,
                    "Parsing file done, input object name:",
                    "Parsing done",
                    JOptionPane.PLAIN_MESSAGE,
                    null, //icon
                    null,
                    file.getName().substring(0, file.getName().lastIndexOf('.')));
            int label = 0;
            if ((objName != null) && (objName.length() > 0) && drawable!=null) {
                drawable.setName(objName);
                drawable.setLabel(label);
            }else{
                drawable = null;
            }

        }
        return drawable;
    }

    public static void saveImageWithFileChooser(BufferedImage image, Component parent) {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                ImageIO.write(image, "png", file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static File getFileWithFileChooser(Component parent){
        JFileChooser fileChooser = new JFileChooser();
        int retValue = fileChooser.showOpenDialog(parent);
        if (retValue == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }
}

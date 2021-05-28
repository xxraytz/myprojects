package com.mobimore.GTest;

import com.mobimore.graphicsObjects.Drawable;
import com.mobimore.graphicsObjects.kinect.GraphicMatrixGradientDrawable;
import com.mobimore.graphicsObjects.kinect.GraphicMatrixSolidDrawable;
import com.mobimore.graphicsObjects.kinect.GraphicsMatrixDrawable;
import com.mobimore.graphicsObjects.kinect.GraphicsMatrixDrawable.ClickActionListener;
import com.mobimore.utils.FileUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;

public class GraphicMatrix implements DrawableLoadCompletionListener, ClickActionListener {
    private JPanel mainGMatrixFrame;
    private JSlider blockSizeSlider;
    private JButton saveImageButton;
    private JPanel drawMatrixPanel;
    private JScrollPane scrollPane1;
    private JLabel clickedFrameLabel;
    private JButton openDoubleMatrixButton;
    private JButton openIntMatrixButton;
    private JButton openOriginalMatrixButton1;
    private JLabel originalMatrixName;
    private GraphicsMatrixDrawable graphicMatrixDrawable;
    private GraphicMatrixSolidDrawable originalMarksMatrixDrawable;
    private JFrame frame;

    public GraphicMatrix(){
        frame = new JFrame("Graphic Matrix");
        frame.setContentPane(mainGMatrixFrame);
        blockSizeSlider.setLabelTable(blockSizeSlider.createStandardLabels(1));
        frame.pack();
        frame.setVisible(true);
        setupButtons();
        //setupMenu();
        openOriginalMatrixButton1.addActionListener(e -> {
            File file = FileUtils.getFileWithFileChooser(frame);
            if (file != null) {
                originalMatrixName.setText(file.getName());
                GraphicMatrixSolidDrawable.startLoadingFromCSVFile(file, "originalMatrix", GraphicMatrixSolidDrawable.CSVType.matrix, this);
            }else{
                originalMatrixName.setText("");
            }
        });

        openDoubleMatrixButton.addActionListener(e -> {
            File file = FileUtils.getFileWithFileChooser(frame);
            if (file != null) {
                frame.setTitle(file.getName());
                GraphicMatrixGradientDrawable.startLoadingFromCSVFile(file, "currentMatrix", this);
            } else {
                frame.setTitle("Graphic Matrix");
            }
        });

        openIntMatrixButton.addActionListener(e -> {
            File file = FileUtils.getFileWithFileChooser(frame);
            if (file != null) {
                frame.setTitle(file.getName());
                GraphicMatrixSolidDrawable.startLoadingFromCSVFile(file, "currentMatrix", GraphicMatrixSolidDrawable.CSVType.svm, this);
            }else{
                frame.setTitle("Graphic Matrix");
            }
        });
    }

    private void setupButtons(){
        saveImageButton.addActionListener(e -> {
            if (graphicMatrixDrawable != null) {
                BufferedImage image = graphicMatrixDrawable.getRenderedImage();
                FileUtils.saveImageWithFileChooser(image, mainGMatrixFrame);
            }
        });
        blockSizeSlider.addChangeListener(e -> {
            if(!blockSizeSlider.getValueIsAdjusting()) {
                if (graphicMatrixDrawable != null) {
                    graphicMatrixDrawable.setRectSize(blockSizeSlider.getValue());
                    drawMatrixPanel.setPreferredSize(graphicMatrixDrawable.getSize());
                    scrollPane1.revalidate();
                }
            }
        });
    }

    private void createUIComponents() {
        drawMatrixPanel = new DrawingPanel();
        ((DrawingPanel) drawMatrixPanel).startUpdate();
    }

    @Override
    public void drawableLoaded(Drawable drawable) {
        GraphicsMatrixDrawable matrix = (GraphicsMatrixDrawable) drawable;
        if (matrix.getName().equalsIgnoreCase("originalMatrix")) {
            /*graphicMatrixDrawable = matrix;
            ((DrawingPanel) drawMatrixPanel).addDrawable(matrix);*/
            originalMarksMatrixDrawable = (GraphicMatrixSolidDrawable) matrix;
            if (graphicMatrixDrawable != null) {
                graphicMatrixDrawable.setOriginalMarks(originalMarksMatrixDrawable);
            }
            ((DrawingPanel) drawMatrixPanel).update();
            return;
        }

        if (matrix.getName().equalsIgnoreCase("currentMatrix")) {
            //graphicMatrixDrawable.setComparisionGMD((GraphicMatrixDrawable) drawable);
            graphicMatrixDrawable = matrix;
            blockSizeSlider.setValue(3);
            ((DrawingPanel) drawMatrixPanel).clearPanel();
            if (originalMarksMatrixDrawable != null) {
                graphicMatrixDrawable.setOriginalMarks(originalMarksMatrixDrawable);
            }
            drawMatrixPanel.setPreferredSize(graphicMatrixDrawable.getSize());
            ((DrawingPanel) drawMatrixPanel).addDrawable(graphicMatrixDrawable);
            scrollPane1.revalidate();
            graphicMatrixDrawable.addOnClickListener(this);
            return;
        }
    }

    @Override
    public void onClicked(GraphicsMatrixDrawable.ClickAction action) {
        clickedFrameLabel.setText(action.getLineName() + " frame number: " + action.getFrameNumber() + " frameMark: " + action.getFrameMark());
    }
}

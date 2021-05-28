package com.mobimore.GTest;

import com.mobimore.camera.Camera;
import com.mobimore.graphicsObjects.Controllable;
import com.mobimore.graphicsObjects.Drawable;
import com.mobimore.graphicsObjects.kinect.KinectDrawable;
import com.mobimore.graphicsObjects.kinect.SkeletonDrawable;
import com.mobimore.graphicsObjects.waveFront.Vect3D;
import com.mobimore.utils.FileUtils;
import com.mobimore.utils.LibSVM;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Random;

/**
 * Created by mobimore on 9/8/16.
 */
class Main implements DrawingPanel.DrawablesListener, FallDetectionListener, SkeletonDrawableObserver {
    private JPanel mainPanel, drawPanel;
    private final JFrame frame;
    private JList<String> objectsList;
    private JButton addButton, removeButton;
    private JButton openFolderButton;
    private JButton buttonStepBack;
    private JButton buttonPause;
    private JButton buttonStepFW;
    private JButton buttonFallStarted;
    private JButton buttonFallEnded;
    private JLabel fallStartedFrameExpLabel;
    private JLabel fallEndedFrameExpLabel;
    private JLabel personsHeightLabel;
    private JButton buttonSaveData;
    private JSlider uppedBoundSlider;
    private JSlider lowerBoundSlider;
    private JCheckBox pauseAtSceneEndCheckBox;
    private JList outliers;
    private JLabel errorRateLabel;
    private JLabel fallDetectedIndicator;
    private JLabel fallStartedFrameAlgoLabel;
    private JLabel fallEndedFrameAlgoLabel;
    private Drawable selectedObj;
    private Camera camera;
    private DefaultListModel<String> listModel;
    private String selectedObjName;
    private final String FORM_TITLE = "HomeCare";
    private Plot plot = new Plot();
    private DefaultListModel<String> model = new DefaultListModel<>();
    private double errorRate = 0;
    private String startDirectory = "D:\\amyFolder\\HumanActivityRecognition\\Kinect_Project\\Databases\\Fall DB Sample";


    private Main() {
        frame = new JFrame(FORM_TITLE);
        frame.setContentPane(mainPanel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        //frame.setResizable(false);

        initMenuBar();

        outliers.setModel(model);

        /*GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        int width = gd.getDisplayMode().getWidth() / 2;
        int height = gd.getDisplayMode().getHeight() / 2;
        drawPanel.setPreferredSize(new Dimension(width, height));*/
        drawPanel.setFocusable(true);

        frame.pack();
        frame.setVisible(true);

        Vect3D cameraPosition = new Vect3D(drawPanel.getWidth() / 2, drawPanel.getHeight() / 2, 300);
        //Vect3D cameraPosition = new Vect3D(256+50, 212, 300);
        camera = new Camera(cameraPosition, new Vect3D(0, 0, 0));
        ((DrawingPanel) drawPanel).setCamera(camera);
        drawPanel.requestFocus();

        setupControlButtons();
        setupFallButtons();
        initDefaultObjects();

        objectsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                clearFallFields();
                selectedObjName = objectsList.getSelectedValue();
                selectedObj = ((DrawingPanel) drawPanel).getDrawable(selectedObjName);
                if (selectedObj instanceof Controllable) {
                    setControlButtonsEnabled(true);
                } else {
                    setControlButtonsEnabled(false);
                }

                if (selectedObj instanceof KinectDrawable) {
                    KinectDrawable d = (KinectDrawable) selectedObj;
                    setFallButtonsEnabled(true);
                    updateFallFields(d);
                    String title = d.getFolderWithFiles();
                    int index = title.lastIndexOf(File.separatorChar);
                    if (index != -1) {
                        title = title.substring(0, index);
                    }
                    frame.setTitle(title);
                } else {
                    setFallButtonsEnabled(false);
                    frame.setTitle(FORM_TITLE);
                }
            }
        });

        addButton.addActionListener(e -> {
            Drawable coordsDrawable = FileUtils.loadWithFileChooser(frame);
            if (coordsDrawable != null) {
                if (!listModel.contains(coordsDrawable.getName())) {
                    coordsDrawable.setColor(new Color(255, 0, 0, 255));
                    ((DrawingPanel) drawPanel).addDrawable(coordsDrawable);
                } else {
                    JOptionPane.showMessageDialog(null, "Object with this name already exists", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
            drawPanel.requestFocus();
        });

        removeButton.addActionListener(e -> {
            ((DrawingPanel) drawPanel).removeDrawable(selectedObjName); //onObjectRemoved is called
            drawPanel.requestFocus();
        });

        openFolderButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser(".");
            KinectDrawable kinectDrawable = null;
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.setCurrentDirectory(new File(startDirectory));
            int retValue = fileChooser.showOpenDialog(frame);
            if (retValue == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                startDirectory = file.getAbsolutePath();
                kinectDrawable = KinectDrawable.fromFolder(file, "drawable_" + new Random().nextInt());
            }
            if (kinectDrawable != null) {
                kinectDrawable.setUpperBound(uppedBoundSlider.getValue());
                kinectDrawable.setLowerBound(lowerBoundSlider.getValue());
                kinectDrawable.setFallDetectionListener(this);
                kinectDrawable.setSkeletonDrawableObserver(this);
                ((DrawingPanel) drawPanel).addDrawable(kinectDrawable);
            }

        });
        uppedBoundSlider.addChangeListener(e -> {
            if (!uppedBoundSlider.getValueIsAdjusting()) {
                if (selectedObj instanceof KinectDrawable) {
                    ((KinectDrawable) selectedObj).setUpperBound(uppedBoundSlider.getValue());
                }
            }
        });
        lowerBoundSlider.addChangeListener(e -> {
            if (!lowerBoundSlider.getValueIsAdjusting()) {
                if (selectedObj instanceof KinectDrawable) {
                    ((KinectDrawable) selectedObj).setLowerBound(lowerBoundSlider.getValue());
                }
            }
        });
        ((DrawingPanel) drawPanel).startUpdate();
    }

    private void initDefaultObjects() {
        //DepthDrawable depthDrawable = DepthDrawable.fromFolder(new File("Depth"), "depthMap");
        //KinectDrawable kinectDrawable = KinectDrawable.fromFolder(new File("1"), "depth");
        //((DrawingPanel) drawPanel).addDrawable(kinectDrawable);
    }

    private void createUIComponents() {
        drawPanel = new DrawingPanel(this);
        listModel = new DefaultListModel<>();
        objectsList = new JList<>(listModel);
        objectsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    public static void main(String[] args) {
        System.setProperty("sun.java2d.opengl", "true"); //hardware acceleration
        SwingUtilities.invokeLater(Main::new);
    }

    @Override
    public void onObjectAdded(final String objectName) {
        listModel.addElement(objectName);
        selectedObjName = objectName;
        objectsList.setSelectedValue(objectName, true);
    }

    @Override
    public void onObjectRemoved(final String objectName) {
        int index = objectsList.getSelectedIndex();
        listModel.remove(listModel.indexOf(objectName));
        int size = listModel.getSize();
        selectedObjName = "";
        selectedObj = null;
        if (size != 0) {
            if (index == size) index--;
            objectsList.setSelectedIndex(index);
            objectsList.ensureIndexIsVisible(index);
        }
        System.gc();
        fallDetectedIndicator.setVisible(false);
        plot.clear();
    }

    @Override
    public void onPanelCleared() {
        listModel.clear();
        selectedObjName = "";
        selectedObj = null;
        System.gc();
    }

    @Override
    public void onFallDetected(int frameNumber) {
        fallDetectedIndicator.setVisible(true);
        fallStartedFrameAlgoLabel.setText(Integer.toString(frameNumber));
        fallEndedFrameAlgoLabel.setText("-");
    }

    @Override
    public void onFallUndetected(int frameNumber) {
        fallDetectedIndicator.setVisible(false);
        fallEndedFrameAlgoLabel.setText(Integer.toString(frameNumber));
    }

    @Override
    public void onNewFrameProcessed(boolean expertVal, LibSVM.SVMResult result, boolean resultVal, int frameNumber) {
        double expertValD = expertVal ? 1 : -1;
        double resultValD = resultVal ? 1 : -1;
        plot.addData(expertValD, result, resultValD);
        if (result.isOutlier) {
            model.addElement("Outlier_"+frameNumber);
        }
        if (selectedObj instanceof KinectDrawable) {
            KinectDrawable kinectDrawable = (KinectDrawable) selectedObj;
            errorRate += Math.abs((expertValD - resultValD) / 2) / kinectDrawable.getFramesCount();
            errorRateLabel.setText(String.format("%.2f", (1-errorRate) * 100) + "%");
        }
    }

    private void setupControlButtons() {
        buttonStepBack.addActionListener(e -> {
            if (selectedObj instanceof Controllable) {
                Controllable selectedObjControllable = (Controllable) selectedObj;
                selectedObjControllable.restart();
                selectedObjControllable.pause();
                buttonPause.setText("|>");
            }
        });
        buttonPause.addActionListener(e -> {
            if (selectedObj instanceof Controllable) {
                Controllable selectedObjControllable = (Controllable) selectedObj;
                if (!selectedObjControllable.isPaused()) {
                    selectedObjControllable.pause();
                    buttonPause.setText("|>");
                } else {
                    selectedObjControllable.resume();
                    buttonPause.setText("||");
                }
            }

        });
        buttonStepFW.addActionListener(e -> {
            if (selectedObj instanceof Controllable) {
                Controllable selectedObjControllable = (Controllable) selectedObj;
                selectedObjControllable.stepFW();
            }
        });
    }

    private void setupFallButtons() {
        buttonFallStarted.addActionListener(e -> {
            if (selectedObj instanceof KinectDrawable) {
                KinectDrawable selectedObjKinectDrawable = (KinectDrawable) selectedObj;
                int frameNumber = selectedObjKinectDrawable.getCurrentFrameNumber();
                selectedObjKinectDrawable.setFallStartedFrame(frameNumber);
                if (frameNumber != -1) {
                    fallStartedFrameExpLabel.setText(Integer.toString(frameNumber));
                } else {
                    fallStartedFrameExpLabel.setText("-");
                }
            }
        });
        buttonFallEnded.addActionListener(e -> {
            if (selectedObj instanceof KinectDrawable) {
                KinectDrawable selectedObjKinectDrawable = (KinectDrawable) selectedObj;
                int frameNumber = selectedObjKinectDrawable.getCurrentFrameNumber();
                selectedObjKinectDrawable.setFallEndedFrame(frameNumber);
                if (frameNumber != -1) {
                    fallEndedFrameExpLabel.setText(Integer.toString(frameNumber));
                } else {
                    fallEndedFrameExpLabel.setText("-");
                }
            }
        });
        buttonSaveData.addActionListener(e -> {
            if (selectedObj instanceof KinectDrawable) {
                KinectDrawable selectedObjKinectDrawable = (KinectDrawable) selectedObj;
                try {
                    selectedObjKinectDrawable.saveDataToFolder();
                } catch (IOException e1) {
                    JOptionPane.showMessageDialog(frame, "Can't save file with fall marks", "Error saving fall marks", JOptionPane.ERROR_MESSAGE);
                    e1.printStackTrace();
                }
            }
        });
    }

    private void setControlButtonsEnabled(boolean enable) {
        buttonStepBack.setEnabled(enable);
        buttonPause.setEnabled(enable);
        buttonStepFW.setEnabled(enable);
    }

    private void setFallButtonsEnabled(boolean enable) {
        buttonFallStarted.setEnabled(enable);
        buttonFallEnded.setEnabled(enable);
        buttonSaveData.setEnabled(enable);
    }

    private void clearFallFields() {
        fallStartedFrameExpLabel.setText("-");
        fallEndedFrameExpLabel.setText("-");
        fallStartedFrameAlgoLabel.setText("-");
        fallEndedFrameAlgoLabel.setText("-");
        personsHeightLabel.setText("-");
        fallDetectedIndicator.setVisible(false);
    }

    private void updateFallFields(KinectDrawable kinectDrawable) {
        int fallStartedFrame = kinectDrawable.getFallStartedFrame();
        int fallEndedFrame = kinectDrawable.getFallEndedFrame();
        double maxHeight = kinectDrawable.getMaxHeight();
        if (fallStartedFrame >= 0 && fallEndedFrame >= 0) {
            fallStartedFrameExpLabel.setText(Integer.toString(fallStartedFrame));
            fallEndedFrameExpLabel.setText(Integer.toString(fallEndedFrame));
        }

        if (maxHeight >= 0) {
            personsHeightLabel.setText(String.format("%.3f", maxHeight));
        }
    }

    private void initMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu utilities = new JMenu("Utilities");
        JMenuItem createGraphicMatrix = new JMenuItem("Create Graphic Matrix");
        JMenuItem showPlotMenu = new JMenuItem("Show plot");
        createGraphicMatrix.addActionListener(e -> new GraphicMatrix());
        showPlotMenu.addActionListener(e -> plot.setVisible(true));
        utilities.add(createGraphicMatrix);
        utilities.add(showPlotMenu);
        menuBar.add(utilities);
        frame.setJMenuBar(menuBar);
    }

    @Override
    public void onSceneEnded(SkeletonDrawable sender) {
        if (pauseAtSceneEndCheckBox.isSelected()) {
            buttonPause.setText("|>");
        } else {
            if (selectedObj instanceof KinectDrawable) {
                ((KinectDrawable) selectedObj).restart();
            }
        }
    }

    @Override
    public void onSceneStarted(SkeletonDrawable sender) {
        plot.clear();
        model.clear();
        errorRate = 0;
        errorRateLabel.setText("-");
        fallStartedFrameAlgoLabel.setText("-");
        fallEndedFrameAlgoLabel.setText("-");
        fallDetectedIndicator.setVisible(false);
    }
}

package com.mobimore.GTest;

import com.mobimore.utils.LibSVM;
import javafx.scene.shape.Shape;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;

public class Plot {
    private JFrame frame;
    private JPanel plotPanel;
    private XYSeries xySeriesExpert = new XYSeries("Expert");
    private XYSeries xySeriesSVM = new XYSeries("SVM");
    private XYSeries xySeriesResult = new XYSeries("CUSUM");
    private XYSeries xySeriesOutliers = new XYSeries("Outliers");
    private XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
    private JFreeChart chart;
    private static final String chartTitle = "Signals plot";
    private static final String xLabel = "frame number";
    private static final String yLabel = "Value";
    private int x = 0;

    public Plot() {
        xySeriesCollection.addSeries(xySeriesOutliers);
        xySeriesCollection.addSeries(xySeriesExpert);
        xySeriesCollection.addSeries(xySeriesSVM);
        xySeriesCollection.addSeries(xySeriesResult);
        chart = ChartFactory.createXYLineChart(chartTitle, xLabel, yLabel, xySeriesCollection, PlotOrientation.VERTICAL, true, true, false);
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setDomainPannable(true);
        plot.setBackgroundPaint(Color.white);
        plot.setRangeGridlinePaint(Color.black);
        plot.setDomainGridlinePaint(Color.black);

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        BasicStroke stroke = new BasicStroke(3f);

        //Outliers
        renderer.setSeriesLinesVisible(0, false);
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesShape(0, new Ellipse2D.Double(-3, -3, 6,6));

        //Expert
        renderer.setSeriesLinesVisible(1, true);
        renderer.setSeriesShapesVisible(1, false);
        renderer.setSeriesStroke(1, stroke);

        //SVM
        renderer.setSeriesLinesVisible(2, true);
        renderer.setSeriesShapesVisible(2, true);
        renderer.setSeriesStroke(2, stroke);

        //CUSUM
        renderer.setSeriesLinesVisible(3, true);
        renderer.setSeriesShapesVisible(3, false);
        renderer.setSeriesStroke(3, stroke);

        //Color setting
        renderer.setSeriesPaint(0,Color.yellow);
        renderer.setSeriesPaint(1,Color.red);
        renderer.setSeriesPaint(2,Color.blue);
        renderer.setSeriesPaint(3,Color.green);
        plot.setRenderer(renderer);

        plotPanel = new ChartPanel(chart);
        plotPanel.setVisible(true);
        plotPanel.setPreferredSize(new Dimension(1000,300));

        frame = new JFrame("Signals Plot");
        frame.setContentPane(plotPanel);
        frame.pack();
    }

    public void setVisible(boolean visible) {
        frame.setVisible(visible);
    }

    private void createUIComponents() {
        plotPanel = new ChartPanel(chart);
    }

    public void addData(double expertValue, LibSVM.SVMResult svmResult, double resultValue) {
        xySeriesExpert.add(x, expertValue);
        xySeriesResult.add(x, resultValue);
        xySeriesSVM.add(x, svmResult.svmVal);
        if (svmResult.isOutlier) {
            xySeriesOutliers.add(x, svmResult.svmVal);

            System.out.println("Outlier");
        }
        x++;
    }

    public void clear(){
        xySeriesExpert.clear();
        xySeriesSVM.clear();
        xySeriesResult.clear();
        xySeriesOutliers.clear();
        x = 0;
    }
}

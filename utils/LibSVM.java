package com.mobimore.utils;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_problem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;

public class LibSVM {
    private static LibSVM ourInstance;
    private svm_model modelMain;
    private svm_model modelOutliers;
    private static double[] averages;
    private static double[] dispersionSQRT;

    static {
        try {
            ourInstance = new LibSVM();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
/*
    private final String PATH = "." + File.separatorChar + "data" + File.separatorChar + "withoutDataset11"+ File.separatorChar + "modelMain.txt";
    private final String PATH_OUTLIERS = "." + File.separatorChar + "data" + File.separatorChar + "withoutDataset11"+ File.separatorChar + "modelOutliers.txt";
    private final String compensationAverages = "." + File.separatorChar + "data" + File.separatorChar + "withoutDataset11" + File.separatorChar + "averageCSV.csv";
    private final String compensationDispersions = "." + File.separatorChar + "data" + File.separatorChar + "withoutDataset11" + File.separatorChar + "dispersionCSV.csv";
*/

    private final String PATH = "." + File.separatorChar + "data" + File.separatorChar + "modelMain.txt";
    private final String PATH_OUTLIERS = "." + File.separatorChar + "data" + File.separatorChar + "modelOutliers.txt";
    private final String compensationAverages = "." + File.separatorChar + "data" + File.separatorChar + "averageCSV.csv";
    private final String compensationDispersions = "." + File.separatorChar + "data" + File.separatorChar + "dispersionCSV.csv";


    private double[] loadCompesations(String path) throws FileNotFoundException {
        Scanner scan = new Scanner(new File(path)).useLocale(Locale.US);
        scan.useDelimiter(",");
        ArrayList<Double> arrayList = new ArrayList<>();
        while (scan.hasNext()) {
            double val = scan.nextDouble();
            arrayList.add(val);
        }
        return arrayList.stream().mapToDouble(i -> i).toArray();
    }

    public static LibSVM getInstance() {
        return ourInstance;
    }

    private LibSVM() throws IOException {
        modelMain = svm.svm_load_model(PATH);
        modelOutliers = svm.svm_load_model(PATH_OUTLIERS);
        averages = loadCompesations(compensationAverages);
        dispersionSQRT = loadCompesations(compensationDispersions);
    }

    //if true - fall detected
    public SVMResult classify(svm_node[] vector){
        compensate(vector);
        boolean outlier = isOutlier(vector);
        if (outlier) {
            return new SVMResult(outlier, 0);
        }
        return new SVMResult(outlier, svm.svm_predict(modelMain, vector));
    }

    private void compensate(svm_node[] vector){
        for (int i = 0, vectorLength = vector.length; i < vectorLength; i++) {
            svm_node node = vector[i];
            node.value = (node.value - averages[i]) / dispersionSQRT[i];
            vector[i] = node;
        }
    }

    private boolean isOutlier(svm_node[] vector){
        return svm.svm_predict(modelOutliers, vector) == -1;
    }

    public static class SVMResult{
        public boolean isOutlier;
        public double svmVal;

        public SVMResult(boolean isOutlier, double svmVal) {
            this.isOutlier = isOutlier;
            this.svmVal = svmVal;
        }

    }
}

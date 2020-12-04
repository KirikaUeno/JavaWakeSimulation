package com.ui;

import com.company.Constants;
import com.image.Image;
import com.image.ImageFactory;
import com.objects.BoxOfPart;
import org.jblas.ComplexDoubleMatrix;
import org.jblas.DoubleMatrix;
import org.opensourcephysics.numerics.Complex;
import org.opensourcephysics.numerics.ComplexEigenvalueDecomposition;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class CirculantsMainPanel extends JPanel {
    private ImageIcon backgroundImage;
    private Timer timer;
    private final MainPanel mainPanel;
    private GraphSpectra graphSpectra= new GraphSpectra();
    private final int[][] graphX = new int[Constants.boxesNumber][Constants.currentSamples];
    private final int[][] graphY = new int[Constants.boxesNumber][Constants.currentSamples];
    private double[][] wakeFunction;
    private double[][] wakeMatrix;
    private double[][] circulantMatrix;
    private BoxOfPart[] boxes = new BoxOfPart[Constants.boxesNumber];

    public double scaleX = 10;
    public double scaleY = 10;

    public CirculantsMainPanel(MainPanel mainPanel){
        setPreferredSize(new Dimension(Constants.boardWight, Constants.boardHeight));
        setFocusable(true);

        this.mainPanel=mainPanel;

        initializeVariables();
    }

    private void initializeVariables() {

        Button calculateSpectra = new Button("calculateSpectra");
        calculateSpectra.addActionListener(e -> calculateSpectra());
        Button swapToSimulation = new Button("swapToSimulation");
        swapToSimulation.addActionListener(e -> { this.timer.stop();mainPanel.swapToSimulation();});

        this.graphSpectra=new GraphSpectra();

        SpringLayout layout = new SpringLayout();

        layout.putConstraint(SpringLayout.WEST, graphSpectra, 0, SpringLayout.WEST, this);
        layout.putConstraint(SpringLayout.NORTH, graphSpectra, 100, SpringLayout.NORTH, this);

        layout.putConstraint(SpringLayout.WEST, calculateSpectra, 5, SpringLayout.WEST, this);
        layout.putConstraint(SpringLayout.NORTH, calculateSpectra, 5, SpringLayout.NORTH, this);

        layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, swapToSimulation, Constants.boardWight/2, SpringLayout.WEST, this);
        layout.putConstraint(SpringLayout.NORTH, swapToSimulation, 5, SpringLayout.NORTH, this);

        setLayout(layout);

        add(swapToSimulation);
        add(calculateSpectra);
        add(graphSpectra);

        this.backgroundImage = ImageFactory.createImage(Image.BACKGROUND);
        this.timer = new Timer(Constants.updateSpeed, new UILoop1(this));

        for(int i = 0; i<Constants.boxesNumber;i++){
            boxes[i] = new BoxOfPart(Constants.z0*Math.cos(2*Math.PI*(i+0.5)/Constants.boxesNumber));
            System.out.println(boxes[i].z);
        }
        //reading wake function from file and calculating wake matrix
        wakeFunction=readWake();
        wakeMatrix=calculateWakeMatrix();
        circulantMatrix=calculateCirculant();
    }

    private void calculateSpectra() {
        Complex[][] mainMatrix;
        Complex[] eigenValues = new Complex[Constants.boxesNumber];
        Complex[][] eigenVectors = new Complex[Constants.boxesNumber][Constants.boxesNumber];
        boolean[] isOk=new boolean[Constants.boxesNumber];
        for(int j=0;j<Constants.currentSamples;j++){
            mainMatrix=makeMatrix(j*Constants.currentSamplesStep);
            ComplexEigenvalueDecomposition.eigen(mainMatrix,eigenValues,eigenVectors,isOk);
            for(int i=0;i<Constants.boxesNumber;i++) {
                graphY[i][j] =-(int)(scaleY*eigenValues[i].re());
                System.out.println(eigenValues[i].re());
                graphX[i][j] =(int)(scaleX*j*Constants.currentSamplesStep);
            }
        }
        graphSpectra.fillGraph(graphX,graphY);
        doOneLoop();
    }

    private Complex[][] makeMatrix(double cur){
        Complex[][] matrix1 = new Complex[Constants.boxesNumber][Constants.boxesNumber];
        for(int i=0;i<Constants.boxesNumber;i++){
            for(int j=0;j<Constants.boxesNumber;j++){
                matrix1[i][j]=new Complex();
                matrix1[i][j].set(Constants.wake*cur*wakeMatrix[i][j],circulantMatrix[i][j]);
            }
        }
        return matrix1;
    }

    private double[][] readWake(){
        try {
            File matrixFile = new File(Constants.WAKE_MATRIX_URL);
            ArrayList<Double> matrixList=new ArrayList<>();
            Scanner s = new Scanner(matrixFile);
            while (!s.hasNextDouble()) {
                s.next();
            }
            while (s.hasNextDouble()) {
                matrixList.add(s.nextDouble());
            }
            System.out.println(matrixList.size());

            double[][] wakeMatrix = new double[matrixList.size()/2][2];
            for(int i=0;i<matrixList.size()/2;i++){
                for(int j=0;j<2;j++){
                    wakeMatrix[i][j]=matrixList.get(2*i+j);
                }
                System.out.println(wakeMatrix[i][0]+" "+wakeMatrix[i][1]);
            }
            return wakeMatrix;

        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
            double[][] wakeMatrix = new double[0][0];
            return wakeMatrix;
        }
    }

    private double findWake(double z){
        double minZ=1000;
        double wake=0;
        for(double[] string: wakeFunction){
            if(Math.abs(string[0]-z)<minZ){
                minZ=Math.abs(string[0]-z);
                wake = string[1];
            }
        }
        return wake;
    }

    private double[][] calculateWakeMatrix(){
        double[][] tempMatrix = new double[Constants.boxesNumber][Constants.boxesNumber];
        for(int i=0;i<Constants.boxesNumber;i++){
            for(int j=0;j<Constants.boxesNumber;j++){
                tempMatrix[i][j]=0;
                if((boxes[i].z-boxes[j].z)<-0.001){
                    tempMatrix[i][j]+=findWake((boxes[i].z-boxes[j].z));
                } else if(((boxes[i].z-boxes[j].z)>-0.001) && ((boxes[i].z-boxes[j].z)<0.001)){
                    tempMatrix[i][j]+=findWake((boxes[i].z-boxes[j].z))/2;
                }
                System.out.print(tempMatrix[i][j]+" ");
            }
            System.out.println("");
        }
        return tempMatrix;
    }

    private double[][] calculateCirculant(){
        double[][] tempMatrix = new double[Constants.boxesNumber][Constants.boxesNumber];
        for(int i=0;i<Constants.boxesNumber;i++){
            for(int j=0;j<Constants.boxesNumber;j++){
                if(i!=j){
                    tempMatrix[i][j]=Math.pow(-1, i-j)/Math.sin((i-j)*Math.PI/(Constants.boxesNumber));
                } else {
                    tempMatrix[i][j]=0;
                }
            }
        }
        return tempMatrix;
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Toolkit.getDefaultToolkit().sync();
    }

    public void doOneLoop() {
        update();
        repaint();
    }

    private void update() {

    }
}

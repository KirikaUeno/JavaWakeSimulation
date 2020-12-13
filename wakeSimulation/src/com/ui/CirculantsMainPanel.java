package com.ui;

import com.company.Constants;
import com.company.MainMouseListener;
import com.image.Image;
import com.image.ImageFactory;
import com.objects.BoxOfPart;
import org.opensourcephysics.numerics.Complex;
import org.opensourcephysics.numerics.ComplexEigenvalueDecomposition;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

/**
 * Main panel for 2nd mode. Has only 2 buttons - calculate spectra and exit. Fill whole mainPanel.
 * Show spectra vs current for circulant approach. Read wakeFunction from a file, calculate wake and circulant
 * matrices for a given number of boxes.
 */
public class CirculantsMainPanel extends JPanel {
    private Timer timer;
    private final MainFrame mainFrame;
    private final GraphPanel graphPanel= new GraphPanel("mode2");
    private double[][] wakeFunction;
    private double[][] wakeMatrix;
    private double[][] circulantMatrix;
    private final BoxOfPart[] boxes = new BoxOfPart[Constants.boxesNumber];
    /**
     * Create panel and make reference to mainPanel to be able to swap modes. Calls initializeVariables().
     */
    public CirculantsMainPanel(MainFrame mainFrame){
        setPreferredSize(new Dimension(Constants.boardWight, Constants.boardHeight));
        setFocusable(true);
        MainMouseListener mainMouseListener = new MainMouseListener(graphPanel);
        graphPanel.addMouseListener(mainMouseListener);
        graphPanel.addMouseMotionListener(mainMouseListener);
        this.mainFrame = mainFrame;

        initializeVariables();
    }
    /**
     * Create buttons, graph field as GraphSpectra, spring layout. Initialize boxes and make wake and circulant matrices.
     */
    private void initializeVariables() {

        Button calculateSpectra = new Button("calculateSpectra");
        calculateSpectra.addActionListener(e -> calculateSpectra());
        Button swapToSimulation = new Button("swapToSimulation");
        swapToSimulation.addActionListener(e -> { this.timer.stop();
            mainFrame.swapToSimulation();});

        SpringLayout layout = new SpringLayout();

        layout.putConstraint(SpringLayout.EAST, graphPanel, 0, SpringLayout.EAST, this);
        layout.putConstraint(SpringLayout.SOUTH, graphPanel, 0, SpringLayout.SOUTH, this);

        layout.putConstraint(SpringLayout.WEST, calculateSpectra, 5, SpringLayout.WEST, this);
        layout.putConstraint(SpringLayout.NORTH, calculateSpectra, 5, SpringLayout.NORTH, this);

        layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, swapToSimulation, Constants.boardWight/2, SpringLayout.WEST, this);
        layout.putConstraint(SpringLayout.NORTH, swapToSimulation, 5, SpringLayout.NORTH, this);

        setLayout(layout);

        add(swapToSimulation);
        add(calculateSpectra);
        add(graphPanel);

        ImageIcon backgroundImage = ImageFactory.createImage(Image.BACKGROUND);
        this.timer = new Timer(Constants.updateSpeed, e->doOneLoop());

        for(int i = 0; i<Constants.boxesNumber;i++){
            boxes[i] = new BoxOfPart(Constants.z0*Math.cos(2*Math.PI*(i+0.5)/Constants.boxesNumber));
            //System.out.println(boxes[i].z);
        }
        //reading wake function from file and calculating wake matrix
        wakeFunction=readWake();
        wakeMatrix=calculateWakeMatrix();
        circulantMatrix=calculateCirculant();
        graphPanel.setName("mode2");
        graphPanel.setPreferredSize(new Dimension(Constants.boardWight, Constants.boardHeight-25));
        graphPanel.setScaleX(10);
        graphPanel.setScaleY(0.05);
        graphPanel.setShiftX(-0.5);
        graphPanel.setShiftY(-0.3);
    }
    /**
     * Calculate eigenValues using osp and fill the main graphPanel, y derived by ws, x multiplied by wake.
     */
    private void calculateSpectra() {
        ArrayList<Double> graphX = new ArrayList<>();
        ArrayList<ArrayList<Double>> graphY = new ArrayList<>();
        Complex[][] mainMatrix;
        Complex[] eigenValues = new Complex[Constants.boxesNumber];
        Complex[][] eigenVectors = new Complex[Constants.boxesNumber][Constants.boxesNumber];
        boolean[] isOk=new boolean[Constants.boxesNumber];
        for(int j=0;j<Constants.currentSamples;j++){
            mainMatrix=makeMatrix(j*Constants.currentSamplesStep);
            ComplexEigenvalueDecomposition.eigen(mainMatrix,eigenValues,eigenVectors,isOk);
            graphX.add(j*Constants.currentSamplesStep*Constants.wake);
            graphY.add(new ArrayList<>());
            for(int i=0;i<Constants.boxesNumber;i++) {
                graphY.get(j).add(eigenValues[i].re()/Constants.zFreq);
                //System.out.println(eigenValues[i].re());
            }
            Collections.sort(graphY.get(j));
        }
        graphPanel.fillGraph(graphX,graphY);
        repaint();
    }
    /**
     * make complex matrix from Re=Constants.wake cur wakeMatrix, Im=-I ws circulantMatrix.
     */
    private Complex[][] makeMatrix(double cur){
        Complex[][] matrix1 = new Complex[Constants.boxesNumber][Constants.boxesNumber];
        for(int i=0;i<Constants.boxesNumber;i++){
            for(int j=0;j<Constants.boxesNumber;j++){
                matrix1[i][j]=new Complex();
                matrix1[i][j].set(Constants.wake*cur*wakeMatrix[i][j],-Constants.zFreq*circulantMatrix[i][j]);
            }
        }
        return matrix1;
    }
    /**
     * read file with wakeFunction and return array of pairs (z wake).
     */
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
            double[][] wakeMatrix = new double[matrixList.size()/2][2];
            for(int i=0;i<matrixList.size()/2;i++){
                for(int j=0;j<2;j++){
                    wakeMatrix[i][j]=matrixList.get(2*i+j);
                }
            }
            return wakeMatrix;

        } catch (FileNotFoundException e) {
            System.out.println("File not found");
            e.printStackTrace();
            return new double[0][0];
        }
    }
    /**
     * find the nearest z0 from file to a given z and return wake(z0).
     */
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
    /**
     * calculate matrix of interactions between boxes. If zi=zj return wake/2, if zi<zj return wake, else return 0.
     */
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
                //System.out.print(tempMatrix[i][j]+" ");
            }
            //System.out.println("");
        }
        return tempMatrix;
    }
    /**
     * Calculate symmetric circulant (numberOfBoxes is odd, modes are symmetric relatively to 0).
     */
    private double[][] calculateCirculant(){
        double[][] tempMatrix = new double[Constants.boxesNumber][Constants.boxesNumber];
        for(int i=0;i<Constants.boxesNumber;i++){
            for(int j=0;j<Constants.boxesNumber;j++){
                if(i!=j){
                    tempMatrix[i][j]=Math.pow(-1, i-j)/Math.sin((i-j)*Math.PI/(Constants.boxesNumber))/2;
                } else {
                    tempMatrix[i][j]=0;
                }
            }
        }
        return tempMatrix;
    }

    /*protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Toolkit.getDefaultToolkit().sync();
    }*/

    public void doOneLoop() {
        update();
        repaint();
    }

    private void update() {

    }
}

package ui;

import company.Config;
import company.MainMouseListener;
import objects.BoxOfPart;
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
    private final BoxOfPart[] boxes = new BoxOfPart[Config.boxesNumber];
    private double wake = 1;
    /**
     * Create panel and make reference to mainPanel to be able to swap modes. Calls initializeVariables().
     */
    public CirculantsMainPanel(MainFrame mainFrame){
        setPreferredSize(new Dimension(Config.boardWight, Config.boardHeight));
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
        TextField wakeField = new TextField("0.05");
        wakeField.addActionListener(e -> this.wake = Double.parseDouble(wakeField.getText()));

        JLabel wakeLabel = new JLabel("wake:");

        SpringLayout layout = new SpringLayout();

        layout.putConstraint(SpringLayout.EAST, graphPanel, 0, SpringLayout.EAST, this);
        layout.putConstraint(SpringLayout.SOUTH, graphPanel, 0, SpringLayout.SOUTH, this);

        layout.putConstraint(SpringLayout.WEST, calculateSpectra, 5, SpringLayout.WEST, this);
        layout.putConstraint(SpringLayout.NORTH, calculateSpectra, 5, SpringLayout.NORTH, this);

        layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, swapToSimulation, Config.boardWight/2, SpringLayout.WEST, this);
        layout.putConstraint(SpringLayout.NORTH, swapToSimulation, 5, SpringLayout.NORTH, this);

        layout.putConstraint(SpringLayout.WEST, wakeLabel, 5, SpringLayout.EAST, calculateSpectra);
        layout.putConstraint(SpringLayout.NORTH, wakeLabel, 5, SpringLayout.SOUTH, this);
        layout.putConstraint(SpringLayout.WEST, wakeField, 5, SpringLayout.EAST, wakeLabel);
        layout.putConstraint(SpringLayout.NORTH, wakeField, 5, SpringLayout.SOUTH, this);


        setLayout(layout);

        add(swapToSimulation);
        add(calculateSpectra);
        add(graphPanel);
        add(wakeLabel);
        add(wakeField);

        this.timer = new Timer(Config.updateSpeed, e->doOneLoop());

        for(int i = 0; i< Config.boxesNumber; i++){
            boxes[i] = new BoxOfPart(Config.z0*Math.cos(2*Math.PI*(i+0.5)/ Config.boxesNumber));
            //System.out.println(boxes[i].z);
        }
        //reading wake function from file and calculating wake matrix
        wakeFunction=readWake();
        wakeMatrix=calculateWakeMatrix();
        circulantMatrix=calculateCirculant();
        graphPanel.setName("mode2");
        graphPanel.setPreferredSize(new Dimension(Config.boardWight, Config.boardHeight-25));
        graphPanel.setScaleX(10);
        graphPanel.setScaleY(0.05);
        graphPanel.setShiftX(-0.5);
        graphPanel.setShiftY(-0.3);
    }
    /**
     * Calculate eigenValues using osp and fill the main graphPanel, y derived by ws, x multiplied by wake.
     */
    private void calculateSpectra() {
        ArrayList<ArrayList<Double>> graphY = new ArrayList<>();
        Complex[][] mainMatrix;
        Complex[] eigenValues = new Complex[Config.boxesNumber];
        Complex[][] eigenVectors = new Complex[Config.boxesNumber][Config.boxesNumber];
        boolean[] isOk=new boolean[Config.boxesNumber];
        for(int j = 0; j< Config.currentSamples; j++){
            mainMatrix=makeMatrix(j* Config.currentSamplesStep);
            ComplexEigenvalueDecomposition.eigen(mainMatrix,eigenValues,eigenVectors,isOk);
            graphY.add(new ArrayList<>());
            graphY.get(j).add(j* Config.currentSamplesStep*wake);
            for(int i = 0; i< Config.boxesNumber; i++) {
                graphY.get(j).add(eigenValues[i].re()/ Config.zFreq);
                //System.out.println(eigenValues[i].re());
            }
            Collections.sort(graphY.get(j));
        }
        graphPanel.fillGraph(graphY);
        repaint();
    }
    /**
     * make complex matrix from Re=Constants.wake cur wakeMatrix, Im=-I ws circulantMatrix.
     */
    private Complex[][] makeMatrix(double cur){
        Complex[][] matrix1 = new Complex[Config.boxesNumber][Config.boxesNumber];
        for(int i = 0; i< Config.boxesNumber; i++){
            for(int j = 0; j< Config.boxesNumber; j++){
                matrix1[i][j]=new Complex();
                matrix1[i][j].set(wake*cur*wakeMatrix[i][j],-Config.zFreq*circulantMatrix[i][j]);
            }
        }
        return matrix1;
    }
    /**
     * read file with wakeFunction and return array of pairs (z wake).
     */
    private double[][] readWake(){
        try {
            File matrixFile = new File(Config.WAKE_MATRIX_URL);
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
        double[][] tempMatrix = new double[Config.boxesNumber][Config.boxesNumber];
        for(int i = 0; i< Config.boxesNumber; i++){
            for(int j = 0; j< Config.boxesNumber; j++){
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
        double[][] tempMatrix = new double[Config.boxesNumber][Config.boxesNumber];
        for(int i = 0; i< Config.boxesNumber; i++){
            for(int j = 0; j< Config.boxesNumber; j++){
                if(i!=j){
                    tempMatrix[i][j]=Math.pow(-1, i-j)/Math.sin((i-j)*Math.PI/(Config.boxesNumber))/2;
                } else {
                    tempMatrix[i][j]=0;
                }
            }
        }
        return tempMatrix;
    }

    public void doOneLoop() {
        update();
        repaint();
    }

    private void update() {

    }
}

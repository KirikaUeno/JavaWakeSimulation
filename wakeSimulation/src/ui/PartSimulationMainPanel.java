package ui;

import company.Constants;
import company.MainKeyListener;
import company.MainMouseListener;
import objects.Particle;
import org.opensourcephysics.numerics.FFT;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.Math;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Panel for 1st operating mode of program. Fill whole field of application.
 * Sets array of particles with air-bag distribution in longitudinal and transverse dimensions.
 */
public class PartSimulationMainPanel extends JPanel {
    private Timer timer;

    private final JLabel label = new JLabel();
    private final JLabel fpsLabel = new JLabel("");
    private final GraphPanel partPanel = new GraphPanel("partPanel");
    private final GraphPanel panel1 = new GraphPanel("panel1");
    private final GraphPanel panel2 = new GraphPanel("panel2");
    private final MainFrame mainFrame;

    private Particle[] particles;

    private final double[] dipoleMoms = new double[Constants.numberOfParticles];
    private double dipoleMom = 0;
    private final Choice showX = new Choice();
    private final Choice showY = new Choice();

    private final ArrayList<Double> pickUpD= new ArrayList<>();
    private double wake=0.000;

    private int fourierMode = 0;
    private double fpsOldTime = System.currentTimeMillis();

    /**
     * Sets panel size, listeners, reference to main panel to change modes of program.
     * Calls initializeVariables()
     */
    public PartSimulationMainPanel(MainFrame mainFrame) {
        setPreferredSize(new Dimension(Constants.boardWight, Constants.boardHeight));

        partPanel.addKeyListener(new MainKeyListener(this));

        MainMouseListener mainMouseListener = new MainMouseListener(partPanel);
        MainMouseListener panel1MouseListener = new MainMouseListener(panel1);
        MainMouseListener panel2MouseListener = new MainMouseListener(panel2);

        partPanel.addMouseListener(mainMouseListener);
        partPanel.addMouseMotionListener(mainMouseListener);
        panel2.addMouseListener(panel2MouseListener);
        panel2.addMouseMotionListener(panel2MouseListener);
        panel1.addMouseListener(panel1MouseListener);
        panel1.addMouseMotionListener(panel1MouseListener);

        setFocusable(true);
        setName("partMainPanel");

        this.mainFrame = mainFrame;
        initializeVariables();
    }
    /**
     * Creates all buttons (label,actions), spring layout, creates array of particles.
     * sets background image and initializes timer
     */
    private void initializeVariables() {
        Button swapToCirculants = new Button("swapToCirculants");
        Button start = new Button("start");
        Button stop = new Button("stop");
        Button doOneStep = new Button("doOneStep");
        Button calculateSpectrum = new Button("spectrum");
        Button countSimulation = new Button("spectrum count");
        Button switchFourierMode = new Button("switch");
        JTextField wakeField = new JTextField(""+wake);

        swapToCirculants.addActionListener(e -> { this.timer.stop();this.mainFrame.swapToCirculants();});
        start.addActionListener(e -> this.timer.start());
        stop.addActionListener(e -> this.timer.stop());
        doOneStep.addActionListener(e -> doOneLoop());
        calculateSpectrum.addActionListener(e -> calculateSpectra());
        countSimulation.addActionListener(e -> countSpectra());
        switchFourierMode.addActionListener(e -> switchFourierMode());
        wakeField.addActionListener(e -> this.wake = Double.parseDouble(wakeField.getText()));

        showX.add("x"); showX.add("px c/wb"); showX.add("z"); showX.add("d eta c/wb");
        showY.add("x"); showY.add("px c/wb"); showY.add("z"); showY.add("d eta c/wb");
        showX.addItemListener(e -> { for (Particle part: particles) {part.setXAxe(showX.getSelectedItem());} partPanel.setXAxe(showX.getSelectedItem()); repaintParticles(); repaint();});
        showY.addItemListener(e -> { for (Particle part: particles) {part.setYAxe(showY.getSelectedItem());} partPanel.setYAxe(showY.getSelectedItem()); repaintParticles(); repaint();});
        showX.select("z");
        showY.select("x");
        partPanel.setXAxe(showX.getSelectedItem());
        partPanel.setYAxe(showY.getSelectedItem());

        label.setText("");

        JLabel wakeLabel = new JLabel("wake:");

        SpringLayout layout = new SpringLayout();

        layout.putConstraint(SpringLayout.EAST, partPanel, 0, SpringLayout.EAST, this);
        layout.putConstraint(SpringLayout.SOUTH, partPanel, 0, SpringLayout.SOUTH, this);
        layout.putConstraint(SpringLayout.EAST, panel1, -5, SpringLayout.EAST, this);
        layout.putConstraint(SpringLayout.NORTH, panel1, -5, SpringLayout.NORTH, this);
        layout.putConstraint(SpringLayout.EAST, panel2, -5, SpringLayout.EAST, this);
        layout.putConstraint(SpringLayout.SOUTH, panel2, -5, SpringLayout.SOUTH, this);

        layout.putConstraint(SpringLayout.WEST, start, 5, SpringLayout.WEST, this);
        layout.putConstraint(SpringLayout.NORTH, start, 5, SpringLayout.NORTH, this);
        layout.putConstraint(SpringLayout.WEST, stop, 5, SpringLayout.EAST, start);
        layout.putConstraint(SpringLayout.NORTH, stop, 5, SpringLayout.NORTH, this);
        layout.putConstraint(SpringLayout.WEST, doOneStep, 5, SpringLayout.EAST, stop);
        layout.putConstraint(SpringLayout.NORTH, doOneStep, 5, SpringLayout.NORTH, this);
        layout.putConstraint(SpringLayout.WEST, calculateSpectrum, 5, SpringLayout.EAST, doOneStep);
        layout.putConstraint(SpringLayout.NORTH, calculateSpectrum, 5, SpringLayout.NORTH, this);
        layout.putConstraint(SpringLayout.WEST, countSimulation, 15, SpringLayout.EAST, label);
        layout.putConstraint(SpringLayout.NORTH, countSimulation, 5, SpringLayout.NORTH, this);

        layout.putConstraint(SpringLayout.WEST, label, 5, SpringLayout.EAST, calculateSpectrum);
        layout.putConstraint(SpringLayout.NORTH, label, 5, SpringLayout.NORTH, this);

        layout.putConstraint(SpringLayout.WEST, showX, 5, SpringLayout.WEST, this);
        layout.putConstraint(SpringLayout.SOUTH, showX, -5, SpringLayout.SOUTH, this);
        layout.putConstraint(SpringLayout.WEST, showY, 5, SpringLayout.EAST, showX);
        layout.putConstraint(SpringLayout.SOUTH, showY, -5, SpringLayout.SOUTH, this);

        layout.putConstraint(SpringLayout.WEST, swapToCirculants, 5, SpringLayout.WEST, this);
        layout.putConstraint(SpringLayout.NORTH, swapToCirculants, 5, SpringLayout.SOUTH, start);

        layout.putConstraint(SpringLayout.WEST, switchFourierMode, 5, SpringLayout.EAST, countSimulation);
        layout.putConstraint(SpringLayout.NORTH, switchFourierMode, 5, SpringLayout.NORTH, this);

        layout.putConstraint(SpringLayout.WEST, wakeLabel, 5, SpringLayout.EAST, swapToCirculants);
        layout.putConstraint(SpringLayout.NORTH, wakeLabel, 5, SpringLayout.SOUTH, start);
        layout.putConstraint(SpringLayout.WEST, wakeField, 5, SpringLayout.EAST, wakeLabel);
        layout.putConstraint(SpringLayout.NORTH, wakeField, 5, SpringLayout.SOUTH, start);

        layout.putConstraint(SpringLayout.NORTH, partPanel.resetScales, 5, SpringLayout.SOUTH, swapToCirculants);
        layout.putConstraint(SpringLayout.WEST, partPanel.resetScales, 5, SpringLayout.WEST, this);
        layout.putConstraint(SpringLayout.NORTH, fpsLabel, 5, SpringLayout.SOUTH, partPanel.resetScales);
        layout.putConstraint(SpringLayout.WEST, fpsLabel, 5, SpringLayout.WEST, this);

        setLayout(layout);
        partPanel.setLayout(layout);

        add(swapToCirculants);
        add(start);
        add(stop);
        add(doOneStep);
        add(calculateSpectrum);
        add(countSimulation);
        partPanel.add(showX);
        partPanel.add(showY);
        add(switchFourierMode);
        partPanel.add(wakeLabel);
        add(wakeField);
        partPanel.add(label);
        partPanel.add(panel1);
        partPanel.add(panel2);
        partPanel.add(fpsLabel);
        add(partPanel);

        this.particles = new Particle[Constants.numberOfParticles];
        for (int i = 0; i < Constants.numberOfParticles; i++) {
            particles[i] = new Particle(10 * Math.cos(Math.PI * 2 * i / Constants.numberOfParticles), 10 * Math.sin(Math.PI * 2 * i / Constants.numberOfParticles)*Constants.xFreq+Constants.Zx, 10 * Math.cos(Math.PI * 2 * i / Constants.numberOfParticles), 10 * Math.sin(Math.PI * 2 * i / Constants.numberOfParticles)*Constants.zFreq/Constants.eta, this);
        }

        this.timer = new Timer(Constants.updateSpeed, e -> doOneLoop());

        panel1.setScaleX(0.02);
        panel1.setScaleY(0.05);

        panel2.setPreferredSize(new Dimension(400, 300));
        panel2.setScaleX(1);
        panel2.setScaleY(0.04);
        panel2.setShiftX(-0.5);
        panel2.setShiftY(0.3);
        partPanel.setScaleX(0.01);
        partPanel.setScaleY(0.01);
        partPanel.setCanBeJoined(false);
        partPanel.setIsCentred(true);
        partPanel.setPreferredSize(new Dimension(Constants.boardWight, Constants.boardHeight));
        partPanel.setBackground(Color.WHITE);

        Arrays.sort(particles, Comparator.comparingDouble(a -> a.z));
        dipoleMoms[0] = particles[0].x1/Constants.numberOfParticles;
        for (int j = 1; j < Constants.numberOfParticles; j++) {
            dipoleMoms[j] = dipoleMoms[j-1] + particles[j].x1/Constants.numberOfParticles;
        }
        drawInfo();
        repaint();

        try {
            FileWriter writer = new FileWriter("out/graph.txt");
            writer.write(""+wake);
            writer.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public void doOneLoop() {
        update();
        repaint();
    }
    /**
     * Writes dipole moment of beam to list, fills graph panel1 with information about particles,
     * make one iteration (one revolution around accelerator)
     */
    private void update() {
        iteration();
        drawInfo();
        fpsLabel.setText(""+getFPS(fpsOldTime));
    }

    private void iteration(){
        for(int k = 0; k<Constants.numberOfw0ForUpdate; k++) {
            //adding pickUp history
            pickUpD.add(dipoleFull());
            pickUpD.add(0.0);

            for (int i = 0; i < (int) ((2 * Math.PI) / Constants.timeStep); i++) {
                Arrays.sort(particles, Comparator.comparingDouble(a -> a.z));
                dipoleMom = 0;
                particles[0].move();
                dipoleMoms[0] = particles[0].x1/Constants.numberOfParticles;
                for (int j = 1; j < Constants.numberOfParticles; j++) {
                    dipoleMoms[j] = dipoleMoms[j-1] + particles[j].x1/Constants.numberOfParticles;
                    dipoleMom = dipoleMoms[j-1] + 0.5*particles[j].x1/Constants.numberOfParticles;
                    particles[j].move();
                }
            }
        }
    }

    private void drawInfo(){
        ArrayList<ArrayList<Double>> graphY = new ArrayList<>();
        for (int j = 0; j < Constants.numberOfParticles; j++) {
            graphY.add(new ArrayList<>());
            graphY.get(j).add(particles[j].x);
            graphY.get(j).add(particles[j].y);
        }
        partPanel.fillGraph(graphY);

        graphY = new ArrayList<>();
        //filling graph panel1 with information about particles (now it is wake_forces/Constants.wake)
        for (int j = 0; j < Constants.numberOfParticles; j++) {
            graphY.add(new ArrayList<>());
            graphY.get(j).add(particles[j].z);
            graphY.get(j).add(dipoleMoms[j]);
        }
        panel1.fillGraph(graphY);
    }

    int getFPS(double oldTime) {
        double newTime = System.currentTimeMillis();
        double delta = newTime - oldTime;

        int fps = (int)(1.0 / (delta/1000));
        fpsOldTime = newTime;

        return fps;
    }
    /**
     * returns full dipole moment of beam
     */
    private double dipoleFull() {
        double sum = 0;
        for (Particle part : particles) {
            sum += part.x1;
        }
        return sum / Constants.numberOfParticles;
    }

    private void repaintParticles(){
        for (Particle part : particles) {
            part.repaint();
        }
    }
    /**
     * is needed to transfer collective information to single particle equations of motion
     */
    public double getDipoleMom() {
        return dipoleMom;
    }
    /**
     * Calculates spectra (Re, Im and abs), filling graph panel2 with fourierAbs, write freq in label
     */
    private void calculateSpectra(){
        int size2=pickUpD.size();
        int size=size2/2;
        FFT fourierAnalysis = new FFT();
        double[] fourier = new double[size2];
        double[] pickUpW1 = new double[size];
        //double[] fourierRe = new double[size2];
        //double[] fourierIm = new double[size2];
        double[] fourierAbs = new double[size2];
        for (int k = 0; k < size2; k++) {
            fourier[k] = pickUpD.get(k);
        }
        for (int k = 0; k < size; k++) {
            pickUpW1[k] = (k+0.0)/(size);
            fourier[k]*=Math.pow(Math.sin(k*Math.PI/(size-1)),2);
        }
        fourier = fourierAnalysis.transform(fourier);
        double max =  1;
        double wMax = 0;
        for (int k = 0; k < size2; k++) {
            if(k%2==0){
                //fourierRe[k/2]=fourier[k];
            } else {
                int n=(k-1)/2;
                //fourierIm[n]=fourier[k];
                fourierAbs[n]=Math.sqrt(fourier[k]*fourier[k]+fourier[k-1]*fourier[k-1]);
                if(fourierAbs[n]>max){
                    max=fourierAbs[n];
                    wMax=(n+1.0)/size;
                }
            }
        }
        ArrayList<ArrayList<Double>> graphY = new ArrayList<>();
        for (int k = 0; k < size; k++) {
            if(fourierMode==1) fourierAbs[k]=Math.log(fourierAbs[k]/max);
            else fourierAbs[k]=fourierAbs[k]/max;
            graphY.add(new ArrayList<>());
            graphY.get(k).add(pickUpW1[k]);
            graphY.get(k).add(fourierAbs[k]);
        }
        panel2.fillGraph(graphY);
        String str = String.format("%.3f%n",wMax);
        label.setText("wx = "+str);
        repaint();
    }

    private void countSpectra(){
        for(int p=0;p<50;p++) {
            for (int k = 0; k < 100; k++) {
                iteration();
            }
            System.out.println(""+(p+1)+" iterations passed");
        }
        calculateSpectra();
        System.out.println("done!");
    }

    private void switchFourierMode(){
        if(fourierMode==1) fourierMode=0;
        else fourierMode=1;
    }

    public double getWake(){
        return wake;
    }
}

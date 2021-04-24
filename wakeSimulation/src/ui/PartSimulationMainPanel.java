package ui;

import company.Constants;
import company.MainKeyListener;
import company.MainMouseListener;
import objects.PartPair;
import objects.Particle;
import org.jblas.util.Random;
import org.opensourcephysics.numerics.FFT;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.Math;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.util.*;

/**
 * Panel for 1st operating mode of program. Fill whole field of application.
 * Sets array of particles with air-bag distribution in longitudinal and transverse dimensions.
 */
public class PartSimulationMainPanel extends JPanel {
    private Timer timer;

    private final JLabel label = new JLabel();
    private final JLabel fpsLabel = new JLabel("");
    private final GraphPanel partPanel = new GraphPanel("partPanel");
    private final GraphPanel partPanel1 = new GraphPanel("partPanel1");
    private final GraphPanel panel1 = new GraphPanel("panel1");
    private final GraphPanel panel2 = new GraphPanel("panel2");
    private final MainFrame mainFrame;

    private Particle[] particles1;
    private Particle[] particles2;

    private final double[] dipoleMomsX = new double[Constants.numberOfParticles];
    private final double[] dipoleMomsY = new double[Constants.numberOfParticles];
    private double dipoleMomX = 0;
    private double dipoleMomY = 0;
    private final Choice showX = new Choice();
    private final Choice showY = new Choice();
    private String xAxis;
    private String yAxis;

    private final ArrayList<Double> pickUpD= new ArrayList<>();
    private double wake=0.000;

    private int fourierMode = 0;
    private double fpsOldTime = System.currentTimeMillis();

    private PartPair[] pairs;
    private int pairIndex = 0;
    private Particle highlightedParticle;
    private Particle highlightedParticle1;

    /**
     * Sets panel size, listeners, reference to main panel to change modes of program.
     * Calls initializeVariables()
     */
    public PartSimulationMainPanel(MainFrame mainFrame) {
        setPreferredSize(new Dimension(Constants.boardWight, Constants.boardHeight));

        partPanel.addKeyListener(new MainKeyListener(this));
        partPanel1.addKeyListener(new MainKeyListener(this));

        MainMouseListener mainMouseListener = new MainMouseListener(partPanel);
        MainMouseListener mainMouseListener1 = new MainMouseListener(partPanel1);
        MainMouseListener panel1MouseListener = new MainMouseListener(panel1);
        MainMouseListener panel2MouseListener = new MainMouseListener(panel2);

        partPanel.addMouseListener(mainMouseListener);
        partPanel.addMouseMotionListener(mainMouseListener);
        partPanel1.addMouseListener(mainMouseListener1);
        partPanel1.addMouseMotionListener(mainMouseListener1);
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

        showX.add("x"); showX.add("px c/wb"); showX.add("z"); showX.add("d eta c/ws");
        showY.add("x"); showY.add("px c/wb"); showY.add("z"); showY.add("d eta c/ws");
        showX.addItemListener(e -> { for (Particle part: particles1) {part.setXAxe(showX.getSelectedItem());}
                    for (Particle part: particles2) {part.setXAxe(showX.getSelectedItem());}
                    partPanel.setXAxe(showX.getSelectedItem());
                    partPanel1.setXAxe(showX.getSelectedItem());
                    changeAxes(); drawInfo();
                    repaint();});
        showY.addItemListener(e -> { for (Particle part: particles1) {part.setYAxe(showY.getSelectedItem());}
                    for (Particle part: particles2) {part.setXAxe(showY.getSelectedItem());}
                    partPanel.setYAxe(showY.getSelectedItem());
                    partPanel1.setYAxe(showY.getSelectedItem());
                    changeAxes(); drawInfo();
                    repaint();});
        showX.select("z");
        showY.select("x");
        partPanel.setXAxe(showX.getSelectedItem());
        partPanel.setYAxe(showY.getSelectedItem());
        partPanel1.setXAxe(showX.getSelectedItem());
        partPanel1.setYAxe(showY.getSelectedItem());

        label.setText("");

        JLabel wakeLabel = new JLabel("wake:");

        SpringLayout layoutMain = new SpringLayout();
        SpringLayout layoutParticles = new SpringLayout();
        SpringLayout layoutGraphs = new SpringLayout();

        layoutMain.putConstraint(SpringLayout.WEST, partPanel1, 0, SpringLayout.WEST, this);
        layoutMain.putConstraint(SpringLayout.VERTICAL_CENTER, partPanel1, 0, SpringLayout.VERTICAL_CENTER, this);
        layoutMain.putConstraint(SpringLayout.EAST, partPanel, 0, SpringLayout.EAST, this);
        layoutMain.putConstraint(SpringLayout.VERTICAL_CENTER, partPanel, 0, SpringLayout.VERTICAL_CENTER, this);
        layoutParticles.putConstraint(SpringLayout.EAST, panel1, -5, SpringLayout.EAST, partPanel);
        layoutParticles.putConstraint(SpringLayout.NORTH, panel1, -5, SpringLayout.NORTH, partPanel);
        layoutParticles.putConstraint(SpringLayout.EAST, panel2, -5, SpringLayout.EAST, partPanel);
        layoutParticles.putConstraint(SpringLayout.SOUTH, panel2, -5, SpringLayout.SOUTH, partPanel);

        layoutMain.putConstraint(SpringLayout.WEST, start, 5, SpringLayout.WEST, this);
        layoutMain.putConstraint(SpringLayout.NORTH, start, 5, SpringLayout.NORTH, this);
        layoutMain.putConstraint(SpringLayout.WEST, stop, 5, SpringLayout.EAST, start);
        layoutMain.putConstraint(SpringLayout.NORTH, stop, 5, SpringLayout.NORTH, this);
        layoutMain.putConstraint(SpringLayout.WEST, doOneStep, 5, SpringLayout.EAST, stop);
        layoutMain.putConstraint(SpringLayout.NORTH, doOneStep, 5, SpringLayout.NORTH, this);
        layoutMain.putConstraint(SpringLayout.WEST, calculateSpectrum, 105, SpringLayout.EAST, doOneStep);
        layoutMain.putConstraint(SpringLayout.NORTH, calculateSpectrum, 5, SpringLayout.NORTH, this);
        layoutMain.putConstraint(SpringLayout.WEST, countSimulation, 365, SpringLayout.WEST, this);
        layoutMain.putConstraint(SpringLayout.NORTH, countSimulation, 5, SpringLayout.NORTH, this);

        layoutParticles.putConstraint(SpringLayout.WEST, label, 300, SpringLayout.WEST, partPanel1);
        layoutParticles.putConstraint(SpringLayout.NORTH, label, 55, SpringLayout.NORTH, partPanel1);

        layoutMain.putConstraint(SpringLayout.WEST, showX, 5, SpringLayout.WEST, this);
        layoutMain.putConstraint(SpringLayout.SOUTH, showX, -5, SpringLayout.SOUTH, this);
        layoutMain.putConstraint(SpringLayout.WEST, showY, 5, SpringLayout.EAST, showX);
        layoutMain.putConstraint(SpringLayout.SOUTH, showY, -5, SpringLayout.SOUTH, this);

        layoutMain.putConstraint(SpringLayout.WEST, swapToCirculants, 5, SpringLayout.WEST, this);
        layoutMain.putConstraint(SpringLayout.NORTH, swapToCirculants, 5, SpringLayout.SOUTH, start);

        layoutMain.putConstraint(SpringLayout.WEST, switchFourierMode, 5, SpringLayout.EAST, countSimulation);
        layoutMain.putConstraint(SpringLayout.NORTH, switchFourierMode, 5, SpringLayout.NORTH, this);

        layoutParticles.putConstraint(SpringLayout.WEST, wakeLabel, 150, SpringLayout.WEST, partPanel1);
        layoutParticles.putConstraint(SpringLayout.NORTH, wakeLabel, 35, SpringLayout.NORTH, partPanel1);
        layoutParticles.putConstraint(SpringLayout.WEST, wakeField, 200, SpringLayout.WEST, partPanel1);
        layoutParticles.putConstraint(SpringLayout.NORTH, wakeField, 35, SpringLayout.NORTH, partPanel1);

        layoutParticles.putConstraint(SpringLayout.NORTH, partPanel.resetScales, 65, SpringLayout.NORTH, partPanel);
        layoutParticles.putConstraint(SpringLayout.WEST, partPanel.resetScales, 5, SpringLayout.WEST, partPanel);
        layoutParticles.putConstraint(SpringLayout.NORTH, partPanel1.resetScales, 65, SpringLayout.NORTH, partPanel1);
        layoutParticles.putConstraint(SpringLayout.WEST, partPanel1.resetScales, 5, SpringLayout.WEST, partPanel1);
        layoutMain.putConstraint(SpringLayout.NORTH, fpsLabel, 55, SpringLayout.SOUTH, swapToCirculants);
        layoutMain.putConstraint(SpringLayout.WEST, fpsLabel, 5, SpringLayout.WEST, this);

        setLayout(layoutMain);
        partPanel.setLayout(layoutParticles);
        partPanel1.setLayout(layoutParticles);

        add(swapToCirculants);
        add(start);
        add(stop);
        add(doOneStep);
        add(calculateSpectrum);
        add(countSimulation);
        add(showX);
        add(showY);
        add(switchFourierMode);
        partPanel1.add(wakeLabel);
        partPanel1.add(wakeField);
        partPanel1.add(label);
        partPanel.add(panel1);
        partPanel.add(panel2);
        add(fpsLabel);
        add(partPanel);
        add(partPanel1);

        this.particles1 = new Particle[Constants.numberOfParticles];
        for (int i = 0; i < Constants.numberOfParticles; i++) {
            double xRand = 10*Random.nextGaussian();
            double pxRand = 10*Random.nextGaussian();
            //pxRand = 0;
            double yRand = 10*Random.nextGaussian();
            double pyRand = 10*Random.nextGaussian();
            particles1[i] = new Particle(xRand, pxRand*Constants.xFreq+Constants.Zx, 10 * Math.cos(Math.PI * 2 * i / Constants.numberOfParticles), 10 * Math.sin(Math.PI * 2 * i / Constants.numberOfParticles)*Constants.zFreq/Constants.eta, yRand, pyRand*Constants.yFreq+Constants.Zy,this);
        }
        this.particles2 = new Particle[Constants.numberOfParticles];
        for (int i = 0; i < Constants.numberOfParticles; i++) {
            double xRand = 10*Random.nextGaussian();
            double pxRand = 10*Random.nextGaussian();
            //pxRand = 0;
            double yRand = 10*Random.nextGaussian();
            double pyRand = 10*Random.nextGaussian();
            particles2[i] = new Particle(xRand, pxRand*Constants.xFreq+Constants.Zx, 10 * Math.cos(Math.PI * 2 * i / Constants.numberOfParticles), 10 * Math.sin(Math.PI * 2 * i / Constants.numberOfParticles)*Constants.zFreq/Constants.eta, yRand, pyRand*Constants.yFreq+Constants.Zy,this);        }

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
        partPanel.setPreferredSize(new Dimension(Constants.boardWight/2-10, Constants.boardHeight));
        partPanel.setBackground(Color.WHITE);
        partPanel.pointSize = 3;
        partPanel1.setScaleX(0.01);
        partPanel1.setScaleY(0.01);
        partPanel1.setCanBeJoined(false);
        partPanel1.setIsCentred(true);
        partPanel1.setPreferredSize(new Dimension(Constants.boardWight/2-10, Constants.boardHeight));
        partPanel1.setBackground(Color.WHITE);
        partPanel1.pointSize = 3;
        panel1.setPreferredSize(new Dimension(200,100));
        panel2.setPreferredSize(new Dimension(200,100));

        Arrays.sort(particles1, Comparator.comparingDouble(a -> a.z));
        dipoleMomsX[0] = particles1[0].x1/Constants.numberOfParticles;
        for (int j = 1; j < Constants.numberOfParticles; j++) {
            dipoleMomsX[j] = dipoleMomsX[j-1] + particles1[j].x1/Constants.numberOfParticles;
        }

        prepareToBeamBeam();
        drawInfo();
        System.out.println(particles1[0].x1);
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
        iterationSimplified();
        //iteration();
        //iterationBeam();
        drawInfo();
        fpsLabel.setText(""+getFPS(fpsOldTime));
    }

    private void iteration(){
        System.out.println("iteration");
        for(int k = 0; k<Constants.numberOfw0ForUpdate; k++) {
            //adding pickUp history
            pickUpD.add(dipoleFull());
            pickUpD.add(0.0);

            for (int i = 0; i < (int) ((2 * Math.PI) / Constants.timeStep); i++) {
                Arrays.sort(particles1, Comparator.comparingDouble(a -> a.z));
                dipoleMomX = 0;
                dipoleMomY = 0;
                particles1[0].move();
                dipoleMomsX[0] = particles1[0].x1/Constants.numberOfParticles;
                dipoleMomsY[0] = particles1[0].y1/Constants.numberOfParticles;
                for (int j = 1; j < Constants.numberOfParticles; j++) {
                    dipoleMomsX[j] = dipoleMomsX[j-1] + particles1[j].x1/Constants.numberOfParticles;
                    dipoleMomX = dipoleMomsX[j-1] + 0.5* particles1[j].x1/Constants.numberOfParticles;
                    dipoleMomsY[j] = dipoleMomsY[j-1] + particles1[j].y1/Constants.numberOfParticles;
                    dipoleMomY = dipoleMomsY[j-1] + 0.5* particles1[j].y1/Constants.numberOfParticles;
                    particles1[j].move();
                }
            }
        }
    }

    private void iterationSimplified(){
        //System.out.println("simplified");
        for(int k = 0; k<Constants.numberOfw0ForUpdate; k++) {
            //adding pickUp history
            pickUpD.add(dipoleFull());
            pickUpD.add(0.0);

            transportBeam(particles1);
            transportBeam(particles2);

            beamBeam();

            changeAxes();
        }
    }

    private void iterationBeam(){
        //System.out.println("simplified");
        for(int k = 0; k<Constants.numberOfw0ForUpdate; k++) {
            //adding pickUp history
            pickUpD.add(dipoleFull());
            pickUpD.add(0.0);

            beamBeamStep(pairs[pairIndex]);

            pairIndex++;
            if(pairIndex==Constants.numberOfParticles*Constants.numberOfParticles){
                pairIndex=0;
                prepareToBeamBeam();
            }
            highlightedParticle = pairs[pairIndex].p1;
            highlightedParticle1 = pairs[pairIndex].p2;

            highlightedParticle = pairs[0].p1;
            highlightedParticle1 = pairs[0].p2;

            changeAxes();
        }
    }

    private void changeAxes(){
        xAxis = showX.getSelectedItem();
        yAxis = showY.getSelectedItem();
        //change axes to draw
        for (int j = 0; j < Constants.numberOfParticles; j++) {
            switch (xAxis) {
                case "x" -> {
                    particles1[j].x = (particles1[j].x1);
                    particles2[j].x = (particles2[j].x1);
                }
                case "px c/wb" -> {
                    particles1[j].x = (particles1[j].px1/Constants.xFreq);
                    particles2[j].x = (particles2[j].px1/Constants.xFreq);
                }
                case "d eta c/ws" -> {
                    particles1[j].x = (particles1[j].d*Constants.eta/Constants.zFreq);
                    particles2[j].x = (particles2[j].d*Constants.eta/Constants.zFreq);
                }
                default -> {
                    particles1[j].x =(particles1[j].z);
                    particles2[j].x =(particles2[j].z);
                }
            }
            switch (yAxis) {
                case "z" -> {
                    particles1[j].y =(particles1[j].z);
                    particles2[j].y =(particles2[j].z);
                }
                case "px c/wb" -> {
                    particles1[j].y = (particles1[j].px1/Constants.xFreq);
                    particles2[j].y = (particles2[j].px1/Constants.xFreq);
                }
                case "d eta c/ws" -> {
                    particles1[j].y = (particles1[j].d*Constants.eta/Constants.zFreq);
                    particles2[j].y = (particles2[j].d*Constants.eta/Constants.zFreq);
                }
                default -> {
                    particles1[j].y = (particles1[j].x1);
                    particles2[j].y = (particles2[j].x1);
                }
            }
        }
    }

    private void transportBeam(Particle[] ps){
        Arrays.sort(ps, Comparator.comparingDouble(a -> a.z));
        dipoleMomsX[0]= ps[0].x1/Constants.numberOfParticles;
        dipoleMomsY[0]= ps[0].y1/Constants.numberOfParticles;
        for (int j = 1; j < Constants.numberOfParticles; j++){
            dipoleMomsX[j] = dipoleMomsX[j-1] + ps[j].x1/Constants.numberOfParticles;
            dipoleMomsY[j] = dipoleMomsY[j-1] + ps[j].y1/Constants.numberOfParticles;
        }

        for (int j = 0; j < Constants.numberOfParticles; j++) {
            dipoleMomX = dipoleMomsX[j] - 0.5 * ps[j].x1 / Constants.numberOfParticles;
            dipoleMomY = dipoleMomsY[j] - 0.5 * ps[j].y1 / Constants.numberOfParticles;
            wakeKick(ps[j]);
        }

        for (int j = 0; j < Constants.numberOfParticles; j++) {
            periodTransfer(ps[j]);
        }
    }

    private void wakeKick(Particle p){
        p.px1+=dipoleMomX *wake;
        p.py1+=dipoleMomY *wake;
    }

    private void periodTransfer(Particle p){
        double x1mid;
        double px1mid;
        double y1mid;
        double py1mid;
        double zMid;
        double dMid;
        x1mid = p.x1*Math.cos(2*Math.PI*Constants.xFreq)+p.px1*Math.sin(2*Math.PI*Constants.xFreq)/Constants.xFreq;
        px1mid = -p.x1*Math.sin(2*Math.PI*Constants.xFreq)*Constants.xFreq+p.px1*Math.cos(2*Math.PI*Constants.xFreq);
        y1mid = p.y1*Math.cos(2*Math.PI*Constants.yFreq)+p.py1*Math.sin(2*Math.PI*Constants.yFreq)/Constants.yFreq;
        py1mid = -p.y1*Math.sin(2*Math.PI*Constants.yFreq)*Constants.yFreq+p.py1*Math.cos(2*Math.PI*Constants.yFreq);
        zMid = p.z*Math.cos(2*Math.PI*Constants.zFreq)-p.d*Math.sin(2*Math.PI*Constants.zFreq)*Constants.eta/Constants.zFreq;
        dMid = p.z*Math.sin(2*Math.PI*Constants.zFreq)*Constants.zFreq/Constants.eta+p.d*Math.cos(2*Math.PI*Constants.zFreq);
        p.x1=x1mid;
        p.px1=px1mid;
        p.y1=y1mid;
        p.py1=py1mid;
        p.z=zMid;
        p.d=dMid;
    }

    private void beamBeam(){
        prepareToBeamBeam();
        for (int i = 0; i < Constants.numberOfParticles*Constants.numberOfParticles; i++) {
            beamBeamStep(pairs[i]);
        }
    }

    public void prepareToBeamBeam(){
        pairs = new PartPair[Constants.numberOfParticles*Constants.numberOfParticles];
        for (int i = 0; i < Constants.numberOfParticles; i++) {
            for (int j = 0; j < Constants.numberOfParticles; j++) {
                pairs[Constants.numberOfParticles*i+j] = new PartPair(particles1[i],particles2[j]);
            }
        }
        Arrays.sort(pairs, Comparator.comparingDouble(a -> (a.distance)));
        Collections.reverse(Arrays.asList(pairs));
    }

    private void beamBeamStep(PartPair pp) {
        pp.p1.x1 -= pp.p1.px1 * pp.distance / 2;
        pp.p2.x1 -= pp.p2.px1 * pp.distance / 2;
        pp.p1.y1 -= pp.p1.py1 * pp.distance / 2;
        pp.p2.y1 -= pp.p2.py1 * pp.distance / 2;
        double xDiffer = pp.p1.x1-pp.p2.x1;
        double yDiffer = pp.p1.y1-pp.p2.y1;
        //count beam beam p's deflections;
        pp.p1.px1 -= Constants.beamForce * xDiffer / Constants.numberOfParticles;
        pp.p1.py1 -= Constants.beamForce * yDiffer / Constants.numberOfParticles;
        pp.p2.px1 += Constants.beamForce * xDiffer / Constants.numberOfParticles;
        pp.p2.py1 += Constants.beamForce * yDiffer / Constants.numberOfParticles;
        //return to initial
        pp.p1.x1 += pp.p1.px1 * pp.distance / 2;
        pp.p2.x1 += pp.p2.px1 * pp.distance / 2;
        pp.p1.y1 += pp.p1.py1 * pp.distance / 2;
        pp.p2.y1 += pp.p2.py1 * pp.distance / 2;
    }

    private void drawInfo(){
        ArrayList<ArrayList<Double>> graphY = new ArrayList<>();
        for (int j = 0; j < Constants.numberOfParticles; j++) {
            if(particles1[j]==highlightedParticle) partPanel.highlightedPoint = j;
            graphY.add(new ArrayList<>());
            graphY.get(j).add(particles1[j].x);
            graphY.get(j).add(particles1[j].y);
        }
        //System.out.println(graphY.toString());
        partPanel.fillGraph(graphY);

        graphY = new ArrayList<>();
        for (int j = 0; j < Constants.numberOfParticles; j++) {
            if(particles2[j]==highlightedParticle1) partPanel1.highlightedPoint = j;
            graphY.add(new ArrayList<>());
            graphY.get(j).add(particles2[j].x);
            graphY.get(j).add(particles2[j].y);
        }
        partPanel1.fillGraph(graphY);

        graphY = new ArrayList<>();
        //filling graph panel1 with information about particles (now it is wake_forces/Constants.wake)
        for (int j = 0; j < Constants.numberOfParticles; j++) {
            graphY.add(new ArrayList<>());
            graphY.get(j).add(particles1[j].z);
            graphY.get(j).add(dipoleMomsX[j]);
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
        for (Particle part : particles1) {
            sum += part.x1;
        }
        return sum / Constants.numberOfParticles;
    }

    private void repaintParticles(){
        for (Particle part : particles1) {
            part.repaint();
        }
    }
    /**
     * is needed to transfer collective information to single particle equations of motion
     */
    public double getDipoleMomX() {
        return dipoleMomX;
    }
    public double getDipoleMomY() {
        return dipoleMomY;
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

    public void countSpectra(){
        xAxis = showX.getSelectedItem();
        yAxis = showY.getSelectedItem();
        for(int p=0;p<50;p++) {
            for (int k = 0; k < 100; k++) {
                iterationSimplified();
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

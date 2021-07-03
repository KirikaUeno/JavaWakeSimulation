package ui;

import company.Config;
import company.MainKeyListener;
import company.MainMouseListener;
import company.MousePointer;
import simulation.SimulationFlow;
import simulation.SpectrumHandling;

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

    private final Choice showX = new Choice();
    private final Choice showY = new Choice();

    private double fpsOldTime = System.currentTimeMillis();

    /**
     * Sets panel size, listeners, reference to main panel to change modes of program.
     * Calls initializeVariables()
     */
    public PartSimulationMainPanel(MainFrame mainFrame) {
        setPreferredSize(new Dimension(Config.boardWight, Config.boardHeight));

        partPanel.addKeyListener(new MainKeyListener(this));
        partPanel1.addKeyListener(new MainKeyListener(this));

        MainMouseListener mainMouseListener = new MainMouseListener(partPanel);
        MainMouseListener mainMouseListener1 = new MainMouseListener(partPanel1);
        MainMouseListener panel1MouseListener = new MainMouseListener(panel1);
        MainMouseListener panel2MouseListener = new MainMouseListener(panel2);
        MousePointer lineAdder = new MousePointer(panel2);

        partPanel.addMouseListener(mainMouseListener);
        partPanel.addMouseMotionListener(mainMouseListener);
        partPanel1.addMouseListener(mainMouseListener1);
        partPanel1.addMouseMotionListener(mainMouseListener1);
        panel2.addMouseListener(panel2MouseListener);
        panel2.addMouseMotionListener(panel2MouseListener);
        panel1.addMouseListener(panel1MouseListener);
        panel1.addMouseMotionListener(panel1MouseListener);
        panel2.addMouseListener(lineAdder);

        setFocusable(true);
        setName("partMainPanel");

        this.mainFrame = mainFrame;
        initializeVariables();
        SimulationFlow.toInitialParameters();
        drawInfo();
        repaint();
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
        Button addIntensity = new Button("+intensity");
        JTextField wakeField = new JTextField(""+ Config.wake);

        swapToCirculants.addActionListener(e -> { this.timer.stop();this.mainFrame.swapToCirculants();});
        start.addActionListener(e -> this.timer.start());
        stop.addActionListener(e -> this.timer.stop());
        doOneStep.addActionListener(e -> doOneLoop());
        calculateSpectrum.addActionListener(e -> calculateSpectrum());
        if(Config.countOnlyOneIntensity) countSimulation.addActionListener(e -> findFreqs());
        else countSimulation.addActionListener(e -> SimulationFlow.countFreqByIntensity());
        switchFourierMode.addActionListener(e -> SpectrumHandling.switchFourierMode());
        wakeField.addActionListener(e -> Config.wake = Double.parseDouble(wakeField.getText()));
        addIntensity.addActionListener(e -> {SimulationFlow.toInitialParameters(); Config.intensity+=1.0/5000; System.out.println("intensity now: "+Config.intensity);});

        showX.add("x"); showX.add("px"); showX.add("z"); showX.add("d");
        showY.add("x"); showY.add("px"); showY.add("z"); showY.add("d");
        showX.addItemListener(e -> {
            partPanel.setXAxe(showX.getSelectedItem());
            partPanel1.setXAxe(showX.getSelectedItem());
            drawInfo();});
        showY.addItemListener(e -> {
            partPanel.setYAxe(showY.getSelectedItem());
            partPanel1.setYAxe(showY.getSelectedItem());
            drawInfo();});
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
        layoutMain.putConstraint(SpringLayout.WEST, addIntensity, 365, SpringLayout.WEST, this);
        layoutMain.putConstraint(SpringLayout.NORTH, addIntensity, 55, SpringLayout.NORTH, this);

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
        add(addIntensity);
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

        this.timer = new Timer(Config.updateSpeed, e -> doOneLoop());

        panel1.setScaleX(0.02);
        panel1.setScaleY(0.05);
        panel1.pointSize = 4;

        panel2.setPreferredSize(new Dimension(400, 300));
        panel2.setScaleX(1);
        panel2.setScaleY(0.04);
        panel2.setShiftX(-0.5);
        panel2.setShiftY(0.3);
        partPanel.setScaleX(0.1);
        partPanel.setScaleY(0.1);
        partPanel.setCanBeJoined(false);
        partPanel.setIsCentred(true);
        partPanel.setPreferredSize(new Dimension(Config.boardWight/2-10, Config.boardHeight));
        partPanel.setBackground(Color.WHITE);
        partPanel.pointSize = 3;
        partPanel1.setScaleX(0.1);
        partPanel1.setScaleY(0.1);
        partPanel1.setCanBeJoined(false);
        partPanel1.setIsCentred(true);
        partPanel1.setPreferredSize(new Dimension(Config.boardWight/2-10, Config.boardHeight));
        partPanel1.setBackground(Color.WHITE);
        partPanel1.pointSize = 3;
        panel1.setPreferredSize(new Dimension(200,100));
        panel2.setPreferredSize(new Dimension(200,100));
    }

    public void doOneLoop() {
        SimulationFlow.update();
        drawInfo();
        fpsLabel.setText(""+getFPS(fpsOldTime));
    }

    private void drawInfo(){
        int[] highlightedIndices = SimulationFlow.findHighlightedIndices();
        partPanel.highlightedPoint = highlightedIndices[0];
        partPanel1.highlightedPoint = highlightedIndices[1];

        String xAxis = showX.getSelectedItem();
        String yAxis = showY.getSelectedItem();
        partPanel.fillGraph(SimulationFlow.getGraphData(1,xAxis,yAxis));

        partPanel1.fillGraph(SimulationFlow.getGraphData(2,xAxis,yAxis));

        panel1.fillGraph(SimulationFlow.getCustomGraphData());

        repaint();
    }

    int getFPS(double oldTime) {
        double newTime = System.currentTimeMillis();
        double delta = newTime - oldTime;

        int fps = (int)(1.0 / (delta/1000));
        fpsOldTime = newTime;

        return fps;
    }

    private void calculateSpectrum(){
        ArrayList<ArrayList<Double>> graphY = new ArrayList<>();
        double[] spectrum = SpectrumHandling.calculateSpectrum(SimulationFlow.getPickUpD());
        double[] pickUpW1 = new double[spectrum.length];
        for (int k = 0; k < spectrum.length; k++) {
            pickUpW1[k] = (k + 0.0) / spectrum.length;
            graphY.add(new ArrayList<>());
            graphY.get(k).add(pickUpW1[k]);
            graphY.get(k).add(spectrum[k]);
        }
        panel2.fillGraph(graphY);
        repaint();
    }

    private void findFreqs(){
        ArrayList<ArrayList<Double>> graphY = new ArrayList<>();
        double[] spectrum = SimulationFlow.countSpectrum();
        double[] pickUpW1 = new double[spectrum.length];
        for (int k = 0; k < spectrum.length; k++) {
            pickUpW1[k] = (k + 0.0) / spectrum.length;
            graphY.add(new ArrayList<>());
            graphY.get(k).add(pickUpW1[k]);
            graphY.get(k).add(spectrum[k]);
        }
        panel2.fillGraph(graphY);
        SpectrumHandling.findFreq(spectrum);
        repaint();
    }
}
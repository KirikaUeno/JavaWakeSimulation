package com.ui;

import com.company.Constants;
import com.company.MainKeyListener;
import com.company.MainMouseListener;
import com.image.*;
import com.image.Image;
import com.objects.Particle;
import org.opensourcephysics.numerics.FFT;
import java.lang.Math;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
/**
 * Panel for 1st operating mode of program. Fill whole field of application.
 * Sets array of particles with air-bag distribution in longitudinal and transverse dimensions.
 */
public class PartSimulationMainPanel extends ResizableJPanel {
    private Timer timer;

    private ImageIcon backgroundImage;

    private final JLabel label = new JLabel();
    private final GraphPanel panel1 = new GraphPanel("panel1");
    private final GraphPanel panel2 = new GraphPanel("panel2");
    private final MainFrame mainFrame;

    private Particle[] particles;

    private final double[] dipoleMoms = new double[Constants.numberOfParticles];
    private double dipoleMom = 0;
    private final Choice showX = new Choice();
    private final Choice showY = new Choice();

    private final ArrayList<Double> pickUpD= new ArrayList<>();
    private final ArrayList<Double> pickUpW = new ArrayList<>();

    private int fourierMode = 0;

    /**
     * Sets panel size, listeners, reference to main panel to change modes of program.
     * Calls initializeVariables()
     */
    public PartSimulationMainPanel(MainFrame mainFrame) {
        setPreferredSize(new Dimension(Constants.boardWight, Constants.boardHeight));
        addKeyListener(new MainKeyListener(this));
        MainMouseListener mainMouseListener = new MainMouseListener(this);
        MainMouseListener panel1MouseListener = new MainMouseListener(panel1);
        MainMouseListener panel2MouseListener = new MainMouseListener(panel2);
        addMouseListener(mainMouseListener);
        addMouseMotionListener(mainMouseListener);
        panel2.addMouseListener(panel2MouseListener);
        panel2.addMouseMotionListener(panel2MouseListener);
        panel1.addMouseListener(panel1MouseListener);
        panel1.addMouseMotionListener(panel1MouseListener);
        setFocusable(true);
        setName("partMainPanel");
        panel2.setName("panel2");
        panel1.setName("panel1");

        this.mainFrame = mainFrame;
        this.setScaleX(10);
        this.setScaleY(10);
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
        Button calculateSpectra = new Button("spectra");
        Button countSimulation = new Button("spectrum count");
        Button switchFourierMode = new Button("switch");

        swapToCirculants.addActionListener(e -> { this.timer.stop();this.mainFrame.swapToCirculants();});
        start.addActionListener(e -> this.timer.start());
        stop.addActionListener(e -> this.timer.stop());
        doOneStep.addActionListener(e -> doOneLoop());
        calculateSpectra.addActionListener(e -> calculateSpectra());
        countSimulation.addActionListener(e -> countSpectra());
        switchFourierMode.addActionListener(e -> switchFourierMode());

        showX.add("x"); showX.add("px c/wb"); showX.add("z"); showX.add("d eta c/wb");
        showY.add("x"); showY.add("px c/wb"); showY.add("z"); showY.add("d eta c/wb");
        showX.select("z");
        showY.select("x");
        showX.addItemListener(e -> { for (Particle part: particles) {part.setXAxe(showX.getSelectedItem());}repaintParticles(); repaint();});
        showY.addItemListener(e -> { for (Particle part: particles) {part.setYAxe(showY.getSelectedItem());}repaintParticles(); repaint();});

        label.setText("");

        SpringLayout layout = new SpringLayout();

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
        layout.putConstraint(SpringLayout.WEST, calculateSpectra, 5, SpringLayout.EAST, doOneStep);
        layout.putConstraint(SpringLayout.NORTH, calculateSpectra, 5, SpringLayout.NORTH, this);
        layout.putConstraint(SpringLayout.WEST, countSimulation, 15, SpringLayout.EAST, label);
        layout.putConstraint(SpringLayout.NORTH, countSimulation, 5, SpringLayout.NORTH, this);

        layout.putConstraint(SpringLayout.WEST, label, 5, SpringLayout.EAST, calculateSpectra);
        layout.putConstraint(SpringLayout.NORTH, label, 5, SpringLayout.NORTH, this);

        layout.putConstraint(SpringLayout.WEST, showX, 5, SpringLayout.WEST, this);
        layout.putConstraint(SpringLayout.SOUTH, showX, -5, SpringLayout.SOUTH, this);
        layout.putConstraint(SpringLayout.WEST, showY, 5, SpringLayout.EAST, showX);
        layout.putConstraint(SpringLayout.SOUTH, showY, -5, SpringLayout.SOUTH, this);

        layout.putConstraint(SpringLayout.WEST, swapToCirculants, 5, SpringLayout.WEST, this);
        layout.putConstraint(SpringLayout.NORTH, swapToCirculants, 5, SpringLayout.SOUTH, start);

        layout.putConstraint(SpringLayout.WEST, switchFourierMode, 5, SpringLayout.EAST, countSimulation);
        layout.putConstraint(SpringLayout.NORTH, switchFourierMode, 5, SpringLayout.NORTH, this);

        setLayout(layout);

        add(swapToCirculants);
        add(label);
        add(panel1);
        add(panel2);
        add(start);
        add(stop);
        add(doOneStep);
        add(calculateSpectra);
        add(countSimulation);
        add(showX);
        add(showY);
        add(switchFourierMode);

        this.particles = new Particle[Constants.numberOfParticles];
        for (int i = 0; i < Constants.numberOfParticles; i++) {
            particles[i] = new Particle(10 * Math.cos(Math.PI * 2 * i / Constants.numberOfParticles), 10 * Math.sin(Math.PI * 2 * i / Constants.numberOfParticles)*Constants.xFreq+Constants.Zx, 10 * Math.cos(Math.PI * 2 * i / Constants.numberOfParticles), 10 * Math.sin(Math.PI * 2 * i / Constants.numberOfParticles)*Constants.zFreq/Constants.eta, this);
        }

        this.backgroundImage = ImageFactory.createImage(Image.BACKGROUND);
        this.timer = new Timer(Constants.updateSpeed, e -> doOneLoop());

        panel1.setScaleX(0.02);
        panel1.setScaleY(0.05);

        panel2.setPreferredSize(new Dimension(500, 400));
        panel2.setScaleX(1);
        panel2.setScaleY(1);
        panel2.setShiftX(-0.5);
        panel2.setShiftY(-0.5);
        panel2.setScaleX(1);
        panel2.setScaleY(0.04);
        panel2.setShiftX(-0.5);
        panel2.setShiftY(0.3);
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.drawImage(backgroundImage.getImage(), 0, 0, null);
        super.paintComponent(g);
        doDrawing(g);
    }
    /**
     * Draw axes, net, particles, changing-scales rectangle
     */
    protected void doDrawing(Graphics g) {
        drawNet(g);

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (Particle part : particles) {
            g2d.drawLine((int) ((part.x)*scaleX+Constants.boardWight/2), (int) (-(part.y)*scaleX+Constants.boardHeight/2), (int) ((part.x)*scaleX+Constants.boardWight/2), (int) (-(part.y)*scaleX+Constants.boardHeight/2));
        }
        g2d.dispose();
    }
    /**
     * Draw axes as bold lines crossing center of panel, draw "numberOfLines" lines equally spaced,
     * occupying whole panel. Draw axes numbers
     */
    private void drawNet(Graphics g) {

        Graphics2D g2d = (Graphics2D) g.create();
        g.setFont(new Font("TimesRoman", Font.PLAIN, 10));
        String str;
        int numberOfLines = 19;
        int mid = ((numberOfLines + 1) / 2);
        for (int i = 0; i < numberOfLines; i++) {
            g.drawLine(0, (int) (0.5 * Constants.boardHeight * ((i + 1.0) / mid)), Constants.boardWight, (int) (0.5 * Constants.boardHeight * ((i + 1.0) / mid)));
            str = String.format("%.1f%n", (int) (Constants.boardHeight * (-(i-mid+1.0) / (2*mid)))/this.scaleY);
            g.drawString(str,Constants.boardWight/2-g.getFontMetrics().stringWidth(str)-2, (int) (0.5 * Constants.boardHeight * ((i + 1.0) / mid))+15);

        }
        for (int i = 0; i < numberOfLines; i++) {
            if(i!=(mid-1)) {
                g.drawLine((int) (0.5 * Constants.boardWight * ((i + 1.0) / mid)), 0, (int) (0.5 * Constants.boardWight * ((i + 1.0) / mid)), Constants.boardHeight);
                str = String.format("%.1f%n", (int) (Constants.boardWight * ((i - mid + 1.0) / (2 * mid))) / this.scaleX);
                g.drawString(str, (int) (0.5 * Constants.boardWight * ((i + 1.0) / mid)) - g.getFontMetrics().stringWidth(str), Constants.boardHeight / 2 + 15);
            }
        }
        g.setFont(new Font("TimesRoman", Font.BOLD, 14));
        g.drawString(showX.getSelectedItem(),(Constants.boardWight - g.getFontMetrics().stringWidth(showX.getSelectedItem()))-5, Constants.boardHeight / 2 + 15);
        g.drawString(showY.getSelectedItem(),(Constants.boardWight/2 - g.getFontMetrics().stringWidth(showY.getSelectedItem()))-5, 15);

        g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));


        g2d.drawLine(0, Constants.boardHeight/2, Constants.boardWight, Constants.boardHeight/2);
        g2d.drawLine(Constants.boardWight/2, 0, Constants.boardWight/2, Constants.boardHeight);

        g2d.dispose();

    }

    @Override
    public void disableRect(){
        isRectExist=false;
        if(((xf-xi)>50) && ((yf-yi)>50)){
            setScales((scaleX /((xf-xi+0.0)/Constants.boardWight)),(scaleY /((yf-yi+0.0)/Constants.boardHeight)));
        }
        if(((xf-xi)<-50) && ((yf-yi)<-50)){
            setScales((scaleX *((xi-xf+0.0)/Constants.boardWight)),(scaleY *((yi-yf+0.0)/Constants.boardHeight)));
        }
        repaint();
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
        //filling graph panel1 with information about particles (now it is wake_forces/Constants.wake)
        ArrayList<Double> graphX = new ArrayList<>();
        ArrayList<ArrayList<Double>> graphY = new ArrayList<>();
        for (int j = 0; j < Constants.numberOfParticles; j++) {
            graphX.add(particles[j].z);
            graphY.add(new ArrayList<>());
            graphY.get(j).add(dipoleMoms[j]);
        }
        panel1.fillGraph(graphX, graphY);

        //iteration
        for(int k = 0; k<Constants.numberOfw0ForUpdate; k++) {
            //adding pickUp history
            pickUpD.add(dipoleFull());
            pickUpD.add(0.0);
            pickUpW.add(0.0);

            for (int i = 0; i < (int) ((2 * Math.PI) / Constants.timeStep); i++) {
                for (int j = 0; j < Constants.numberOfParticles; j++) {
                    dipoleMoms[j] = countDipoleMom(particles[j]);
                }
                for (int j = 0; j < Constants.numberOfParticles; j++) {
                    dipoleMom = dipoleMoms[j];
                    particles[j].move();
                }
            }
        }
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
     * returns sum of particles transverse coordinate that ahead of p
     */
    private double countDipoleMom(Particle p) {
        double sum = 0;
        for (Particle part : particles) {
            if (part.z > p.z) {
                sum += part.x1;
            }
        }
        return sum / Constants.numberOfParticles;
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
        FFT fourierAnalysis = new FFT();
        double[] fourier = new double[pickUpD.size()];
        double[] pickUpW1 = new double[pickUpW.size()];
        //double[] fourierRe = new double[pickUpD.size()];
        //double[] fourierIm = new double[pickUpD.size()];
        double[] fourierAbs = new double[pickUpD.size()];
        for (int k = 0; k < pickUpD.size(); k++) {
            fourier[k] = pickUpD.get(k);
        }
        for (int k = 0; k < pickUpW.size(); k++) {
            pickUpW1[k] = (k+0.0)/(pickUpW.size());
            fourier[k]*=Math.pow(Math.sin(k*Math.PI/(pickUpW.size()-1)),2);
        }
        //printing figure of D(n)
        /*double[] tempD = new double[pickUpW.size()];
        for (int k = 0; k < pickUpD.size(); k++) {
            if(k%2==0) tempD[k/2] = pickUpD.get(k);
        }
        panel1.fillGraph(pickUpW1,tempD);*/
        fourier = fourierAnalysis.transform(fourier);
        double max =  1;
        double wMax=0;
        for (int k = 0; k < pickUpD.size(); k++) {
            if(k%2==0){
                //fourierRe[k/2]=fourier[k];
            } else {
                int n=(k-1)/2;
                //fourierIm[n]=fourier[k];
                fourierAbs[n]=Math.sqrt(fourier[k]*fourier[k]+fourier[k-1]*fourier[k-1]);
                if(fourierAbs[n]>max){
                    max=fourierAbs[n];
                    wMax=(n+1.0)/pickUpW.size();
                }
            }
        }
        ArrayList<Double> graphX = new ArrayList<>();
        ArrayList<ArrayList<Double>> graphY = new ArrayList<>();
        for (int k = 0; k < pickUpW.size(); k++) {
            if(fourierMode==1) fourierAbs[k]=Math.log(fourierAbs[k]/max);
            else fourierAbs[k]=fourierAbs[k]/max;
            graphX.add(pickUpW1[k]);
            graphY.add(new ArrayList<>());
            graphY.get(k).add(fourierAbs[k]);
        }
        panel2.fillGraph(graphX, graphY);
        String str = String.format("%.3f%n",wMax);
        label.setText("wx = "+str);
        repaint();
    }

    private void countSpectra(){
        for(int p=0;p<50;p++) {
            for (int k = 0; k < 100; k++) {
                pickUpD.add(dipoleFull());
                pickUpD.add(0.0);
                pickUpW.add(0.0);
                for (int i = 0; i < (int) ((2 * Math.PI * Constants.numberOfw0ForUpdate) / Constants.timeStep); i++) {
                    for (int j = 0; j < Constants.numberOfParticles; j++) {
                        dipoleMoms[j] = countDipoleMom(particles[j]);
                    }
                    for (int j = 0; j < Constants.numberOfParticles; j++) {
                        dipoleMom = dipoleMoms[j];
                        particles[j].move();
                    }
                }
            }
            System.out.println(""+(p+1)+" iterations passed");
        }
        calculateSpectra();
    }

    private void switchFourierMode(){
        if(fourierMode==1) fourierMode=0;
        else fourierMode=1;
    }
}

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
import java.util.Arrays;

public class PartSimulationMainPanel extends JPanel {
    private Timer timer;

    private ImageIcon backgroundImage;

    private final JLabel label = new JLabel();
    private final GraphPanel panel1 = new GraphPanel();
    private final GraphPanel panel2 = new GraphPanel();
    private final MainPanel mainPanel;

    private Particle[] particles;

    private final double[] dipoleMoms = new double[Constants.numberOfParticles];
    private double dipoleMom = 0;
    public double scaleXSimulation = 10;
    public double scaleYSimulation = 10;
    private int xi = 0;
    private int yi = 0;
    private int xf = 0;
    private int yf = 0;
    private boolean isRectExist=false;
    private final ArrayList<Double> pickUpD= new ArrayList<>();
    private final ArrayList<Double> pickUpW = new ArrayList<>();


    private final double[] graphX = new double[Constants.numberOfParticles];
    private final double[] graphY = new double[Constants.numberOfParticles];

    public PartSimulationMainPanel(MainPanel mainPanel) {
        setPreferredSize(new Dimension(Constants.boardWight, Constants.boardHeight));
        addKeyListener(new MainKeyListener(this));
        MainMouseListener mainMouseListener = new MainMouseListener(this);
        addMouseListener(mainMouseListener);
        addMouseMotionListener(mainMouseListener);
        setFocusable(true);

        this.mainPanel=mainPanel;
        initializeVariables();
    }

    private void initializeVariables() {
        Button swapToCirculants = new Button("swapToCirculants");
        Button start = new Button("start");
        Button stop = new Button("stop");
        Button doOneStep = new Button("doOneStep");
        Button calculateSpectra = new Button("spectra");
        Button scaleXUp = new Button("scaleX+");
        Button scaleXDown = new Button("scaleX -");
        Button scaleYUp = new Button("scaleY+");
        Button scaleYDown = new Button("scaleY -");

        swapToCirculants.addActionListener(e -> { this.timer.stop();this.mainPanel.swapToCirculants();});
        start.addActionListener(e -> this.timer.start());
        stop.addActionListener(e -> this.timer.stop());
        doOneStep.addActionListener(e -> doOneLoop());
        calculateSpectra.addActionListener(e -> calculateSpectra());
        scaleXUp.addActionListener(e -> {this.scaleXSimulation+=5;repaint();repaintParticles();});
        scaleXDown.addActionListener(e -> {this.scaleXSimulation-=5;repaint();repaintParticles();});
        scaleYUp.addActionListener(e -> {this.scaleYSimulation+=5;repaint();repaintParticles();});
        scaleYDown.addActionListener(e -> {this.scaleYSimulation-=5;repaint();repaintParticles();});

        label.setText("");

        SpringLayout layout = new SpringLayout();

        layout.putConstraint(SpringLayout.EAST, panel1, -5, SpringLayout.EAST, this);
        layout.putConstraint(SpringLayout.NORTH, panel1, -5, SpringLayout.NORTH, this);
        layout.putConstraint(SpringLayout.EAST, panel2, -5, SpringLayout.EAST, this);
        layout.putConstraint(SpringLayout.NORTH, panel2, 5, SpringLayout.SOUTH, panel1);

        layout.putConstraint(SpringLayout.WEST, start, 5, SpringLayout.WEST, this);
        layout.putConstraint(SpringLayout.NORTH, start, 5, SpringLayout.NORTH, this);
        layout.putConstraint(SpringLayout.WEST, stop, 5, SpringLayout.EAST, start);
        layout.putConstraint(SpringLayout.NORTH, stop, 5, SpringLayout.NORTH, this);
        layout.putConstraint(SpringLayout.WEST, doOneStep, 5, SpringLayout.EAST, stop);
        layout.putConstraint(SpringLayout.NORTH, doOneStep, 5, SpringLayout.NORTH, this);
        layout.putConstraint(SpringLayout.WEST, calculateSpectra, 5, SpringLayout.EAST, doOneStep);
        layout.putConstraint(SpringLayout.NORTH, calculateSpectra, 5, SpringLayout.NORTH, this);

        layout.putConstraint(SpringLayout.WEST, label, 5, SpringLayout.EAST, swapToCirculants);
        layout.putConstraint(SpringLayout.NORTH, label, 5, SpringLayout.NORTH, this);

        layout.putConstraint(SpringLayout.WEST, scaleXUp, 5, SpringLayout.EAST, doOneStep);
        layout.putConstraint(SpringLayout.NORTH, scaleXUp, 5, SpringLayout.NORTH, this);
        layout.putConstraint(SpringLayout.WEST, scaleXDown, 0, SpringLayout.WEST, scaleXUp);
        layout.putConstraint(SpringLayout.NORTH, scaleXDown, 5, SpringLayout.SOUTH, scaleXUp);
        layout.putConstraint(SpringLayout.WEST, scaleYUp, 5, SpringLayout.EAST, scaleXUp);
        layout.putConstraint(SpringLayout.NORTH, scaleYUp, 5, SpringLayout.NORTH, this);
        layout.putConstraint(SpringLayout.WEST, scaleYDown, 0, SpringLayout.WEST, scaleYUp);
        layout.putConstraint(SpringLayout.NORTH, scaleYDown, 5, SpringLayout.SOUTH, scaleYUp);

        layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, swapToCirculants, Constants.boardWight/2, SpringLayout.WEST, this);
        layout.putConstraint(SpringLayout.NORTH, swapToCirculants, 5, SpringLayout.NORTH, this);

        setLayout(layout);

        add(swapToCirculants);
        add(label);
        add(panel1);
        add(panel2);
        add(start);
        add(stop);
        add(doOneStep);
        add(calculateSpectra);
        /*add(scaleXUp);
        add(scaleXDown);
        add(scaleYUp);
        add(scaleYDown);*/

        this.particles = new Particle[Constants.numberOfParticles];
        for (int i = 0; i < Constants.numberOfParticles; i++) {
            particles[i] = new Particle(10 * Math.cos(Math.PI * 2 * i / Constants.numberOfParticles), 10 * Math.sin(Math.PI * 2 * i / Constants.numberOfParticles)+5, 10 * Math.cos(Math.PI * 2 * i / Constants.numberOfParticles), 10 * Math.sin(Math.PI * 2 * i / Constants.numberOfParticles), this);
        }

        this.backgroundImage = ImageFactory.createImage(Image.BACKGROUND);
        this.timer = new Timer(Constants.updateSpeed, new UiLoop(this));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.drawImage(backgroundImage.getImage(), 0, 0, null);

        doDrawing(g);
        Toolkit.getDefaultToolkit().sync();


    }

    private void doDrawing(Graphics g) {

        drawNet(g);

        Graphics2D g2d = (Graphics2D) g.create();

        g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        for (Particle part : particles) {
            g2d.drawLine(part.getX(), part.getY(), part.getX(), part.getY());
            // g.drawImage(part.getImage(), part.getX(), part.getY(), this);
        }
        g2d.dispose();

        drawRect(g);
    }

    private void drawNet(Graphics g) {

        Graphics2D g2d = (Graphics2D) g.create();
        String str;
        int numberOfLines = 19;
        int mid = ((numberOfLines + 1) / 2);
        for (int i = 0; i < numberOfLines; i++) {
            g.drawLine(0, (int) (0.5 * Constants.boardHeight * ((i + 1.0) / mid)), Constants.boardWight, (int) (0.5 * Constants.boardHeight * ((i + 1.0) / mid)));
            str = String.format("%.1f%n", (int) (Constants.boardHeight * (-(i-mid+1.0) / (2*mid)))/this.scaleYSimulation);
            g.drawString(str,Constants.boardWight/2-g.getFontMetrics().stringWidth(str)-2, (int) (0.5 * Constants.boardHeight * ((i + 1.0) / mid))+15);

        }
        for (int i = 0; i < numberOfLines; i++) {
            if(i!=(mid-1)) {
                g.drawLine((int) (0.5 * Constants.boardWight * ((i + 1.0) / mid)), 0, (int) (0.5 * Constants.boardWight * ((i + 1.0) / mid)), Constants.boardHeight);
                str = String.format("%.1f%n", (int) (Constants.boardWight * ((i - mid + 1.0) / (2 * mid))) / this.scaleXSimulation);
                g.drawString(str, (int) (0.5 * Constants.boardWight * ((i + 1.0) / mid)) - g.getFontMetrics().stringWidth(str), Constants.boardHeight / 2 + 15);
            }
        }

        g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        g2d.drawLine(0, Constants.boardHeight/2, Constants.boardWight, Constants.boardHeight/2);
        g2d.drawLine(Constants.boardWight/2, 0, Constants.boardWight/2, Constants.boardHeight);

        g2d.dispose();

    }

    private void drawRect(Graphics g){
        if(isRectExist) g.drawRect(Math.min(xi,xf),Math.min(yi,yf),Math.abs(xf-xi),Math.abs(yf-yi));
    }

    public void setRect(int xi,int yi, int xf, int yf){
        this.xi=xi;
        this.xf=xf;
        this.yi=yi;
        this.yf=yf;
        this.isRectExist=true;
    }

    public void disableRect(){
        this.isRectExist=false;
        if(((xf-xi)>50) && ((yf-yi)>50)){
            setScales((int)(scaleXSimulation/((xf-xi+0.0)/Constants.boardWight)),(int)(scaleYSimulation/((yf-yi+0.0)/Constants.boardHeight)));
            repaint();
            repaintParticles();
        }
        if(((xf-xi)<-50) && ((yf-yi)<-50)){
            setScales((int)(scaleXSimulation*((xf-xi+0.0)/Constants.boardWight)),(int)(scaleYSimulation*((yf-yi+0.0)/Constants.boardHeight)));
        repaint();
        repaintParticles();
        }
        else repaint();
    }

    public void setScales(int scaleX, int scaleY){
        this.scaleXSimulation=scaleX;
        this.scaleYSimulation=scaleY;
        if(scaleXSimulation==0) this.scaleXSimulation=1;
        if(scaleYSimulation==0) this.scaleYSimulation=1;
    }

    public void doOneLoop() {
        update();
        repaint();
    }

    private void update() {
        //adding pickUp history
        pickUpD.add(dipoleFull());
        pickUpD.add(0.0);
        pickUpW.add(0.0);

        //filling graph panel1 with information about particles (now it is wake_forces/Constants.wake)
        for (int j = 0; j < Constants.numberOfParticles; j++) {
            graphX[j] = (4 * particles[j].z);
            graphY[j] = (4 * dipoleMoms[j]);
        }
        panel1.fillGraph(graphX, graphY);

        //iteration
        for (int i = 0; i < (int) ((2 * Math.PI * Constants.stepPartOfw0) / Constants.timeStep); i++) {
            for (int j = 0; j < Constants.numberOfParticles; j++) {
                dipoleMoms[j] = countDipoleMom(particles[j]);
            }
            for (int j = 0; j < Constants.numberOfParticles; j++) {
                dipoleMom = dipoleMoms[j];
                particles[j].move();
            }
        }
        //label.setText("1");
    }

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

    private double countDipoleMom(Particle p) {
        double sum = 0;
        for (Particle part : particles) {
            if (part.z < p.z) {
                sum += part.x1;
            }
        }
        return sum / Constants.numberOfParticles;
    }

    public double getDipoleMom() {
        return dipoleMom;
    }

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
            pickUpW1[k] = (k+0.0)*Constants.graphWight/(pickUpW.size())-(Constants.graphWight+0.0)/2;
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
                //fourierIm[(k-1)/2]=fourier[k];
                fourierAbs[(k-1)/2]=Math.sqrt(fourier[k]*fourier[k]+fourier[k-1]*fourier[k-1]);
                if(fourierAbs[(k-1)/2]>max){
                    max=fourierAbs[(k-1)/2];
                    wMax=((k-1.0)/2)/pickUpW.size();
                }
            }
        }
        for (int k = 0; k < pickUpW.size(); k++) {
            fourierAbs[k]=Constants.graphHeight*fourierAbs[k]/max-(Constants.graphHeight+0.0)/2;
        }
        panel2.fillGraph(pickUpW1, fourierAbs);
        /*ArrayList<Double> w1= new ArrayList<>();
        for (int i=0;i<pickUpD.size();i++) {
            if(fourier[i]>=max/3){
                max=fourier[i];
                w1.add((i+0.0)/pickUpD.size());
            }
        }
        System.out.println(w1.toString());*/
        String str = String.format("%.3f%n",wMax);
        label.setText("wx = "+str);
        repaint();
    }
}

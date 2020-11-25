package com.ui;

import com.company.Constants;
import com.company.MainKeyListener;
import com.image.*;
import com.image.Image;
import com.objects.Particle;

import java.lang.Math;

import javax.swing.*;
import java.awt.*;

public class MainPanel extends JPanel {
    private Timer timer;

    private ImageIcon backgroundImage;

    private final JLabel label = new JLabel();
    private final GraphPanel panel1 = new GraphPanel();

    private Particle[] particles;

    private final double[] dipoleMoms = new double[Constants.numberOfParticles];
    private double dipoleMom = 0;
    private final int[] graphX = new int[Constants.numberOfParticles];
    private final int[] graphY = new int[Constants.numberOfParticles];

    public MainPanel() {
        setPreferredSize(new Dimension(Constants.boardWight, Constants.boardHeight));
        addKeyListener(new MainKeyListener(this));
        setFocusable(true);

        initializeVariables();
    }

    private void initializeVariables() {
        Button start = new Button("start");
        Button stop = new Button("stop");
        Button doOneStep = new Button("doOneStep");

        start.addActionListener(e -> this.timer.start());
        stop.addActionListener(e -> this.timer.stop());
        doOneStep.addActionListener(e -> doOneLoop());

        label.setText("");

        SpringLayout layout = new SpringLayout();

        layout.putConstraint(SpringLayout.EAST, panel1, -5, SpringLayout.EAST, this);
        layout.putConstraint(SpringLayout.NORTH, panel1, -5, SpringLayout.NORTH, this);

        layout.putConstraint(SpringLayout.WEST, start, 5, SpringLayout.WEST, this);
        layout.putConstraint(SpringLayout.NORTH, start, 5, SpringLayout.NORTH, this);
        layout.putConstraint(SpringLayout.WEST, stop, 5, SpringLayout.EAST, start);
        layout.putConstraint(SpringLayout.NORTH, stop, 5, SpringLayout.NORTH, this);
        layout.putConstraint(SpringLayout.WEST, doOneStep, 5, SpringLayout.EAST, stop);
        layout.putConstraint(SpringLayout.NORTH, doOneStep, 5, SpringLayout.NORTH, this);

        layout.putConstraint(SpringLayout.WEST, label, 5, SpringLayout.EAST, doOneStep);
        layout.putConstraint(SpringLayout.NORTH, label, 5, SpringLayout.NORTH, this);

        setLayout(layout);

        add(label);
        add(panel1);
        add(start);
        add(stop);
        add(doOneStep);

        this.particles = new Particle[Constants.numberOfParticles];
        for (int i = 0; i < Constants.numberOfParticles; i++) {
            particles[i] = new Particle(10 * Math.cos(Math.PI * 2 * i / Constants.numberOfParticles), 10 * Math.sin(Math.PI * 2 * i / Constants.numberOfParticles), 10 * Math.cos(Math.PI * 2 * i / Constants.numberOfParticles), 10 * Math.sin(Math.PI * 2 * i / Constants.numberOfParticles), this);
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

        Graphics2D g2d = (Graphics2D) g.create();
        int numberOfLines = 19;
        for (int i = 0; i < numberOfLines; i++) {
            g.drawLine(0, (int) (0.5 * Constants.boardHeight * ((i + 1.0) / ((numberOfLines + 1) / 2))), Constants.boardWight, (int) (0.5 * Constants.boardHeight * ((i + 1.0) / ((numberOfLines + 1) / 2))));
        }
        for (int i = 0; i < numberOfLines; i++) {
            g.drawLine((int) (0.5 * Constants.boardWight * ((i + 1.0) / ((numberOfLines + 1) / 2))), 0, (int) (0.5 * Constants.boardWight * ((i + 1.0) / ((numberOfLines + 1) / 2))), Constants.boardHeight);
        }

        g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        g2d.drawLine(0, Constants.boardHeight/2, Constants.boardWight, Constants.boardHeight/2);
        g2d.drawLine(Constants.boardWight/2, 0, Constants.boardWight/2, Constants.boardHeight);

        for (Particle part : particles) {
            g2d.drawLine(part.getX(), part.getY(), part.getX(), part.getY());
            // g.drawImage(part.getImage(), part.getX(), part.getY(), this);
        }
        g2d.dispose();

    }


    public void doOneLoop() {
        update();
        repaint();
    }

    private void update() {
        for (int i = 0; i < (int) ((2 * Math.PI * Constants.stepPartOfBet) / Constants.timeStep); i++) {
            for (int j = 0; j < Constants.numberOfParticles; j++) {
                dipoleMoms[j] = countDipoleMom(particles[j]);
            }
            for (int j = 0; j < Constants.numberOfParticles; j++) {
                dipoleMom = dipoleMoms[j];
                particles[j].move();
                graphX[j] = (int) (4 * particles[j].z);
                graphY[j] = -(int) (4 * dipoleMoms[j]);
            }
            panel1.fillGraph(graphX, graphY);
        }
        //label.setText("1");
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
}

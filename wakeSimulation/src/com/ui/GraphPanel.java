package com.ui;

import com.company.Constants;

import javax.swing.*;
import java.awt.*;

public class GraphPanel extends JPanel {
    private double[] graphX;
    private double[] graphY;

    public GraphPanel() {
        setPreferredSize(new Dimension(Constants.graphWight, Constants.graphHeight));
        setFocusable(true);

        initializeVariables();
    }

    private void initializeVariables() {
        this.graphX = new double[Constants.numberOfParticles];
        this.graphY = new double[Constants.numberOfParticles];
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Toolkit.getDefaultToolkit().sync();

        Graphics2D g2d = (Graphics2D) g.create();
        int numberOfLines = 9;
        for (int i = 0; i < numberOfLines; i++) {
            g.drawLine(0, (int) (0.5 * Constants.graphHeight * ((i + 1.0) / ((numberOfLines + 1) / 2))), Constants.graphWight, (int) (0.5 * Constants.graphHeight * ((i + 1.0) / ((numberOfLines + 1) / 2))));
        }
        for (int i = 0; i < numberOfLines; i++) {
            g.drawLine((int) (0.5 * Constants.graphWight * ((i + 1.0) / ((numberOfLines + 1) / 2))), 0, (int) (0.5 * Constants.graphWight * ((i + 1.0) / ((numberOfLines + 1) / 2))), Constants.graphHeight);
        }

        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        g2d.drawLine(0, Constants.graphHeight/2, Constants.graphWight, Constants.graphHeight/2);
        g2d.drawLine(Constants.graphWight/2, 0, Constants.graphWight/2, Constants.graphHeight);

        for (int i = 0; i < graphX.length; i++) {
            g2d.drawLine((int)graphX[i] + Constants.graphWight / 2, -(int)graphY[i] + Constants.graphHeight / 2, (int)graphX[i] + Constants.graphWight / 2, -(int)graphY[i] + Constants.graphHeight / 2);
        }
        g2d.dispose();
    }

    public void fillGraph(double[] ax, double[] ay) {
        graphX = ax;
        graphY = ay;
    }

    /*public void update(){
        repaint();
    }*/


}

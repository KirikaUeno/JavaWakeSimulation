package com.ui;

import com.company.Constants;

import javax.swing.*;
import java.awt.*;

public class GraphSpectra extends JPanel {
    private int[][] graphX;
    private int[][] graphY;
    private int wight=Constants.boardWight;
    private int height=Constants.boardHeight-100;

    public GraphSpectra() {
        setPreferredSize(new Dimension(wight, height));
        setFocusable(true);

        initializeVariables();
    }

    private void initializeVariables() {
        this.graphX = new int[Constants.boxesNumber][Constants.currentSamples];
        this.graphY = new int[Constants.boxesNumber][Constants.currentSamples];
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Toolkit.getDefaultToolkit().sync();

        Graphics2D g2d = (Graphics2D) g.create();
        int numberOfLines = 19;
        for (int i = 0; i < numberOfLines; i++) {
            g.drawLine(0, (int) (0.5 * height * ((i + 1.0) / ((numberOfLines + 1) / 2))), wight, (int) (0.5 * height* ((i + 1.0) / ((numberOfLines + 1) / 2))));
        }
        for (int i = 0; i < numberOfLines; i++) {
            g.drawLine((int) (0.5 * wight * ((i + 1.0) / ((numberOfLines + 1) / 2))), 0, (int) (0.5 * wight * ((i + 1.0) / ((numberOfLines + 1) / 2))), height);
        }

        g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        g2d.drawLine(0, height/2, wight, height/2);
        g2d.drawLine(wight/2, 0, wight/2, height);

        g2d.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for(int j = 0; j < Constants.currentSamples;j++){
            for (int i = 0; i < Constants.boxesNumber; i++) {
                g2d.drawLine(graphX[i][j] + wight / 2, graphY[i][j] + height / 2, graphX[i][j] + wight/ 2, graphY[i][j] + height / 2);
            }
        }
        g2d.dispose();
    }

    public void fillGraph(int[][] ax, int[][] ay) {
        graphX = ax;
        graphY = ay;
    }

    /*public void update(){
        repaint();
    }*/

}

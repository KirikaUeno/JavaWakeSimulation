package com.ui;

import com.company.Constants;

import java.awt.*;
import java.util.ArrayList;

/**
 * takes 2 arrays - x and y in pixels and draw y(x) points with 0 in the center of panel with sizes graphWight/Height.
 * Has net without numbers.
 */
public class GraphPanel extends ResizableJPanel {
    private ArrayList<Double> graphX;
    private ArrayList<ArrayList<Double>> graphY;

    public GraphPanel(String name) {
        setPreferredSize(new Dimension(Constants.graphWight, Constants.graphHeight));
        setFocusable(true);
        setName(name);
        initializeVariables();
    }

    private void initializeVariables() {
        this.graphX = new ArrayList<>();
        this.graphY = new ArrayList<>();
        Button resetScales = new Button("reset scales");
        resetScales.addActionListener(e->{resetScales(); repaint();});
        add(resetScales);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g.create();

        drawLines(g);

        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        //сделать проверку: если при каждом i размер y одинакоывый, то соединять точки
        int count=0;
        if(graphY.size()>0) count = graphY.get(0).size();
        boolean canBeJoined = true;
        for (int i = 0; i < graphX.size(); i++) {
            if (count != graphY.get(i).size()) {
                canBeJoined = false;
                break;
            }
        }
        int wight=this.getWidth();
        int height=this.getHeight();
        if(canBeJoined){
            for (int i = 0; i < graphX.size()-1; i++) {
                for (int j=0;j<count;j++) {
                    g2d.drawLine((int) (wight * (graphX.get(i) * scaleX + 0.5 + shiftX)), -(int) (height * (graphY.get(i).get(j) * scaleY - 0.5 + shiftY)), (int) (wight * (graphX.get(i+1) * scaleX + 0.5 + shiftX)), -(int) (height * (graphY.get(i+1).get(j) * scaleY - 0.5 + shiftY)));
                }
            }
        } else {
            for (int i = 0; i < graphX.size(); i++) {
                for (double y : graphY.get(i)) {
                    g2d.drawLine((int) (wight * (graphX.get(i) * scaleX + 0.5 + shiftX)), -(int) (height * (y * scaleY - 0.5 + shiftY)), (int) (wight * (graphX.get(i) * scaleX + 0.5 + shiftX)), -(int) (height * (y * scaleY - 0.5 + shiftY)));
                }
            }
        }
        g2d.dispose();
    }

    private void drawLines(Graphics g){
        double x1 = -(shiftX+0.5)/scaleX;
        double x2 = (0.5-shiftX)/scaleX;
        double y2 = (-shiftY+0.5)/scaleY;
        double y1 = -(0.5+shiftY)/scaleY;
        double stepX = (x2-x1)/10;
        int countX=0;
        double stepY = (y2-y1)/10;
        int countY=0;
        while((int)(stepX)==0){
            stepX=stepX*10;
            countX++;
        }
        while((int)(stepY)==0){
            stepY=stepY*10;
            countY++;
        }
        stepX=intUp(stepX);
        stepY=intUp(stepY);
        double x0=x1;
        double y0=y1;
        for(int i=0;i<countX;i++){
            stepX/=10;
            x0*=10;
        }
        for(int i=0;i<countY;i++){
            stepY/=10;
            y0*=10;
        }
        x0=(int)x0;
        y0=(int)y0;
        for(int i=0;i<countX;i++){
            x0/=10;
        }
        for(int i=0;i<countY;i++){
            y0/=10;
        }
        String str;
        int numberOfLines = 9;
        int mid = ((numberOfLines + 1) / 2);
        int wight=this.getWidth();
        int height=this.getHeight();
        for (int i = 1; i < numberOfLines+1; i++) {
            if(i!=mid) {
                g.drawLine(0, -(int) (height * ((y0 + i * stepY) * scaleY - 0.5 + shiftY)), wight, -(int) (height * ((y0 + i * stepY) * scaleY - 0.5 + shiftY)));
                str = String.format("%1." + countY + "f", (y0 + i * stepY));
                g.drawString(str, (int) (wight * ((x0 + mid * stepX) * scaleX + 0.5 + shiftX)) - g.getFontMetrics().stringWidth(str) - 2, -(int) (height * ((y0 + i * stepY) * scaleY - 0.5 + shiftY)) + 15);
            }
        }
        for (int i = 1; i < numberOfLines+1; i++) {
            if(i!=mid) {
                g.drawLine((int) (wight * ((x0 + i * stepX) * scaleX + 0.5 + shiftX)), 0, (int) (wight * ((x0 + i * stepX) * scaleX + 0.5 + shiftX)), height);
                str = String.format("%1." + countX + "f", (x0 + i * stepX));
                g.drawString(str, (int) (wight * ((x0 + i * stepX) * scaleX + 0.5 + shiftX)) - g.getFontMetrics().stringWidth(str), -(int) (height * ((y0 + mid * stepY) * scaleY - 0.5 + shiftY)) + 15);
            }
        }
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawLine(0, -(int)(height* ((y0 + mid * stepY)*scaleY-0.5+shiftY)), wight, -(int)(height* ((y0 + mid * stepY)*scaleY-0.5+shiftY)));
        g2d.drawLine((int) (wight * ((x0 + mid * stepX) * scaleX + 0.5 + shiftX)), 0, (int) (wight * ((x0 + mid * stepX) * scaleX + 0.5 + shiftX)), height);
        g2d.drawLine(0,0,wight,0);
        g2d.drawLine(0,height,wight,height);
        g2d.drawLine(0,0,0,height);
        g2d.drawLine(wight,0,wight,height);
        g2d.dispose();
    }

    private int intUp(double i){
        if((int)i-i==0) return (int)i;
        else return (int)(i+1);
    }

    public void fillGraph(ArrayList<Double> ax, ArrayList<ArrayList<Double>> ay) {
        graphX = ax;
        graphY = ay;
    }
}
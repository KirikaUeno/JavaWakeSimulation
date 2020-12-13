package com.ui;

import javax.swing.*;
import java.awt.*;

public class ResizableJPanel extends JPanel {
    protected int xi = 0;
    protected int yi = 0;
    protected int xf = 0;
    protected int yf = 0;
    protected boolean isRectExist=false;
    private double scaleX0 = 1;
    private double scaleY0 = 1;
    private double shiftX0 = 0;
    private double shiftY0 = 0;
    protected double scaleX = 1;
    protected double scaleY = 1;
    protected double shiftX = 0;
    protected double shiftY = 0;

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(isRectExist) g.drawRect(Math.min(xi,xf),Math.min(yi,yf),Math.abs(xf-xi),Math.abs(yf-yi));
        Toolkit.getDefaultToolkit().sync();
    }

    public void setRect(int xi,int yi, int xf, int yf){
        this.xi=xi;
        this.xf=xf;
        this.yi=yi;
        this.yf=yf;
        this.isRectExist=true;
    }

    public void disableRect(){
        int wight=this.getWidth();
        int height=this.getHeight();
        this.isRectExist=false;
        if(((xf-xi)>10) && ((yf-yi)>10)){
            setScales((scaleX /((xf-xi+0.0)/wight)),(scaleY /((yf-yi+0.0)/height)));
            setShifts((shiftX*wight-(xf+xi+0.0-wight)/2)/(xf-xi),(shiftY*height+(yf+yi+0.0-height)/2)/(yf-yi));
        }
        if(((xf-xi)<-10) && ((yf-yi)<-10)){
            //setScales((scaleX *((xi-xf+0.0)/wight)),(scaleY *((yi-yf+0.0)/height)));
        }
        repaint();
    }

    protected void setScales(double scaleX, double scaleY){
        this.scaleX =scaleX;
        this.scaleY =scaleY;
        if(this.scaleX ==0) this.scaleX =1;
        if(this.scaleY ==0) this.scaleY =1;
    }

    protected void setShifts(double shiftX, double shiftY){
        this.shiftX=shiftX;
        this.shiftY=shiftY;
    }

    protected void resetScales() {
        scaleX=scaleX0;
        scaleY=scaleY0;
        shiftY=shiftY0;
        shiftX=shiftX0;
    }

    protected void setScaleX(double a){
        this.scaleX0=a;
        resetScales();
    }

    protected void setScaleY(double b){
        this.scaleY0=b;
        resetScales();
    }

    protected void setShiftX(double a){
        this.shiftX0=a;
        resetScales();
    }

    protected void setShiftY(double b){
        this.shiftY0=b;
        resetScales();
    }
}

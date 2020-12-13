package com.objects;

import com.company.Constants;
import com.ui.PartSimulationMainPanel;


public class Particle {
    public double x1;
    public double px1;
    public double z;
    public double d;
    public double x;
    public double y;
    private final PartSimulationMainPanel gp;
    private String xAxe="z";
    private String yAxe="x";

    public Particle(double x, double px, double z, double d, PartSimulationMainPanel gap) {
        this.x1 = x;
        this.px1 = px;
        this.z = z;
        this.d = d;
        gp = gap;
        initialize();
    }

    private void initialize() {
        x = z;
        y =(x1);
    }

    public void move() {
        double x1mid;
        double px1mid;
        double zMid;
        double dMid;

        x1mid = x1 + Constants.timeStep * px1 * 0.5;
        px1mid = px1 + 0.5 * Constants.timeStep * (-x1*Constants.xFreq*Constants.xFreq + Constants.wake * gp.getDipoleMom());
        zMid = z - 0.5 * Constants.timeStep * d * Constants.eta;
        dMid = d + 0.5 * Constants.timeStep * (z*Constants.zFreq*Constants.zFreq/Constants.eta + x1 * 0);

        x1 += Constants.timeStep * px1mid;
        px1 += Constants.timeStep * (-x1mid*Constants.xFreq*Constants.xFreq + Constants.wake * gp.getDipoleMom());
        z += -Constants.timeStep * dMid* Constants.eta;
        d += Constants.timeStep * (zMid*Constants.zFreq*Constants.zFreq/Constants.eta + x1mid * 0);

        repaint();
    }

    public void repaint() {
        switch (xAxe) {
            case "x" -> x = (x1);
            case "px c/wb" -> x = (px1/Constants.xFreq);
            case "d" -> x = (d*Constants.eta/Constants.zFreq);
            default -> x =(z);
        }
        switch (yAxe) {
            case "z" -> y =(z);
            case "px c/wb" -> y = (px1/Constants.xFreq);
            case "d eta c/ws" -> y = (d*Constants.eta/Constants.zFreq);
            default -> y = (x1);
        }
    }

    public void setXAxe(String s){
        xAxe=s;
    }
    public void setYAxe(String s){
        yAxe=s;
    }
}

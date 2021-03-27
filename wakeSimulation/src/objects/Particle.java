package objects;

import company.Constants;
import ui.PartSimulationMainPanel;


public class Particle{
    public double x1;
    public double px1;
    public double z;
    public double d;
    public double x;
    public double y;
    private final PartSimulationMainPanel gp;
    private String xAxe="z";
    private String yAxe="x";
    private boolean isEuler = true;

    public Particle(double x, double px, double z, double d, PartSimulationMainPanel gap){
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

        if(isEuler) {
            //euler
            x1mid = x1 + Constants.timeStep * px1 * 0.5;
            px1mid = px1 + 0.5 * Constants.timeStep * (-x1 * Constants.xFreq * Constants.xFreq + gp.getWake() * gp.getDipoleMom());
            zMid = z - 0.5 * Constants.timeStep * d * Constants.eta;
            dMid = d + 0.5 * Constants.timeStep * (z * Constants.zFreq * Constants.zFreq / Constants.eta + x1 * 0);

            x1 += Constants.timeStep * px1mid;
            px1 += Constants.timeStep * (-x1mid * Constants.xFreq * Constants.xFreq + gp.getWake() * gp.getDipoleMom());
            z += -Constants.timeStep * dMid * Constants.eta;
            d += Constants.timeStep * (zMid * Constants.zFreq * Constants.zFreq / Constants.eta + x1mid * 0);
        } else {
            //runge kutti 4
            x1mid = x1 + (k1x(x1, px1, z, d) + 2 * k2x(x1, px1, z, d) + 2 * k3x(x1, px1, z, d) + k4x(x1, px1, z, d)) / 6;
            px1mid = px1 + (k1px(x1, px1, z, d) + 2 * k2px(x1, px1, z, d) + 2 * k3px(x1, px1, z, d) + k4px(x1, px1, z, d)) / 6;
            zMid = z + (k1z(x1, px1, z, d) + 2 * k2z(x1, px1, z, d) + 2 * k3z(x1, px1, z, d) + k4z(x1, px1, z, d)) / 6;
            dMid = d + (k1d(x1, px1, z, d) + 2 * k2d(x1, px1, z, d) + 2 * k3d(x1, px1, z, d) + k4d(x1, px1, z, d)) / 6;
            x1 = x1mid;
            px1 = px1mid;
            z = zMid;
            d = dMid;
        }

        repaint();
    }

    private double fx(double x, double px, double z, double d){
        return px;
    }
    private double fpx(double x, double px, double z, double d){
        return -x*Constants.xFreq*Constants.xFreq + gp.getWake() * gp.getDipoleMom();
    }
    private double fz(double x, double px, double z, double d){
        return -d* Constants.eta;
    }
    private double fd(double x, double px, double z, double d){
        return z*Constants.zFreq*Constants.zFreq/Constants.eta + x1* 0;
    }

    private double k1x(double x, double px, double z, double d){
        return fx(x,px,z,d)*Constants.timeStep;
    }
    private double k1px(double x, double px, double z, double d){
        return fpx(x,px,z,d)*Constants.timeStep;
    }
    private double k1z(double x, double px, double z, double d){
        return fz(x,px,z,d)*Constants.timeStep;
    }
    private double k1d(double x, double px, double z, double d){
        return fd(x,px,z,d)*Constants.timeStep;
    }

    private double k2x(double x, double px, double z, double d){
        return fx(x+k1x(x,px,z,d)/2,px+k1px(x,px,z,d)/2,  z+k1z(x,px,z,d)/2,  d+k1d(x,px,z,d)/2)*Constants.timeStep;
    }
    private double k2px(double x, double px, double z, double d){
        return fpx( x+k1x(x,px,z,d)/2,px+k1px(x,px,z,d)/2,  z+k1z(x,px,z,d)/2,  d+k1d(x,px,z,d)/2)*Constants.timeStep;
    }
    private double k2z(double x, double px, double z, double d){
        return fz( x+k1x(x,px,z,d)/2,px+k1px(x,px,z,d)/2,  z+k1z(x,px,z,d)/2,  d+k1d(x,px,z,d)/2)*Constants.timeStep;
    }
    private double k2d(double x, double px, double z, double d){
        return fd( x+k1x(x,px,z,d)/2,px+k1px(x,px,z,d)/2,  z+k1z(x,px,z,d)/2,  d+k1d(x,px,z,d)/2)*Constants.timeStep;
    }

    private double k3x(double x, double px, double z, double d){
        return fx(x+k2x(x,px,z,d)/2,px+k2px(x,px,z,d)/2,  z+k2z(x,px,z,d)/2,  d+k2d(x,px,z,d)/2)*Constants.timeStep;
    }
    private double k3px(double x, double px, double z, double d){
        return fpx( x+k2x(x,px,z,d)/2,px+k2px(x,px,z,d)/2,  z+k2z(x,px,z,d)/2,  d+k2d(x,px,z,d)/2)*Constants.timeStep;
    }
    private double k3z(double x, double px, double z, double d){
        return fz( x+k2x(x,px,z,d)/2,px+k2px(x,px,z,d)/2,  z+k2z(x,px,z,d)/2,  d+k2d(x,px,z,d)/2)*Constants.timeStep;
    }
    private double k3d(double x, double px, double z, double d){
        return fd( x+k2x(x,px,z,d)/2,px+k2px(x,px,z,d)/2,  z+k2z(x,px,z,d)/2,  d+k2d(x,px,z,d)/2)*Constants.timeStep;
    }

    private double k4x(double x, double px, double z, double d){
        return fx(x+k3x(x,px,z,d),px+k3px(x,px,z,d),  z+k3z(x,px,z,d),  d+k3d(x,px,z,d))*Constants.timeStep;
    }
    private double k4px(double x, double px, double z, double d){
        return fpx( x+k3x(x,px,z,d),px+k3px(x,px,z,d),  z+k3z(x,px,z,d),  d+k3d(x,px,z,d))*Constants.timeStep;
    }
    private double k4z(double x, double px, double z, double d){
        return fz( x+k3x(x,px,z,d),px+k3px(x,px,z,d),  z+k3z(x,px,z,d),  d+k3d(x,px,z,d))*Constants.timeStep;
    }
    private double k4d(double x, double px, double z, double d){
        return fd( x+k3x(x,px,z,d),px+k3px(x,px,z,d),  z+k3z(x,px,z,d),  d+k3d(x,px,z,d))*Constants.timeStep;
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

    public String toString(){
        return (""+z);
    }
}

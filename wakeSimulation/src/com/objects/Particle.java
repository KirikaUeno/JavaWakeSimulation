package com.objects;

import com.company.Constants;
import com.image.Image;
import com.image.ImageFactory;
import com.ui.PartSimulationMainPanel;

import javax.swing.*;

public class Particle extends Sprite {
    public double x1;
    public double px1;
    public double z;
    public double d;
    private final PartSimulationMainPanel gp;

    public Particle(double x, double px, double z, double d, PartSimulationMainPanel gap) {
        this.x1 = x;
        this.px1 = px;
        this.z = z;
        this.d = d;
        gp = gap;
        initialize();
    }

    private void initialize() {
        ImageIcon imageIcon = ImageFactory.createImage(Image.POINT);
        if (imageIcon != null) {
            setImage(imageIcon.getImage());
        }
        x = Constants.boardWight / 2 + (int) (gp.scaleXSimulation * z);
        y = Constants.boardHeight / 2 - (int) (gp.scaleYSimulation * x1);
    }

    @Override
    public void move() {
        double x1mid;
        double px1mid;
        double zMid;
        double dMid;

        x1mid = x1 + Constants.timeStep * px1 * 0.5*Constants.xFreq;
        px1mid = px1 + 0.5 * Constants.timeStep * (-x1*Constants.xFreq + Constants.wake * gp.getDipoleMom());
        zMid = z - 0.5 * Constants.timeStep * d;
        dMid = d + 0.5 * Constants.timeStep * (z + x1 * 0);

        x1 += Constants.timeStep * px1mid*Constants.xFreq;
        px1 += Constants.timeStep * (-x1mid*Constants.xFreq + Constants.wake * gp.getDipoleMom());
        d += Constants.timeStep * (zMid + x1mid * 0);
        z += -Constants.timeStep * dMid;

        x = Constants.boardWight / 2 + (int) (gp.scaleXSimulation * z);
        y = Constants.boardHeight / 2- (int) (gp.scaleYSimulation * x1);
    }

    @Override
    public void repaint() {
        x = Constants.boardWight / 2 + (int) (gp.scaleXSimulation * z);
        y = Constants.boardHeight / 2- (int) (gp.scaleYSimulation * x1);
    }
}

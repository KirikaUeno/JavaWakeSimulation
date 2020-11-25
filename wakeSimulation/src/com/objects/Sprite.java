package com.objects;

import java.awt.*;

public abstract class Sprite {
    private Image image;

    protected int x;
    protected int y;

    public abstract void move();

    public Sprite() {

    }

    public void setImage(Image image) {
        this.image = image;
    }

    public Image getImage() {
        return this.image;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getY() {
        return this.y;
    }

    public int getX() {
        return this.x;
    }
}

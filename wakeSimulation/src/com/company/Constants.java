package com.company;

public class Constants {

    private Constants() {

    }

    public static final String title = "WakeSimulation";
    public static final int boardWight = 1080;
    public static final int boardHeight = 720;

    public static final int graphWight = 320;
    public static final int graphHeight = 180;

    public static final String WAKE_MATRIX_URL = "inputMatrix.txt";
    public static final String ICON_IMAGE_URL = "images/appIcon.jpg";
    public static final String POINT_IMAGE_URL = "images/point.png";
    public static final String BACKGROUND_IMAGE_URL = "images/background.png";


    public static final int updateSpeed = 10;
    public static final double wake = 0;
    public static final int numberOfParticles = 101;
    public static final double timeStep = 1.0 / 1000;
    //stepPartOfw0 - part of revolution period counted by one iteration - dont change! dont know how to handle it yet
    public static final double stepPartOfw0 = 1;
    public static final double xFreq = 0.8518751;

    public static final int boxesNumber = 3;
    public static final int currentSamples = 101;
    public static final double currentSamplesStep = 0.1;
    public static final double z0 = 2;
}

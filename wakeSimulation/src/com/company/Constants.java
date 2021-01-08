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


    public static final int updateSpeed = 20;
    public static final int numberOfParticles = 101;
    public static final double timeStep = 1.0 / 100;
    public static final int numberOfw0ForUpdate = 2;
    public static final double xFreq = 0.1818751;
    public static final double zFreq = 0.0042751;
    public static final double eta = 0.00058;
    public static final double Zx = 0.05;

    public static final int boxesNumber = 33;
    public static final int currentSamples = 101;
    public static final double currentSamplesStep = 0.05;
    public static final double z0 = 2;
}

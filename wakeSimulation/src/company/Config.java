package company;

public class Config {

    private Config() {    }

    /**window settings**/
    public static final String title = "WakeSimulation";
    public static final int boardWight = 1080;
    public static final int boardHeight = 720;
    public static final int updateSpeed = 10;

    /**default graph size**/
    public static final int graphWight = 320;
    public static final int graphHeight = 180;

    /**outer files**/
    public static final String WAKE_MATRIX_URL = "inputMatrix.txt";
    public static final String GRAPH1_URL = "out/graph5.txt";
    public static final String GRAPH2_URL = "out/graph5.txt";

    /**simulation parameters - what to do**/
    public static final boolean countOnlyOneIntensity = true;
    public static final double lowerBorder = 0.15;
    public static final double upperBorder = 0.25;
    public static final double lowerIntensity = 0.01;
    public static final double upperIntensity = 10.01;
    public static final double intensitySteps = 50;
    public static final boolean isMomentHistorySoloParticle = false;
    public static final boolean isMomentHistoryY = false;
    public static final boolean isMomentHistory2ndBeam = true;
    public static final boolean isFullBeamBeam = true;
    public static final boolean isBeamBeamInSpace = true;
    public static final boolean isDifferentXYIntensities = false;
    public static final boolean isDifferentBeamsIntensities = false;
    public static final boolean countOnlyOneDimension = true;
    public static final boolean countNonlinearities = true;
    public static final boolean areBeamsGauss = true;
    public static final String updateStep = "matrix"; // matrix / equations / beamBeamStep

    /**simulation exterior parameters**/
    public static final double xFreq = 0.17;
    public static final double yFreq = 0.1851;
    public static final double zFreq = 0.004;
    public static final double eta = -0.036; //not used due to normalization
    public static final double L = 0.00;
    /**px and py kicks in normalized variables**/
    public static double Zx = 0.01;
    public static double Zx1 = -0;
    public static final double Zy = 0;
    /**in this simulation, length = z0/beta; d = normalized(d0)/beta**/
    public static final double length = 1.5;
    /**main parameters**/
    public static double wake=0.000;
    //coordinates are normalized -> intensity = 4 pi xi, where xi = N r0 beta / (4 pi gamma sigma^2)
    public static double intensity = 0.15;
    public static double beta = 5;
    public static double alfaDivSigX = 0;

    /**general simulation interior parameters**/
    public static final int numberOfParticles = 1001;
    public static final double timeStep = 1.0 / 50;
    public static final int numberOfw0ForUpdate = 1;

    /**simulation interior parameters - spectrum parameters**/
    public static final int step0 = 10000;
    public static final double lowerValue = 0.01;
    public static final int averOver = 16;
    public static final double stepGrowth = 20;
    public static final double threshold = 1.2;
    public static int fourierMode = 1;
    /**simulation interior parameters - particle motion parameters**/
    public static final boolean isEuler = true;
    public static final boolean isWakeOn = false;


    /**cir settings**/
    public static final int boxesNumber = 33;
    public static final int currentSamples = 101;
    public static final double currentSamplesStep = 0.05;
    public static final double z0 = 2;
}

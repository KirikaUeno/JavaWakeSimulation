package simulation;

import company.Config;
import objects.PartPair;
import objects.Particle;
import org.apache.commons.math3.special.Erf;
import org.opensourcephysics.numerics.specialfunctions.ErrorFunction;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class SimulationFlow {
    private static Particle[] particles1;
    private static Particle[] particles2;

    private static PartPair[] pairs = new PartPair[Config.numberOfParticles* Config.numberOfParticles];
    private static int pairIndex = 0;

    private static ArrayList<Double> pickUpD= new ArrayList<>();

    private static Particle highlightedParticle;
    private static Particle highlightedParticle1;

    public static void update() {
        for(int k = 0; k< Config.numberOfw0ForUpdate; k++) {
            addBetatronHistory();
            makeOneTurn();
        }
    }

    private static void addBetatronHistory(){
        if(!Config.isMomentHistorySoloParticle) {
            if (Config.isMomentHistoryY) {
                if (Config.isMomentHistory2ndBeam) pickUpD.add(dipoleFull(2, "y"));
                else pickUpD.add(dipoleFull(1, "y"));
            } else {
                if (Config.isMomentHistory2ndBeam) pickUpD.add(dipoleFull(2, "x"));
                else pickUpD.add(dipoleFull(1, "x"));
            }
        } else {
            Particle part = null;
            if (Config.isMomentHistory2ndBeam){
                for(Particle p:particles1){
                    if(p.index==1) part = p;
                }
            } else {
                for(Particle p:particles2){
                    if(p.index==1) part = p;
                }
            }
            if(part!=null) {
                if (Config.isMomentHistoryY) {
                    pickUpD.add(part.y);
                } else {
                    pickUpD.add(part.x);
                }
            } else {
                pickUpD.add(0.0);
            }
        }
        pickUpD.add(0.0);
    }

    private static void makeOneTurn(){
        switch(Config.updateStep){
            case "matrix" -> iterationSimplified();
            case "equations" -> iteration();
            case "beamBeamStep" -> iterationBeam();
            default -> throw new IllegalStateException("Unexpected value: " + Config.updateStep);
        }
    }

    private static void iteration() {
        ParticlesMovement.equationsPeriod(particles1);
        ParticlesMovement.equationsPeriod(particles2);
    }

    private static void iterationSimplified() {
        ParticlesMovement.transportBeam(particles1);
        ParticlesMovement.transportBeam(particles2);
        ParticlesMovement.beamBeam(particles1, particles2);
        //ParticlesMovement.solenoidsRotation(particles1);
        //ParticlesMovement.solenoidsRotation(particles2);
    }

    private static void iterationBeam() {
        if(pairIndex==0) prepareToBeamBeam();
        ParticleMovement.beamBeamStep(pairs[pairIndex]);
        pairIndex++;
        if (pairIndex == Config.numberOfParticles * Config.numberOfParticles) {
            pairIndex = 0;
        }
        highlightedParticle = pairs[pairIndex].p1;
        highlightedParticle1 = pairs[pairIndex].p2;

        //highlightedParticle = pairs[0].p1;
        //highlightedParticle1 = pairs[0].p2;
    }

    public static void prepareToBeamBeam(){
        pairs = ParticlesMovement.prepareToBeamBeam(particles1, particles2);
    }

    private static double dipoleFull(int psIndex, String axis) {
        double sum = 0;
        if(axis.equals("y")) {
            if (psIndex == 2) {
                for (Particle part : particles2) {
                    sum += part.y;
                }
            } else {
                for (Particle part : particles1) {
                    sum += part.y;
                }
            }
        } else {
            if (psIndex == 2) {
                for (Particle part : particles2) {
                    sum += part.x;
                }
            } else {
                for (Particle part : particles1) {
                    sum += part.x;
                }
            }
        }
        return sum / Config.numberOfParticles;
    }

    public static void toInitialParameters(){
        particles1 = new Particle[Config.numberOfParticles];
        double x;
        double y;
        double px;
        double py;
        double z;
        double d;
        Random random = new Random();
        for (int i = 0; i < Config.numberOfParticles; i++) {
            double phase = i*2*Math.PI/ Config.numberOfParticles;
            x = 0;
            px = 0;
            y = 0;
            py = 0;
            z = Config.length*Math.cos(phase);
            d = Config.length*Math.sin(phase);
            if(Config.areBeamsGauss) {
                x = -Math.sqrt(2) * Erf.erfcInv(2 * random.nextDouble());
                px = -Math.sqrt(2) * Erf.erfcInv(2 * random.nextDouble());
                y = -Math.sqrt(2) * Erf.erfcInv(2 * random.nextDouble());
                py = -Math.sqrt(2) * Erf.erfcInv(2 * random.nextDouble());
                z = -Config.length*Math.sqrt(2)*Erf.erfcInv(2*random.nextDouble());
                d = -Config.length*Math.sqrt(2)*Erf.erfcInv(2*random.nextDouble());
            }
            particles1[i] =  new Particle(x, px+ Config.Zx, z, d, y, py+ 0);
        }
        particles1[Config.numberOfParticles/3].index = 1;
        particles2 = new Particle[Config.numberOfParticles];
        for (int i = 0; i < Config.numberOfParticles; i++) {
            double phase = i*2*Math.PI/ Config.numberOfParticles;
            x = 1*Math.cos(phase);
            px = 1*Math.sin(phase);
            y = 1*Math.cos(phase);
            py = 1*Math.sin(phase);
            z = Config.length*Math.cos(phase);
            d = Config.length*Math.sin(-phase);
            if(Config.areBeamsGauss) {
                x = -Math.sqrt(2) * Erf.erfcInv(2 * random.nextDouble());
                px = -Math.sqrt(2) * Erf.erfcInv(2 * random.nextDouble());
                y = -Math.sqrt(2) * Erf.erfcInv(2 * random.nextDouble());
                py = -Math.sqrt(2) * Erf.erfcInv(2 * random.nextDouble());
                z = -Config.length*Math.sqrt(2)*Erf.erfcInv(2*random.nextDouble());
                d = -Config.length*Math.sqrt(2)*Erf.erfcInv(2*random.nextDouble());
            }
            particles2[i] = new Particle(x, px+ Config.Zx1, z, d, y, py+ Config.Zy);
        }
        particles2[Config.numberOfParticles/3].index = 1;
        pickUpD= new ArrayList<>();
    }

    public static ArrayList<ArrayList<Double>> getGraphData(int partsIndex, String xAxis, String yAxis){
        ArrayList<ArrayList<Double>> graphData = new ArrayList<>();
        for (int j = 0; j < Config.numberOfParticles; j++) {
            graphData.add(new ArrayList<>());
            double x;
            double y;
            if (partsIndex == 2) {
                switch (xAxis) {
                    case "x" -> x = (particles2[j].x);
                    case "px" -> x = (particles2[j].px);
                    case "d" -> x = (particles2[j].d);
                    default -> x = (particles2[j].z);
                }
                switch (yAxis) {
                    case "z" -> y = (particles2[j].z);
                    case "px" -> y = (particles2[j].px);
                    case "d" -> y = (particles2[j].d);
                    default -> y = (particles2[j].x);
                }
            } else {
                switch (xAxis) {
                    case "x" -> x = (particles1[j].x);
                    case "px" -> x = (particles1[j].px);
                    case "d" -> x = (particles1[j].d);
                    default -> x = (particles1[j].z);
                }
                switch (yAxis) {
                    case "z" -> y = (particles1[j].z);
                    case "px" -> y = (particles1[j].px);
                    case "d" -> y = (particles1[j].d);
                    default -> y = (particles1[j].x);
                }
            }
            graphData.get(j).add(x);
            graphData.get(j).add(y);
        }
        return graphData;
    }

    public static ArrayList<ArrayList<Double>> getCustomGraphData(){
        ArrayList<ArrayList<Double>> graphData = new ArrayList<>();
        for (int j = 0; j < Config.numberOfParticles; j++) {
            graphData.add(new ArrayList<>());
            graphData.get(j).add(particles1[j].z);
            graphData.get(j).add(ParticlesMovement.dipoleMomsY[j]);
        }
        return graphData;
    }

    public static int[] findHighlightedIndices(){
        int[] ans = new int[2];
        ans[0]=-1;
        ans[1]=-1;
        if(highlightedParticle!=null) {
            if (highlightedParticle1 != null) {
                for (int j = 0; j < Config.numberOfParticles; j++) {
                    if (particles1[j] == highlightedParticle) ans[0] = j;
                    if (particles2[j] == highlightedParticle1) ans[1] = j;
                }
            }
        }
        return ans;
    }

    public static void countFreqByIntensity(){
        String fileName;
        if(Config.isMomentHistoryY) fileName = Config.GRAPH2_URL;
        else fileName = Config.GRAPH1_URL;
        try {
            FileWriter writer = new FileWriter(fileName);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < Config.intensitySteps; i++) {
            /*Config.intensity = i*(Config.upperIntensity-Config.lowerIntensity)/Config.intensitySteps+Config.lowerIntensity;
            System.out.println(Config.intensity);*/
            Config.Zx = i*(Config.upperIntensity-Config.lowerIntensity)/Config.intensitySteps+Config.lowerIntensity;
            System.out.println(Config.Zx);
            toInitialParameters();
            double[] freqs = SpectrumHandling.findFreq(countSpectrum());
            for (double freq : freqs) {
                BufferedWriter bw = null;
                try {
                    bw = new BufferedWriter(new FileWriter(fileName, true));
                    bw.write(Config.Zx+" "+freq);
                    bw.newLine();
                    bw.flush();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                } finally {
                    if (bw != null) try {
                        bw.close();
                    } catch (IOException ignored){

                    }
                }
            }
            System.out.println("step " + i);
        }
        System.out.println("done!");
    }

    public static double[] countSpectrum(){
        for(int p=0;p<100;p++) {
            //System.out.println("beginning"+(p+1)+"/100");
            for (int k = 0; k < 100; k++) {
                update();
            }
        }
        return SpectrumHandling.calculateSpectrum(pickUpD);
    }

    public static ArrayList<Double> getPickUpD(){
        return pickUpD;
    }
}

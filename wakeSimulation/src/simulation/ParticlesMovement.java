package simulation;

import company.Config;
import objects.PartPair;
import objects.Particle;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class ParticlesMovement {
    public static final double[] dipoleMomsX = new double[Config.numberOfParticles];
    public static final double[] dipoleMomsY = new double[Config.numberOfParticles];
    private static double dipoleMomX = 0;
    private static double dipoleMomY = 0;

    public static void transportBeam(Particle[] ps){
        if(Config.isWakeOn) {
            Arrays.sort(ps, Comparator.comparingDouble(a -> a.z));
            dipoleMomsX[0] = ps[0].x / Config.numberOfParticles;
            dipoleMomsY[0] = ps[0].y / Config.numberOfParticles;
            for (int j = 1; j < Config.numberOfParticles; j++) {
                dipoleMomsX[j] = dipoleMomsX[j - 1] + ps[j].x / Config.numberOfParticles;
                dipoleMomsY[j] = dipoleMomsY[j - 1] + ps[j].y / Config.numberOfParticles;
            }

            for (int j = 0; j < Config.numberOfParticles; j++) {
                dipoleMomX = dipoleMomsX[j] - 0.5 * ps[j].x / Config.numberOfParticles;
                dipoleMomY = dipoleMomsY[j] - 0.5 * ps[j].y / Config.numberOfParticles;
                ps[j].px +=dipoleMomX *Config.wake;
                ps[j].py +=dipoleMomY *Config.wake;
            }
        }
        for (int j = 0; j < Config.numberOfParticles; j++) {
            ParticleMovement.periodTransfer(ps[j]);
        }
    }

    public static void beamBeam(Particle[] ps1, Particle[] ps2){
        if(Config.isFullBeamBeam){
            PartPair[] pairs = prepareToBeamBeam(ps1, ps2);
            for (int i = 0; i < Config.numberOfParticles* Config.numberOfParticles; i++) {
                ParticleMovement.beamBeamStep(pairs[i]);
            }
        } else if (Config.isBeamBeamInSpace){
            for (Particle p : ps1) {
                ParticleMovement.spaceTransfer(p,-p.z);
            }
            for (Particle p : ps2) {
                ParticleMovement.spaceTransfer(p,-p.z);
            }
            fullBeamBeamKick(ps1,ps2);
            for (Particle p : ps1) {
                ParticleMovement.spaceTransfer(p,p.z);
            }
            for (Particle p : ps2) {
                ParticleMovement.spaceTransfer(p,p.z);
            }
        } else {
            for (Particle p : ps1) {
                ParticleMovement.phaseTransfer(p,-p.z,-p.z,0);
            }
            for (Particle p : ps2) {
                ParticleMovement.phaseTransfer(p,-p.z,-p.z,0);
            }
            fullBeamBeamKick(ps1,ps2);
            for (Particle p : ps1) {
                ParticleMovement.phaseTransfer(p,p.z,p.z,0);
            }
            for (Particle p : ps2) {
                ParticleMovement.phaseTransfer(p,p.z,p.z,0);
            }
        }
    }

    private static void fullBeamBeamKick(Particle[] ps1, Particle[] ps2){
        double ps1XSum = particlesXSum(ps1);
        double ps2XSum = particlesXSum(ps2);
        double ps1YSum = particlesYSum(ps1);
        double ps2YSum = particlesYSum(ps2);
        if(Config.isDifferentXYIntensities) {
            for (Particle p : ps1) {
                p.px -= Config.intensity * (p.x - ps2XSum / Config.numberOfParticles) / 2;
                p.py -= Config.intensity * (p.y - ps2YSum / Config.numberOfParticles) * 2;
            }
            for (Particle p : ps2) {
                p.px -= Config.intensity * (p.x - ps1XSum / Config.numberOfParticles) / 2;
                p.py -= Config.intensity * (p.y - ps1YSum / Config.numberOfParticles) * 2;
            }
        } else if(Config.isDifferentBeamsIntensities){
            for (Particle p : ps1) {
                p.px -= Config.intensity * (p.x - ps2XSum / Config.numberOfParticles)*2;
                p.py -= Config.intensity * (p.y - ps2YSum / Config.numberOfParticles)*2;
            }
            for (Particle p : ps2) {
                p.px -= Config.intensity * (p.x - ps1XSum / Config.numberOfParticles)/2;
                p.py -= Config.intensity * (p.y - ps1YSum / Config.numberOfParticles)/2;
            }
        } else {
            for (Particle p : ps1) {
                p.px -= Config.intensity * (p.x - ps2XSum / Config.numberOfParticles);
                p.py -= Config.intensity * (p.y - ps2YSum / Config.numberOfParticles);
            }
            for (Particle p : ps2) {
                p.px -= Config.intensity * (p.x - ps1XSum / Config.numberOfParticles);
                p.py -= Config.intensity * (p.y - ps1YSum / Config.numberOfParticles);
            }
        }
    }

    public static void solenoidsRotation(Particle[] ps){
        for(Particle p: ps){
            p.px+=Config.L*p.py;
            p.py-=Config.L*p.px;
        }
    }

    private static double particlesXSum(Particle[] ps){
        double sum = 0;
        for(Particle p: ps) sum+=p.x;
        return sum;
    }
    private static double particlesYSum(Particle[] ps){
        double sum = 0;
        for(Particle p: ps) sum+=p.y;
        return sum;
    }

    public static PartPair[] prepareToBeamBeam(Particle[] ps1, Particle[] ps2){
        PartPair[] pairs = new PartPair[Config.numberOfParticles* Config.numberOfParticles];
        for (int i = 0; i < Config.numberOfParticles; i++) {
            for (int j = 0; j < Config.numberOfParticles; j++) {
                pairs[Config.numberOfParticles*i+j] = new PartPair(ps1[i],ps2[j]);
            }
        }
        Arrays.sort(pairs, Comparator.comparingDouble(a -> (a.distance)));
        Collections.reverse(Arrays.asList(pairs));
        return pairs;
    }

    public static void equationsPeriod(Particle[] ps){
        for (int i = 0; i < (int) ((2 * Math.PI) / Config.timeStep); i++) {
            Arrays.sort(ps, Comparator.comparingDouble(a -> a.z));
            dipoleMomX = 0;
            dipoleMomY = 0;
            ParticleMovement.moveParticleStep(ps[0]);
            dipoleMomsX[0] = ps[0].x / Config.numberOfParticles;
            dipoleMomsY[0] = ps[0].y / Config.numberOfParticles;
            for (int j = 1; j < Config.numberOfParticles; j++) {
                dipoleMomsX[j] = dipoleMomsX[j - 1] + ps[j].x / Config.numberOfParticles;
                dipoleMomX = dipoleMomsX[j - 1] + 0.5 * ps[j].x / Config.numberOfParticles;
                dipoleMomsY[j] = dipoleMomsY[j - 1] + ps[j].y / Config.numberOfParticles;
                dipoleMomY = dipoleMomsY[j - 1] + 0.5 * ps[j].y / Config.numberOfParticles;
                ParticleMovement.moveParticleStep(ps[j]);
            }
        }
    }

    public static double getDipoleMomX() {
        return dipoleMomX;
    }
    public static double getDipoleMomY() {
        return dipoleMomY;
    }
}
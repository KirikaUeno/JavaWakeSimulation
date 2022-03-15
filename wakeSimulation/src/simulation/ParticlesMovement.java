package simulation;

import company.Config;
import objects.PartPair;
import objects.Particle;
import org.apache.commons.math3.analysis.function.Sqrt;

import java.util.*;

public class ParticlesMovement {
    public static final double[] dipoleMomsX = new double[Config.numberOfParticles];
    public static final double[] dipoleMomsY = new double[Config.numberOfParticles];
    private static double dipoleMomX = 0;
    private static double dipoleMomY = 0;
    public static double sizeX = 0;

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
        sizeX=0;
        double meanX=0;
        for (Particle p : ps1) {
            meanX+=p.x/Config.numberOfParticles;
        }
        for (Particle p : ps1) {
            sizeX+=(p.x-meanX)*(p.x-meanX)/Config.numberOfParticles;
        }
        sizeX= Math.sqrt(sizeX);
        double sizeX1=0;
        meanX=0;
        for (Particle p : ps2) {
            meanX+=p.x/Config.numberOfParticles;
        }
        for (Particle p : ps2) {
            sizeX1+=(p.x-meanX)*(p.x-meanX)/Config.numberOfParticles;
        }
        sizeX1= Math.sqrt(sizeX1);
        sizeX=(sizeX1+sizeX)/2;
        if(Config.isFullBeamBeam){
            PartPair[] pairs = prepareToBeamBeam(ps1, ps2);
            if (Config.isBeamBeamInSpace){
                for (Particle p : ps1) {
                    ParticleMovement.spaceTransfer(p,p.z/Config.beta);
                }
                for (Particle p : ps2) {
                    ParticleMovement.spaceTransfer(p,p.z/Config.beta);
                }
                for (int i = 0; i < Config.numberOfParticles* Config.numberOfParticles; i++) {
                    ParticleMovement.beamBeamStep(pairs[i]);
                }
                for (Particle p : ps1) {
                    ParticleMovement.spaceTransfer(p,-p.z/Config.beta);
                }
                for (Particle p : ps2) {
                    ParticleMovement.spaceTransfer(p,-p.z/Config.beta);
                }
            } else {
                for (Particle p : ps1) {
                    ParticleMovement.phaseTransfer(p,p.z/Config.beta,p.z/Config.beta,0);
                }
                for (Particle p : ps2) {
                    ParticleMovement.phaseTransfer(p,p.z/Config.beta,p.z/Config.beta,0);
                }
                for (int i = 0; i < Config.numberOfParticles* Config.numberOfParticles; i++) {
                    ParticleMovement.beamBeamStep(pairs[i]);
                }
                for (Particle p : ps1) {
                    ParticleMovement.phaseTransfer(p,-p.z/Config.beta,-p.z/Config.beta,0);
                }
                for (Particle p : ps2) {
                    ParticleMovement.phaseTransfer(p,-p.z/Config.beta,-p.z/Config.beta,0);
                }
            }
        } else if (Config.isBeamBeamInSpace){
            for (Particle p : ps1) {
                ParticleMovement.spaceTransfer(p,-p.z/Config.beta);
            }
            for (Particle p : ps2) {
                ParticleMovement.spaceTransfer(p,-p.z/Config.beta);
            }
            fullBeamBeamKick(ps1,ps2);
            for (Particle p : ps1) {
                ParticleMovement.spaceTransfer(p,p.z/Config.beta);
            }
            for (Particle p : ps2) {
                ParticleMovement.spaceTransfer(p,p.z/Config.beta);
            }
        } else {
            for (Particle p : ps1) {
                ParticleMovement.phaseTransfer(p,-p.z/Config.beta,-p.z/Config.beta,0);
            }
            for (Particle p : ps2) {
                ParticleMovement.phaseTransfer(p,-p.z/Config.beta,-p.z/Config.beta,0);
            }
            fullBeamBeamKick(ps1,ps2);
            for (Particle p : ps1) {
                ParticleMovement.phaseTransfer(p,p.z/Config.beta,p.z/Config.beta,0);
            }
            for (Particle p : ps2) {
                ParticleMovement.phaseTransfer(p,p.z/Config.beta,p.z/Config.beta,0);
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
            /*for (Particle p : ps1) {
                p.px -= Config.intensity * (p.x - ps2XSum / Config.numberOfParticles);
                p.py -= Config.intensity * (p.y - ps2YSum / Config.numberOfParticles);
            }
            for (Particle p : ps2) {
                p.px -= Config.intensity * (p.x - ps1XSum / Config.numberOfParticles);
                p.py -= Config.intensity * (p.y - ps1YSum / Config.numberOfParticles);
            }*/
            if(Config.countOnlyOneDimension) {
                Arrays.stream(ps1).parallel().forEach(p->p.px -= Config.intensity * (particlesXWeightSum(p, ps2) / Config.numberOfParticles));
                Arrays.stream(ps2).parallel().forEach(p->p.px -= Config.intensity * (particlesXWeightSum(p, ps1) / Config.numberOfParticles));
                /*for (Particle p : ps1) {
                    p.px -= Config.intensity * (particlesXWeightSum(p, ps2) / Config.numberOfParticles);
                }
                for (Particle p : ps2) {
                    p.px -= Config.intensity * (particlesXWeightSum(p, ps1) / Config.numberOfParticles);
                }*/
            } else {
                for (Particle p : ps1) {
                    p.px -= Config.intensity * (particlesXWeightSum(p, ps2) / Config.numberOfParticles);
                    p.py -= Config.intensity * (particlesYWeightSum(p, ps2) / Config.numberOfParticles);
                }
                for (Particle p : ps2) {
                    p.px -= Config.intensity * (particlesXWeightSum(p, ps1) / Config.numberOfParticles);
                    p.py -= Config.intensity * (particlesYWeightSum(p, ps1) / Config.numberOfParticles);
                }
            }
        }
    }

    public static double kick(double r){
        return (r==0)?0.5:Math.exp(-r*r/2)-(1-Math.exp(-r*r/2))/(r*r);
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

    private static double particlesXWeightSum(Particle p, Particle[] ps){
        double sum = 0;
        for (Particle p1: ps) {
            sum += (p.x - p1.x) * kick(Config.alfaDivSigX * (p.z - p1.z));
        }
        return sum;
    }
    private static double particlesYWeightSum(Particle p, Particle[] ps){
        double sum = 0;
        for (Particle p1: ps) {
            sum += (p.y - p1.y) * kick(Config.alfaDivSigX * (p.z - p1.z));
        }
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

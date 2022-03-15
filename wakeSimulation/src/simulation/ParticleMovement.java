package simulation;

import company.Config;
import objects.PartPair;
import objects.Particle;

public class ParticleMovement {
    private static double wx;
    private static double wy;
    private static double wz;

    public static void periodTransfer(Particle p){
        phaseTransfer(p,2*Math.PI* Config.xFreq,2*Math.PI* Config.yFreq,2*Math.PI* Config.zFreq);
    }

    public static void phaseTransfer(Particle p, double phaseX, double phaseY, double phaseZ){
        double x1mid;
        double px1mid;
        double y1mid;
        double py1mid;
        double zMid;
        double dMid;
        x1mid = p.x *Math.cos(phaseX)+p.px*Math.sin(phaseX);
        px1mid = -p.x *Math.sin(phaseX)+p.px*Math.cos(phaseX);
        if(!Config.countOnlyOneDimension) {
            y1mid = p.y * Math.cos(phaseY) + p.py * Math.sin(phaseY);
            py1mid = -p.y * Math.sin(phaseY) + p.py * Math.cos(phaseY);
            p.y =y1mid;
            p.py =py1mid;
        }
        zMid = p.z*Math.cos(phaseZ)-p.d*Math.sin(phaseZ);
        dMid = p.z*Math.sin(phaseZ)+p.d*Math.cos(phaseZ);
        p.x =x1mid;
        p.px =px1mid;
        p.z=zMid;
        p.d=dMid;
    }

    public static void spaceTransfer(Particle p, double z){
        p.x += p.px * z;
        if(!Config.countOnlyOneDimension) p.y += p.py * z;
    }

    public static void beamBeamKick(Particle p1, Particle p2){
        double kick=0;
        if(Config.countNonlinearities){
            double xDiffer = (p1.x - p2.x)/ParticlesMovement.sizeX;
            kick=4*Config.intensity * ((1-Math.exp(-xDiffer*xDiffer/2))/xDiffer) / Config.numberOfParticles;
        }
        else{
            double xDiffer = p1.x - p2.x;
            kick = Config.intensity * xDiffer / Config.numberOfParticles;
        }
        p1.px -= kick;
        p2.px += kick;
        if(!Config.countOnlyOneDimension) {
            double yDiffer = p1.y - p2.y;
            p1.py -= Config.intensity * yDiffer / Config.numberOfParticles;
            p2.py += Config.intensity * yDiffer / Config.numberOfParticles;
        }
    }

    public static void beamBeamStep(PartPair pp) {
        if(Config.isBeamBeamInSpace) {
            spaceTransfer(pp.p1, -(pp.distance/Config.beta) / 2);
            spaceTransfer(pp.p2, -(pp.distance/Config.beta) / 2);
            beamBeamKick(pp.p1,pp.p2);
            spaceTransfer(pp.p1, (pp.distance/Config.beta) / 2);
            spaceTransfer(pp.p2, (pp.distance/Config.beta) / 2);
        } else {
            phaseTransfer(pp.p1, -(pp.distance/Config.beta) / 2, -(pp.distance/Config.beta) / 2, 0);
            phaseTransfer(pp.p2, -(pp.distance/Config.beta) / 2, -(pp.distance/Config.beta) / 2, 0);
            beamBeamKick(pp.p1,pp.p2);
            phaseTransfer(pp.p1, (pp.distance/Config.beta) / 2, (pp.distance/Config.beta) / 2, 0);
            phaseTransfer(pp.p2, (pp.distance/Config.beta) / 2, (pp.distance/Config.beta) / 2, 0);
        }
    }

    public static void moveParticleStep(Particle p){
        double xMid;
        double pxMid;
        double yMid;
        double pyMid;
        double zMid;
        double dMid;

        if(Config.isEuler) {
            //euler
            wx = Config.xFreq*Math.PI*2;
            wy = Config.xFreq*Math.PI*2;
            wz = Config.zFreq*Math.PI*2;
            xMid = p.x + Config.timeStep * p.px * 0.5 * wx;
            pxMid = p.px + 0.5 * Config.timeStep * (-p.x * wx + Config.wake * ParticlesMovement.getDipoleMomX());
            yMid = p.y + Config.timeStep * p.py * 0.5 * wy;
            pyMid = p.py + 0.5 * Config.timeStep * (-p.y * wy + Config.wake * ParticlesMovement.getDipoleMomY());
            zMid = p.z - 0.5 * Config.timeStep * p.d;
            dMid = p.d + 0.5 * Config.timeStep * (p.z * wz / Config.eta + p.x * 0);

            p.x += Config.timeStep * pxMid * wx;
            p.px += Config.timeStep * (-xMid * wx + Config.wake * ParticlesMovement.getDipoleMomX());
            p.y += Config.timeStep * pyMid * wy;
            p.py += Config.timeStep * (-yMid * wy + Config.wake * ParticlesMovement.getDipoleMomY());
            p.z += -Config.timeStep * dMid;
            p.d += Config.timeStep * (zMid * wz / Config.eta + xMid * 0);
        } else {
            //runge kutti 4
            xMid = p.x + (k1("x",p.x, p.px, p.y,p.py, p.z, p.d) + 2 * k2("x",p.x, p.px, p.y,p.py, p.z, p.d) + 2 * k3("x",p.x, p.px, p.y,p.py, p.z, p.d) + k4("x",p.x, p.px, p.y,p.py, p.z, p.d)) / 6;
            pxMid = p.px + (k1("px",p.x, p.px, p.y,p.py, p.z, p.d) + 2 * k2("px",p.x, p.px, p.y,p.py, p.z, p.d)+ 2 * k3("px",p.x, p.px, p.y,p.py, p.z, p.d) + k4("px",p.x, p.px, p.y,p.py, p.z, p.d)) / 6;
            yMid = p.y + (k1("y",p.x, p.px, p.y,p.py, p.z, p.d) + 2 * k2("y",p.x, p.px, p.y,p.py, p.z, p.d) + 2 * k3("y",p.x, p.px, p.y,p.py, p.z, p.d) + k4("y",p.x, p.px, p.y,p.py, p.z, p.d)) / 6;
            pyMid = p.py + (k1("py",p.x, p.px, p.y,p.py, p.z, p.d) + 2 * k2("py",p.x, p.px, p.y,p.py, p.z, p.d)+ 2 * k3("py",p.x, p.px, p.y,p.py, p.z, p.d) + k4("py",p.x, p.px, p.y,p.py, p.z, p.d)) / 6;
            zMid = p.z + (k1("z",p.x, p.px, p.y,p.py, p.z, p.d) + 2 * k2("z",p.x, p.px, p.y,p.py, p.z, p.d) + 2 * k3("z",p.x, p.px, p.y,p.py, p.z, p.d) + k4("z",p.x, p.px, p.y,p.py, p.z, p.d)) / 6;
            dMid = p.d + (k1("d",p.x, p.px, p.y,p.py, p.z, p.d) + 2 * k2("d",p.x, p.px, p.y,p.py, p.z, p.d) + 2 * k3("d",p.x, p.px, p.y,p.py, p.z, p.d) + k4("d",p.x, p.px, p.y,p.py, p.z, p.d)) / 6;
            p.x = xMid;
            p.px = pxMid;
            p.y = yMid;
            p.py = pyMid;
            p.z = zMid;
            p.d = dMid;
        }
    }

    private static double f(String var, double x, double px, double y, double py, double z, double d){
        switch (var) {
            case "x" -> {
                return px * wx;
            }
            case "px" ->{
                return -x* wx + Config.wake * ParticlesMovement.getDipoleMomX();
            }
            case "y" -> {
                return py * wy;
            }
            case "py" ->{
                return -y* wy + Config.wake * ParticlesMovement.getDipoleMomY();
            }
            case "z" -> {
                return -d;
            }
            case "d" ->{
                return z* wz/ Config.eta + x* 0;
            }
        }
        return px * wx;
    }

    private static double k1(String var, double x, double px, double y, double py, double z, double d){
        return f(var,x,px,y,py,z,d)* Config.timeStep;
    }

    private static double k2(String var, double x, double px, double y, double py, double z, double d){
        return f(var,x+k1("x",x,px,y,py,z,d)/2,px+k1("px",x,px,y,py,z,d)/2, y+k1("y",x,px,y,py,z,d)/2,py+k1("py",x,px,y,py,z,d)/2, z+k1("z",x,px,y,py,z,d)/2,  d+k1("d",x,px,y,py,z,d)/2)* Config.timeStep;
    }

    private static double k3(String var, double x, double px, double y, double py, double z, double d){
        return f(var,x+k2("x",x,px,y,py,z,d)/2,px+k2("px",x,px,y,py,z,d)/2, y+k2("y",x,px,y,py,z,d)/2,py+k2("py",x,px,y,py,z,d)/2, z+k2("z",x,px,y,py,z,d)/2,  d+k2("d",x,px,y,py,z,d)/2)* Config.timeStep;
    }

    private static double k4(String var, double x, double px, double y, double py, double z, double d){
        return f(var,x+k3("x",x,px,y,py,z,d),px+k3("px",x,px,y,py,z,d), y+k3("y",x,px,y,py,z,d),py+k3("py",x,px,y,py,z,d), z+k3("z",x,px,y,py,z,d),  d+k3("d",x,px,y,py,z,d))* Config.timeStep;
    }
}

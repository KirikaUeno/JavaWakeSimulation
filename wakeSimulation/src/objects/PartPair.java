package objects;

public class PartPair {
    public Particle p1;
    public Particle p2;
    public double distance;

    public PartPair(Particle p1, Particle p2){
        this.p1=p1;
        this.p2=p2;
        this.distance = p1.z+p2.z;
    }
}

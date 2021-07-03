package objects;

public class Particle{
    public double x;
    public double px;
    public double z;
    public double d;
    public double y;
    public double py;
    public int index = 0;

    public Particle(double x, double px, double z, double d, double y, double py){
        this.x = x;
        this.px = px;
        this.z = z;
        this.d = d;
        this.y = y;
        this.py = py;
    }

    public String toString(){
        return (""+z);
    }
}

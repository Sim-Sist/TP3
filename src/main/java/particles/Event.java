package main.java.particles;

public class Event implements Comparable<Event> {
    private Double time;
    private Particle p1, p2;

    public Event(Double time, Particle p1, Particle p2) {

        this.time = time > 0 ? time : null;
        this.p1 = p1;
        this.p2 = p2;
    }

    public Event(Double time, Particle p) {
        this.time = time > 0 ? time : null;
        this.p1 = p;
        this.p2 = null;
    }

    public Double getTime() {
        return time;
    }

    public Particle getP1() {
        return p1;
    }

    public Particle getP2() {
        return p2;
    }

    public Particle[] getParticles() {
        Particle[] p = new Particle[2];
        p[0] = p1;
        p[1] = p2;
        return p;
    }

    public boolean isWallCollision() {
        return p2 == null;
    }

    public boolean isParticleCollision() {
        return p2 != null;
    }

    @Override
    public int compareTo(Event o) {
        return this.time.compareTo(o.getTime());
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Event))
            return false;
        Event ep = (Event) o;
        return this.time.equals(ep.time) && this.p1.equals(ep.p1) && this.p2.equals(ep.p2);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(String.format("time: %g ", this.time))
                .append(String.format("p1:%d p2:%d\n", this.p1.getIndex(), this.p2.getIndex()));
        return str.toString();
    }
}
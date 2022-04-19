package particles;

import java.lang.RuntimeException;
import java.util.LinkedList;
import java.util.List;

public class Event implements Comparable<Event> {
    private Double time;
    private Particle p1, p2;
    private static final double EPSILON = 0.0001;

    public Event(Double time, Particle p1, Particle p2) {
        if (time < 0) {
            // System.out.println(String.format("Error time less than 0 (%f)",
            // index, time));
        }
        if (time == null) {
            System.out.println(String.format("Error: time null"));
        }
        this.time = time;
        this.p1 = p1;
        this.p2 = p2;
    }

    public Event(Double time, Particle p) {
        this(time, p, null);
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

    public List<Particle> getParticles() {
        List<Particle> list = new LinkedList<>();
        if (p1 != null)
            list.add(p1);
        if (p2 != null)
            list.add(p2);
        return list;
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
        return Math.abs(this.time - ep.getTime()) < EPSILON &&
                this.getParticles().size() == ep.getParticles().size() &&
                this.getParticles().containsAll(ep.getParticles());
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("Event: ");
        str.append(String.format("[ time: %g || ", this.time));
        int index = 0;
        for (Particle p : getParticles()) {
            str.append(String.format("p%d:%d", index++, p.getIndex())).append(' ');
        }
        str.append("]");

        return str.toString();
    }
}
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

    public boolean isWallCollision() {
        return p2 == null;
    }

    public boolean isParticleCollision() {
        return p2 != null;
    }

    public boolean includes(Particle p) {
        return getP1().equals(p) || (isParticleCollision() && getP2().equals(p));
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
                (ep.includes(getP1()) && ep.includes(getP2()));
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("Event: ");
        str.append(String.format("[ time: %g || ", this.time));
        int index = 0;
        str.append(String.format("p%d:%d", index++, getP1().getIndex())).append(' ');
        if (isParticleCollision()) {
            str.append(String.format("p%d:%d", index++, getP2().getIndex())).append(' ');
        }
        str.append("]");

        return str.toString();
    }
}
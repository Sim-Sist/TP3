package particles;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.management.Query;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.lang.Math;

public class CollisionManager {
    private Double spaceSize;
    private List<Particle> particles;
    private double elapsedSimTime = 0;
    private static final double EPSILON = 0.0001;
    public CollisionManager(Particle[] particles, Double spaceSize) {
        this.particles = Arrays.asList(particles);
        this.spaceSize = spaceSize;
    }

    public double updateTime(Event e) {
        double deltaT = e.getTime() - elapsedSimTime;
        elapsedSimTime = e.getTime();
        return deltaT;
    }

    public Set<Event> predictCollisions(Particle p) {
        Set<Event> collisions = new HashSet<>();
        collisions.add(wallCollision(p));
        collisions.addAll(particleCollisions(p, particles));
        return collisions;
    }

    public Event wallCollision(Particle p) {
        Double deltaT = this.getTimeForNearestWall(p);
        if (deltaT < -EPSILON) {
            System.out.println(String.format("Wall collision time less than 0: %f", deltaT));
        }
        return new Event(elapsedSimTime + deltaT, p);
    }

    /**
     * Compute collisions between p and each particle from pList
     */
    public Set<Event> particleCollisions(Particle p, List<Particle> pList) {
        Set<Event> events = new HashSet<>();
        for (Particle p2 : pList) {
            if (p2.equals(p))
                continue;
            Double deltaT = this.getTimeForNearestCollision(p, p2);
            if (deltaT < -EPSILON) {
                System.out.println(String.format("Particle collision time less than 0: %f", deltaT));
            }
            if (!deltaT.isInfinite()) {
                events.add(new Event(elapsedSimTime + deltaT, p, p2));
            }
        }
        return events;
    }

    public Set<Event> computeAllCollisions() {
        Set<Event> collisions = new HashSet<>();
        List<Particle> remainingParticles = new LinkedList<>(particles);

        for (Particle particle : particles) {
            remainingParticles.remove(particle);

            collisions.add(wallCollision(particle));
            collisions.addAll(particleCollisions(particle, remainingParticles));
        }
        return collisions;
    }

    public void resolveCollision(Event e) {
        if (e.isWallCollision()) {
            bounceAgainstWall(e.getP1());
            return;
        }
        updateOnCollision(e.getP1(), e.getP2());
    }

    public void updateOnCollision(Particle p1, Particle p2) {
        Vector dR = deltaR(p1, p2);
        Double impulseX = impulse(p1, p2) * dR.getX() / sigma(p1, p2);
        Double impulseY = impulse(p1, p2) * dR.getY() / sigma(p1, p2);

        p1.updateVelocity(
                p1.getVx() + impulseX / p1.getMass(),
                p1.getVy() + impulseY / p1.getMass());
        p2.updateVelocity(
                p2.getVx() - impulseX / p2.getMass(),
                p2.getVy() - impulseY / p2.getMass());
    }

    private boolean touchesVerticalWall(Particle p) {
        return (p.getX() - p.radius < EPSILON) || (this.spaceSize - (p.getX() + p.radius) < EPSILON);
    }

    private boolean touchesHorizontalWall(Particle p) {
        return (p.getY() - p.radius < EPSILON) || (this.spaceSize - (p.getY() + p.radius) < EPSILON);
    }

    public void bounceAgainstWall(Particle p) {
        if (touchesVerticalWall(p)) {
            p.updateVelocity(-p.getVx(), p.getVy());
        }
        if (touchesHorizontalWall(p)) {
            p.updateVelocity(p.getVx(), -p.getVy());
        }
    }

    private Double impulse(Particle p1, Particle p2) {
        return (2 * p1.mass * p2.mass * deltaRV(p1, p2))
                / (sigma(p1, p2) * (p1.mass + p2.mass));
    }

    private Double getTimeForNearestCollision(Particle p1, Particle p2) {
        if (deltaRV(p1, p2) >= 0)
            return Double.POSITIVE_INFINITY;
        if (valueD(p1, p2) < 0)
            return Double.POSITIVE_INFINITY;
        return calculatedTime(p1, p2);
    }

    private Double getTimeForNearestWall(Particle p) {
        Double timeX, timeY;

        if (p.getVx() > 0)
            timeX = (spaceSize - p.radius - p.getX()) / p.getVx();
        else if (p.getVx() < 0) {
            timeX = (p.radius - p.getX()) / p.getVx();
        } else {
            timeX = Double.POSITIVE_INFINITY;
        }

        if (p.getVy() > 0)
            timeY = (spaceSize - p.radius - p.getY()) / p.getVy();
        else if (p.getVy() < 0) {
            timeY = (p.radius - p.getY()) / p.getVy();
        } else {
            timeY = Double.POSITIVE_INFINITY;
        }
        return Math.min(timeX, timeY);
    }

    private Double sigma(Particle p1, Particle p2) {
        return p1.radius + p2.radius;
    }

    private Vector deltaR(Particle p1, Particle p2) {
        return new Vector(p2.x - p1.x, p2.y - p1.y);
    }

    private Vector deltaV(Particle p1, Particle p2) {
        return new Vector(p2.getVx() - p1.getVx(), p2.getVy() - p1.getVy());
    }

    private Double deltaR2(Particle p1, Particle p2) {
        return deltaR(p1, p2).squared();
    }

    private Double deltaV2(Particle p1, Particle p2) {
        return deltaV(p1, p2).squared();
    }

    private Double deltaRV(Particle p1, Particle p2) {
        return deltaV(p1, p2).times(deltaR(p1, p2));
    }

    private Double valueD(Particle p1, Particle p2) {
        return (Math.pow(deltaRV(p1, p2), 2.0)
                - (deltaV2(p1, p2) * (deltaR2(p1, p2) - Math.pow(sigma(p1, p2), 2.0))));
    }

    private Double calculatedTime(Particle p1, Particle p2) {
        return -(deltaRV(p1, p2) + Math.sqrt(valueD(p1, p2))) / deltaV2(p1, p2);
    }

    private class Vector {
        private Double x;
        private Double y;

        public Vector(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public Double getX() {
            return x;
        }

        public Double getY() {
            return y;
        }

        public double times(Vector v) {
            return this.getX() * v.getX() + this.getY() * v.getY();
        }

        public double squared() {
            return this.times(this);
        }
    }
}
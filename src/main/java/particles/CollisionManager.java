package particles;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.lang.Math;

public class CollisionManager {
    private Double spaceSize;
    private Particle[] particles;
    private double elapsedSimTime = 0;
    private static final double EPSILON = 0.0001;
    public CollisionManager(Particle[] particles, Double spaceSize) {
        this.particles = particles;
        this.spaceSize = spaceSize;
    }

    public double updateTime(Event e) {
        double deltaT = e.getTime() - elapsedSimTime;
        elapsedSimTime = e.getTime();
        return deltaT;
    }

    public Set<Event> wallCollision() {
        return wallCollision(particles);
    }

    public Set<Event> wallCollision(List<Particle> p) {
        return wallCollision((Particle[]) p.toArray(Particle[]::new));
    }

    public Set<Event> wallCollision(Particle p1) {
        return wallCollision(new Particle[] { p1 });
    }

    public Set<Event> wallCollision(Particle[] p) {
        Set<Event> events = new HashSet<>();
        for (Particle p1 : p) {
            Double deltaT = this.getTimeForNearestWall(p1);
            if (deltaT < 0) {
                System.out.println(String.format("Wall collision time less than 0: %f", deltaT));
            }
            events.add(new Event(elapsedSimTime + deltaT, p1));
        }
        return events;
    }


    public Set<Event> particleCollision() {
        return particleCollision(particles);
    }

    public Set<Event> particleCollision(List<Particle> p) {
        return particleCollision((Particle[]) p.toArray(Particle[]::new));

    }

    public Set<Event> particleCollision(Particle p1, Particle p2) {
        return particleCollision(new Particle[] { p1, p2 });
    }

    public Set<Event> particleCollision(Particle[] p) {
        Set<Event> events = new HashSet<>();
        for (Particle p1 : p) {
            for (Particle p2 : particles) {
                Double deltaT = this.getTimeForNearestCollision(p1, p2);
                if (deltaT < 0) {
                    System.out.println(String.format("Particle collision time less than 0: %f", deltaT));
                }
                if (!deltaT.isInfinite()) {
                    events.add(new Event(elapsedSimTime + deltaT, p1, p2));
                }
            }
        }
        return events;
    }


    public Set<Event> computeCollisions() {
        Set<Event> events = new HashSet<>();
        List<Particle> particlesToCompute = new LinkedList<>(Arrays.asList(particles));

        for (Particle particle : particles) {
            particlesToCompute.remove(particle);

            // compute all collisions and keep the first one
            SortedSet<Event> particleCollisionsSet = new TreeSet<>(Event::compareTo);
            particleCollisionsSet.addAll(wallCollision(particle));
            for (Particle p2 : particlesToCompute) {
                particleCollisionsSet.addAll(particleCollision(particle, p2));
            }

            events.add(particleCollisionsSet.first());
        }
        return events;
    }

    public void collision(List<Particle> particles) {
        if (particles.size() == 1)
            updateOnCollision(particles.get(0));
        else
            updateOnCollision(particles.get(0), particles.get(1));
    }

    public void updateOnCollision(Particle p1, Particle p2) {
        Double impulseX = impulse(p1, p2) * Math.abs((p1.getX() - p2.getX()) / sigma(p1, p2));
        Double impulseY = impulse(p1, p2) * Math.abs((p1.getY() - p2.getY()) / sigma(p1, p2));

        p1.updateVelocity(p1.getVx() + impulseX / p1.getMass(), p1.getVy() + impulseY / p1.getMass());
        p2.updateVelocity(p2.getVx() + impulseX / p2.getMass(), p2.getVy() + impulseY / p2.getMass());
    }

    private boolean touchesVerticalWall(Particle p) {
        return (p.getX() - p.radius < EPSILON) || (this.spaceSize - (p.getX() + p.radius) < EPSILON);
    }

    private boolean touchesHorizontalWall(Particle p) {
        return (p.getY() - p.radius < EPSILON) || (this.spaceSize - (p.getY() + p.radius) < EPSILON);
    }

    public void updateOnCollision(Particle p) {
        if (touchesVerticalWall(p)) {
            p.updateVelocity(-p.getVx(), p.getVy());
        }
        if (touchesHorizontalWall(p)) {
            p.updateVelocity(p.getVx(), -p.getVy());
        }
    }

    private Double impulse(Particle p1, Particle p2) {
        return 2 * p1.mass * p2.mass * (deltaRV(deltaR(p1, p2), deltaV(p1, p2))) / sigma(p1, p2) * (p1.mass + p2.mass);
    }

    private Double getTimeForNearestCollision(Particle p1, Particle p2) {
        if (deltaRV(deltaR(p1, p2), deltaV(p1, p2)) >= 0)
            return Double.POSITIVE_INFINITY;
        if (valueD(p1, p2) < 0)
            return Double.POSITIVE_INFINITY;
        return calculatedTime(p1, p2);
    }

    private Double getTimeForNearestWall(Particle p) {
        if (p.getVelocity() == 0) {
            return Double.POSITIVE_INFINITY;
        }
        Double timeX, timeY;

        if (p.getVx() == 0)
            timeX = Double.POSITIVE_INFINITY;
        else if (p.getVx() < 0) {
            timeX = (0 + p.radius - p.getX()) / p.getVx();
        } else {
            timeX = (spaceSize - p.radius - p.getX()) / p.getVx();
        }

        if (p.getVy() == 0)
            timeY = Double.POSITIVE_INFINITY;
        else if (p.getVy() < 0) {
            timeY = (0 + p.radius - p.getY()) / p.getVy();
        } else {
            timeY = (spaceSize - p.radius - p.getY()) / p.getVy();
        }
        return Math.min(timeX, timeY);
    }

    private Double sigma(Particle p1, Particle p2) {
        return p1.radius + p2.radius;
    }

    private Double[] deltaR(Particle p1, Particle p2) {
        Double[] deltaR = new Double[2];
        deltaR[0] = p1.x - p2.x;
        deltaR[1] = p1.y - p2.y;
        return deltaR;
    }

    private Double[] deltaV(Particle p1, Particle p2) {
        Double[] deltaV = new Double[2];
        deltaV[0] = p1.getVx() - p2.getVx();
        deltaV[1] = p1.getVy() - p2.getVy();
        return deltaV;
    }

    private Double deltaR2(Double[] deltaR) {
        return deltaR[0] * deltaR[0] + deltaR[1] * deltaR[1];
    }

    private Double deltaV2(Double[] deltaV) {
        return deltaV[0] * deltaV[0] + deltaV[1] * deltaV[1];
    }

    private Double deltaRV(Double[] deltaR, Double[] deltaV) {
        return deltaR[0] * deltaV[0] + deltaR[1] * deltaV[1];
    }

    private Double valueD(Particle p1, Particle p2) {
        Double[] deltaRValue = deltaR(p1, p2);
        Double[] deltaVValue = deltaV(p1, p2);
        return (Math.pow(deltaRV(deltaRValue, deltaVValue), 2.0)
                - (deltaV2(deltaVValue) * (deltaR2(deltaRValue) - Math.pow(sigma(p1, p2), 2.0))));
    }

    private Double calculatedTime(Particle p1, Particle p2) {
        Double[] deltaVValue = deltaV(p1, p2);
        Double[] deltaRValue = deltaV(p1, p2);
        Double deltaRVValue = deltaRV(deltaRValue, deltaVValue);

        return (deltaRVValue + Math.sqrt(valueD(p1, p2))) / deltaV2(deltaVValue);
    }
}
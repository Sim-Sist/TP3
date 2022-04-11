package main.java.particles;

import java.util.Set;
import java.util.HashSet;
import java.lang.Math;

public class CollisionManager {
    private Double spaceSize;
    private Particle[] particles;

    public CollisionManager(Particle[] particles, Double spaceSize) {
        this.particles = particles;
        this.spaceSize = spaceSize;
    }

    public Set<Event> wallCollision() {
        Set<Event> events = new HashSet<>();
        for (Particle p : particles) {

            Double time = this.getTimeForNearestWall(p);
            events.add(new Event(time, p));
        }
        return events;
    }

    public Set<Event> wallCollision(Particle[] p) {
        Set<Event> events = new HashSet<>();
        for (Particle p1 : p) {
            Double time = this.getTimeForNearestWall(p1);
            events.add(new Event(time, p1));
        }
        return events;
    }

    public Set<Event> particleCollision() {
        Set<Event> events = new HashSet<>();
        for (int i = 0; i < particles.length; i++) {
            for (int j = i + 1; j < particles.length; j++) {
                Double time = this.getTimeForNearestCollision(particles[i], particles[j]);
                if (time != null) {
                    events.add(new Event(time, particles[i], particles[j]));
                }
            }
        }
        return events;
    }

    public Set<Event> particleCollision(Particle[] p) {
        Set<Event> events = new HashSet<>();
        for (int i = 0; i < p.length; i++) {
            for (int j = i + 1; j < particles.length; j++) {
                Double time = this.getTimeForNearestCollision(p[i], particles[j]);
                if (time != null) {
                    events.add(new Event(time, p[i], p[j]));
                }
            }
        }
        return events;
    }

    public Set<Event> computeCollisions() {
        Set<Event> events = new HashSet<>();
        events.addAll(this.wallCollision());
        events.addAll(this.particleCollision());
        return events;
    }

    public void collision(Particle[] particles) {
        if (particles[1] == null)
            updateOnCollision(particles[0]);
        else
            updateOnCollision(particles[0], particles[1]);
    }

    public void updateOnCollision(Particle p1, Particle p2) {
        Double impulseX = impulse(p1, p2) * Math.abs((p1.getX() - p2.getX()) / sigma(p1, p2));
        Double impulseY = impulse(p1, p2) * Math.abs((p1.getY() - p2.getY()) / sigma(p1, p2));

        p1.updateVelocity(p1.getVx() + impulseX / p1.getMass(), p1.getVy() + impulseY / p1.getMass());
        p2.updateVelocity(p2.getVx() + impulseX / p2.getMass(), p2.getVy() + impulseY / p2.getMass());
    }

    public void updateOnCollision(Particle p) {
        if ((p.getX() - p.radius < 0) || (p.getX() + p.radius > this.spaceSize)) {
            p.updateVelocity(-p.getVx(), p.getVy());
        }
        if ((p.getY() - p.radius < 0) || (p.getY() + p.radius > this.spaceSize)) {
            p.updateVelocity(p.getVx(), -p.getVy());
        }
    }

    private Double impulse(Particle p1, Particle p2) {
        return 2 * p1.mass * p2.mass * (deltaRV(deltaR(p1, p2), deltaV(p1, p2))) / sigma(p1, p2) * (p1.mass + p2.mass);
    }

    private Double getTimeForNearestCollision(Particle p1, Particle p2) {
        if (deltaRV(deltaR(p1, p2), deltaV(p1, p2)) >= 0)
            return null;
        if (valueD(p1, p2) < 0)
            return null;
        return calculatedTime(p1, p2);
    }

    private Double getTimeForNearestWall(Particle p) {
        Double timeX1 = (0 + p.radius - p.getX()) / p.getVx();
        Double timeX2 = (spaceSize - p.radius - p.getX()) / p.getVx();
        Double timeY1 = (0 + p.radius - p.getY()) / p.getVy();
        Double timeY2 = (spaceSize - p.radius - p.getY()) / p.getVy();

        // Nose por que no lo toma, existe el de doubles.
        return Math.min(timeX1, Math.min(timeX2, Math.min(timeY1, timeY2)));
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
        deltaV[0] = p1.velocity - p2.velocity;
        deltaV[1] = p1.speedAngle - p2.speedAngle;
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
        return (Math.pow(deltaRV(deltaRValue, deltaVValue), 2)
                - (deltaV2(deltaVValue) * (deltaR2(deltaRValue) - sigma(p1, p2) * sigma(p1, p2))));
    }

    private Double calculatedTime(Particle p1, Particle p2) {
        Double[] deltaVValue = deltaV(p1, p2);
        Double[] deltaRValue = deltaV(p1, p2);
        Double deltaRVValue = deltaRV(deltaRValue, deltaVValue);

        return -(deltaRVValue + Math.sqrt(valueD(p1, p2))) / deltaV2(deltaVValue);
    }
}
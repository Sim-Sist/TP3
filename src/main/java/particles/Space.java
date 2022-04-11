package main.java.particles;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javafx.scene.effect.ColorInput;
import main.java.output.*;

public class Space {
    /*** Simulation parameters ***/
    private double size;
    private Particle[] particles;
    private double criticalRadius = 50;
    private Double constantRadius = null;
    private Double constantVelocity = null;
    private Double bigRadius = 0.7;
    private Double smallRadius = 0.2;
    private Double bigMass = 2.0;
    private Double littleMass = 0.9;
    private final static double DEFAULT_MIN_RADIUS = 1, DEFAULT_MAX_RADIUS = 10;
    private double minRadius = DEFAULT_MIN_RADIUS, maxRadius = DEFAULT_MAX_RADIUS;
    private final static double DEFAULT_MIN_VELOCITY = 0.03, DEFAULT_MAX_VELOCITY = 0.1;
    private double minVelocity = DEFAULT_MIN_VELOCITY, maxVelocity = DEFAULT_MAX_VELOCITY;
    /*** Output file vars ***/
    private String staticFileName, dynamicFileName;
    /*** Class variables ***/
    private SpaceOutputManager oManager;
    private CollisionManager cManager;
    private double step;
    private double noiseLimit;
    private Set<Event> eventsArray;
    // private Particle[] collisionParticles = new Particle[2];

    // Defaults to random radii between 1 and 10, and default random velocities
    // between 0.3 and 1
    public Space(double size, double criticalRadius, int particlesAmount, double noiseLimit) {
        System.out.println("Space initialized with:");
        System.out.println(particlesAmount + " particles");
        System.out.println("Size of " + size);
        System.out.println("Noise of " + noiseLimit);
        System.out.println();
        this.size = size;
        this.criticalRadius = criticalRadius;
        this.noiseLimit = noiseLimit;
        this.particles = new Particle[particlesAmount];
        this.oManager = new SpaceOutputManager(this);
        this.cManager = new CollisionManager(particles, size);
    }

    // Sets constant radius for all particles
    public void setRadii(double constantRadius) {
        this.constantRadius = constantRadius;
    }

    // Sets particles' radii as a random number between minRadius and maxRadius
    public void setRadii(double minRadius, double maxRadius) {
        this.constantRadius = null;// turn off constant radii
        if (minRadius < 0 || minRadius > maxRadius)
            throw new RuntimeException("Invalid values for radius' limits");
        this.minRadius = minRadius;
        this.maxRadius = maxRadius;
    }

    public void setVelocities(double constantVelocity) {
        this.constantVelocity = constantVelocity;
    }

    public void setVelocities(double minVelocity, double maxVelocity) {
        this.constantVelocity = null;
        if (minVelocity < 0 || minVelocity > maxVelocity)
            throw new RuntimeException("Invalid values for radius' limits");
        this.minVelocity = minVelocity;
        this.maxVelocity = maxVelocity;
    }

    public void initialize() {
        generateSystem();
    }

    private boolean overlaps(Particle p) {
        for (int i = 0; i < particles.length; i++) {
            if (particles[i] == null || particles[i].getIndex() == p.getIndex())
                continue;
            if (p.distanceTo(particles[i]) < 0)
                return true;
        }
        return false;
    }

    private void generateSystem() {
        step = 0;
        Random rnd = new Random();
        for (int i = 0; i < particles.length; i++) {

            double radius;
            double mass;
            double velocity;
            double speedAngle;
            double x, y;
            if (i == 0) {
                radius = bigRadius;
                mass = this.bigMass;
                velocity = 0;
                speedAngle = 0;
                x = this.size / 2;
                y = this.size / 2;
            } else {
                mass = this.littleMass;
                radius = smallRadius;
                velocity = (constantVelocity == null)
                        ? (rnd.nextDouble() * (maxVelocity - minVelocity) + minVelocity)
                        : constantVelocity;
                speedAngle = rnd.nextDouble() * (2 * Math.PI);
                x = rnd.nextDouble() * size;
                y = rnd.nextDouble() * size;
            }

            Particle p = new Particle(
                    i, // index
                    x, // x
                    y, // y
                    velocity, // velocity
                    speedAngle, // speedAngle
                    radius,
                    mass);

            if (p.x < p.radius || (p.x + p.radius) > this.size || p.y < p.radius || (p.y + p.radius) > size) {
                i--;
                continue;
            }
            if (p.radius > 0 && overlaps(p)) {
                i--;
                continue;
            } else {
                particles[i] = p;
            }
        }
        outputInitialState();
        outputNextState();
        step++;
    }

    @SuppressWarnings("unchecked")
    public void computeNextStep() {
        // Si previemente no habia nada, es el primer evento y debo calcular todo
        if (eventsArray.size() == 0) {
            this.eventsArray = cManager.computeCollisions();
        } else {
            // Si ya tiene cosas dentro, debo recalcular solo para las particulas que
            // colisionaron.
            Event prevCollsionEvent = this.eventsArray.stream().min(Event::compareTo).get();

            // TODO: Debo borrar elementos del array? va a ser enorme sino.
            this.eventsArray.addAll(cManager.wallCollision(prevCollsionEvent.getParticles()));
            this.eventsArray.addAll(cManager.particleCollision(prevCollsionEvent.getParticles()));
        }

        Event collsionEvent = this.eventsArray.stream().min(Event::compareTo).get();

        // Llevo todas las particulas hasta el punto de la colision.
        for (Particle p : particles) {
            p.update(collsionEvent.getTime());
        }
        // Guardo el estado del sistema.

        // Veo como rebotan las que colisionaron.
        cManager.collision(collsionEvent.getParticles());
        outputNextState();
        step++;
    }

    public double getSize() {
        return size;
    }

    public Particle[] getParticles() {
        return particles;
    }

    public double getCriticalRadius() {
        return criticalRadius;
    }

    public Set<Event> getEvents() {
        return this.eventsArray;
    }

    public void setStaticFileName(String filename) {
        this.staticFileName = filename;
    }

    public void setDynamicFileName(String filename) {
        this.dynamicFileName = filename;
    }

    private void outputInitialState() {
        boolean success;
        if (staticFileName == null) {
            success = this.oManager.outputInitialState();
        } else {
            success = this.oManager.outputInitialState(staticFileName);
        }
        if (!success) {
            System.out.println("There was an error while generating initial's stat output");
        }
    }

    private void outputNextState() {
        boolean success;
        if (dynamicFileName == null) {
            success = this.oManager.outputState(step);
        } else {
            success = this.oManager.outputState(step, dynamicFileName);
        }
        if (!success) {
            System.out.println("There was an error while generating dynamic states' output");
        }
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("Space:\n");
        for (Particle p : particles) {
            str.append(p).append('\n');
        }
        return str.toString();
    }
}
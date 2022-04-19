package particles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import output.*;
import output.logging.Logger;
import particles.fx.Color;

public class Space {
    /*** Simulation parameters ***/
    private final static double DEFAULT_MIN_RADIUS = 1, DEFAULT_MAX_RADIUS = 10;
    private final static double DEFAULT_MIN_VELOCITY = 0.03, DEFAULT_MAX_VELOCITY = 0.1;
    private double size;
    private Particle[] particles;
    private Double constantRadius = null;
    private Double constantVelocity = null;
    private double bigRadius;
    private double smallRadius;
    private double bigMass;
    private double littleMass;
    private double minRadius = DEFAULT_MIN_RADIUS, maxRadius = DEFAULT_MAX_RADIUS;
    private double minVelocity = DEFAULT_MIN_VELOCITY, maxVelocity = DEFAULT_MAX_VELOCITY;
    /*** Output file vars ***/
    private String staticFileName, dynamicFileName;
    /*** Class variables ***/
    private SpaceOutputManager oManager;
    private Logger logger = new Logger("space");
    private CollisionManager cManager;
    private int step;
    private Queue<Event> eventsQueue = new PriorityQueue<>(Event::compareTo);
    private double elapsedSimTime = 0;
    // private Particle[] collisionParticles = new Particle[2];

    // Defaults to random radii between 1 and 10, and default random velocities
    // between 0.3 and 1
    public Space(double size, int particlesAmount, double smallRadius, double bigRadius, double littleMass,
            double bigMass) {

        StringBuilder msg = new StringBuilder();
        msg.append(String.format(
                "Space initialized with:\n" +
                        "- %d particles\n" +
                        "- Size of %.3f\n" +
                        "\n",
                particlesAmount, size));
        logger.log(msg.toString());
        this.size = size;
        this.particles = new Particle[particlesAmount];
        this.smallRadius = smallRadius;
        this.bigRadius = bigRadius;
        this.littleMass = littleMass;
        this.bigMass = bigMass;
        this.oManager = new SpaceOutputManager(this);
        this.cManager = new CollisionManager(particles, size);
    }

    // // Sets constant radius for all particles
    // public void setRadii(double constantRadius) {
    // this.constantRadius = constantRadius;
    // }

    // // Sets particles' radii as a random number between minRadius and maxRadius
    // public void setRadii(double minRadius, double maxRadius) {
    // this.constantRadius = null;// turn off constant radii
    // if (minRadius < 0 || minRadius > maxRadius)
    // throw new RuntimeException("Invalid values for radius' limits");
    // this.minRadius = minRadius;
    // this.maxRadius = maxRadius;
    // }

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
        logger.log("Initializing...");
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
            Color color;
            if (i == 0) {
                radius = bigRadius;
                mass = this.bigMass;
                velocity = 0;
                speedAngle = 0;
                x = this.size / 2;
                y = this.size / 2;
                color = new Color(0, 0, 0);
            } else {
                mass = this.littleMass;
                radius = this.smallRadius;
                velocity = (this.constantVelocity == null)
                        ? (rnd.nextDouble() * (maxVelocity - minVelocity) + minVelocity)
                        : constantVelocity;
                speedAngle = rnd.nextDouble() * (2 * Math.PI);
                x = rnd.nextDouble() * (this.size - 2 * radius) + radius;
                y = rnd.nextDouble() * (this.size - 2 * radius) + radius;
                color = new Color(255, 255, 255);
            }

            Particle p = new Particle(
                    i, // index
                    x, // x
                    y, // y
                    velocity, // velocity
                    speedAngle, // speedAngle
                    radius,
                    mass,
                    color);

            if (outOfBounds(p)) {
                i--;
                continue;
            }
            if (p.radius > 0 && overlaps(p)) {
                i--;
                continue;
            }

            if (p == null) {
                System.out.println("ERROR ON PARTICLE ASIGNMENT");
                // throw new Exception();
            }
            particles[i] = p;
        }
        outputInitialState();
    }

    private boolean outOfBounds(Particle p) {
        return p.x < p.radius || (p.x + p.radius) > this.size || p.y < p.radius || (p.y + p.radius) > this.size;
    }

    public Event getNextEvent() {
        return this.eventsQueue.peek();
    }

    @SuppressWarnings("unchecked")
    public void computeNextStep() {
        if (step == 0) {
            eventsQueue.addAll(cManager.computeAllCollisions());
        }
        outputNextState();

        // GET NEXT
        Event collsionEvent = eventsQueue.poll();

        // UPDATE
        // Llevo todas las particulas hasta el punto de la colision.
        double deltaT = cManager.updateTime(collsionEvent);
        for (Particle p : particles) {
            p.update(deltaT);
        }
        // Veo como rebotan las que colisionaron. -> update state
        cManager.resolveCollision(collsionEvent);

        eventsQueue.removeIf(e -> e.includes(collsionEvent.getP1()) ||
                e.includes(collsionEvent.getP2()));

        eventsQueue.addAll(cManager.predictCollisions(collsionEvent.getP1()));
        if (collsionEvent.isParticleCollision()) {
            eventsQueue.addAll(cManager.predictCollisions(collsionEvent.getP2()));
        }

        step++;
    }

    public double getSize() {
        return size;
    }

    public Particle[] getParticles() {
        return particles;
    }

    public Queue<Event> getEvents() {
        return this.eventsQueue;
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
        str.append("-Particles:\n");
        for (Particle p : particles) {
            str.append(p).append('\n');
        }
        str.append("-Events:\n");
        for (Event e : eventsQueue) {
            str.append(e).append('\n');
        }
        str.append("-Next Event: ").append(getNextEvent()).append('\n');
        return str.toString();
    }
}
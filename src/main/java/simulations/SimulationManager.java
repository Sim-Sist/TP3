package simulations;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import output.logging.Logger;
import output.logging.ProgressBar;
import particles.Particle;
import particles.Space;

public class SimulationManager {
    /**** Logging ****/
    private static final boolean DEBUG = true;
    /**** Default Values ****/
    private static final double SIZE = 500; // L
    private static final int DEFAULT_PARTICLES_AMOUNT = 10000; // N
    private static final double MIN_RADIUS = 1, MAX_RADIUS = 2;
    private static final double CONSTANT_RADIUS = 0;
    private static final double DEFAULT_MAX_VELOCITY = 0.2;
    // private static final double MASS = 0.03;
    private final static double DEFAULT_BIG_MASS = 2.0, DEFAULT_LITTLE_MASS = 0.9;
    private final static double DEFAULT_BIG_RADIUS = 0.7, DEFAULT_SMALL_RADIUS = 0.2;

    /**** Analysis values ****/
    private static final int MAX_STEPS = 3000;

    /**** Analysis utils ****/
    private int steps = 0;

    /**** Final parameters ****/
    private int usedParticlesAmount;
    private double usedSize;

    /**** Class components ****/
    private Space space;
    private Logger logger = new Logger("sim-manager");
    private Tester tester = new Tester();

    public SimulationManager() {
        usedParticlesAmount = DEFAULT_PARTICLES_AMOUNT;
        usedSize = SIZE;
    }

    public SimulationManager(int particlesAmount, double size) {
        usedParticlesAmount = particlesAmount;
        usedSize = size;
    }

    public void simulate() {
        boolean errors = false;
        logger.log("Starting simulation...");

        space = new Space(
                usedSize,
                usedParticlesAmount,
                DEFAULT_SMALL_RADIUS,
                DEFAULT_BIG_RADIUS,
                DEFAULT_LITTLE_MASS,
                DEFAULT_BIG_MASS);
        space.setVelocities(0, DEFAULT_MAX_VELOCITY);

        ProgressBar pBar;
        try {
            pBar = logger.progressBar(MAX_STEPS);
        } catch (Exception e) {
            return;
        }

        space.initialize();


        errors = errors || !tester.testSpace(0);

        while (!endOfSimulation()) {

            space.computeNextStep();

            boolean ok = tester.testSpace(steps);
            errors = errors || !ok;
            pBar.next();

            if (DEBUG) {
                String stepMsg = String.format("==================Step %d", steps);
                // logger.logFile(stepMsg + "\n");
                // logger.logFile(space.toString());
            }

            steps++;
        }
        pBar.abort();

        logger.log(String.format("%sERRORS DETECTED", errors ? "" : "NO "));

    }

    private boolean endOfSimulation() {
        return steps == MAX_STEPS;
    }

    private class Tester {
        private List<Double> times = new ArrayList<>();
        private List<Speed> oldSpeeds = new ArrayList<>();
        private Logger testerLogger = new Logger("sim-manager|test");

        public boolean testSpace(int step) {
            boolean errors = false;
            testerLogger.logFile(String.format("For step %d:\n", step));

            if (timeIsUnordered()) {
                errors = true;
                testerLogger.logFile(
                        String.format(
                                "Time is not in order:\n" +
                                        "\t-last: %.3f\n" +
                                        "\t-previous: %.3f\n\n",
                                times.get(times.size() - 1), times.get(times.size() - 2)));
            }
            for (Particle p : space.getParticles()) {
                if (isOutOfBounds(p)) {
                    errors = true;
                    testerLogger.logFile(
                            String.format(
                                    "Particle is out of bounds:\n" +
                                            "\tparticle: %s\n\n",
                                    p.toString()));
                }
            }
            if (!errors) {
                testerLogger.logFile("Everything OK\n");
            }
            testerLogger.logFile("-----------------------------------------\n");
            return !errors;
        }

        private boolean timeIsUnordered() {
            return times.size() > 1 && times.get(times.size() - 1) < times.get(times.size() - 2);
    }

    private boolean isOutOfBounds(Particle p) {
        return (p.getX() - p.radius) < -0.1 || (p.getX() + p.radius) > space.getSize() + 0.1
                || (p.getY() - p.radius) < -0.1
                || (p.getY() + p.radius) > space.getSize() + 0.1;
    }
}

private class Speed {
    private double vx;
    private double vy;

    public Speed(double x, double y) {
        this.vx = x;
        this.vy = y;
    }

    public double getX() {
        return vx;
        }

        public double getY() {
            return vy;
        }

        public void setX(double x) {
            this.vx = x;

        }

        public void setY(double y) {
            this.vy = y;
        }

        public void update(double x, double y) {
            this.vx = x;
            this.vy = y;
        }
    }

}
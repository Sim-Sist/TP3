package simulations;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import output.logging.ConsoleColors;
import output.logging.Logger;
import output.logging.ProgressBar;
import particles.Particle;
import particles.Space;

public class SimulationManager {
    /**** Logging ****/
    private static final boolean DEBUG = true;
    /**** Default Values ****/
    private static final double SIZE = 6; // L
    private static final int DEFAULT_PARTICLES_AMOUNT = 140; // N
    private static final double MIN_RADIUS = 1, MAX_RADIUS = 2;
    private static final double CONSTANT_RADIUS = 0;
    private static final double DEFAULT_MAX_SPEED = 0.2;
    // private static final double MASS = 0.03;
    private final static double DEFAULT_BIG_MASS = 2.0, DEFAULT_LITTLE_MASS = 0.9;
    private final static double DEFAULT_BIG_RADIUS = 0.7, DEFAULT_SMALL_RADIUS = 0.2;

    /**** Simulation constants ****/
    static final double BOLTZMANN_CONSTANT = 1.3806503e-23;

    /**** Analysis values ****/
    private static final int MAX_STEPS = 20000;
    private static final double MAX_SIM_TIME = 200;
    private double lastEventTime;

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

    public void MSDSimulationSuite(){
        logger.log(String.format("Starting simulation suite for %s...",ConsoleColors.addColor("msd",ConsoleColors.PURPLE_BOLD_BRIGHT)));

        List<Integer> counter = IntStream.range(0, 10).boxed().toList();
        for (int count : counter) {
            simulate(140, 2.0, space -> {
                logger.log(String.format("Simulation %d:", count));
                space.setStaticFileName(String.format("static-info%03d.txt", count));
                space.setDynamicFileName(String.format("dynamic-info%03d.txt", count));
            });
        }
    }
    public void aLotOfSimulations() {
        logger.log("Starting simulation suite...");

        List<Integer> counter = IntStream.range(0, 50).boxed().toList();
        for (int count : counter) {
            simulate(130, DEFAULT_MAX_SPEED, space -> {
                logger.log(String.format("Simulation %d:", count));
                space.setStaticFileName(String.format("static-info%03d.txt", count));
                space.setDynamicFileName(String.format("dynamic-info%03d.txt", count));
            });
        }
    }

    public void maxSpeedSimulationSuite() {
        String title = String.format("Starting simulation suite for %s...",ConsoleColors.addColor("temperature",ConsoleColors.PURPLE_BOLD));
        logger.log(title);

        List<Double> maxSpeeds = DoubleStream.of(0.5,1,1.5,2).boxed().toList();
        Iterator<Integer> counter = IntStream.range(0, maxSpeeds.size()).boxed().toList().iterator();
        for(double maxSpeed : maxSpeeds){
            simulate(DEFAULT_PARTICLES_AMOUNT,maxSpeed,s ->{
                Integer count = counter.next();
                logger.log(String.format("Simulation %d: (max speed=%f)", count,maxSpeed));
                s.setStaticFileName(String.format("static-info%03d.txt", count));
                s.setDynamicFileName(String.format("dynamic-info%03d.txt", count));
            });
        }
    }

    public void amountOfParticlesInSpaceSimulationSuite() {
        String title = String.format("Starting simulation suite for %s...",ConsoleColors.addColor("amount of particles",ConsoleColors.PURPLE_BOLD));
        logger.log(title);

        List<Integer> pAmounts = new LinkedList<>(Stream.of(100, 110, 120, 130, 140).toList());
        Iterator<Integer> counter = IntStream.range(0, pAmounts.size()).boxed().toList().iterator();
        for (int pAmount : pAmounts) {
            simulate(pAmount, DEFAULT_MAX_SPEED, s -> {
                Integer count = counter.next();
                logger.log(String.format("Simulation %d:", count));
                s.setStaticFileName(String.format("static-info%03d.txt", count));
                s.setDynamicFileName(String.format("dynamic-info%03d.txt", count));
            });
        }

    }

    public void simulate() {
        simulate(usedParticlesAmount, DEFAULT_MAX_SPEED, s -> {
        });
    }

    public void simulate(int particles, double maxSpeed, Consumer<Space> spaceInit) {
        steps = 0;
        logger.log("Starting simulation...");

        space = new Space(
                usedSize,
                particles,
                DEFAULT_SMALL_RADIUS,
                DEFAULT_BIG_RADIUS,
                DEFAULT_LITTLE_MASS,
                DEFAULT_BIG_MASS);

        space.setVelocities(0, maxSpeed);
        spaceInit.accept(space);

        ProgressBar pBar;
        try {
            pBar = logger.progressBar(MAX_STEPS);
        } catch (Exception e) {
            return;
        }

        space.initialize();

        // errors = errors || !tester.testSpace(0);

        lastEventTime = 0;
        while (!endOfSimulation()) {
            space.computeNextStep();
            pBar.next();
            lastEventTime = space.getNextEvent().getTime();

            if (DEBUG) {
                String stepMsg = String.format("==================Step %d", steps);
                // logger.logFile(stepMsg + "\n");
                // logger.logFile(space.toString());
            }

            steps++;
        }
        pBar.abort();


    }

    private boolean endOfSimulation() {
//        return steps >= MAX_STEPS || touchesWall(space.getParticles()[0]);
                return lastEventTime >= MAX_SIM_TIME || touchesWall(space.getParticles()[0]);
    }

    private boolean touchesWall(Particle p) {
        return p.getX() - p.radius == 0 || p.getX() + p.radius == space.getSize() ||
                p.getY() - p.radius == 0 || p.getY() + p.radius == space.getSize();
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
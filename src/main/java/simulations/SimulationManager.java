package main.java.simulations;

import java.util.List;
import main.java.particles.Space;

public class SimulationManager {
    /**** Logging ****/
    private static final boolean DEBUG = true;
    /**** Default Values ****/
    private static final double SIZE = 20; // L
    private static final int PARTICLES = 400; // N
    private static final double CRITICAL_RADIUS = 1;
    private static final double MIN_RADIUS = 1, MAX_RADIUS = 2;
    private static final double CONSTANT_RADIUS = 0;
    private static final double VELOCITY = 0.03;
    private static final double NOISE_LIMIT = 3.5;

    /**** Analysis values ****/
    private static final int MAX_STEPS = 2000;

    /**** Analysis utils ****/
    private int steps = 0;

    /**** Final parameters ****/
    private int usedParticlesAmount;
    private double usedSize, usedNoise;

    /**** Class components ****/
    private Space space;

    public SimulationManager() {
        usedParticlesAmount = PARTICLES;
        usedSize = SIZE;
        usedNoise = NOISE_LIMIT;
    }

    public SimulationManager(int particlesAmount, double size, double noise) {
        usedParticlesAmount = particlesAmount;
        usedSize = size;
        usedNoise = noise;
    }

    public void simulate() {
        System.out.println("Starting simulation...");

        space = new Space(usedSize, CRITICAL_RADIUS, usedParticlesAmount, usedNoise);
        space.setRadii(CONSTANT_RADIUS);
        space.setVelocities(VELOCITY);
        space.initialize();

        while (steps < MAX_STEPS) {
            if (DEBUG)
                System.out.println(String.format("Step %d", steps));
            space.computeNextStep();
            steps++;
        }
        if (DEBUG)
            System.out.println();

    }

    public void simulationSuiteForNoise(List<Double> noiseValues) {
        int simNumber = 0;
        for (Double noiseValue : noiseValues) {
            steps = 0;

            space = new Space(usedSize, CRITICAL_RADIUS, usedParticlesAmount, noiseValue);
            space.setRadii(CONSTANT_RADIUS);
            space.setVelocities(VELOCITY);
            space.setStaticFileName(String.format("static-info%03d.txt", simNumber));
            space.setDynamicFileName(String.format("dynamic-info%03d.txt", simNumber));
            space.initialize();

            System.out.println(String.format("Starting simulation %d...", simNumber++));
            while (steps++ < MAX_STEPS) {
                if (DEBUG)
                    System.out.println(String.format("\tStep %d", steps));
                space.computeNextStep();
            }
            System.out.println();
        }
    }

    public void simulationSuiteForDensity(List<Double> densityValues) {
        int simNumber = 0;
        for (Double densityValue : densityValues) {
            steps = 0;

            space = new Space(usedSize, CRITICAL_RADIUS, (int) (densityValue * usedSize * usedSize), usedNoise);
            space.setRadii(CONSTANT_RADIUS);
            space.setVelocities(VELOCITY);
            space.setStaticFileName(String.format("static-info%03d.txt", simNumber));
            space.setDynamicFileName(String.format("dynamic-info%03d.txt", simNumber));
            space.initialize();

            System.out.println(String.format("Starting simulation %d...", simNumber++));
            while (steps++ < MAX_STEPS) {
                if (DEBUG)
                    System.out.println(String.format("\tStep %d", steps));
                space.computeNextStep();
            }
            if (DEBUG)
                System.out.println();
        }
    }

}
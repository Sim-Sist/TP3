package particles;

import particles.fx.Color;

public class Particle {
    private int index;
    public double velocity, speedAngle;
    public double x, y;
    public double radius;
    public double mass;
    public Color color;

    public Particle(int index, double x, double y, double velocity, double speedAngle, double radius, double mass,
            Color color) {
        this.index = index;
        this.x = x;
        this.y = y;

        this.velocity = velocity;
        this.speedAngle = speedAngle;

        this.radius = radius;
        this.mass = mass;

        this.color = color;
    }

    public double seeTravelX(double time) {
        return this.x + this.velocity * Math.cos(this.speedAngle) * time;
    }

    public double seeTravelY(double time) {
        return this.y + this.velocity * Math.sin(this.speedAngle) * time;
    }

    public void update(Double deltaT) {
        this.x = this.x + this.velocity * Math.cos(this.speedAngle) * deltaT;
        this.y = this.y + this.velocity * Math.sin(this.speedAngle) * deltaT;
    }

    public void update() {
        this.x = this.x + this.velocity * Math.cos(this.speedAngle);
        this.y = this.y + this.velocity * Math.sin(this.speedAngle);
    }

    public void updateV(double v) {
        this.velocity = v;
    }

    public void updateAngle(double angle) {
        this.speedAngle = angle;
    }

    public void updateVelocity(double vx, double vy) {
        this.velocity = Math.sqrt(vx * vx + vy * vy);
        double theta = Math.atan2(vy, vx);
        this.speedAngle = theta >= 0 ? theta : 2 * Math.PI + theta;
    }

    public int getIndex() {
        return index;
    }

    public double getVelocity() {
        return this.velocity;
    }

    public double getVx() {
        return velocity * Math.cos(this.speedAngle);
    }

    public double getVy() {
        return velocity * Math.sin(this.speedAngle);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getMass() {
        return mass;
    }

    public double distanceTo(Particle p) {
        double deltaX = this.x - p.x;
        double deltaY = this.y - p.y;
        return (Math.sqrt(deltaX * deltaX + deltaY * deltaY)) - (this.radius + p.radius);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Particle))
            return false;
        Particle p = (Particle) o;
        return p.x == x && p.y == y && p.index == index && p.radius == radius;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(String.format("%d: ", index))
                .append(String.format("r:%.3f x:%.3f  y:%.3f vx:%.3f vy:%.3f", radius, x, y, getVx(), getVy()));
        return str.toString();
    }

}
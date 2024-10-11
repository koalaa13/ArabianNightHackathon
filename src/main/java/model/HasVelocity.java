package model;

public class HasVelocity extends Point {
    public Point velocity;

    public HasVelocity afterNSeconds(double time) {
        var result = new HasVelocity();
        result.velocity = velocity;
        result.x = x + velocity.x * time;
        result.y = y + velocity.y * time;
        return result;
    }
}

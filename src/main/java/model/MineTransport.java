package model;

public class MineTransport extends HasVelocity {
    public Point anomalyAcceleration;
    public double attackCooldownMs;
    public int deathCount;
    public double health;
    public String id;
    public Point selfAcceleration;
    public double shieldCooldownMs;
    public double shieldLeftMs;
    public String status;

    public Point afterNSeconds(Point acc, double time) {
        var totalAcc = acc.add(anomalyAcceleration);
        var shift = velocity.mul(time).add(totalAcc.mul(time * time / 2));
        return this.add(shift);
    }

    @Override
    public String toString() {
        return "MineTransport{" +
                "anomalyAcceleration=" + anomalyAcceleration +
                ", attackCooldownMs=" + attackCooldownMs +
                ", deathCount=" + deathCount +
                ", health=" + health +
                ", id='" + id + '\'' +
                ", selfAcceleration=" + selfAcceleration +
                ", shieldCooldownMs=" + shieldCooldownMs +
                ", shieldLeftMs=" + shieldLeftMs +
                ", status='" + status + '\'' +
                ", velocity=" + velocity +
                ", x=" + x +
                ", y=" + y +
                '}';
    }
}

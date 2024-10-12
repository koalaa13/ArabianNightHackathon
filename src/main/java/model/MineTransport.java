package model;

public class MineTransport extends HasVelocity implements Cloneable {
    public Point anomalyAcceleration;
    public int attackCooldownMs;
    public int deathCount;
    public double health;
    public String id;
    public Point selfAcceleration;
    public int shieldCooldownMs;
    public int shieldLeftMs;
    public String status;

//    private Point getShift(Point acc, double time) {
//        return velocity.mul(time).add(totalAcc.mul(time * time / 2));
//    }

    public MineTransport afterNSeconds(Point acc, double time) {
        try {
            var t = (MineTransport) clone();
//            if (time > 0.4) {
//
//            } else {
//
//            }
            var totalAcc = acc.add(anomalyAcceleration);
            var shift = velocity.mul(time).add(totalAcc.mul(time * time / 2));
            t.x += shift.x;
            t.y += shift.y;
            return t;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
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

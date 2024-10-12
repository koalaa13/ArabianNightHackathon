package model;

public class EnemyTransport extends HasVelocity implements Cloneable {
    public int health;
    public double killBounty;
    public int shieldLeftMs;
    public String status;

    public EnemyTransport afterNSeconds(double time) {
        try {
            var t = (EnemyTransport) clone();
            var coords = super.afterNSeconds(time);
            t.x = coords.x;
            t.y = coords.y;
            return t;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}

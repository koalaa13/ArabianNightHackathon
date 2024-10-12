package model;

import java.util.List;

public class WorldInfo {
    public List<Anomaly> anomalies;
    public int attackCooldownMs;
    public double attackDamage;
    public double attackExplosionRadius;
    public double attackRange;
    public List<Coin> bounties;
    public List<EnemyTransport> enemies;
    public Point mapSize;
    public double maxAccel;
    public double maxSpeed;
    public String name;
    public double points;
    public double reviveTimeoutSec;
    public int shieldCooldownMs;
    public int shieldTimeMs;
    public double transportRadius;
    public List<MineTransport> transports;
    public List<EnemyTransport> wantedList;
}
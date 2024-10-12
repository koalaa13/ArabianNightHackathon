package logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.EnemyTransport;
import model.MineTransport;
import model.Point;
import model.WorldInfo;
import model.request.Request;

public class Shooting {
    // максимально втупую, радиус атаки небольшой
    public static List<Point> getPointsCanShoot(WorldInfo worldInfo, MineTransport transport) {
        var nextPosTransport = transport.afterNSeconds(0.4);
        double x = nextPosTransport.x;
        double y = nextPosTransport.y;
        double attackRange = worldInfo.attackRange;
        List<Point> res = new ArrayList<>();

        for (int i = (int) Math.max(0.0, x - attackRange); i <= Math.min(worldInfo.mapSize.x, x + attackRange); i += 2) {
            for (int j = (int) Math.max(0.0, y - attackRange); j <= Math.min(worldInfo.mapSize.y, y + attackRange); j += 2) {
                Point p = new Point(i, j);
                double dist = nextPosTransport.distTo(p);
                if (dist <= attackRange) {
                    res.add(p);
                }
            }
        }
        return res;
    }

    private static final double TAKE_FROM_ENEMY_PERCENT = 0.03;
    private static final double DEATH_LOSE_PERCENT = 0.05;

    private static List<MineTransport> canShootPoint(Point toShoot, WorldInfo worldInfo) {
        return worldInfo.transports.stream()
                .filter(Shooting::canTransportShoot)
                .filter(t -> toShoot.distTo(t.afterNSeconds(0.4)) <= worldInfo.attackRange)
                .toList();
    }

    private static long getCanShootPointCount(Point toShoot, WorldInfo worldInfo) {
        return canShootPoint(toShoot, worldInfo).size();
    }

    public static Metric getShootingPointMetric(Point toShoot, WorldInfo worldInfo) {
        long canShootThisPointCount = getCanShootPointCount(toShoot, worldInfo);
        double canDealDamage = canShootThisPointCount * worldInfo.attackDamage;
        Metric positive = worldInfo.enemies
                .stream()
                .filter(e -> toShoot.distTo(e.afterNSeconds(0.4)) <= worldInfo.attackExplosionRadius)
                .filter(e -> e.shieldLeftMs == 0)
                .map(e -> {
                    double cost = e.health > canDealDamage ?
                            0.0 :
                            e.killBounty;
                    return new Metric(cost, Math.min(e.health, canDealDamage));
                })
                .reduce(
                        new Metric(0.0, 0.0),
                        (was, toAdd) -> new Metric(was.cost + toAdd.cost, was.health + toAdd.health)
                );
        // Если гасим по своим, то надо это учесть
        Metric negative = worldInfo.transports
                .stream()
                .filter(t -> toShoot.distTo(t.afterNSeconds(0.4)) <= worldInfo.attackExplosionRadius)
                .filter(t -> t.shieldLeftMs == 0)
                .map(t -> {
                    double cost = t.health > canDealDamage ?
                            0.0 :
                            worldInfo.points * DEATH_LOSE_PERCENT;
                    return new Metric(cost, Math.min(t.health, canDealDamage));
                })
                .reduce(
                        new Metric(0.0, 0.0),
                        (was, toAdd) -> new Metric(was.cost + toAdd.cost, was.health + toAdd.health)
                );

        return new Metric(positive.cost - negative.cost, positive.health - negative.health);
    }

    public static class Metric {
        public double cost;
        public double health;

        public Metric(double cost, double health) {
            this.cost = cost;
            this.health = health;
        }
    }

    private static boolean canTransportShoot(MineTransport t) {
        return t.attackCooldownMs == 0 && t.status.equals("alive");
    }

    // id -> Point
    public static Map<String, Point> getPointsToShoot(WorldInfo info) {
        Map<String, Point> res = new HashMap<>();
        List<MineTransport> canShootTransport = info.transports.stream().filter(Shooting::canTransportShoot).toList();
        long readyToAttackCount = canShootTransport.size();
        Map<Point, Metric> metricMap = new HashMap<>();
        List<Point> allPoints = new ArrayList<>();
        canShootTransport.forEach(t -> {
                    for (var p : getPointsCanShoot(info, t)) {
                        if (metricMap.containsKey(p)) {
                            continue;
                        }
                        allPoints.add(p);
                        metricMap.put(
                                p,
                                getShootingPointMetric(p, info)
                        );
                    }
                });
        allPoints.sort((p1, p2) -> {
            Metric m1 = metricMap.get(p1);
            Metric m2 = metricMap.get(p2);
            if (m1.cost != m2.cost) {
                return -Double.compare(m1.cost, m2.cost);
            }
            return -Double.compare(m1.health, m2.health);
        });
        for (int i = 0; readyToAttackCount > res.size() && i < allPoints.size(); ++i) {
            Point p = allPoints.get(i);
            for (MineTransport t : canShootTransport) {
                if (t.afterNSeconds(0.4).distTo(p) <= info.attackRange) {
                    res.putIfAbsent(t.id, p);
                }
            }
        }
        return res;
    }

    public static void setShoots(WorldInfo info, Request req) {
        var points = getPointsToShoot(info);
        for (var we : req.transports) {
            if (points.containsKey(we.id)) {
                we.setAttack(points.get(we.id));
            }
        }
    }

    public static void main(String[] args) {
        WorldInfo worldInfo = new WorldInfo();
        worldInfo.mapSize = new Point(10000, 10000);

        worldInfo.attackRange = 200;
        worldInfo.attackDamage = 10;
        worldInfo.points = 100;

        //-----------------------------
        MineTransport mineTransport1 = new MineTransport();
        mineTransport1.x = 1000;
        mineTransport1.y = 1000;
        mineTransport1.health = 10;
        mineTransport1.id = "1";

        MineTransport mineTransport2 = new MineTransport();
        mineTransport2.x = 2000;
        mineTransport2.y = 2000;
        mineTransport2.health = 10;
        mineTransport2.id = "2";

        MineTransport mineTransport3 = new MineTransport();
        mineTransport3.x = 3000;
        mineTransport3.y = 3000;
        mineTransport3.health = 10;
        mineTransport3.id = "3";

        MineTransport mineTransport4 = new MineTransport();
        mineTransport4.x = 4000;
        mineTransport4.y = 4000;
        mineTransport4.health = 10;
        mineTransport4.id = "4";

        MineTransport mineTransport5 = new MineTransport();
        mineTransport5.x = 5000;
        mineTransport5.y = 5000;
        mineTransport5.health = 10;
        mineTransport5.id = "5";

        worldInfo.transports = List.of(mineTransport1, mineTransport2, mineTransport3, mineTransport4, mineTransport5);

        //-----------------------------
        EnemyTransport enemyTransport = new EnemyTransport();
        enemyTransport.shieldLeftMs = 0;
        enemyTransport.health = 10;
        enemyTransport.killBounty = 100;
        enemyTransport.x = 1100;
        enemyTransport.y = 1000;
        enemyTransport.velocity = new Point(0, 0);

        worldInfo.enemies = List.of(enemyTransport);
        //--------------------
        long start = System.currentTimeMillis();
        System.out.println(getPointsToShoot(worldInfo));
        long finish = System.currentTimeMillis();
        System.out.println("Elapsed time: " + (finish - start));
    }
}

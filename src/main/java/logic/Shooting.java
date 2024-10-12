package logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.EnemyTransport;
import model.HasVelocity;
import model.MineTransport;
import model.Point;
import model.WorldInfo;
import model.request.Request;

public class Shooting {
    // Сколько сабтиков (изменений состояний игры) случается в секунду
    private static final int SUBTICKS_COUNT = 10;
    private static final int SUBTICKS_SHOT_DELAY = 4;
    private static final double VELOCITY_DIV = (double) SUBTICKS_SHOT_DELAY / (double) SUBTICKS_COUNT;

    // Куда я (примерно) должен стрелять на упреждение
    public static Point getFuturePos(HasVelocity hasVelocityModel) {
        double curX = hasVelocityModel.x;
        double curY = hasVelocityModel.y;

        double diffX = hasVelocityModel.velocity.x / VELOCITY_DIV;
        double diffY = hasVelocityModel.velocity.y / VELOCITY_DIV;

        return new Point((int) curX + diffX, (int) curY + diffY);
    }

    // максимально втупую, радиус атаки небольшой
    public static List<Point> getPointsCanShoot(WorldInfo worldInfo, MineTransport transport) {
        double x = transport.x;
        double y = transport.y;
        double attackRange = worldInfo.attackRange;
        List<Point> res = new ArrayList<>();

        for (int i = (int) Math.max(0.0, x - attackRange); i <= Math.min(worldInfo.mapSize.x, x + attackRange); ++i) {
            for (int j = (int) Math.max(0.0, y - attackRange); j <= Math.min(worldInfo.mapSize.y, y + attackRange); ++j) {
                Point p = new Point(i, j);
                double dist = transport.distTo(p);
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
                .filter(t -> t.attackCooldownMs == 0)
                .filter(t -> toShoot.distTo(t) <= worldInfo.attackRange)
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
                .filter(e -> toShoot.distTo(e) <= worldInfo.attackExplosionRadius)
                .filter(e -> e.health <= canDealDamage)
                .filter(e -> e.shieldLeftMs == 0)
                .map(e -> new Metric(e.killBounty, Math.min(e.health, canDealDamage)))
                .reduce(
                        new Metric(0.0, 0.0),
                        (was, toAdd) -> new Metric(was.cost + toAdd.cost, was.health + toAdd.health)
                );
        // Если гасим по своим, то надо это учесть
        Metric negative = worldInfo.transports
                .stream()
                .filter(t -> toShoot.distTo(t) <= worldInfo.attackExplosionRadius)
                .filter(t -> t.health <= canDealDamage)
                .filter(t -> t.shieldLeftMs == 0)
                .map(t -> new Metric(worldInfo.points * DEATH_LOSE_PERCENT, Math.min(t.health, canDealDamage)))
                .reduce(
                        new Metric(0.0, 0.0),
                        (was, toAdd) -> new Metric(was.cost + toAdd.cost, was.health + toAdd.health)
                );

        return new Metric(positive.cost - negative.cost, positive.health - negative.health);
    }
//
//    public static double getShootingPointHealthCost(Point toShoot, WorldInfo worldInfo) {
//        long canShootThisPointCount = getCanShootPointCount(toShoot, worldInfo);
//        double canDealDamage = canShootThisPointCount * worldInfo.attackDamage;
//        double cost = worldInfo.enemies
//                .stream()
//                .filter(e -> toShoot.distTo(e) <= worldInfo.attackExplosionRadius)
//                .filter(e -> e.shieldLeftMs == 0)
//                .map(e -> Math.min(e.health, canDealDamage))
//                .reduce(0.0, Double::sum);
//        // Если гасим по своим, то надо это учесть
//        cost += worldInfo.transports
//                .stream()
//                .filter(t -> toShoot.distTo(t) <= worldInfo.attackExplosionRadius)
//                .filter(t -> t.shieldLeftMs == 0)
//                .map(t -> -Math.min(t.health, canDealDamage))
//                .reduce(0.0, Double::sum);
//        return cost;
//}

    public static class Metric {
        public double cost;
        public double health;

        public Metric(double cost, double health) {
            this.cost = cost;
            this.health = health;
        }
    }

    // id -> Point
    public static Map<String, Point> getPointsToShoot(WorldInfo info) {
        Map<String, Point> res = new HashMap<>();
        long readyToAttackCount = info.transports.stream().filter(t -> t.attackCooldownMs == 0).count();
        Map<Point, Metric> metricMap = new HashMap<>();
        List<Point> allPoints = new ArrayList<>();
        info.transports.stream()
                .filter(t -> t.attackCooldownMs == 0)
                .forEach(t -> {
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
            if (metricMap.get(p).cost <= 0 && metricMap.get(p).health <= 0) {
                break;
            }
            for (MineTransport t : info.transports) {
                if (t.distTo(p) <= info.attackRange) {
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

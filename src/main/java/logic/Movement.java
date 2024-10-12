package logic;

import model.*;
import model.request.Request;
import model.request.RequestTransport;

import java.util.*;

public class Movement {
    private static Point getAcc(MineTransport cur, Point dest, double time) {
        var S = dest.sub(cur);
        var A = S.mul(2 / (time * time)).sub(cur.velocity.mul(2 / time)).sub(cur.anomalyAcceleration);
        return A;
    }

    private static boolean canReach(double maxAccel, MineTransport cur, Point dest, double time) {
        var S = dest.sub(cur);
        var V = cur.velocity;
//        boolean finishSpeedGood = S.mul(2 / time).sub(V).length() <= maxAccel;
//        if (!finishSpeedGood) return false;
        var A = S.mul(2 / (time * time)).sub(V.mul(2 / time)).sub(cur.anomalyAcceleration);
        boolean accGood = A.length() < maxAccel * 9 / 10;
        if (!accGood) return false;
//        var newCur = cur.afterNSecondsNoDelay(A, 0.4);
//        boolean nextSpeedGood = newCur.velocity.length() <= maxAccel;
//        if (!nextSpeedGood) return false;
        return true;
    }

    public static double minAvailableReachTime(double maxAccel, MineTransport cur, Point dest) {
        double l = 0.5, r = 30.0;
        for (int i = 0; i < 20; i++) {
            double m = (l + r) / 2;
            if (canReach(maxAccel, cur, dest, m)) {
                r = m;
            } else {
                l = m;
            }
        }
        return r;
    }

    private static Point accToPoint(WorldInfo info, MineTransport cur, Point dest) {
        var expectedTime = minAvailableReachTime(info.maxAccel, cur, dest);
        var acc = getAcc(cur, dest, expectedTime);
        return acc.shrink(info.maxAccel * 9 / 10);
    }

    private static double distFromCoverToPoint(HasVelocity anoma, MineTransport cur, Point acc, double time) {
        return cur.afterNSecondsNoDelay(acc, time).distTo(anoma.afterNSeconds(time + 0.4));
    }

    private static boolean checkCover(HasVelocity other, MineTransport cur, Point acc) {
        var dist = Double.MAX_VALUE;
        for (double t = 0.0; t < 1.6; t += 0.3) {
            dist = Math.min(dist, distFromCoverToPoint(other, cur, acc, t));
        }
        return dist < 12;
    }

    private static boolean isDangerous(WorldInfo info, MineTransport cur, Point c) {
        boolean res = false;
        var chosenAcc = accToPoint(info, cur, c);
        for (var anoma : info.anomalies) {
            var dist = Double.MAX_VALUE;
            for (double t = 0.0; t < 2.4; t += 0.5) {
                dist = Math.min(dist, distFromCoverToPoint(anoma, cur, chosenAcc, t));
            }
            res |= dist < 3 * Math.min(anoma.radius, anoma.effectiveRadius);
        }
        for (var we : info.transports) {
            if (we.id.equals(cur.id)) continue;
            res |= checkCover(we, cur, chosenAcc);
        }
        for (var enemy : info.enemies) {
            res |= checkCover(enemy, cur, chosenAcc);
        }
        return res;
    }

    private static Optional<Coin> getBestCoin(WorldInfo info, MineTransport cur) {
        return info.bounties.stream()
                .filter(c -> cur.distTo(c) < 200)
                .filter(c -> !isDangerous(info, cur, c))
                .max(Comparator.comparingDouble(c -> c.points - c.distTo(cur)));
    }

    private static Point getBestDest(WorldInfo info, MineTransport cur, Point dest) {
        for (int i = 0; i < 10; ++i) {
            int r = cur.id.hashCode() % 3000 - 1500;
            var choice = dest.add(new Point(r, r));
            if (!isDangerous(info, cur, choice)) return choice;
        }
        return dest;
    }

    private static Point getSimpleAcceleration(WorldInfo info, RequestTransport we, boolean isGreedy) {
        var destPoint = moneyCenter(info);
        var cur = info.transports.stream().filter(t -> t.id.equals(we.id)).findFirst().get();
        cur = cur.afterNSeconds(0.4);
        if (inMoneyZone(cur, destPoint) || isGreedy) {
            var bestCoin = getBestCoin(info, cur);
            if (bestCoin.isPresent()) {
                System.out.println(we.id + " go to coin. Time: " + minAvailableReachTime(info.maxAccel, cur, bestCoin.get()));
                return accToPoint(info, cur, bestCoin.get());
            }
        }
        return accToPoint(info, cur, getBestDest(info, cur, destPoint));
    }

    // TODO: Visualize cover status (is it able to move to coin)
    public static void setAccelerations(WorldInfo info, Request req, boolean isGreedy) {
        for (var we : req.transports) {
            var acc = getSimpleAcceleration(info, we, isGreedy);
            we.setAcceleration(acc);
        }
    }

    public static Point moneyCenter(WorldInfo info) {
        return new Point(info.mapSize.x * 5 / 10, info.mapSize.y * 5 / 10);
    }

    public static boolean inMoneyZone(MineTransport cur, Point destPoint) {
        return cur.distTo(destPoint) <= 4000;
    }
}

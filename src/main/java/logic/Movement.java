package logic;

import model.*;
import model.request.Request;
import model.request.RequestTransport;

import java.util.*;

public class Movement {
    private static Point accToPoint(WorldInfo info, MineTransport cur, Point dest) {
        var acc = cur.anomalyAcceleration.mul(-1)
                .add(dest.add(cur.afterNSeconds(0.5).mul(-1)));
        return acc.shrink(info.maxAccel * 4 / 5);
    }

    private static double distFromCoverToPoint(Anomaly anoma, MineTransport cur, Point acc, double time) {
        return cur.afterNSeconds(acc, time).distTo(anoma.afterNSeconds(time));
    }

    private static boolean isDangerous(WorldInfo info, MineTransport cur, Point c) {
        boolean res = false;
        var chosenAcc = accToPoint(info, cur, c);
        for (var anoma : info.anomalies) {
            var dist = Double.MAX_VALUE;
            for (double t = 0.0; t < 1.9; t += 0.5) {
                dist = Math.min(dist, distFromCoverToPoint(anoma, cur, chosenAcc, t));
            }
            res |= dist < 2 * Math.min(anoma.radius, anoma.effectiveRadius);
        }
        return res;
    }

    private static Optional<Coin> getBestCoin(WorldInfo info, MineTransport cur) {
        return info.bounties.stream()
                .filter(c -> !isDangerous(info, cur, c))
                .filter(c -> c.distTo(cur) < 300)
                .max(Comparator.comparingDouble(c -> c.points - c.distTo(cur)));
    }

    private static Point getSimpleAcceleration(WorldInfo info, RequestTransport we) {
        var destPoint = new Point(info.mapSize.x * 9 / 10, info.mapSize.y * 9 / 10);
        var radius = Math.max(info.mapSize.x, info.mapSize.y) / 10;
        var cur = info.transports.stream().filter(t -> t.id.equals(we.id)).findFirst().get();
        cur = cur.afterNSeconds(cur.selfAcceleration, 0.4);
        if (cur.x >= destPoint.x - radius && cur.y >= destPoint.y - radius) {
            var bestCoin = getBestCoin(info, cur);
            if (bestCoin.isPresent()) {
                return accToPoint(info, cur, bestCoin.get());
            } else {
                var shift = 200 - cur.hashCode() % 1500;
                var randomPoint = new Point(destPoint.x + shift, destPoint.y + shift);
                return accToPoint(info, cur, randomPoint);
            }
        } else {
            return accToPoint(info, cur, destPoint);
        }
    }

    // TODO: Visualize cover status (is it able to move to coin)
    public static void setAccelerations(WorldInfo info, Request req) {
        for (var we : req.transports) {
            var acc = getSimpleAcceleration(info, we);
            we.setAcceleration(acc);
        }
    }
}

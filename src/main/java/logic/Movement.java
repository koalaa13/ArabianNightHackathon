package logic;

import model.Coin;
import model.MineTransport;
import model.Point;
import model.WorldInfo;
import model.request.Request;
import model.request.RequestTransport;

import java.util.*;

public class Movement {
    private static Point accToPoint(WorldInfo info, MineTransport cur, Point dest) {
        var acc = new Point(
                -cur.velocity.x / 2 + (dest.x - (cur.x + cur.velocity.x)),
                -cur.velocity.y / 2 + (dest.y - (cur.y + cur.velocity.y))
        );
        return acc.shrink(info.maxAccel * 3 / 4);
    }

    private static double distFromSegmentToPoint(Point a, MineTransport cur, Point c) {
        return 0.0;
    }

    private static boolean isDangerous(WorldInfo info, MineTransport cur, Point c) {
        boolean res = false;
        for (var anoma : info.anomalies) {
            var dist = distFromSegmentToPoint(anoma, cur, c);
            res |= dist < 2 * Math.min(anoma.radius, anoma.effectiveRadius);
        }
        return res;
    }

    private static Optional<Coin> getBestCoin(WorldInfo info, MineTransport cur) {
        return info.bounties.stream()
                .filter(c -> !isDangerous(info, cur, c))
                .filter(c -> c.distTo(cur) < 150)
                .max(Comparator.comparingDouble(c -> c.points - c.distTo(cur)));
    }

    private static Point getSimpleAcceleration(WorldInfo info, RequestTransport we) {
        var destPoint = new Point(info.mapSize.x * 9 / 10, info.mapSize.y * 9 / 10);
        var radius = Math.max(info.mapSize.x, info.mapSize.y) / 10;
        var cur = info.transports.stream().filter(t -> t.id.equals(we.id)).findFirst().get();
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

    public static void getAccelerations(WorldInfo info, Request req) {
        for (var we : req.transports) {
            var acc = getSimpleAcceleration(info, we);
            we.setAcceleration(acc);
        }
    }
}

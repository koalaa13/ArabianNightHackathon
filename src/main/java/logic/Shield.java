package logic;

import model.WorldInfo;
import model.request.Request;

public class Shield {
    public static void setShields(WorldInfo info, Request req) {
        for (var we : req.transports) {
            var cur = info.transports.stream().filter(t -> t.id.equals(we.id)).findFirst().get();
            if (cur.shieldCooldownMs < 100 && cur.shieldLeftMs < 1) {
                we.setActivateShield(true);
            }
        }
    }
}

import java.util.Collections;

import api.ApiController;
import api.Controller;
import logic.Movement;
import logic.Shield;
import logic.Shooting;
import model.WorldInfo;
import model.request.Request;
import model.request.RequestTransport;
import visual.GraphVisualizer;

public class Main {
    public static final Request EMPTY_REQUEST = new Request()
            .setTransports(Collections.emptyList());

    public static void main(String[] args) throws InterruptedException {
        final Controller controller = ApiController.getTestInstance();
        WorldInfo info = controller.getInfo(EMPTY_REQUEST);
        GraphVisualizer visualizer = new GraphVisualizer(info);
        long lastIteration = System.currentTimeMillis();
        long minIteration = Long.MAX_VALUE;
        long sumIteration = 0;
        long shootTime = 0;
        long moveTime = 0;
        for (int it = 1; ; ++it) {
            Thread.sleep(300);
            var req = new Request().setTransports(
                    info.transports.stream().filter(t -> t.health > 0).map(t -> new RequestTransport().setId(t.id)
                    ).toList());
            long t1 = System.currentTimeMillis();
            Movement.setAccelerations(info, req);
            long t2 = System.currentTimeMillis();
            Shield.setShields(info, req);
            long t3 = System.currentTimeMillis();
            Shooting.setShoots(info, req);
            long t4 = System.currentTimeMillis();
            shootTime += t4 - t3;
            moveTime += t2 - t1;

            info = controller.getInfo(req);
            visualizer.setWorldAndReq(info, req);
            visualizer.updateGraph();
            long newLastIteration = System.currentTimeMillis();
            long diff = newLastIteration - lastIteration;
            minIteration = Math.min(minIteration, diff);
            sumIteration += diff;
            System.out.println("Iteration " + it +
                    ".   Last it: " + diff + " ms., min it: " + minIteration + " ms., avg it: " + (sumIteration / it) + " ms" +
                    ".   Avg shoot it: " + (shootTime / it) + " ms., avg move it: " + (moveTime / it) + " ms.");
            lastIteration = newLastIteration;
        }
    }
}

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import api.ApiController;
import api.Controller;
import logic.Movement;
import model.MineTransport;
import model.Point;
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
        long startTime = System.currentTimeMillis();
        for (int it = 0; ; ++it) {
            Thread.sleep(400);
            var req = new Request().setTransports(
                    info.transports.stream().map(t -> new RequestTransport()).toList());
            Movement.getAccelerations(info, req);
            info = controller.getInfo(req);
            visualizer.setWorld(info);
            visualizer.updateGraph();
            long passedTime = System.currentTimeMillis() - startTime;
            System.out.println("Passed: " + passedTime);
        }
    }
}

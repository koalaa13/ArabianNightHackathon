import java.util.Collections;
import java.util.List;

import api.ApiController;
import api.Controller;
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
        for (int it = 0; ; ++it) {
            Thread.sleep(400);
            String id = info.transports.get(0).id;

            info = controller.getInfo(
                    new Request()
                            .setTransports(List.of(new RequestTransport()
                            .setId(id).setAcceleration(new Point(1.0, 1.0))))
            );
            visualizer.setWorld(info);
            visualizer.updateGraph();

            MineTransport transport = info.transports.stream()
                    .filter(t -> t.id.equals(id))
                    .toList().get(0);

            System.out.println(transport.velocity);
        }
    }
}

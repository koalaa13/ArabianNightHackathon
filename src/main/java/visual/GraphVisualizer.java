package visual;


import model.Anomaly;
import model.MineTransport;
import model.Point;
import model.WorldInfo;
import model.request.Request;
import model.request.RequestTransport;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

public class GraphVisualizer extends JFrame {
    private final int W = 900;
    private final int W2 = 350;
    private final int H = 900;

    private double startX;
    private double startY;
    private double zoom;

    private WorldInfo world;
    private Request req;

    private JPanel canvas;
    private JLabel status;

    public GraphVisualizer(WorldInfo world) {
        super("canvas");
        setWorld(world);
        resetZoom();

        setSize(W + W2, H);

        setBackground(Color.DARK_GRAY);

//        addMouseListener(new MouseListener(){
//            public void mouseClicked(MouseEvent e){
//                selectedX = (int) (e.getX() / zoom);
//                selectedY = (int) ((e.getY() - 25) / zoom);
//                System.out.printf("Move to %d %d\n", selectedX, selectedY);
//                updateGraph();
//            }
//
//            public void mouseEntered(MouseEvent arg0) {}
//            public void mouseExited(MouseEvent arg0) {}
//            public void mousePressed(MouseEvent arg0) {}
//            public void mouseReleased(MouseEvent arg0) {}
//        });

        canvas = new JPanel() {
            public void paint(Graphics g) {
                drawWorld(g);
            }
        };

        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));

        sidePanel.add(Box.createRigidArea(new Dimension(0, 15)));
        JButton buttonResetZoom = new JButton("Reset zoom");
        buttonResetZoom.addActionListener(e -> resetZoom());
        sidePanel.add(buttonResetZoom);

        sidePanel.add(Box.createRigidArea(new Dimension(0, 15)));
        JButton buttonZoom = new JButton("Zoom to rich space");
        buttonZoom.addActionListener(e -> setZoom());
        sidePanel.add(buttonZoom);

        sidePanel.add(Box.createRigidArea(new Dimension(0, 15)));
        status = new JLabel();
        sidePanel.add(status);

        canvas.setMaximumSize(new Dimension(W, H));
        sidePanel.setMaximumSize(new Dimension(W2, H));

        setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));
        add(canvas);
        add(sidePanel);
        show();
    }

    private synchronized void resetZoom() {
        startX = 0.0;
        startY = 0.0;
        zoom = getZoom();
        updateGraph();
    }

    private synchronized void setZoom() {
        startX = world.mapSize.x * 2 / 3;
        startY = world.mapSize.y * 2 / 3;
        zoom = getZoom();
        updateGraph();
    }

    private String coverInfo(MineTransport we, Optional<RequestTransport> weR) {
        String res =
                "Id: " + we.id + "<br>" +
                "Health: " + we.health + "<br>" +
                "X: " + (int) we.x + " Y: " + (int) we.y + "<br>";
        if (weR.isPresent()) {
            res += "-->X: " + (int) weR.get().acceleration.x +
                    " -->Y: " + (int) weR.get().acceleration.y + "<br>";
        }
        return res;
    }

    private synchronized void drawWorld(Graphics g) {
        if (world == null) {
            System.out.println("No world");
            return;
        }
        g.setColor(Color.YELLOW);
        for (var bounty : world.bounties) {
            drawPoint(g, bounty);
        }
        g.setColor(Color.RED);
        for (var enemy : world.enemies) {
            drawPoint(g, enemy);
        }
        StringBuilder coverInfo = new StringBuilder();
        for (var we : world.transports) {
            g.setColor(we.shieldLeftMs > 0 ? Color.BLUE : Color.CYAN);
            drawPoint(g, we);
            var weR = req.transports.stream().filter(t -> t.id.equals(we.id)).findAny();
            if (weR.isPresent()) {
                g.setColor(Color.BLACK);
                drawLine(g, we, we.add(weR.get().acceleration.mul(0.7 / zoom)));
            }
            coverInfo.append(coverInfo(we, weR)).append("<br>");
        }
        for (var anoma : world.anomalies) {
            drawCircle(g, anoma);
        }
        status.setText("<html>" + "Gold: " + world.points + "<br>" + coverInfo + "</html>");
    }

    private double getZoom() {
        return W / (world.mapSize.x - startX);
    }

    private int pixelX(double pointX) {
        return (int) ((pointX - startX) * zoom);
    }

    private int pixelY(double pointY) {
        return (int) ((pointY - startY) * zoom);
    }

    private void drawCircle(Graphics g, Anomaly anoma) {
        g.setColor(anoma.strength > 0 ? Color.MAGENTA : Color.PINK);
        drawPoint(g, anoma);
        double exRadius = Math.max(anoma.radius, anoma.effectiveRadius);
        double inRadius = Math.min(anoma.radius, anoma.effectiveRadius);
        double zoom = getZoom();
        g.drawOval(
                pixelX(anoma.x - exRadius),
                pixelY(anoma.y - exRadius),
                (int) (2 * exRadius * zoom),
                (int) (2 * exRadius * zoom));
        g.fillOval(
                pixelX(anoma.x - inRadius),
                pixelY(anoma.y - inRadius),
                (int) (2 * inRadius * zoom),
                (int) (2 * inRadius * zoom));
    }

    private void drawPoint(Graphics g, Point p) {
        int size = (int) Math.max(zoom * 10, 1.0);
        g.drawRect(pixelX(p.x), pixelY(p.y), size, size);
    }

    private void drawLine(Graphics g, Point p1, Point p2) {
        g.drawLine(pixelX(p1.x), pixelY(p1.y), pixelX(p2.x), pixelY(p2.y));
    }

    public synchronized void updateGraph() {
        repaint();
    }

    public synchronized void setWorld(WorldInfo world) {
        this.world = world;
    }

    public synchronized void setWorldAndReq(WorldInfo world, Request req) {
        this.world = world;
        this.req = req;
    }
}

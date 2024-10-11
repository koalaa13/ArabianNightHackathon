package visual;


import model.Anomaly;
import model.Point;
import model.WorldInfo;

import javax.swing.*;
import java.awt.*;

public class GraphVisualizer extends JFrame {
    private final int W = 1000;
    private final int W2 = 350;
    private final int H = 1000;

    private double startX;
    private double startY;
    private double zoom;

    private WorldInfo world;

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

        status = new JLabel();
        sidePanel.add(status);

        sidePanel.add(Box.createRigidArea(new Dimension(0, 15)));
        JButton buttonResetZoom = new JButton("Reset zoom");
        buttonResetZoom.addActionListener(e -> resetZoom());
        sidePanel.add(buttonResetZoom);

        sidePanel.add(Box.createRigidArea(new Dimension(0, 15)));
        JButton buttonZoom = new JButton("Zoom to rich space");
        buttonZoom.addActionListener(e -> setZoom());
        sidePanel.add(buttonZoom);

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
    }

    private synchronized void setZoom() {
        startX = world.mapSize.x * 2 / 3;
        startY = world.mapSize.y * 2 / 3;
        zoom = getZoom();
    }

    private synchronized void drawWorld(Graphics g) {
        status.setText("Gold: " + world.points);
        g.setColor(Color.YELLOW);
        for (var bounty : world.bounties) {
            drawPoint(g, bounty);
        }
        g.setColor(Color.RED);
        for (var enemy : world.enemies) {
            drawPoint(g, enemy);
        }
        for (var we : world.transports) {
            g.setColor(we.shieldLeftMs > 0 ? Color.BLUE : Color.CYAN);
            drawPoint(g, we);
        }
        for (var anoma : world.anomalies) {
            drawCircle(g, anoma);
        }
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
        int size = (int) Math.max(zoom * 5, 1.0);
        g.drawRect(pixelX(p.x), pixelY(p.y), size, size);
    }

    public synchronized void updateGraph() {
        repaint();
    }

    public synchronized void setWorld(WorldInfo world) {
        this.world = world;
    }
}

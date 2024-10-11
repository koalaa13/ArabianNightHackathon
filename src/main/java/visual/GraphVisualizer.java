package visual;


import model.Anomaly;
import model.Point;
import model.WorldInfo;

import javax.swing.*;
import java.awt.*;

public class GraphVisualizer extends JFrame {
    private double zoom = 0.1;
    private WorldInfo world;

//    private List<Ship> myShips = new ArrayList<>();

//    private IslandMap.Tiles myShipsTiles = new IslandMap.Tiles();

//    private IslandMap.Tiles enemyShipsTiles = new IslandMap.Tiles();

    private JPanel canvas;

    private boolean needShield = false;

//    private long selectedX = 0;
//
//    private long selectedY = 0;
//
//    private Zone zone = null;

    public GraphVisualizer(WorldInfo world) {
        super("canvas");

        setWorld(world);

        int W = (int) (world.mapSize.x + 2);
        int H = (int) (world.mapSize.y + 2);

        setSize((int) Math.ceil(W * zoom), (int) Math.ceil(H * zoom));

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

//        status = new JLabel();
//        sidePanel.add(status);
//
//        sidePanel.add(Box.createRigidArea(new Dimension(0, 15)));
//
//        JSlider slider = new JSlider(5, 26, observeSpace);
//        slider.addChangeListener(e -> setObserveSpace(slider.getValue()));
//        slider.setMajorTickSpacing(3);
//        slider.setMinorTickSpacing(1);
//        slider.setPaintLabels(true);
//        slider.setSnapToTicks(true);
//        sidePanel.add(slider);
//
//        sidePanel.add(Box.createRigidArea(new Dimension(0, 15)));
//        JCheckBox cb = new JCheckBox("Limit gold (5)");
//        cb.addItemListener(e -> setLimitGold(cb.isSelected()));
//        sidePanel.add(cb);
//
//        sidePanel.add(Box.createRigidArea(new Dimension(0, 15)));
//        JCheckBox cb2 = new JCheckBox("Dense");
//        cb2.addItemListener(e -> setDenseMode(cb2.isSelected()));
//        sidePanel.add(cb2);
//
//        sidePanel.add(Box.createRigidArea(new Dimension(0, 15)));
//        JButton button = new JButton("Build random");
//        button.addActionListener(e -> setRandomFutureBlocks());
//        sidePanel.add(button);
//
//        sidePanel.add(Box.createRigidArea(new Dimension(0, 15)));
//        JButton button2 = new JButton("Build far away");
//        button2.addActionListener(e -> setFarFutureBlocks(1, false));
//        sidePanel.add(button2);
//
//        sidePanel.add(Box.createRigidArea(new Dimension(0, 15)));
//        JButton button3 = new JButton("Build circle");
//        button3.addActionListener(e -> setFarFutureBlocks(1, true));
//        sidePanel.add(button3);
//
//        sidePanel.add(Box.createRigidArea(new Dimension(0, 15)));
//        JButton buttonNear = new JButton("Build near");
//        buttonNear.addActionListener(e -> setFarFutureBlocks(-1, false));
//        sidePanel.add(buttonNear);
//
//        sidePanel.add(Box.createRigidArea(new Dimension(0, 15)));
//        JButton buttonLeft = new JButton("Build left");
//        buttonLeft.addActionListener(e -> setDirFutureBlocks(-1, 0));
//        sidePanel.add(buttonLeft);
//
//        sidePanel.add(Box.createRigidArea(new Dimension(0, 15)));
//        JButton buttonRight = new JButton("Build right");
//        buttonRight.addActionListener(e -> setDirFutureBlocks(1, 0));
//        sidePanel.add(buttonRight);
//
//        sidePanel.add(Box.createRigidArea(new Dimension(0, 15)));
//        JButton buttonUp = new JButton("Build up");
//        buttonUp.addActionListener(e -> setDirFutureBlocks(0, -1));
//        sidePanel.add(buttonUp);
//
//        sidePanel.add(Box.createRigidArea(new Dimension(0, 15)));
//        JButton buttonDown = new JButton("Build down");
//        buttonDown.addActionListener(e -> setDirFutureBlocks(0, 1));
//        sidePanel.add(buttonDown);
//
//        canvas.setMaximumSize(new Dimension(W, H));
//        sidePanel.setMaximumSize(new Dimension(W2, H));
//
//        setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));
        add(canvas);
//        add(sidePanel);
        show();
    }

    private void drawWorld(Graphics g) {
        for (var bounty : world.bounties) {
            drawPoint(g, bounty, Color.YELLOW);
        }
        for (var enemy : world.enemies) {
            drawPoint(g, enemy, Color.RED);
        }
        for (var we : world.transports) {
            drawPoint(g, we, we.shieldLeftMs > 0 ? Color.BLUE : Color.CYAN);
        }
        for (var anoma : world.anomalies) {
            drawCircle(g, anoma);
        }
    }

    private void drawCircle(Graphics g, Anomaly anoma) {
        drawPoint(g, anoma, Color.MAGENTA);
        double exRadius = Math.max(anoma.radius, anoma.effectiveRadius);
        double inRadius = Math.min(anoma.radius, anoma.effectiveRadius);
        g.drawOval(
                (int) ((anoma.x - exRadius) * zoom),
                (int) ((anoma.y - exRadius) * zoom),
                (int) (2 *  exRadius * zoom),
                (int) (2 *  exRadius * zoom));
        g.fillOval(
                (int) ((anoma.x - inRadius) * zoom),
                (int) ((anoma.y - inRadius) * zoom),
                (int) (2 *  inRadius * zoom),
                (int) (2 *  inRadius * zoom));
    }

    private void drawPoint(Graphics g, Point p, Color c) {
        g.setColor(c);
        g.drawRect((int) (p.x * zoom), (int) (p.y * zoom), 1, 1);
    }

    public void updateGraph() {
        repaint();
    }

    public void setWorld(WorldInfo world) {
        this.world = world;
    }
}

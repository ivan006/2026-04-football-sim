package fpsjframe;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SimPanel extends JPanel {

    private final World world;
    private final Runnable onExit;

    private int hoverRow = -1;
    private int hoverCol = -1;

    private Timer repaintTimer;

    private final JLabel statusLabel;
    private final JLabel grassLabel;
    private final JLabel timeLabel;
    private final JButton pauseBtn;
    private final JButton graphsBtn;

    public SimPanel(World world, Runnable onExit) {
        this.world = world;
        this.onExit = onExit;

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(FPSJFrame.WIDTH, FPSJFrame.HEIGHT));
        setFocusable(true);

        // ---- Top bar ----
        JPanel topBar = new JPanel(new BorderLayout(8, 0));
        topBar.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

        statusLabel = new JLabel();
        statusLabel.setFont(new Font("Monospaced", Font.BOLD, 13));
        topBar.add(statusLabel, BorderLayout.WEST);

        JPanel centerInfo = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        centerInfo.setOpaque(false);
        grassLabel = new JLabel();
        grassLabel.setFont(new Font("Monospaced", Font.BOLD, 13));
        grassLabel.setForeground(new Color(80, 200, 80));
        timeLabel = new JLabel();
        timeLabel.setFont(new Font("Monospaced", Font.PLAIN, 13));
        centerInfo.add(grassLabel);
        centerInfo.add(timeLabel);
        topBar.add(centerInfo, BorderLayout.CENTER);

        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        rightBtns.setOpaque(false);

        graphsBtn = new JButton("Graphs");
        graphsBtn.putClientProperty("JButton.buttonType", "borderless");
        graphsBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        graphsBtn.addActionListener(e -> {
            world.hud.toggleGraph();
            positionModal();
        });

        pauseBtn = new JButton();
        pauseBtn.putClientProperty("JButton.buttonType", "borderless");
        pauseBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        pauseBtn.addActionListener(e -> {
            if (world.getStatus() == World.Status.RUNNING)
                world.pause();
            else
                world.resume();
            updateTopBar();
        });

        JButton exitBtn = new JButton("← Exit");
        exitBtn.putClientProperty("JButton.buttonType", "borderless");
        exitBtn.setForeground(new Color(200, 80, 80));
        exitBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        exitBtn.addActionListener(e -> {
            stopRepaint();
            onExit.run();
        });

        rightBtns.add(graphsBtn);
        rightBtns.add(pauseBtn);
        rightBtns.add(exitBtn);
        topBar.add(rightBtns, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // ---- Layered pane: canvas + modal ----
        JLayeredPane layered = new JLayeredPane() {
            @Override
            public void doLayout() {
                // Canvas fills everything
                for (Component c : getComponents()) {
                    if (c instanceof GraphModal)
                        continue;
                    c.setBounds(0, 0, getWidth(), getHeight());
                }
                positionModal();
            }
        };

        JPanel canvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                drawGrid(g2);
                for (Grass grass : world.grassList)
                    grass.draw(g2, hoverRow, hoverCol);
            }
        };
        canvas.setBackground(Color.BLACK);
        canvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                hoverRow = e.getY() / FPSJFrame.TILE_SIZE;
                hoverCol = e.getX() / FPSJFrame.TILE_SIZE;
            }
        });

        layered.add(canvas, JLayeredPane.DEFAULT_LAYER);
        layered.add(world.hud.graphModal(), JLayeredPane.PALETTE_LAYER);
        add(layered, BorderLayout.CENTER);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_G) {
                    world.hud.toggleGraph();
                    positionModal();
                }
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    if (world.hud.isGraphVisible()) {
                        world.hud.toggleGraph();
                        positionModal();
                    } else {
                        stopRepaint();
                        onExit.run();
                    }
                }
            }
        });

        updateTopBar();
        repaintTimer = new Timer(16, e -> {
            repaint();
            updateTopBar();
        });
        repaintTimer.start();
    }

    private void positionModal() {
        GraphModal modal = world.hud.graphModal();
        JLayeredPane layered = (JLayeredPane) modal.getParent();
        if (layered == null)
            return;
        int pw = layered.getWidth();
        int ph = layered.getHeight();
        if (pw == 0 || ph == 0)
            return;
        int margin = 40;
        modal.setBounds(margin, margin, pw - margin * 2, ph - margin * 2);
        layered.revalidate();
        layered.repaint();
    }

    private void updateTopBar() {
        boolean paused = world.getStatus() == World.Status.PAUSED;
        statusLabel.setText(world.getName());
        pauseBtn.setText(paused ? "Resume" : "Pause");
        pauseBtn.setForeground(paused ? new Color(200, 160, 40) : new Color(80, 200, 80));
        grassLabel.setText("Grass: " + world.getGrassPopulation());
        timeLabel.setText("⏱  " + world.getElapsedTime());
        graphsBtn.setForeground(world.hud.isGraphVisible() ? new Color(80, 200, 80) : null);
    }

    private void stopRepaint() {
        if (repaintTimer != null)
            repaintTimer.stop();
    }

    private void drawGrid(Graphics2D g) {
        for (int row = 0; row < FPSJFrame.GRID_ROWS; row++) {
            for (int col = 0; col < FPSJFrame.GRID_COLS; col++) {
                int x = col * FPSJFrame.TILE_SIZE;
                int y = row * FPSJFrame.TILE_SIZE;
                g.setColor(new Color(20, 20, 20));
                g.fillRect(x, y, FPSJFrame.TILE_SIZE, FPSJFrame.TILE_SIZE);
                g.setColor(new Color(35, 35, 35));
                g.drawRect(x, y, FPSJFrame.TILE_SIZE, FPSJFrame.TILE_SIZE);
            }
        }
    }
}
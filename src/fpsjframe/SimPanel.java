package fpsjframe;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SimPanel extends JPanel {

    private final World world;
    private final Runnable onExit;

    private Timer repaintTimer;

    private final JLabel statusLabel;
    private final JLabel scoreLabel;
    private final JLabel timeLabel;
    private final JButton pauseBtn;
    private final JButton graphsBtn;

    private static final Color PITCH_DARK = new Color(34, 120, 50);
    private static final Color PITCH_LIGHT = new Color(40, 140, 58);
    private static final Color LINE_COL = new Color(255, 255, 255, 180);

    public SimPanel(World world, Runnable onExit) {
        this.world = world;
        this.onExit = onExit;

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(FPSJFrame.WIDTH, FPSJFrame.HEIGHT));
        setFocusable(true);

        JPanel topBar = new JPanel(new BorderLayout(8, 0));
        topBar.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

        statusLabel = new JLabel();
        statusLabel.setFont(new Font("Monospaced", Font.BOLD, 13));
        topBar.add(statusLabel, BorderLayout.WEST);

        JPanel centerInfo = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        centerInfo.setOpaque(false);
        scoreLabel = new JLabel();
        scoreLabel.setFont(new Font("Monospaced", Font.BOLD, 13));
        scoreLabel.setForeground(new Color(80, 200, 80));
        timeLabel = new JLabel();
        timeLabel.setFont(new Font("Monospaced", Font.PLAIN, 13));
        centerInfo.add(scoreLabel);
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

        JLayeredPane layered = new JLayeredPane() {
            @Override
            public void doLayout() {
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
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                drawPitch(g2);
                world.ball.draw(g2);
                world.player1.draw(g2);
                world.player2.draw(g2);
            }
        };
        canvas.setBackground(PITCH_DARK);

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

        world.startThread();
        updateTopBar();
        repaintTimer = new Timer(16, e -> {
            repaint();
            updateTopBar();
        });
        repaintTimer.start();
    }

    private void drawPitch(Graphics2D g) {
        int W = FPSJFrame.WIDTH;
        int H = FPSJFrame.HEIGHT - 40;
        int stripeW = W / 10;
        for (int i = 0; i < 10; i++) {
            g.setColor(i % 2 == 0 ? PITCH_DARK : PITCH_LIGHT);
            g.fillRect(i * stripeW, 0, stripeW, H);
        }
        g.setColor(LINE_COL);
        g.setStroke(new BasicStroke(2f));
        g.drawRect(40, 20, W - 80, H - 40);
        g.drawLine(W / 2, 20, W / 2, H - 20);
        int cr = 60;
        g.drawOval(W / 2 - cr, H / 2 - cr, cr * 2, cr * 2);
        g.fillOval(W / 2 - 4, H / 2 - 4, 8, 8);
        int paW = 120, paH = 200;
        g.drawRect(40, H / 2 - paH / 2, paW, paH);
        g.drawRect(W - 40 - paW, H / 2 - paH / 2, paW, paH);
        int gW = 20, gH = 100;
        g.setColor(new Color(255, 255, 255, 220));
        g.setStroke(new BasicStroke(3f));
        g.drawRect(20, H / 2 - gH / 2, gW, gH);
        g.drawRect(W - 40, H / 2 - gH / 2, gW, gH);
    }

    private void positionModal() {
        GraphModal modal = world.hud.graphModal();
        JLayeredPane layered = (JLayeredPane) modal.getParent();
        if (layered == null)
            return;
        int pw = layered.getWidth(), ph = layered.getHeight();
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
        scoreLabel.setText("Goals: " + world.getScore());
        timeLabel.setText("⏱  " + world.getElapsedTime());
        graphsBtn.setForeground(world.hud.isGraphVisible() ? new Color(80, 200, 80) : null);
    }

    private void stopRepaint() {
        if (repaintTimer != null)
            repaintTimer.stop();
        world.pause();
    }
}
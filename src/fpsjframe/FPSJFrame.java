package fpsjframe;

import javax.swing.*;

public class FPSJFrame {

    public static final int GRID_COLS = 50;
    public static final int GRID_ROWS = 30;
    public static final int TILE_SIZE = 32;
    public static final int WIDTH = GRID_COLS * TILE_SIZE;
    public static final int HEIGHT = GRID_ROWS * TILE_SIZE;

    public static void main(String[] args) {
        try {
            Class<?> laf = Class.forName("com.formdev.flatlaf.FlatDarkLaf");
            laf.getMethod("setup").invoke(null);
        } catch (Exception e) {
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Ecological Simulation");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            frame.setSize(WIDTH, HEIGHT);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            showSelect(frame);
        });
    }

    static void showSelect(JFrame frame) {
        show(frame, new WorldSelectScreen(frame, () -> showCreate(frame)));
    }

    static void showCreate(JFrame frame) {
        show(frame, new CreateWorldPanel(world -> showSim(frame, world), () -> showSelect(frame)));
    }

    static void showSim(JFrame frame, World world) {
        show(frame, new SimPanel(world, () -> showSelect(frame)));
    }

    static void show(JFrame frame, java.awt.Component c) {
        frame.getContentPane().removeAll();
        frame.getContentPane().add(c);
        frame.revalidate();
        frame.repaint();
        if (c instanceof SimPanel)
            c.requestFocusInWindow();
    }
}
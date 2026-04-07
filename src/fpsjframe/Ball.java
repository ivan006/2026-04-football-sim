package fpsjframe;

public class Ball {
    public float x, y;

    private static final int COLS = FPSJFrame.GRID_COLS;
    private static final int ROWS = FPSJFrame.GRID_ROWS;
    private static final int TS = FPSJFrame.TILE_SIZE;

    public Ball() {
        reset();
    }

    public void reset() {
        x = (COLS / 2) * TS + TS / 2f;
        y = (ROWS / 2) * TS + TS / 2f;
    }

    public void draw(java.awt.Graphics2D g) {
        int r = 6;
        g.setColor(java.awt.Color.WHITE);
        g.fillOval((int) (x - r), (int) (y - r), r * 2, r * 2);
        g.setColor(java.awt.Color.DARK_GRAY);
        g.drawOval((int) (x - r), (int) (y - r), r * 2, r * 2);
    }
}
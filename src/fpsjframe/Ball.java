package fpsjframe;

import java.awt.*;

public class Ball {

    public float x, y;
    public float vx, vy;

    private static final float FRICTION = 0.97f;
    private static final float STOP_THRESH = 0.1f;

    // Right goal line x
    static final float GOAL_LINE_X = FPSJFrame.GRID_COLS * FPSJFrame.TILE_SIZE - 40f;
    static final float GOAL_TOP_Y = (FPSJFrame.GRID_ROWS / 2f) * FPSJFrame.TILE_SIZE - 50f;
    static final float GOAL_BOT_Y = (FPSJFrame.GRID_ROWS / 2f) * FPSJFrame.TILE_SIZE + 50f;

    public boolean loose = false; // true when ball is in free flight (passed/kicked)

    public Ball() {
        reset();
    }

    public void reset() {
        x = (FPSJFrame.GRID_COLS / 2f) * FPSJFrame.TILE_SIZE;
        y = (FPSJFrame.GRID_ROWS / 2f) * FPSJFrame.TILE_SIZE;
        vx = 0;
        vy = 0;
        loose = false;
    }

    /**
     * Kick the ball toward a target at a given power (pixels/tick initial speed).
     */
    public void kick(float targetX, float targetY, float power) {
        float dx = targetX - x;
        float dy = targetY - y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist == 0)
            return;
        vx = (dx / dist) * power;
        vy = (dy / dist) * power;
        loose = true;
    }

    /**
     * Tick ball physics. Returns true if ball crossed the goal line (goal scored).
     */
    public boolean tick() {
        if (!loose)
            return false;

        x += vx;
        y += vy;
        vx *= FRICTION;
        vy *= FRICTION;

        // Stop if slow enough
        if (Math.abs(vx) < STOP_THRESH && Math.abs(vy) < STOP_THRESH) {
            vx = 0;
            vy = 0;
            loose = false;
        }

        // Goal check — crossed right goal line within post height
        if (x >= GOAL_LINE_X && y >= GOAL_TOP_Y && y <= GOAL_BOT_Y) {
            loose = false;
            return true;
        }

        return false;
    }

    public boolean isStopped() {
        return !loose && vx == 0 && vy == 0;
    }

    public void draw(Graphics2D g) {
        int r = 6;
        g.setColor(Color.WHITE);
        g.fillOval((int) (x - r), (int) (y - r), r * 2, r * 2);
        g.setColor(Color.DARK_GRAY);
        g.drawOval((int) (x - r), (int) (y - r), r * 2, r * 2);
    }
}
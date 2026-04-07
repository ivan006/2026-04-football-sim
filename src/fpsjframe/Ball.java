package fpsjframe;

import java.awt.*;

public class Ball {

    public float x, y;
    public float vx, vy;

    private static final float FRICTION = 0.97f;
    private static final float STOP_THRESH = 0.1f;
    private static final float BOUNCE_DAMP = 0.6f;

    // Pitch boundaries (matches SimPanel drawPitch)
    private static final float LEFT = 40f;
    private static final float RIGHT = FPSJFrame.GRID_COLS * FPSJFrame.TILE_SIZE - 40f;
    private static final float TOP = 20f;
    private static final float BOTTOM = FPSJFrame.GRID_ROWS * FPSJFrame.TILE_SIZE - 60f;

    // Goal posts on right side
    static final float GOAL_LINE_X = RIGHT;
    static final float GOAL_TOP_Y = (FPSJFrame.GRID_ROWS / 2f) * FPSJFrame.TILE_SIZE - 50f;
    static final float GOAL_BOT_Y = (FPSJFrame.GRID_ROWS / 2f) * FPSJFrame.TILE_SIZE + 50f;

    public boolean loose = false;

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
     * Tick ball physics.
     * Returns true if ball crossed the goal line (goal scored).
     */
    public boolean tick() {
        if (!loose)
            return false;

        x += vx;
        y += vy;
        vx *= FRICTION;
        vy *= FRICTION;

        // Goal check — must happen before right wall bounce
        if (x >= GOAL_LINE_X && y >= GOAL_TOP_Y && y <= GOAL_BOT_Y) {
            loose = false;
            vx = 0;
            vy = 0;
            return true;
        }

        // Boundary bouncing
        if (x <= LEFT) {
            x = LEFT;
            vx = Math.abs(vx) * BOUNCE_DAMP;
        }
        if (x >= RIGHT) {
            x = RIGHT;
            vx = -Math.abs(vx) * BOUNCE_DAMP;
        }
        if (y <= TOP) {
            y = TOP;
            vy = Math.abs(vy) * BOUNCE_DAMP;
        }
        if (y >= BOTTOM) {
            y = BOTTOM;
            vy = -Math.abs(vy) * BOUNCE_DAMP;
        }

        // Stop if slow enough
        if (Math.abs(vx) < STOP_THRESH && Math.abs(vy) < STOP_THRESH) {
            vx = 0;
            vy = 0;
            loose = false;
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
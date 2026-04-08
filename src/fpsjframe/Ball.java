package fpsjframe;

import java.awt.*;

public class Ball {

    public float x, y;
    public float vx, vy;

    private static final float FRICTION = 0.97f;
    private static final float STOP_THRESH = 0.1f;
    private static final float BOUNCE_DAMP = 0.6f;
    public boolean possessed = false;

    // Must match SimPanel.drawPitch exactly
    // W = FPSJFrame.WIDTH, H = FPSJFrame.HEIGHT - 40
    private static final float W = FPSJFrame.WIDTH;
    private static final float H = FPSJFrame.HEIGHT - 40;
    private static final float LEFT = 40f;
    private static final float RIGHT = W - 40f;
    private static final float TOP = 20f;
    private static final float BOTTOM = H - 20f;

    // Right goal (matches: drawRect(W-40, H/2-50, 20, 100))
    static final float GOAL_LINE_X = RIGHT;
    static final float GOAL_TOP_Y = H / 2f - 50f;
    static final float GOAL_BOT_Y = H / 2f + 50f;

    public boolean loose = false;

    public Ball() {
        reset();
    }

    public void reset() {
        x = W / 2f;
        y = H / 2f;
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
     * Returns true if ball crossed the goal line (goal scored).
     */
    public boolean tick() {
        if (!loose)
            return false;

        x += vx;
        y += vy;
        vx *= FRICTION;
        vy *= FRICTION;

        // Goal check before right wall bounce
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
        if (possessed)
            return; // player draws it at their feet
        int r = 6;
        g.setColor(Color.WHITE);
        g.fillOval((int) (x - r), (int) (y - r), r * 2, r * 2);
        g.setColor(Color.DARK_GRAY);
        g.drawOval((int) (x - r), (int) (y - r), r * 2, r * 2);
    }
}
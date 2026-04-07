package fpsjframe;

import java.awt.*;

public class Player {

    public enum State {
        NO_BALL, HAS_BALL
    }

    public float x, y;
    public float angle = 0; // radians, direction player faces
    public State state = State.NO_BALL;

    private static final float SPEED_SEEK = 2.5f;
    private static final float SPEED_DRIBBLE = 1.8f;
    private static final float PICKUP_DIST = 18f;
    private static final float GOAL_DIST = 30f;

    // Goal centre (right goal)
    private static final float GOAL_X = (FPSJFrame.GRID_COLS) * FPSJFrame.TILE_SIZE - FPSJFrame.TILE_SIZE;
    private static final float GOAL_Y = (FPSJFrame.GRID_ROWS / 2f) * FPSJFrame.TILE_SIZE;

    public int score = 0;

    public Player() {
        reset();
    }

    public void reset() {
        x = FPSJFrame.TILE_SIZE * 5;
        y = (FPSJFrame.GRID_ROWS / 2f) * FPSJFrame.TILE_SIZE;
        state = State.NO_BALL;
    }

    public void tick(Ball ball) {
        switch (state) {
            case NO_BALL -> seekBall(ball);
            case HAS_BALL -> dribbleToGoal(ball);
        }
    }

    private void seekBall(Ball ball) {
        float dx = ball.x - x;
        float dy = ball.y - y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist < PICKUP_DIST) {
            state = State.HAS_BALL;
            return;
        }
        angle = (float) Math.atan2(dy, dx);
        x += (dx / dist) * SPEED_SEEK;
        y += (dy / dist) * SPEED_SEEK;
    }

    private void dribbleToGoal(Ball ball) {
        float dx = GOAL_X - x;
        float dy = GOAL_Y - y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist < GOAL_DIST) {
            score++;
            ball.reset();
            reset();
            return;
        }
        angle = (float) Math.atan2(dy, dx);
        float nx = dx / dist;
        float ny = dy / dist;
        x += nx * SPEED_DRIBBLE;
        y += ny * SPEED_DRIBBLE;
        ball.x = x;
        ball.y = y;
    }

    public void draw(Graphics2D g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate((int) x, (int) y);
        g2.rotate(angle + Math.PI / 2); // rotate to face direction

        // Shadow
        g2.setColor(new Color(0, 0, 0, 60));
        g2.fillOval(-9, -9, 20, 20);

        // Body (circle)
        g2.setColor(state == State.HAS_BALL ? new Color(80, 180, 255) : new Color(220, 220, 220));
        g2.fillOval(-8, -8, 16, 16);
        g2.setColor(Color.DARK_GRAY);
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawOval(-8, -8, 16, 16);

        // Arms outstretched (horizontal lines from body)
        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(state == State.HAS_BALL ? new Color(80, 180, 255) : new Color(200, 200, 200));
        // left arm
        g2.drawLine(-8, -2, -18, 4);
        // right arm
        g2.drawLine(8, -2, 18, 4);

        g2.dispose();
    }
}
package fpsjframe;

import java.awt.*;
import java.util.LinkedList;
import java.util.Queue;

public class Player {

    public float x, y;
    public float angle = 0;
    public boolean hasBall = false;
    boolean hasPassed = false;

    private Objective currentObjective;
    private final Queue<Objective> objectiveQueue = new LinkedList<>();

    private static final float SPEED_SEEK = 2.5f;
    private static final float SPEED_DRIBBLE = 1.8f;
    private static final float SPEED_RETURN = 2.0f;
    private static final float PASS_RANGE = 120f;
    private static final float PASS_POWER = 4f;
    private static final float PICKUP_DIST = 18f;
    private static final float ARRIVE_DIST = 10f;

    private static final float W = FPSJFrame.WIDTH;
    private static final float H = FPSJFrame.HEIGHT - 40;

    static final float START_X = FPSJFrame.TILE_SIZE * 5;
    static final float START_Y = H / 2f;
    static final float GOAL_X = W - 40f;
    static final float GOAL_Y = H / 2f;
    static final float BALL_CENTER_X = W / 2f;
    static final float BALL_CENTER_Y = H / 2f;

    public int score = 0;

    public Player() {
        x = START_X;
        y = START_Y;
        enqueueNormalCycle();
        // do NOT call nextObjective() here
    }

    private void enqueueNormalCycle() {
        objectiveQueue.add(Objective.OBTAIN_BALL);
        objectiveQueue.add(Objective.ADVANCE_TO_GOAL);
        objectiveQueue.add(Objective.PASS_TO_GOAL);
    }

    private void enqueueAfterGoal() {
        objectiveQueue.add(Objective.OBTAIN_BALL);
        objectiveQueue.add(Objective.CARRY_TO_CENTER);
        objectiveQueue.add(Objective.KICKOFF_RESET);
        enqueueNormalCycle();
    }

    private void nextObjective() {
        currentObjective = objectiveQueue.poll();
    }

    public Objective getCurrentObjective() {
        return currentObjective;
    }

    public void tick(Ball ball) {
        if (currentObjective == null)
            nextObjective();
        if (currentObjective == null)
            return;
        switch (currentObjective) {
            case OBTAIN_BALL -> obtainBall(ball);
            case ADVANCE_TO_GOAL -> advanceToGoal(ball);
            case PASS_TO_GOAL -> passToGoal(ball);
            case CARRY_TO_CENTER -> carryToCenter(ball);
            case KICKOFF_RESET -> kickoffReset();
        }
    }

    private void obtainBall(Ball ball) {
        if (ball.loose)
            return;
        float dx = ball.x - x;
        float dy = ball.y - y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist < PICKUP_DIST) {
            hasBall = true;
            nextObjective();
            return;
        }
        angle = (float) Math.atan2(dy, dx);
        x += (dx / dist) * SPEED_SEEK;
        y += (dy / dist) * SPEED_SEEK;
    }

    private void advanceToGoal(Ball ball) {
        float dx = GOAL_X - x;
        float dy = GOAL_Y - y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist < PASS_RANGE) {
            nextObjective();
            return;
        }
        angle = (float) Math.atan2(dy, dx);
        x += (dx / dist) * SPEED_DRIBBLE;
        y += (dy / dist) * SPEED_DRIBBLE;
        ball.x = x;
        ball.y = y;
    }

    private void passToGoal(Ball ball) {
        if (!hasPassed) {
            float dx = GOAL_X - x;
            float dy = GOAL_Y - y;
            angle = (float) Math.atan2(dy, dx);
            hasBall = false;
            ball.kick(GOAL_X, GOAL_Y, PASS_POWER);
            hasPassed = true;
        }
        // wait — World.tick() calls onGoal() or onPassFailed()
    }

    public void onGoal(Ball ball) {
        score++;
        hasPassed = false;
        ball.vx = 0;
        ball.vy = 0;
        ball.loose = false;
        enqueueAfterGoal();
        nextObjective();
    }

    public void onPassFailed(Ball ball) {
        hasPassed = false;
        hasBall = false;
        objectiveQueue.clear();
        enqueueNormalCycle();
        nextObjective(); // moves to OBTAIN_BALL
    }

    private void carryToCenter(Ball ball) {
        float dx = BALL_CENTER_X - x;
        float dy = BALL_CENTER_Y - y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        // always keep ball on player while carrying
        ball.x = x;
        ball.y = y;
        ball.loose = false;
        if (dist < ARRIVE_DIST) {
            hasBall = false;
            nextObjective();
            return;
        }
        angle = (float) Math.atan2(dy, dx);
        x += (dx / dist) * SPEED_DRIBBLE;
        y += (dy / dist) * SPEED_DRIBBLE;
    }

    private void kickoffReset() {
        float dx = START_X - x;
        float dy = START_Y - y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist < ARRIVE_DIST) {
            x = START_X;
            y = START_Y;
            nextObjective();
            return;
        }
        angle = (float) Math.atan2(dy, dx);
        x += (dx / dist) * SPEED_RETURN;
        y += (dy / dist) * SPEED_RETURN;
    }

    public void draw(Graphics2D g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate((int) x, (int) y);
        g2.rotate(angle - Math.PI / 2);

        g2.setColor(new Color(0, 0, 0, 60));
        g2.fillOval(-9, -9, 20, 20);

        g2.setColor(hasBall ? new Color(80, 180, 255) : new Color(220, 220, 220));
        g2.fillOval(-8, -8, 16, 16);
        g2.setColor(Color.DARK_GRAY);
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawOval(-8, -8, 16, 16);

        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(hasBall ? new Color(80, 180, 255) : new Color(200, 200, 200));
        g2.drawLine(-8, -2, -18, 4);
        g2.drawLine(8, -2, 18, 4);

        if (hasBall) {
            g2.setColor(Color.WHITE);
            g2.fillOval(-5, 14, 10, 10);
            g2.setColor(Color.DARK_GRAY);
            g2.drawOval(-5, 14, 10, 10);
        }

        g2.dispose();
    }
}
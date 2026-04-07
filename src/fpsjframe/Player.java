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
    final Queue<Objective> objectiveQueue = new LinkedList<>();

    public Player passTarget = null;

    private static final float SPEED_SEEK = 2.5f;
    private static final float SPEED_DRIBBLE = 1.8f;
    private static final float SPEED_RETURN = 2.0f;
    private static final float PASS_POWER = 4f;
    private static final float PICKUP_DIST = 18f;
    private static final float ARRIVE_DIST = 10f;
    private static final float PASS_RANGE = 120f;

    // Separation
    private static final float MIN_PASS_DIST = 180f; // must be this far apart to pass
    private static final float IDEAL_DIST = 220f; // target separation distance
    private static final float SEP_SPEED = 1.2f;

    private static final float W = FPSJFrame.WIDTH;
    private static final float H = FPSJFrame.HEIGHT - 40;

    final float startX;
    final float startY;

    static final float GOAL_X = W - 40f;
    static final float GOAL_Y = H / 2f;
    static final float BALL_CENTER_X = W / 2f;
    static final float BALL_CENTER_Y = H / 2f;

    private final Color bodyColor;
    public int score = 0;

    public Player(float startX, float startY, Color bodyColor) {
        this.startX = startX;
        this.startY = startY;
        this.bodyColor = bodyColor;
        x = startX;
        y = startY;
    }

    public void setObjectives(Objective... objectives) {
        objectiveQueue.clear();
        for (Objective o : objectives)
            objectiveQueue.add(o);
        currentObjective = null;
    }

    void nextObjective() {
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
            case GET_READY_TO_PASS -> getReadyToPass();
            case PASS_TO_FRIEND -> passToFriend(ball);
            case GET_READY_TO_RECEIVE -> getReadyToReceive();
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

    private void getReadyToPass() {
        if (passTarget == null) {
            nextObjective();
            return;
        }
        float dx = x - passTarget.x;
        float dy = y - passTarget.y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        // face the target while moving
        angle = (float) Math.atan2(-dy, -dx);
        if (dist >= MIN_PASS_DIST) {
            nextObjective(); // far enough — ready to pass
            return;
        }
        // move away from target to gain separation
        if (dist > 0) {
            x += (dx / dist) * SEP_SPEED;
            y += (dy / dist) * SEP_SPEED;
        }
        // keep ball glued while repositioning
        if (hasBall) {
            // carried silently
        }
    }

    private void passToFriend(Ball ball) {
        if (!hasPassed && passTarget != null) {
            float dx = passTarget.x - x;
            float dy = passTarget.y - y;
            angle = (float) Math.atan2(dy, dx);
            hasBall = false;
            ball.kick(passTarget.x, passTarget.y, PASS_POWER);
            hasPassed = true;
        }
    }

    private void getReadyToReceive() {
        if (passTarget == null) {
            nextObjective();
            return;
        }
        float dx = x - passTarget.x;
        float dy = y - passTarget.y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        // face the passer
        angle = (float) Math.atan2(-dy, -dx);
        if (dist >= MIN_PASS_DIST) {
            nextObjective(); // in position — wait for ball
            return;
        }
        if (dist > 0) {
            x += (dx / dist) * SEP_SPEED;
            y += (dy / dist) * SEP_SPEED;
        }
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
    }

    public void onPassComplete() {
        hasPassed = false;
        // re-queue pass cycle
        objectiveQueue.clear();
        objectiveQueue.add(Objective.GET_READY_TO_PASS);
        objectiveQueue.add(Objective.PASS_TO_FRIEND);
        currentObjective = null;
    }

    public void onGoal(Ball ball) {
        score++;
        hasPassed = false;
        ball.vx = 0;
        ball.vy = 0;
        ball.loose = false;
        objectiveQueue.clear();
        objectiveQueue.add(Objective.OBTAIN_BALL);
        objectiveQueue.add(Objective.CARRY_TO_CENTER);
        objectiveQueue.add(Objective.KICKOFF_RESET);
        objectiveQueue.add(Objective.OBTAIN_BALL);
        objectiveQueue.add(Objective.ADVANCE_TO_GOAL);
        objectiveQueue.add(Objective.PASS_TO_GOAL);
        currentObjective = null;
    }

    public void onPassFailed(Ball ball) {
        hasPassed = false;
        hasBall = false;
        objectiveQueue.clear();
        objectiveQueue.add(Objective.OBTAIN_BALL);
        objectiveQueue.add(Objective.GET_READY_TO_PASS);
        objectiveQueue.add(Objective.PASS_TO_FRIEND);
        currentObjective = null;
    }

    private void carryToCenter(Ball ball) {
        float dx = BALL_CENTER_X - x;
        float dy = BALL_CENTER_Y - y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
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
        float dx = startX - x;
        float dy = startY - y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist < ARRIVE_DIST) {
            x = startX;
            y = startY;
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

        g2.setColor(hasBall ? bodyColor.brighter() : bodyColor);
        g2.fillOval(-8, -8, 16, 16);
        g2.setColor(Color.DARK_GRAY);
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawOval(-8, -8, 16, 16);

        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(hasBall ? bodyColor.brighter() : bodyColor.darker());
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
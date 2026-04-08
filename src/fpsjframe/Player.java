package fpsjframe;

import java.awt.*;

public class Player {

    // ── Identity ──────────────────────────────────────────────────────────────
    private final Color bodyColor;
    final float startX, startY;

    // ── State ─────────────────────────────────────────────────────────────────
    public float x, y;
    public float angle = 0;
    public boolean hasBall = false;
    public static Player ballOwner = null;

    // ── Hierarchy ─────────────────────────────────────────────────────────────
    public ActivityType activityType = ActivityType.TRAINING;
    public PlayerPhase phase = PlayerPhase.SEEKS_POSSESSION;
    public Action currentAction = Action.MOVE_TO_BALL;

    // ── Pass state ────────────────────────────────────────────────────────────
    public Player passTarget = null;
    public boolean readyToPass = false; // separated enough to pass
    boolean hasPassed = false;

    // ── Post-goal carry state ─────────────────────────────────────────────────
    private boolean scoredGoal = false;
    private boolean carryingBack = false;
    private boolean resetting = false;

    // ── Tuning ────────────────────────────────────────────────────────────────
    private static final float SPEED_SEEK = 2.5f;
    private static final float SPEED_DRIBBLE = 1.8f;
    private static final float SPEED_RETURN = 2.0f;
    private static final float PASS_POWER = 4f;
    private static final float PICKUP_DIST = 18f;
    private static final float ARRIVE_DIST = 10f;
    private static final float PASS_RANGE = 120f;
    private static final float MIN_PASS_DIST = 180f;
    private static final float SEP_SPEED = 1.2f;

    // ── Pitch constants ───────────────────────────────────────────────────────
    private static final float W = FPSJFrame.WIDTH;
    private static final float H = FPSJFrame.HEIGHT - 40;

    static final float GOAL_X = W - 40f;
    static final float GOAL_Y = H / 2f;
    static final float BALL_CENTER_X = W / 2f;
    static final float BALL_CENTER_Y = H / 2f;

    public int score = 0;

    // ── Constructor ───────────────────────────────────────────────────────────
    public Player(float startX, float startY, Color bodyColor) {
        this.startX = startX;
        this.startY = startY;
        this.bodyColor = bodyColor;
        x = startX;
        y = startY;
    }

    // ── Main tick ─────────────────────────────────────────────────────────────
    public void tick(Ball ball) {
        updatePhase();
        updateAction();

        switch (phase) {
            case HAS_POSSESSION -> tickHasPossession(ball);
            case SEEKS_POSSESSION -> tickSeeksPossession(ball);
        }
    }

    /** Phase is purely derived from state — no assignment needed elsewhere. */
    private void updatePhase() {
        phase = hasBall ? PlayerPhase.HAS_POSSESSION : PlayerPhase.SEEKS_POSSESSION;
    }

    /**
     * Action is derived from phase + contextual state.
     * This is the decision layer — what should I do right now?
     */
    private void updateAction() {
        switch (phase) {
            case HAS_POSSESSION -> {
                if (scoredGoal && carryingBack) {
                    currentAction = Action.CARRY_TO_CENTER;
                } else if (scoredGoal && resetting) {
                    currentAction = Action.KICKOFF_RESET;
                } else if (!readyToPass) {
                    currentAction = Action.GET_READY_TO_PASS;
                } else {
                    currentAction = Action.PASS_TO_FRIEND;
                }
            }
            case SEEKS_POSSESSION -> {
                if (resetting) {
                    currentAction = Action.KICKOFF_RESET;
                } else if (passTarget != null && !passTarget.hasBall && !hasPassed) {
                    // nobody has the ball — get separation while waiting
                    currentAction = Action.GET_SEPARATION;
                } else {
                    currentAction = Action.MOVE_TO_BALL;
                }
            }
        }
    }

    // ── HAS_POSSESSION actions ────────────────────────────────────────────────
    private void tickHasPossession(Ball ball) {
        switch (currentAction) {
            case GET_READY_TO_PASS -> getReadyToPass();
            case PASS_TO_FRIEND -> passToFriend(ball);
            case CARRY_TO_CENTER -> carryToCenter(ball);
            case KICKOFF_RESET -> kickoffReset();
            default -> {
            }
        }
    }

    // ── SEEKS_POSSESSION actions ──────────────────────────────────────────────
    private void tickSeeksPossession(Ball ball) {
        switch (currentAction) {
            case MOVE_TO_BALL -> moveToBall(ball);
            case GET_SEPARATION -> getSeparation();
            case KICKOFF_RESET -> kickoffReset();
            default -> {
            }
        }
    }

    // ── Action implementations ────────────────────────────────────────────────

    private void getReadyToPass() {
        if (passTarget == null) {
            readyToPass = true;
            return;
        }
        float dx = x - passTarget.x;
        float dy = y - passTarget.y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        angle = (float) Math.atan2(-dy, -dx); // face target
        if (dist >= MIN_PASS_DIST) {
            readyToPass = true;
            return;
        }
        if (dist > 0) {
            x += (dx / dist) * SEP_SPEED;
            y += (dy / dist) * SEP_SPEED;
        }
    }

    private void passToFriend(Ball ball) {
        if (!hasPassed && passTarget != null) {
            float dx = passTarget.x - x;
            float dy = passTarget.y - y;
            angle = (float) Math.atan2(dy, dx);
            hasBall = false;
            ballOwner = null;
            readyToPass = false;
            ball.kick(passTarget.x, passTarget.y, PASS_POWER);
            hasPassed = true;
        }
    }

    private void moveToBall(Ball ball) {
        if (ball.loose)
            return;
        if (ballOwner != null && ballOwner != this)
            return;
        float dx = ball.x - x;
        float dy = ball.y - y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist < PICKUP_DIST) {
            hasBall = true;
            ballOwner = this;
            hasPassed = false;
            return;
        }
        angle = (float) Math.atan2(dy, dx);
        x += (dx / dist) * SPEED_SEEK;
        y += (dy / dist) * SPEED_SEEK;
    }

    private void getSeparation() {
        if (passTarget == null)
            return;
        float dx = x - passTarget.x;
        float dy = y - passTarget.y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        angle = (float) Math.atan2(-dy, -dx);
        if (dist < MIN_PASS_DIST && dist > 0) {
            x += (dx / dist) * SEP_SPEED;
            y += (dy / dist) * SEP_SPEED;
        }
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
            ballOwner = null;
            carryingBack = false;
            resetting = true;
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
            resetting = false;
            scoredGoal = false;
            return;
        }
        angle = (float) Math.atan2(dy, dx);
        x += (dx / dist) * SPEED_RETURN;
        y += (dy / dist) * SPEED_RETURN;
    }

    // ── External events ───────────────────────────────────────────────────────

    /** Called by World when this player's pass reaches the friend. */
    public void onPassComplete() {
        hasPassed = false;
        readyToPass = false;
    }

    /** Called by World when ball crossed goal line. */
    public void onGoal(Ball ball) {
        score++;
        hasPassed = false;
        readyToPass = false;
        scoredGoal = true;
        carryingBack = true;
        resetting = false;
        ball.vx = 0;
        ball.vy = 0;
        ball.loose = false;
        // player walks to ball then carries it — moveToBall handles pickup
        // then updateAction will see scoredGoal+carryingBack → CARRY_TO_CENTER
    }

    /** Called by World when pass stopped short. */
    public void onPassFailed() {
        hasPassed = false;
        readyToPass = false;
        ballOwner = null;
    }

    // ── Rendering ─────────────────────────────────────────────────────────────
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
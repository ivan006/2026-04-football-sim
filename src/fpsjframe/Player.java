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

    // Current resolved action
    private SeekObject currentSeek = SeekObject.BALL;
    private KickObject currentKick = null;
    private boolean kicking = false;

    // ── Pass state ────────────────────────────────────────────────────────────
    public Player passTarget = null;
    public boolean readyToPass = false;
    boolean hasPassed = false;

    // ── Post-goal state ───────────────────────────────────────────────────────
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
        resolveAction(ball);
        executeAction(ball);
    }

    /** Phase derived purely from state. */
    private void updatePhase() {
        phase = hasBall ? PlayerPhase.HAS_POSSESSION : PlayerPhase.SEEKS_POSSESSION;
    }

    /**
     * Resolve what to seek or kick this tick.
     * Only picks from what the current phase's ActionSet permits.
     */
    private void resolveAction(Ball ball) {
        ActionSet allowed = phase.actionSet;
        kicking = false;
        currentKick = null;

        switch (phase) {
            case HAS_POSSESSION -> {
                if (scoredGoal && carryingBack && allowed.canSeek(SeekObject.CENTER)) {
                    currentSeek = SeekObject.CENTER;
                } else if (scoredGoal && resetting && allowed.canSeek(SeekObject.START)) {
                    currentSeek = SeekObject.START;
                } else if (!readyToPass && allowed.canSeek(SeekObject.FRIEND)) {
                    currentSeek = SeekObject.FRIEND; // get separation
                } else if (readyToPass && !hasPassed && allowed.canKick(KickObject.FRIEND)) {
                    kicking = true;
                    currentKick = KickObject.FRIEND;
                }
            }
            case SEEKS_POSSESSION -> {
                if (resetting && allowed.canSeek(SeekObject.START)) {
                    currentSeek = SeekObject.START;
                } else if (passTarget != null && passTarget.hasBall && allowed.canSeek(SeekObject.RELATIVE_POS)) {
                    currentSeek = SeekObject.RELATIVE_POS; // get separation while other has ball
                } else if (allowed.canSeek(SeekObject.BALL)) {
                    currentSeek = SeekObject.BALL;
                }
            }
        }
    }

    /** Execute the resolved seek or kick. */
    private void executeAction(Ball ball) {
        if (kicking) {
            kick(ball, currentKick);
        } else {
            seek(ball, currentSeek);
        }
    }

    // ── Core seek — one method, all targets ───────────────────────────────────
    private void seek(Ball ball, SeekObject obj) {
        float tx, ty, speed;

        switch (obj) {
            case BALL -> {
                if (ball.loose)
                    return;
                if (ballOwner != null && ballOwner != this)
                    return;
                tx = ball.x;
                ty = ball.y;
                speed = SPEED_SEEK;
            }
            case FRIEND -> {
                if (passTarget == null)
                    return;
                // seek separation — move away from friend until MIN_PASS_DIST
                float dx = x - passTarget.x;
                float dy = y - passTarget.y;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                angle = (float) Math.atan2(-dy, -dx);
                if (dist >= MIN_PASS_DIST) {
                    readyToPass = true;
                    return;
                }
                if (dist > 0) {
                    x += (dx / dist) * SEP_SPEED;
                    y += (dy / dist) * SEP_SPEED;
                }
                return;
            }
            case CENTER -> {
                tx = BALL_CENTER_X;
                ty = BALL_CENTER_Y;
                speed = SPEED_DRIBBLE;
                ball.x = x;
                ball.y = y;
                ball.loose = false;
            }
            case START -> {
                tx = startX;
                ty = startY;
                speed = SPEED_RETURN;
            }
            case GOAL -> {
                tx = GOAL_X;
                ty = GOAL_Y;
                speed = SPEED_DRIBBLE;
                ball.x = x;
                ball.y = y;
            }
            case RELATIVE_POS -> {
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
                return;
            }
            default -> {
                return;
            }
        }

        float dx = tx - x;
        float dy = ty - y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (obj == SeekObject.BALL && dist < PICKUP_DIST) {
            hasBall = true;
            ballOwner = this;
            hasPassed = false;
            return;
        }

        if (obj == SeekObject.CENTER && dist < ARRIVE_DIST) {
            hasBall = false;
            ballOwner = null;
            carryingBack = false;
            resetting = true;
            return;
        }

        if (obj == SeekObject.START && dist < ARRIVE_DIST) {
            x = startX;
            y = startY;
            resetting = false;
            scoredGoal = false;
            return;
        }

        if (dist == 0)
            return;
        angle = (float) Math.atan2(dy, dx);
        x += (dx / dist) * speed;
        y += (dy / dist) * speed;
    }

    // ── Core kick — one method, all targets ───────────────────────────────────
    private void kick(Ball ball, KickObject obj) {
        if (hasPassed)
            return;
        float tx, ty;
        switch (obj) {
            case FRIEND -> {
                if (passTarget == null)
                    return;
                tx = passTarget.x;
                ty = passTarget.y;
            }
            case GOAL -> {
                tx = GOAL_X;
                ty = GOAL_Y;
            }
            default -> {
                return;
            }
        }
        float dx = tx - x;
        float dy = ty - y;
        angle = (float) Math.atan2(dy, dx);
        hasBall = false;
        ballOwner = null;
        readyToPass = false;
        ball.kick(tx, ty, PASS_POWER);
        hasPassed = true;
    }

    // ── External events ───────────────────────────────────────────────────────
    public void onPassComplete() {
        hasPassed = false;
        readyToPass = false;
    }

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
    }

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
package fpsjframe;

import java.awt.Color;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class World implements Runnable {

    public enum Status {
        RUNNING, PAUSED
    }

    private final String id;
    private String name;
    private final String createdAt;

    public final int attackPower;
    public final int defensePower;
    public final int movementPower;
    public final int sightRange;

    private volatile Status status = Status.PAUSED;
    private static final long TICK_NS = 1_000_000_000L / 60;

    private static final float H = FPSJFrame.HEIGHT - 40;

    final Tile[][] grid = new Tile[FPSJFrame.GRID_ROWS][FPSJFrame.GRID_COLS];

    final Player player1 = new Player(FPSJFrame.TILE_SIZE * 5, H / 2f, new Color(80, 180, 255));
    final Player player2 = new Player(FPSJFrame.TILE_SIZE * 15, H / 2f, new Color(255, 140, 80));

    final Ball ball = new Ball();
    final Hud hud = new Hud();

    private Thread simThread;
    private long elapsedMs = 0;
    private long lastResumeMs = 0;

    public World(String name, int attackPower, int defensePower,
            int movementPower, int sightRange) {
        this.id = java.util.UUID.randomUUID().toString();
        this.name = name;
        this.attackPower = attackPower;
        this.defensePower = defensePower;
        this.movementPower = movementPower;
        this.sightRange = sightRange;
        this.createdAt = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"));
        initGrid();

        player1.passTarget = player2;
        player2.passTarget = player1;
    }

    private void initGrid() {
        for (int r = 0; r < FPSJFrame.GRID_ROWS; r++)
            for (int c = 0; c < FPSJFrame.GRID_COLS; c++)
                grid[r][c] = new Tile();
    }

    public void startThread() {
        lastResumeMs = System.currentTimeMillis();
        status = Status.RUNNING;
        simThread = new Thread(this, "world-" + id);
        simThread.setDaemon(true);
        simThread.start();
    }

    public void pause() {
        if (status == Status.RUNNING)
            elapsedMs += System.currentTimeMillis() - lastResumeMs;
        status = Status.PAUSED;
    }

    public void resume() {
        if (status == Status.PAUSED) {
            lastResumeMs = System.currentTimeMillis();
            status = Status.RUNNING;
            if (simThread == null || !simThread.isAlive()) {
                simThread = new Thread(this, "world-" + id);
                simThread.setDaemon(true);
                simThread.start();
            }
        }
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        while (true) {
            if (status == Status.PAUSED) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    return;
                }
                continue;
            }
            long now = System.nanoTime();
            if (now - lastTime >= TICK_NS) {
                lastTime = now;
                tick();
            }
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    private void tick() {
        boolean goal = ball.tick();

        if (goal) {
            if (player1.hasPassed)
                player1.onGoal(ball);
            else
                player2.onGoal(ball);
        } else {
            checkPassArrival(player1, player2);
            checkPassArrival(player2, player1);

            if (ball.isStopped() && !player1.hasBall && !player2.hasBall) {
                if (player1.hasPassed)
                    player1.onPassFailed();
                else if (player2.hasPassed)
                    player2.onPassFailed();
            }
        }

        // Ball always follows its owner
        if (player1.hasBall) {
            ball.x = player1.x;
            ball.y = player1.y;
        } else if (player2.hasBall) {
            ball.x = player2.x;
            ball.y = player2.y;
        }

        player1.tick(ball);
        player2.tick(ball);
        hud.tick(player1.score + player2.score);
    }

    private void checkPassArrival(Player passer, Player receiver) {
        if (!passer.hasPassed)
            return;
        if (!ball.loose)
            return;
        float dx = receiver.x - ball.x;
        float dy = receiver.y - ball.y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist < 24f) {
            ball.vx = 0;
            ball.vy = 0;
            ball.loose = false;
            ball.x = receiver.x;
            ball.y = receiver.y;
            passer.hasBall = false;
            passer.hasPassed = false;
            Player.ballOwner = null;
            passer.onPassComplete();
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String n) {
        name = n;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public Status getStatus() {
        return status;
    }

    public int getScore() {
        return player1.score + player2.score;
    }

    public String getElapsedTime() {
        long total = elapsedMs;
        if (status == Status.RUNNING)
            total += System.currentTimeMillis() - lastResumeMs;
        long s = (total / 1000) % 60;
        long m = (total / 60000) % 60;
        long h = total / 3600000;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    public int getGrassPopulation() {
        return getScore();
    }
}
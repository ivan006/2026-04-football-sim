package fpsjframe;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class World implements Runnable {

    public enum Status {
        RUNNING, PAUSED
    }

    // Identity
    private final String id;
    private String name;
    private final String createdAt;

    // Config (set once at creation)
    public final int attackPower;
    public final int defensePower;
    public final int movementPower;
    public final int sightRange;

    // Status
    private volatile Status status = Status.PAUSED;

    // Simulation state
    private static final int GRID_COLS = FPSJFrame.GRID_COLS;
    private static final int GRID_ROWS = FPSJFrame.GRID_ROWS;
    private static final long TICK_NS = 1_000_000_000L / 60;

    final Tile[][] grid = new Tile[GRID_ROWS][GRID_COLS];
    final List<Grass> grassList = new CopyOnWriteArrayList<>();
    final Hud hud = new Hud();

    private boolean gridFull = false;
    private int claimedCount = 0;
    private Thread simThread;

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
        applyConfig();
        spawnInitialGrass();
    }

    private void applyConfig() {
        Grass.ATTACK_POWER = attackPower;
        Grass.DEFENSE_POWER = defensePower;
        Grass.MOVEMENT_POWER = movementPower;
        Grass.SIGHT_RANGE = sightRange;
    }

    private void initGrid() {
        for (int row = 0; row < GRID_ROWS; row++)
            for (int col = 0; col < GRID_COLS; col++)
                grid[row][col] = new Tile();
    }

    private void spawnInitialGrass() {
        int centerRow = GRID_ROWS / 2;
        int centerCol = GRID_COLS / 2;
        grassList.add(new Grass(
                centerCol * FPSJFrame.TILE_SIZE,
                centerRow * FPSJFrame.TILE_SIZE,
                centerRow, centerCol));
    }

    // Elapsed time — only accumulates while RUNNING
    private long elapsedMs = 0;
    private long lastResumeMs = 0;

    public void onTileClaimed() {
        claimedCount++;
        if (claimedCount >= GRID_ROWS * GRID_COLS)
            gridFull = true;
    }

    public boolean isGridFull() {
        return gridFull;
    }

    // Start the background thread
    public void startThread() {
        lastResumeMs = System.currentTimeMillis();
        status = Status.RUNNING;
        simThread = new Thread(this, "world-" + id);
        simThread.setDaemon(true);
        simThread.start();
    }

    public void pause() {
        if (status == Status.RUNNING) {
            elapsedMs += System.currentTimeMillis() - lastResumeMs;
        }
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
        applyConfig(); // ensure config is applied for this world on its tick
        for (Grass g : grassList)
            g.tick(grid, grassList, this);
        grassList.removeIf(Grass::isDead);
        int pop = (int) grassList.stream().filter(gr -> !gr.isDying()).count();
        hud.tick(pop);
    }

    // Getters
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
        return (int) grassList.stream().filter(gr -> !gr.isDying()).count();
    }
}
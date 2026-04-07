package fpsjframe;

import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Grass {

    private static final GrassRenderer renderer = new GrassRenderer();

    // Stats (set by player before sim starts)
    public static int ATTACK_POWER = 5;
    public static int DEFENSE_POWER = 5;
    public static int MOVEMENT_POWER = 5;
    public static int SIGHT_RANGE = 5;

    // Energy
    private static final int MAX_ENERGY = 20;
    private int energy = MAX_ENERGY / 2; // start at 50%

    // Shield - each energy unit has a shield, this tracks current shield on the
    // "top" unit
    private int shield;

    // Position in pixels (smooth movement)
    private float px;
    private float py;

    // Target tile (grid coords)
    private int targetRow;
    private int targetCol;

    // Claimed tile (grid coords), -1 if not yet claimed
    private int claimedRow = -1;
    private int claimedCol = -1;

    private boolean settled = false;
    private boolean dead = false;
    private int fadeTicksRemaining = 0;
    private static final int FADE_DURATION = 60;

    // Attack accumulator: ticks until next energy gain
    private int attackTicksRemaining;

    // Quadrant offset: top-left quadrant of a tile
    private static final float QUADRANT_OFFSET_X = 0.0f; // left half
    private static final float QUADRANT_OFFSET_Y = 0.0f; // top half

    public Grass(float startPx, float startPy, int targetRow, int targetCol) {
        this.px = startPx;
        this.py = startPy;
        this.targetRow = targetRow;
        this.targetCol = targetCol;
        this.shield = DEFENSE_POWER;
        this.attackTicksRemaining = attackInterval();
    }

    private int attackInterval() {
        return (int) (120.0 / ATTACK_POWER);
    }

    public void tick(Tile[][] grid, List<Grass> grassList, World world) {
        if (dead) {
            if (fadeTicksRemaining > 0)
                fadeTicksRemaining--;
            return;
        }
        if (!settled) {
            moveTowardTarget(grid, world);
        } else {
            if (claimedRow == -1) {
                triggerDeath();
                return;
            }
            attack(grid, grassList);
        }
    }

    private void triggerDeath() {
        dead = true;
        fadeTicksRemaining = FADE_DURATION;
    }

    private void moveTowardTarget(Tile[][] grid, World world) {
        if (findNearestUnclaimedTile(grid, SIGHT_RANGE) == null) {
            triggerDeath();
            return;
        }
        if (grid[targetRow][targetCol].isClaimedByGrass()) {
            int[] nearest = findNearestUnclaimedTile(grid, Double.MAX_VALUE);
            if (nearest == null) {
                energy = 0;
                triggerDeath();
                return;
            }
            targetRow = nearest[0];
            targetCol = nearest[1];
        }

        float targetPx = targetCol * FPSJFrame.TILE_SIZE;
        float targetPy = targetRow * FPSJFrame.TILE_SIZE;
        float speed = MOVEMENT_POWER * 0.5f;
        float dx = targetPx - px;
        float dy = targetPy - py;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist <= speed) {
            px = targetPx;
            py = targetPy;
            settled = true;
            claimedRow = targetRow;
            claimedCol = targetCol;
            grid[claimedRow][claimedCol].claimByGrass();
            world.onTileClaimed();
        } else {
            px += (dx / dist) * speed;
            py += (dy / dist) * speed;
        }
    }

    private void attack(Tile[][] grid, List<Grass> grassList) {
        attackTicksRemaining--;
        if (attackTicksRemaining <= 0) {
            attackTicksRemaining = attackInterval();
            energy++;
            if (energy >= MAX_ENERGY)
                reproduce(grid, grassList);
        }
    }

    private void reproduce(Tile[][] grid, List<Grass> grassList) {
        int[] nearest = findNearestUnclaimedTile(grid, SIGHT_RANGE);
        if (nearest == null)
            return;

        energy = MAX_ENERGY / 2;

        float offPx = px + FPSJFrame.TILE_SIZE;
        float offPy = py;

        // Only spawn offspring if there's a free tile within 5 of their spawn point
        Grass offspring = new Grass(offPx, offPy, nearest[0], nearest[1]);
        offspring.energy = MAX_ENERGY / 2;

        // Check from offspring's perspective
        if (offspring.findNearestUnclaimedTile(grid, SIGHT_RANGE) == null) {
            offspring.triggerDeath();
        }

        grassList.add(offspring);
    }

    private static final java.util.Random rng = new java.util.Random();

    private int[] findNearestUnclaimedTile(Tile[][] grid, double maxDist) {
        int bestRow = -1, bestCol = -1;
        double bestDistSq = Double.MAX_VALUE;
        double maxDistSq = maxDist * maxDist;

        int myRow = settled ? claimedRow : targetRow;
        int myCol = settled ? claimedCol : targetCol;

        // Randomise scan order to remove directional bias
        int total = FPSJFrame.GRID_ROWS * FPSJFrame.GRID_COLS;
        int[] indices = new int[total];
        for (int i = 0; i < total; i++)
            indices[i] = i;
        for (int i = total - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            int tmp = indices[i];
            indices[i] = indices[j];
            indices[j] = tmp;
        }

        for (int idx : indices) {
            int row = idx / FPSJFrame.GRID_COLS;
            int col = idx % FPSJFrame.GRID_COLS;
            if (!grid[row][col].isClaimedByGrass()) {
                double dRow = row - myRow;
                double dCol = col - myCol;
                double distSq = dRow * dRow + dCol * dCol;
                if (distSq <= maxDistSq && distSq < bestDistSq) {
                    bestDistSq = distSq;
                    bestRow = row;
                    bestCol = col;
                }
            }
        }

        return (bestRow == -1) ? null : new int[] { bestRow, bestCol };
    }

    public void draw(Graphics2D g, int hoverRow, int hoverCol) {
        renderer.draw(g, this, hoverRow, hoverCol);
    }

    // Getters for renderer
    public float getPx() {
        return px;
    }

    public float getPy() {
        return py;
    }

    public float getFadeAlpha() {
        return fadeTicksRemaining / (float) FADE_DURATION;
    }

    public float getEnergyRatio() {
        return Math.min(1f, Math.max(0f, energy / (float) MAX_ENERGY));
    }

    public float getShieldRatio() {
        return Math.min(1f, Math.max(0f, shield / (float) DEFENSE_POWER));
    }

    public boolean isDead() {
        return dead && fadeTicksRemaining <= 0;
    }

    public boolean isDying() {
        return dead;
    }

    public boolean isSettled() {
        return settled;
    }

    public int getClaimedRow() {
        return claimedRow;
    }

    public int getClaimedCol() {
        return claimedCol;
    }
}
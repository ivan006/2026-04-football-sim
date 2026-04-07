package fpsjframe;

import java.awt.*;

public class GrassRenderer {

    public void draw(Graphics2D g, Grass grass, int hoverRow, int hoverCol) {
        int drawX = (int) grass.getPx();
        int drawY = (int) grass.getPy();
        int half = FPSJFrame.TILE_SIZE / 2;

        Composite original = g.getComposite();
        if (grass.isDying() && !grass.isDead()) {
            float alpha = grass.getFadeAlpha();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        }

        drawTuft(g, drawX + half / 2, drawY + half);

        boolean hovered = grass.isSettled()
                && grass.getClaimedRow() == hoverRow
                && grass.getClaimedCol() == hoverCol;
        if (hovered && !grass.isDying())
            drawBars(g, drawX, drawY, grass);

        g.setComposite(original);
    }

    private void drawTuft(Graphics2D g, int cx, int baseY) {
        g.setColor(new Color(60, 180, 60));
        g.setStroke(new BasicStroke(2));

        int bladeLen = FPSJFrame.TILE_SIZE / 3;
        g.drawLine(cx, baseY, cx - 4, baseY - bladeLen);
        g.drawLine(cx, baseY, cx, baseY - bladeLen - 2);
        g.drawLine(cx, baseY, cx + 4, baseY - bladeLen);

        g.setStroke(new BasicStroke(1));
    }

    private void drawBars(Graphics2D g, int x, int y, Grass grass) {
        int barWidth = FPSJFrame.TILE_SIZE / 2 - 2;
        int barHeight = 3;
        int barX = x + 1;

        // Shield bar
        int shieldBarY = y - 10;
        g.setColor(Color.DARK_GRAY);
        g.fillRect(barX, shieldBarY, barWidth, barHeight);
        g.setColor(new Color(80, 160, 255));
        int shieldFill = (int) (grass.getShieldRatio() * barWidth);
        g.fillRect(barX, shieldBarY, shieldFill, barHeight);

        // Energy bar
        int energyBarY = shieldBarY + barHeight + 1;
        g.setColor(Color.DARK_GRAY);
        g.fillRect(barX, energyBarY, barWidth, barHeight);
        float er = grass.getEnergyRatio();
        g.setColor(new Color((int) ((1 - er) * 200), (int) (er * 200), 0));
        g.fillRect(barX, energyBarY, (int) (er * barWidth), barHeight);
    }
}
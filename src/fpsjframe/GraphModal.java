package fpsjframe;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GraphModal extends JPanel {

    // Data
    private static final int SAMPLE_TICKS = 60;
    static final int PAGE_SAMPLES = 120; // 2 min per page

    private int ticksSinceLastSample = 0;
    private int totalSamples = 0;
    private int currentPage = 0;
    private boolean userNavigated = false;

    private final OrganismGraph grassGraph = new OrganismGraph("Grass", new Color(80, 240, 80), true);
    private final OrganismGraph herbGraph = new OrganismGraph("Herbivore", new Color(220, 200, 80), false);
    private final OrganismGraph carnGraph = new OrganismGraph("Carnivore", new Color(220, 80, 80), false);

    // ---------------------------------------------------------------
    // OrganismGraph — pure canvas panel, no layout knowledge
    // ---------------------------------------------------------------
    static class OrganismGraph extends JPanel {
        final String name;
        final Color color;
        boolean active;
        private final List<Integer> allSamples = new ArrayList<>();
        private int page = 0;
        private int startSec = 0;

        OrganismGraph(String name, Color color, boolean active) {
            this.name = name;
            this.color = color;
            this.active = active;
            setOpaque(false);
        }

        void addSample(int val) {
            allSamples.add(val);
        }

        void setPage(int p, int sec) {
            this.page = p;
            this.startSec = sec;
        }

        int totalPages() {
            return Math.max(1, (int) Math.ceil(allSamples.size() / (double) PAGE_SAMPLES));
        }

        List<Integer> pageSlice() {
            int start = page * PAGE_SAMPLES;
            int end = Math.min(start + PAGE_SAMPLES, allSamples.size());
            if (start >= allSamples.size())
                return new ArrayList<>();
            return new ArrayList<>(allSamples.subList(start, end));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics;
            int x = 0, y = 0, w = getWidth(), h = getHeight();

            // Panel background
            g.setColor(new Color(18, 18, 18));
            g.fillRoundRect(x, y, w, h, 10, 10);
            g.setColor(active ? color.darker() : new Color(50, 50, 50));
            g.drawRoundRect(x, y, w - 1, h - 1, 10, 10);

            // Title
            g.setFont(new Font("Monospaced", Font.BOLD, 13));
            g.setColor(active ? color : new Color(70, 70, 70));
            g.drawString(name, x + 12, y + 18);

            if (!active) {
                g.setFont(new Font("Monospaced", Font.ITALIC, 12));
                g.setColor(new Color(70, 70, 70));
                g.drawString("Coming soon", w / 2 - 40, h / 2);
                return;
            }

            List<Integer> slice = pageSlice();
            if (slice.size() < 2) {
                g.setFont(new Font("Monospaced", Font.ITALIC, 12));
                g.setColor(new Color(100, 100, 100));
                g.drawString("Collecting data...", x + 12, h / 2);
                return;
            }

            int padL = 52, padR = 14, padT = 26, padB = 26;
            int gx = x + padL, gy = y + padT;
            int gw = w - padL - padR, gh = h - padT - padB;

            int maxVal = (int) (FPSJFrame.GRID_COLS * FPSJFrame.GRID_ROWS * 1.3);
            for (int v : slice)
                if (v > maxVal)
                    maxVal = v;

            // Y grid — thick every 500, fine every 100
            g.setFont(new Font("Monospaced", Font.PLAIN, 10));
            for (int val = 0; val <= maxVal; val += 100) {
                int lineY = gy + gh - val * gh / maxVal;
                if (lineY < gy || lineY > gy + gh)
                    continue;
                boolean major = (val % 500 == 0);
                g.setColor(major ? new Color(70, 70, 70) : new Color(30, 30, 30));
                g.setStroke(major ? new BasicStroke(1f) : new BasicStroke(0.5f));
                g.drawLine(gx, lineY, gx + gw, lineY);
                if (major) {
                    g.setStroke(new BasicStroke(1));
                    g.setColor(new Color(140, 140, 140));
                    g.drawString(String.valueOf(val), gx - 48, lineY + 4);
                }
            }
            g.setStroke(new BasicStroke(1));

            // X grid — thick every 10s, fine every 2s
            for (int sec = 0; sec <= PAGE_SAMPLES; sec += 2) {
                int lineX = gx + sec * gw / PAGE_SAMPLES;
                if (lineX < gx || lineX > gx + gw)
                    continue;
                boolean major = (sec % 10 == 0);
                g.setColor(major ? new Color(70, 70, 70) : new Color(30, 30, 30));
                g.setStroke(major ? new BasicStroke(1f) : new BasicStroke(0.5f));
                g.drawLine(lineX, gy, lineX, gy + gh);
                if (major) {
                    int absSec = startSec + sec;
                    g.setStroke(new BasicStroke(1));
                    g.setColor(new Color(140, 140, 140));
                    g.drawString(String.format("%d:%02d", absSec / 60, absSec % 60),
                            lineX - 12, gy + gh + 14);
                }
            }
            g.setStroke(new BasicStroke(1));

            // Axes
            g.setColor(new Color(80, 80, 80));
            g.drawLine(gx, gy, gx, gy + gh);
            g.drawLine(gx, gy + gh, gx + gw, gy + gh);

            // Data line
            g.setColor(color);
            g.setStroke(new BasicStroke(2));
            int n = slice.size();
            for (int i = 1; i < n; i++) {
                int x1 = gx + (i - 1) * gw / PAGE_SAMPLES;
                int y1 = gy + gh - slice.get(i - 1) * gh / maxVal;
                int x2 = gx + i * gw / PAGE_SAMPLES;
                int y2 = gy + gh - slice.get(i) * gh / maxVal;
                g.drawLine(x1, y1, x2, y2);
            }
            g.setStroke(new BasicStroke(1));
        }
    }

    // ---------------------------------------------------------------
    // GraphModal — a real JPanel with proper Swing layout
    // ---------------------------------------------------------------
    public GraphModal() {
        setLayout(new BorderLayout(0, 0));
        setVisible(false);
        setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80), 1, true));
        setBackground(new Color(12, 12, 12));

        // Top bar: title + close button
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 16, 4, 10));

        JLabel titleLabel = new JLabel("Population Graphs");
        titleLabel.setFont(new Font("Monospaced", Font.BOLD, 18));
        topBar.add(titleLabel, BorderLayout.WEST);

        JButton closeBtn = new JButton("✕  Close");
        closeBtn.putClientProperty("JButton.buttonType", "borderless");
        closeBtn.setForeground(new Color(200, 80, 80));
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> setVisible(false));
        topBar.add(closeBtn, BorderLayout.EAST);

        // Pagination bar
        JPanel pageBar = new JPanel(new BorderLayout(8, 0));
        pageBar.setOpaque(false);
        pageBar.setBorder(BorderFactory.createEmptyBorder(0, 16, 6, 16));

        JButton prevBtn = new JButton("< Older");
        prevBtn.putClientProperty("JButton.buttonType", "borderless");
        prevBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel pageLabel = new JLabel("", SwingConstants.CENTER);
        pageLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));
        pageLabel.setForeground(new Color(160, 160, 160));

        JButton nextBtn = new JButton("Newer >");
        nextBtn.putClientProperty("JButton.buttonType", "borderless");
        nextBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        prevBtn.addActionListener(e -> {
            if (currentPage > 0) {
                currentPage--;
                userNavigated = true;
            }
            refreshPage(pageLabel, prevBtn, nextBtn);
        });
        nextBtn.addActionListener(e -> {
            if (currentPage < totalPages() - 1) {
                currentPage++;
                userNavigated = currentPage < totalPages() - 1;
            }
            refreshPage(pageLabel, prevBtn, nextBtn);
        });

        pageBar.add(prevBtn, BorderLayout.WEST);
        pageBar.add(pageLabel, BorderLayout.CENTER);
        pageBar.add(nextBtn, BorderLayout.EAST);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(topBar, BorderLayout.NORTH);
        header.add(pageBar, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);

        // Three stacked graph panels
        JPanel graphArea = new JPanel(new GridLayout(3, 1, 0, 8));
        graphArea.setOpaque(false);
        graphArea.setBorder(BorderFactory.createEmptyBorder(4, 12, 12, 12));
        graphArea.add(grassGraph);
        graphArea.add(herbGraph);
        graphArea.add(carnGraph);

        add(graphArea, BorderLayout.CENTER);

        // Store refs for tick updates
        this.pageLabel = pageLabel;
        this.prevBtn = prevBtn;
        this.nextBtn = nextBtn;
    }

    private final JLabel pageLabel;
    private final JButton prevBtn, nextBtn;

    private void refreshPage(JLabel lbl, JButton prev, JButton next) {
        int pages = totalPages();
        lbl.setText("Page " + (currentPage + 1) + " / " + pages + "  (2 min)");
        prev.setEnabled(currentPage > 0);
        next.setEnabled(currentPage < pages - 1);
        int startSec = currentPage * PAGE_SAMPLES;
        grassGraph.setPage(currentPage, startSec);
        herbGraph.setPage(currentPage, startSec);
        carnGraph.setPage(currentPage, startSec);
        repaint();
    }

    // ---------------------------------------------------------------
    public void tick(int grassPop) {
        ticksSinceLastSample++;
        if (ticksSinceLastSample >= SAMPLE_TICKS) {
            ticksSinceLastSample = 0;
            totalSamples++;
            grassGraph.addSample(grassPop);
            if (!herbGraph.active)
                herbGraph.addSample(0);
            if (!carnGraph.active)
                carnGraph.addSample(0);
            if (!userNavigated)
                currentPage = totalPages() - 1;
            refreshPage(pageLabel, prevBtn, nextBtn);
        }
    }

    int totalPages() {
        return Math.max(1, (int) Math.ceil(totalSamples / (double) PAGE_SAMPLES));
    }

    public void toggle() {
        setVisible(!isVisible());
        if (isVisible())
            refreshPage(pageLabel, prevBtn, nextBtn);
    }

    // Hud.draw no longer needs to draw the modal — SimPanel handles visibility
    public void draw(Graphics2D g, int pw, int ph) {
        /* no-op — modal is a real JPanel now */ }

    public void onClick(int mx, int my) {
        /* no-op */ }
}
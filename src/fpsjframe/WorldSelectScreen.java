package fpsjframe;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

public class WorldSelectScreen extends JPanel {

    private final JFrame frame;
    private final Runnable onCreateNew;

    public WorldSelectScreen(JFrame frame, Runnable onCreateNew) {
        this.frame = frame;
        this.onCreateNew = onCreateNew;
        setLayout(new BorderLayout());
        build();
        new javax.swing.Timer(2000, e -> build()).start();
    }

    private void build() {
        removeAll();

        // Title bar
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBorder(BorderFactory.createEmptyBorder(24, 28, 12, 28));

        JLabel title = new JLabel("Worlds");
        title.setFont(new Font("Monospaced", Font.BOLD, 28));
        titleBar.add(title, BorderLayout.WEST);

        if (onCreateNew != null) {
            JButton newBtn = new JButton("+ New World");
            newBtn.putClientProperty("JButton.buttonType", "default");
            newBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            newBtn.addActionListener(e -> onCreateNew.run());
            titleBar.add(newBtn, BorderLayout.EAST);
        }

        add(titleBar, BorderLayout.NORTH);

        List<World> worlds = WorldManager.getWorlds();

        if (worlds.isEmpty()) {
            JLabel empty = new JLabel("No worlds yet. Create one to begin.", SwingConstants.CENTER);
            empty.setForeground(UIManager.getColor("Label.disabledForeground"));
            add(empty, BorderLayout.CENTER);
        } else {
            JPanel list = new JPanel();
            list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
            list.setBorder(BorderFactory.createEmptyBorder(0, 28, 28, 28));

            for (World w : worlds) {
                list.add(makeWorldCard(w));
                list.add(Box.createVerticalStrut(10));
            }

            JScrollPane scroll = new JScrollPane(list);
            scroll.setBorder(null);
            add(scroll, BorderLayout.CENTER);
        }

        revalidate();
        repaint();
    }

    private JPanel makeWorldCard(World world) {
        boolean running = world.getStatus() == World.Status.RUNNING;

        JPanel card = new JPanel(new BorderLayout(20, 0));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor"), 1, true),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        // LEFT: name + date only (status moves into pause button)
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setPreferredSize(new Dimension(200, 0));

        JLabel nameLabel = new JLabel(world.getName());
        nameLabel.setFont(new Font("Monospaced", Font.BOLD, 15));

        JLabel dateLabel = new JLabel(world.getCreatedAt());
        dateLabel.setFont(new Font("Monospaced", Font.PLAIN, 11));
        dateLabel.setForeground(UIManager.getColor("Label.disabledForeground"));

        left.add(nameLabel);
        left.add(Box.createVerticalStrut(3));
        left.add(dateLabel);

        // CENTER: inputs + population table with proper spanning headers
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        // Section headers — INPUTS spans 4 cols, POPULATION spans 1
        JPanel headerRow = new JPanel(new BorderLayout(0, 0));
        headerRow.setOpaque(false);

        // INPUTS header — takes 4/5 of the space
        JPanel inputsHeader = new JPanel(new BorderLayout());
        inputsHeader.setOpaque(false);
        inputsHeader.add(sectionLabel("INPUTS"), BorderLayout.WEST);

        // POPULATION header — takes 1/5
        JPanel popHeader = new JPanel(new BorderLayout());
        popHeader.setOpaque(false);
        popHeader.add(sectionLabel("POPULATION"), BorderLayout.WEST);

        JPanel headerSplit = new JPanel(new GridLayout(1, 6, 16, 0));
        headerSplit.setOpaque(false);
        headerSplit.add(sectionLabel("INPUTS"));
        headerSplit.add(new JLabel());
        headerSplit.add(new JLabel());
        headerSplit.add(new JLabel());
        headerSplit.add(new JLabel());
        headerSplit.add(sectionLabel("POPULATION"));
        center.add(headerSplit);
        center.add(Box.createVerticalStrut(3));

        // Sub-headers with full words
        JPanel subHeader = new JPanel(new GridLayout(1, 6, 16, 0));
        subHeader.setOpaque(false);
        for (String h : new String[] { "Organism", "Attack", "Defense", "Speed", "Sight", "Count" })
            subHeader.add(sectionLabel(h));
        center.add(subHeader);
        center.add(Box.createVerticalStrut(3));

        // Data rows — organism name prepended
        String[][] inputRows = {
                { "Grass", String.valueOf(world.attackPower), String.valueOf(world.defensePower),
                        String.valueOf(world.movementPower), String.valueOf(world.sightRange),
                        String.valueOf(world.getGrassPopulation()) },
                { "Herbivore", "—", "—", "—", "—", "—" },
                { "Carnivore", "—", "—", "—", "—", "—" }
        };
        Color[] colors = { new Color(80, 200, 80), null, null };

        for (int r = 0; r < inputRows.length; r++) {
            JPanel row = new JPanel(new GridLayout(1, 6, 16, 0));
            row.setOpaque(false);
            Color c = colors[r] != null ? colors[r] : UIManager.getColor("Label.disabledForeground");
            for (String val : inputRows[r]) {
                JLabel lbl = new JLabel(val);
                lbl.setFont(new Font("Monospaced", Font.PLAIN, 12));
                lbl.setForeground(c);
                row.add(lbl);
            }
            center.add(row);
        }

        // RIGHT: buttons — pause button shows status + action together
        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));

        JButton enterBtn = new JButton("Enter →");
        enterBtn.putClientProperty("JButton.buttonType", "default");
        enterBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        enterBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        enterBtn.addActionListener(e -> enterWorld(world));

        // Status + toggle in one button: "● Running — Pause" or "⏸ Paused — Resume"
        JButton pauseBtn = new JButton(running ? "Pause" : "Resume");
        pauseBtn.putClientProperty("JButton.buttonType", "borderless");
        pauseBtn.setForeground(running ? new Color(80, 200, 80) : new Color(200, 160, 40));
        pauseBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        pauseBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        pauseBtn.addActionListener(e -> {
            if (world.getStatus() == World.Status.RUNNING)
                world.pause();
            else
                world.resume();
            refresh();
        });

        JButton deleteBtn = new JButton("Delete");
        deleteBtn.putClientProperty("JButton.buttonType", "borderless");
        deleteBtn.setForeground(new Color(200, 80, 80));
        deleteBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        deleteBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        deleteBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(frame,
                    "Delete \"" + world.getName() + "\"?", "Confirm",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                WorldManager.removeWorld(world);
                refresh();
            }
        });

        buttons.add(enterBtn);
        buttons.add(Box.createVerticalStrut(4));
        buttons.add(pauseBtn);
        buttons.add(Box.createVerticalStrut(4));
        buttons.add(deleteBtn);

        card.add(left, BorderLayout.WEST);
        card.add(center, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.EAST);

        return card;
    }

    private JPanel makeTable(String[] headers, String[][] rows, Color[] rowColors) {
        int cols = headers.length;
        JPanel table = new JPanel(new GridLayout(rows.length + 1, cols, 12, 3));
        table.setOpaque(false);

        // Header row
        for (String h : headers) {
            JLabel lbl = new JLabel(h);
            lbl.setFont(new Font("Monospaced", Font.BOLD, 10));
            lbl.setForeground(UIManager.getColor("Label.disabledForeground"));
            table.add(lbl);
        }

        // Data rows
        for (int r = 0; r < rows.length; r++) {
            Color c = (rowColors != null && r < rowColors.length && rowColors[r] != null)
                    ? rowColors[r]
                    : UIManager.getColor("Label.disabledForeground");
            for (int col = 0; col < cols; col++) {
                JLabel lbl = new JLabel(rows[r][col]);
                lbl.setFont(new Font("Monospaced", Font.PLAIN, 12));
                lbl.setForeground(c);
                table.add(lbl);
            }
        }
        return table;
    }

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Monospaced", Font.BOLD, 10));
        l.setForeground(UIManager.getColor("Label.disabledForeground"));
        return l;
    }

    private JLabel statRow(String organism, String value, Color color) {
        JLabel l = new JLabel(organism + ":  " + value);
        l.setFont(new Font("Monospaced", Font.PLAIN, 12));
        if (color != null)
            l.setForeground(color);
        return l;
    }

    private void enterWorld(World world) {
        SimPanel sim = new SimPanel(world, () -> {
            frame.getContentPane().removeAll();
            frame.getContentPane().add(this);
            frame.revalidate();
            frame.repaint();
            refresh();
        });
        frame.getContentPane().removeAll();
        frame.getContentPane().add(sim);
        frame.revalidate();
        frame.repaint();
        sim.requestFocusInWindow();
    }

    public void refresh() {
        build();
    }
}
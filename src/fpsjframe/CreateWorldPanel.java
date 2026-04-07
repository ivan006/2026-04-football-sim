package fpsjframe;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.function.Consumer;

public class CreateWorldPanel extends JPanel {

    public CreateWorldPanel(Consumer<World> onWorldCreated) {
        this(onWorldCreated, null);
    }

    public CreateWorldPanel(Consumer<World> onWorldCreated, Runnable onBack) {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        int row = 0;

        // Back button
        if (onBack != null) {
            JButton backBtn = new JButton("← Back");
            backBtn.putClientProperty("JButton.buttonType", "borderless");
            backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            backBtn.addActionListener(e -> onBack.run());
            JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            backPanel.setOpaque(false);
            backPanel.add(backBtn);
            gbc.gridy = row++;
            gbc.insets = new Insets(12, 18, 0, 18);
            add(backPanel, gbc);
        }

        // Title
        JLabel title = new JLabel("New World");
        title.setFont(new Font("Monospaced", Font.BOLD, 32));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = row++;
        gbc.insets = new Insets(24, 18, 4, 18);
        add(title, gbc);

        // Subtitle
        JLabel subtitle = new JLabel("Configure organisms before the simulation begins.");
        subtitle.setForeground(UIManager.getColor("Label.disabledForeground"));
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 18, 16, 18);
        add(subtitle, gbc);

        // World name
        JTextField nameField = new JTextField("New World");
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 18, 20, 18);
        add(nameField, gbc);

        // Sliders
        JSlider attackSlider = new JSlider(1, 10, 5);
        JSlider defenseSlider = new JSlider(1, 10, 5);
        JSlider movementSlider = new JSlider(1, 10, 5);
        JSlider sightSlider = new JSlider(1, 10, 5);

        // Table
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 18, 20, 18);
        add(makeConfigTable(attackSlider, defenseSlider, movementSlider, sightSlider), gbc);

        // Start button
        JButton startButton = new JButton("Begin Simulation");
        startButton.putClientProperty("JButton.buttonType", "default");
        startButton.setFont(new Font("Monospaced", Font.BOLD, 16));
        startButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        startButton.addActionListener(e -> {
            String worldName = nameField.getText().isBlank() ? null : nameField.getText().trim();
            World w = WorldManager.createWorld(
                    worldName,
                    attackSlider.getValue(),
                    defenseSlider.getValue(),
                    movementSlider.getValue(),
                    sightSlider.getValue());
            onWorldCreated.accept(w);
        });

        gbc.gridy = row++;
        gbc.insets = new Insets(8, 80, 40, 80);
        add(startButton, gbc);
    }

    private JPanel makeConfigTable(JSlider atk, JSlider def, JSlider spd, JSlider sight) {
        JPanel table = new JPanel(new BorderLayout(0, 0));
        table.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor"), 1, true));

        // Column headers
        String[] headers = { "Organism", "Attack", "Defense", "Speed", "Sight" };
        JPanel headerRow = new JPanel(new GridLayout(1, headers.length, 0, 0));
        for (String h : headers) {
            JLabel lbl = new JLabel(h, SwingConstants.CENTER);
            lbl.setFont(new Font("Monospaced", Font.BOLD, 11));
            lbl.setForeground(UIManager.getColor("Label.disabledForeground"));
            lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 1, UIManager.getColor("Component.borderColor")),
                    BorderFactory.createEmptyBorder(6, 8, 6, 8)));
            headerRow.add(lbl);
        }
        table.add(headerRow, BorderLayout.NORTH);

        // Rows
        JPanel[] sliderRows = {
                makeTableRow("Grass", new Color(80, 200, 80), atk, def, spd, sight),
                makeTableRow("Herbivore", UIManager.getColor("Label.disabledForeground"), null, null, null, null),
                makeTableRow("Carnivore", UIManager.getColor("Label.disabledForeground"), null, null, null, null),
        };

        JPanel body = new JPanel(new GridLayout(sliderRows.length, 1, 0, 0));
        for (JPanel r : sliderRows)
            body.add(r);
        table.add(body, BorderLayout.CENTER);

        return table;
    }

    private JPanel makeTableRow(String organism, Color color, JSlider atk, JSlider def, JSlider spd, JSlider sight) {
        JPanel row = new JPanel(new GridLayout(1, 5, 0, 0));

        // Organism name cell
        JLabel nameLbl = new JLabel(organism);
        nameLbl.setFont(new Font("Monospaced", Font.BOLD, 13));
        nameLbl.setForeground(color);
        nameLbl.setBorder(cellBorder());
        row.add(nameLbl);

        JSlider[] sliders = { atk, def, spd, sight };
        for (JSlider slider : sliders) {
            if (slider == null) {
                JLabel placeholder = new JLabel("—", SwingConstants.CENTER);
                placeholder.setFont(new Font("Monospaced", Font.PLAIN, 12));
                placeholder.setForeground(UIManager.getColor("Label.disabledForeground"));
                placeholder.setBorder(cellBorder());
                row.add(placeholder);
            } else {
                row.add(makeSliderCell(slider, color));
            }
        }

        return row;
    }

    private JPanel makeSliderCell(JSlider slider, Color color) {
        JPanel cell = new JPanel(new BorderLayout(0, 2));
        cell.setBorder(cellBorder());

        JLabel valueLabel = new JLabel(String.valueOf(slider.getValue()), SwingConstants.CENTER);
        valueLabel.setFont(new Font("Monospaced", Font.BOLD, 13));
        valueLabel.setForeground(color);
        slider.addChangeListener(e -> valueLabel.setText(String.valueOf(slider.getValue())));

        slider.setOpaque(false);
        cell.add(valueLabel, BorderLayout.NORTH);
        cell.add(slider, BorderLayout.CENTER);
        return cell;
    }

    private Border cellBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, UIManager.getColor("Component.borderColor")),
                BorderFactory.createEmptyBorder(8, 10, 8, 10));
    }
}
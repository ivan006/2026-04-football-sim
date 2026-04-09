import javax.swing.*;
import java.awt.*;

public class SwingForm {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Swing Form");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 200);

        JPanel panel = new JPanel(new GridBagLayout()); // center layout
        JPanel form = new JPanel(new GridLayout(3, 2, 5, 5));

        form.add(new JLabel("Name:"));
        form.add(new JTextField(10));
        form.add(new JLabel("Email:"));
        form.add(new JTextField(10));
        form.add(new JButton("Submit"));

        panel.add(form);
        frame.add(panel);
        frame.setVisible(true);
    }
}
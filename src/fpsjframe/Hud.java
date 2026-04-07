package fpsjframe;

import java.awt.*;

public class Hud {

    private final GraphModal graphModal = new GraphModal();

    public Hud() {
    }

    public GraphModal graphModal() {
        return graphModal;
    }

    public void tick(int grassPop) {
        graphModal.tick(grassPop);
    }

    public void toggleGraph() {
        graphModal.toggle();
    }

    public boolean isGraphVisible() {
        return graphModal.isVisible();
    }

    public boolean onClick(int mouseX, int mouseY) {
        return false;
    }

    public void draw(Graphics2D g, int panelWidth, int panelHeight) {
        /* modal is a real JPanel now */ }
}
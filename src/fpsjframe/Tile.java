package fpsjframe;

public class Tile {

    private boolean claimedByGrass = false;

    public boolean isClaimedByGrass() {
        return claimedByGrass;
    }

    public void claimByGrass() {
        this.claimedByGrass = true;
    }

    public void releaseGrass() {
        this.claimedByGrass = false;
    }
}
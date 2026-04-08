package fpsjframe;

public enum PlayerPhase {

    HAS_POSSESSION(ActionSet.HAS_POSSESSION),
    SEEKS_POSSESSION(ActionSet.SEEKS_POSSESSION);

    public final ActionSet actionSet;

    PlayerPhase(ActionSet actionSet) {
        this.actionSet = actionSet;
    }
}
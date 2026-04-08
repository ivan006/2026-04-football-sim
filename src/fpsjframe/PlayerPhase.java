package fpsjframe;

public enum PlayerPhase {

    HAS_POSSESSION(PlayerActionSet.HAS_POSSESSION),
    SEEKS_POSSESSION(PlayerActionSet.SEEKS_POSSESSION);

    public final PlayerActionSet playerActionSet;

    PlayerPhase(PlayerActionSet playerActionSet) {
        this.playerActionSet = playerActionSet;
    }
}
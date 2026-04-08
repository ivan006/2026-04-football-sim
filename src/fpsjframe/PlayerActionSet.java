package fpsjframe;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class PlayerPlayerActionSet {

    public final Set<PlayerActionSeekObject> seeks;
    public final Set<PlayerActionKickObject> kicks;

    public PlayerActionSet(Set<PlayerActionSeekObject> seeks, Set<PlayerActionKickObject> kicks) {
        this.seeks = Collections.unmodifiableSet(seeks);
        this.kicks = Collections.unmodifiableSet(kicks);
    }

    public boolean canSeek(PlayerActionSeekObject obj) {
        return seeks.contains(obj);
    }

    public boolean canKick(PlayerActionKickObject obj) {
        return kicks.contains(obj);
    }

    // ── Predefined action sets per phase ─────────────────────────────────────

    public static final PlayerActionSet HAS_POSSESSION = new PlayerActionSet(
            EnumSet.of(PlayerActionSeekObject.FRIEND, PlayerActionSeekObject.CENTER, PlayerActionSeekObject.START),
            EnumSet.of(PlayerActionKickObject.FRIEND, PlayerActionKickObject.GOAL));

    public static final PlayerActionSet SEEKS_POSSESSION = new PlayerActionSet(
            EnumSet.of(PlayerActionSeekObject.BALL, PlayerActionSeekObject.RELATIVE_POS),
            EnumSet.noneOf(PlayerActionKickObject.class));
}
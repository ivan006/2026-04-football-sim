package fpsjframe;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class ActionSet {

    public final Set<SeekObject> seeks;
    public final Set<KickObject> kicks;

    public ActionSet(Set<SeekObject> seeks, Set<KickObject> kicks) {
        this.seeks = Collections.unmodifiableSet(seeks);
        this.kicks = Collections.unmodifiableSet(kicks);
    }

    public boolean canSeek(SeekObject obj) {
        return seeks.contains(obj);
    }

    public boolean canKick(KickObject obj) {
        return kicks.contains(obj);
    }

    // ── Predefined action sets per phase ─────────────────────────────────────

    public static final ActionSet HAS_POSSESSION = new ActionSet(
            EnumSet.of(SeekObject.FRIEND, SeekObject.CENTER, SeekObject.START),
            EnumSet.of(KickObject.FRIEND, KickObject.GOAL));

    public static final ActionSet SEEKS_POSSESSION = new ActionSet(
            EnumSet.of(SeekObject.BALL, SeekObject.RELATIVE_POS),
            EnumSet.noneOf(KickObject.class));
}
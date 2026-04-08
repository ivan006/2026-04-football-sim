package fpsjframe;

public class TeamAction {

    public final boolean crossPlay;
    public final TeamActionMigrateObject migrate; // null = not migrating

    public TeamAction(boolean crossPlay, TeamActionMigrateObject migrate) {
        this.crossPlay = crossPlay;
        this.migrate   = migrate;
    }

    public boolean isCrossPlay() { return crossPlay; }
    public boolean isMigrating() { return migrate != null; }

    // ── Presets ───────────────────────────────────────────────────────────────

    /** Cross play only, no migration. Used in PASSIVE team phase. */
    public static final TeamAction CROSS_PLAY_ONLY =
        new TeamAction(true, null);

    /** Cross play + migrate to final third. Used in ATTACK. */
    public static final TeamAction ATTACK =
        new TeamAction(true, TeamActionMigrateObject.FINAL_THIRD);

    /** No cross play + migrate to own third. Used in DEFENCE. */
    public static final TeamAction DEFENCE =
        new TeamAction(false, TeamActionMigrateObject.OWN_THIRD);

    /** Cross play + migrate to middle. Used in transitions. */
    public static final TeamAction TRANSITION =
        new TeamAction(true, TeamActionMigrateObject.MIDDLE_THIRD);
}
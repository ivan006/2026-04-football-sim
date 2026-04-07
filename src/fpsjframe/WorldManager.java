package fpsjframe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class WorldManager {

    private static final List<World> worlds = new ArrayList<>();

    public static World createWorld(String name, int attack, int defense,
            int movement, int sight) {
        if (name == null || name.isBlank()) {
            name = "New World " + LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"));
        }
        World w = new World(name, attack, defense, movement, sight);
        worlds.add(w);
        w.startThread();
        return w;
    }

    public static List<World> getWorlds() {
        return Collections.unmodifiableList(worlds);
    }

    public static void removeWorld(World w) {
        w.pause();
        worlds.remove(w);
    }
}
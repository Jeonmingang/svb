package com.minkang.ultimate.sdefense.game;

import org.bukkit.entity.EntityType;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class Stage {
    private final int number;
    private final Map<EntityType, Integer> vanilla = new EnumMap<EntityType, Integer>(EntityType.class);
    private final Map<String, Integer> mythic = new HashMap<String, Integer>();

    public Stage(int number) { this.number = number; }

    public int number() { return number; }
    public Map<EntityType, Integer> vanilla() { return vanilla; }
    public Map<String, Integer> mythic() { return mythic; }

    public int totalCount() {
        int sum = 0;
        for (Integer v : vanilla.values()) sum += v;
        for (Integer v : mythic.values()) sum += v;
        return sum;
    }
}

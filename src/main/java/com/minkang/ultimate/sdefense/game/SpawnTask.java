package com.minkang.ultimate.sdefense.game;

import com.minkang.ultimate.sdefense.mythic.MythicHook;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class SpawnTask extends BukkitRunnable {

    private final Arena arena;
    private final List<Object> queue = new ArrayList<Object>(); // EntityType or String(MM id)
    private final int perTick;
    private boolean finished = false;
    private final MythicHook mythic;

    public SpawnTask(Arena arena, Stage stage, int perTick) {
        this.arena = arena;
        this.perTick = Math.max(1, perTick);
        this.mythic = new MythicHook();

        for (Map.Entry<EntityType, Integer> e : stage.vanilla().entrySet()) {
            int count = Math.max(0, e.getValue());
            for (int i = 0; i < count; i++) queue.add(e.getKey());
        }
        for (Map.Entry<String, Integer> e : stage.mythic().entrySet()) {
            int count = Math.max(0, e.getValue());
            for (int i = 0; i < count; i++) queue.add("MM:" + e.getKey());
        }
        Collections.shuffle(queue, new Random());
    }

    public boolean isFinished() { return finished; }

    @Override
    public void run() {
        if (queue.isEmpty()) { finished = true; cancel(); return; }
        int n = Math.min(perTick, queue.size());
        for (int i = 0; i < n; i++) {
            Object token = queue.remove(queue.size() - 1);
            Location at = arena.randomSpawn();
            LivingEntity le = null;
            if (token instanceof EntityType) {
                try { le = (LivingEntity) at.getWorld().spawnEntity(at, (EntityType) token); }
                catch (Throwable ignored) { le = null; }
            } else if (token instanceof String) {
                String s = (String) token;
                if (s.startsWith("MM:")) {
                    String mmId = s.substring(3);
                    if (mythic.isAvailable()) le = mythic.spawnMythic(mmId, at);
                    else le = null; // skip for safety
                }
            }
            if (le != null) {
                le.setRemoveWhenFarAway(false);
                arena.registerSpawned(le);
            }
        }
    }
}

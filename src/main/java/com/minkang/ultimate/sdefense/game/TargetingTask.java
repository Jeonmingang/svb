package com.minkang.ultimate.sdefense.game;

import org.bukkit.Bukkit;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class TargetingTask extends BukkitRunnable {
    private final GameManager gm;
    public TargetingTask(GameManager gm) { this.gm = gm; }

    @Override
    public void run() {
        Arena a = gm.getArena();
        if (a == null || !a.isRunning()) return;
        Set<UUID> pm = a.getAliveMobs();
        if (pm.isEmpty()) return;

        java.util.List<Player> targets = new java.util.ArrayList<Player>();
        for (UUID u : a.getParticipants()) {
            Player p = Bukkit.getPlayer(u);
            if (p != null && p.isOnline() && p.getGameMode() == org.bukkit.GameMode.SURVIVAL) targets.add(p);
        }
        if (targets.isEmpty()) return;

        for (UUID id : new HashSet<UUID>(pm)) {
            LivingEntity le = a.findMob(id);
            if (le == null || le.isDead()) continue;
            if (!(le instanceof Monster)) continue;
            Creature c = (Creature) le;

            Player nearest = null;
            double best = Double.MAX_VALUE;
            for (Player t : targets) {
                if (!t.getWorld().equals(le.getWorld())) continue;
                double d = t.getLocation().distanceSquared(le.getLocation());
                if (d < best) { best = d; nearest = t; }
            }
            if (nearest != null) {
                try { c.setTarget(nearest); } catch (Throwable ignored) {}
            }
        }
    }
}

package com.minkang.ultimate.sdefense.listener;

import com.minkang.ultimate.sdefense.barricade.BarricadeManager;
import com.minkang.ultimate.sdefense.game.Arena;
import com.minkang.ultimate.sdefense.game.GameManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.UUID;

public class EntityListener implements Listener {
    private final GameManager gm;
    private final BarricadeManager bm;

    public EntityListener(GameManager gm, BarricadeManager bm) {
        this.gm = gm;
        this.bm = bm;
    }

    @EventHandler
    public void onDeath(EntityDeathEvent e) {
        Arena a = gm.getArena();
        if (a == null || !a.isRunning()) return;
        UUID id = e.getEntity().getUniqueId();
        a.onMobDeath(id);
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player victim = (Player) e.getEntity();
        double reduce = bm.combinedDamageReduceNear(victim.getLocation());
        if (reduce > 0.0) {
            double dmg = e.getDamage() * (1.0 - reduce);
            e.setDamage(dmg);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Arena a = gm.getArena();
        if (a == null || !a.isRunning()) return;
        Player p = e.getEntity();
        a.onPlayerDeath(p);
    }
}

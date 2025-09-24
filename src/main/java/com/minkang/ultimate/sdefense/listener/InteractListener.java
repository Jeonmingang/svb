package com.minkang.ultimate.sdefense.listener;

import com.minkang.ultimate.sdefense.barricade.Barricade;
import com.minkang.ultimate.sdefense.barricade.BarricadeManager;
import com.minkang.ultimate.sdefense.game.GameManager;
import com.minkang.ultimate.sdefense.util.Msg;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class InteractListener implements Listener {
    private final BarricadeManager bm;
    private final GameManager gm;

    public InteractListener(BarricadeManager bm, GameManager gm) { this.bm = bm; this.gm = gm; }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (!e.hasBlock()) return;
        if (gm.getArena() == null) return;
        Block b = e.getClickedBlock();
        if (b == null) return;
        if (!bm.isBarricade(b.getLocation())) return;

        Player p = e.getPlayer();
        if (!gm.getArena().isParticipant(p)) return;

        if (p.getInventory().getItemInMainHand() != null &&
                p.getInventory().getItemInMainHand().getType() == Material.EMERALD) {
            bm.tryUpgrade(p, b.getLocation());
            e.setCancelled(true);
            return;
        }

        Barricade bc = bm.get(b.getLocation());
        if (bc != null) {
            Msg.send(p, "&e바리케이드 상태 &7| 레벨: &f" + bc.getLevel() + " &7체력: &f" + bc.getHealth() + "/" + bc.getMaxHealth());
        }
    }
}

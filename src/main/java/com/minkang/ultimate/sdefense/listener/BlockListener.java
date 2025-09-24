package com.minkang.ultimate.sdefense.listener;

import com.minkang.ultimate.sdefense.barricade.BarricadeManager;
import com.minkang.ultimate.sdefense.game.GameManager;
import com.minkang.ultimate.sdefense.util.Msg;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockListener implements Listener {
    private final BarricadeManager bm;
    private final GameManager gm;
    public BlockListener(BarricadeManager bm, GameManager gm) { this.bm = bm; this.gm = gm; }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        if (!bm.isBarricadeItem(e.getItemInHand())) return;
        if (gm.getArena() == null) return;
        Player p = e.getPlayer();
        if (!gm.getArena().isParticipant(p)) {
            Msg.send(p, "&7참가 중에만 바리케이드를 설치할 수 있습니다.");
            e.setCancelled(true);
            return;
        }
        Block b = e.getBlockPlaced();
        bm.placeBarricade(p, b.getLocation());
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if (gm.getArena() == null) return;
        if (!bm.isBarricade(e.getBlock().getLocation())) return;

        Player p = e.getPlayer();
        boolean allow = false;
        if (!allow && p.hasPermission("sdef.admin")) allow = true;
        if (!allow && p.isSneaking()) allow = true;
        if (!allow) {
            e.setCancelled(true);
            Msg.send(p, "&7바리케이드는 관리자 또는 웅크린 상태에서만 제거할 수 있습니다.");
            return;
        }
        bm.remove(e.getBlock().getLocation(), true);
        e.getBlock().setType(Material.AIR);
        Msg.send(p, "&c바리케이드를 제거했습니다.");
    }
}

package com.minkang.ultimate.sdefense.listener;

import com.minkang.ultimate.sdefense.UltimateSurvivalDefense;
import com.minkang.ultimate.sdefense.reward.RewardManager;
import com.minkang.ultimate.sdefense.util.Msg;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InventoryListener implements Listener {

    private final RewardManager rm;

    public InventoryListener(RewardManager rm) {
        this.rm = rm;
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player)) return;
        Player p = (Player) e.getPlayer();
        RewardManager.RewardGUIHolder holder = rm.getOpenEditor(p.getUniqueId());
        if (holder == null) return;
        Inventory inv = e.getInventory();
        // collect items
        List<ItemStack> items = new ArrayList<ItemStack>();
        for (ItemStack it : inv.getContents()) {
            if (it == null) continue;
            if (it.getType() == Material.AIR) continue;
            items.add(it.clone());
        }
        rm.setRewards(holder.getStage(), items);

        // return items to player
        HashMap<Integer, ItemStack> over = p.getInventory().addItem(items.toArray(new ItemStack[0]));
        if (!over.isEmpty()) {
            for (ItemStack left : over.values()) {
                p.getWorld().dropItemNaturally(p.getLocation(), left);
            }
        }

        Msg.send(p, "&a스테이지 " + holder.getStage() + " 보상을 저장했습니다.");
        rm.closeEditor(p.getUniqueId());
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        // allow default interactions; no cancellation, this GUI is a simple container
        // but we ensure it's our editor by presence of holder; no special action
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();
        RewardManager.RewardGUIHolder holder = rm.getOpenEditor(p.getUniqueId());
        if (holder == null) return;
        // Let players move items freely; nothing to cancel
    }
}

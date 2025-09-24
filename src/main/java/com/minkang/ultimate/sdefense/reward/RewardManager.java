package com.minkang.ultimate.sdefense.reward;

import com.minkang.ultimate.sdefense.UltimateSurvivalDefense;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class RewardManager {

    private final UltimateSurvivalDefense plugin;
    private final Map<Integer, List<ItemStack>> rewards = new HashMap<Integer, List<ItemStack>>();
    private final Map<UUID, RewardGUIHolder> openEditors = new HashMap<UUID, RewardGUIHolder>();

    public RewardManager(UltimateSurvivalDefense plugin) {
        this.plugin = plugin;
        loadFromConfig();
    }

    @SuppressWarnings("unchecked")
    private void loadFromConfig() {
        rewards.clear();
        FileConfiguration cfg = plugin.getConfig();
        if (!cfg.isConfigurationSection("rewards.stage")) return;
        for (String key : cfg.getConfigurationSection("rewards.stage").getKeys(false)) {
            int stage = 0;
            try { stage = Integer.parseInt(key); } catch (Exception ignored) {}
            if (stage <= 0) continue;
            List<ItemStack> list = new ArrayList<ItemStack>();
            List<?> raw = cfg.getList("rewards.stage." + key);
            if (raw != null) {
                for (Object o : raw) {
                    if (o instanceof ItemStack) {
                        list.add(((ItemStack) o).clone());
                    }
                }
            }
            rewards.put(stage, list);
        }
    }

    public List<ItemStack> getRewards(int stage) {
        List<ItemStack> src = rewards.get(stage);
        List<ItemStack> out = new ArrayList<ItemStack>();
        if (src != null) {
            for (ItemStack it : src) if (it != null) out.add(it.clone());
        }
        return out;
    }

    public void setRewards(int stage, List<ItemStack> items) {
        List<ItemStack> cleaned = new ArrayList<ItemStack>();
        for (ItemStack it : items) {
            if (it == null) continue;
            if (it.getType().isAir()) continue;
            cleaned.add(it.clone());
        }
        rewards.put(stage, cleaned);
        plugin.getConfig().set("rewards.stage." + stage, cleaned);
        plugin.saveConfig();
    }

    public void giveRewards(int stage, Collection<Player> players) {
        List<ItemStack> list = getRewards(stage);
        if (list.isEmpty()) return;
        for (Player p : players) {
            for (ItemStack it : list) {
                HashMap<Integer, ItemStack> over = p.getInventory().addItem(it.clone());
                if (!over.isEmpty()) {
                    for (ItemStack left : over.values()) {
                        Location l = p.getLocation();
                        l.getWorld().dropItemNaturally(l, left);
                    }
                }
            }
        }
    }

    public void openEditor(Player admin, int stage) {
        int size = 54;
        String title = "스테이지 " + stage + " 보상 설정";
        RewardGUIHolder holder = new RewardGUIHolder(stage);
        Inventory inv = Bukkit.createInventory(holder, size, title);
        // preload current rewards
        List<ItemStack> cur = getRewards(stage);
        int idx = 0;
        for (ItemStack it : cur) {
            if (idx >= size) break;
            inv.setItem(idx++, it);
        }
        admin.openInventory(inv);
        openEditors.put(admin.getUniqueId(), holder);
    }

    public RewardGUIHolder getOpenEditor(UUID uuid) {
        return openEditors.get(uuid);
    }

    public void closeEditor(UUID uuid) {
        openEditors.remove(uuid);
    }

    public static class RewardGUIHolder implements InventoryHolder {
        private final int stage;
        public RewardGUIHolder(int stage) { this.stage = stage; }
        public int getStage() { return stage; }
        @Override public Inventory getInventory() { return null; }
    }
}

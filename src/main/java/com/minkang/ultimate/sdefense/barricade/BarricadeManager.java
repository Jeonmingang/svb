package com.minkang.ultimate.sdefense.barricade;

import com.minkang.ultimate.sdefense.UltimateSurvivalDefense;
import com.minkang.ultimate.sdefense.util.Msg;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class BarricadeManager {

    private final UltimateSurvivalDefense plugin;
    private final Map<Location, Barricade> map = new HashMap<Location, Barricade>();

    private final Material itemMaterial;
    private final String itemName;
    private final List<String> itemLore;

    private final int baseHealth;
    private final int healthPerLevel;
    private final int maxLevel;
    private final double dmgReducePerLevel;
    private final double dmgRange;
    private final int dmgPerMobPerTick;
    private final Map<Integer, Integer> upgradeCost = new HashMap<Integer, Integer>();

    public BarricadeManager(UltimateSurvivalDefense plugin) {
        this.plugin = plugin;
        FileConfiguration cfg = plugin.getConfig();

        Material mat = Material.OAK_FENCE;
        try {
            String s = cfg.getString("barricade.item.material", "OAK_FENCE");
            mat = Material.valueOf(s.toUpperCase(Locale.ROOT));
        } catch (Exception ignored) {}
        this.itemMaterial = mat;
        this.itemName = cfg.getString("barricade.item.name", "&e바리케이드");
        this.itemLore = cfg.getStringList("barricade.item.lore");

        this.baseHealth = cfg.getInt("barricade.base_health", 200);
        this.healthPerLevel = cfg.getInt("barricade.health_per_level", 100);
        this.maxLevel = cfg.getInt("barricade.max_level", 5);
        this.dmgReducePerLevel = cfg.getDouble("barricade.dmg_reduce_per_level", 0.10D);
        this.dmgRange = cfg.getDouble("barricade.dmg_tick_range", 2.5D);
        this.dmgPerMobPerTick = cfg.getInt("barricade.dmg_per_mob_per_tick", 4);

        ConfigurationSection sec = cfg.getConfigurationSection("barricade.upgrade_cost");
        if (sec != null) {
            for (String k : sec.getKeys(false)) {
                int lv = safeLevel(k);
                if (lv <= 0) continue;
                upgradeCost.put(lv, sec.getInt(k, 1));
            }
        }
    }

    private int safeLevel(String s) {
        int lv = 0;
        boolean ok = false;
        if (!ok && s.toLowerCase(Locale.ROOT).startsWith("level_")) {
            try { lv = Integer.parseInt(s.substring("level_".length())); ok = true; } catch (Exception ignored) {}
        }
        if (!ok) return 0;
        return lv;
    }

    public org.bukkit.inventory.ItemStack createItem(int amount) {
        org.bukkit.inventory.ItemStack it = new org.bukkit.inventory.ItemStack(itemMaterial, amount);
        org.bukkit.inventory.meta.ItemMeta m = it.getItemMeta();
        m.setDisplayName(Msg.color(itemName));
        java.util.List<String> lore = new java.util.ArrayList<String>();
        for (String s : itemLore) lore.add(Msg.color(s));
        lore.add(Msg.color("&8[SDEF]"));
        m.setLore(lore);
        m.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        it.setItemMeta(m);
        return it;
    }

    public boolean isBarricadeItem(ItemStack it) {
        if (it == null) return false;
        if (it.getType() != itemMaterial) return false;
        if (!it.hasItemMeta()) return false;
        if (!it.getItemMeta().hasLore()) return false;
        java.util.List<String> lore = it.getItemMeta().getLore();
        if (lore == null) return false;
        boolean found = false;
        for (String s : lore) {
            if (ChatColor.stripColor(s).contains("[SDEF]")) { found = true; break; }
        }
        return found;
    }

    public void placeBarricade(Player owner, Location loc) {
        int level = 1;
        int max = baseHealth + healthPerLevel * (level - 1);
        Barricade b = new Barricade(loc, owner.getUniqueId(), level, max, max);
        map.put(loc, b);
        owner.sendMessage(Msg.prefix() + Msg.color("&a바리케이드를 설치했습니다. &7(레벨 " + level + ", 체력 " + max + ")"));
    }

    public boolean isBarricade(Location loc) { return map.containsKey(loc); }
    public Barricade get(Location loc) { return map.get(loc); }

    public void remove(Location loc, boolean dropItem) {
        Barricade b = map.remove(loc);
        if (b == null) return;
        if (dropItem) loc.getWorld().dropItemNaturally(loc.add(0.5, 0.5, 0.5), createItem(1));
    }

    public int countInWorld(World w) {
        int c = 0;
        for (Location l : map.keySet()) if (l.getWorld().equals(w)) c++;
        return c;
    }

    public void startDamageTask(int ticks) {
        new org.bukkit.scheduler.BukkitRunnable() {
            @Override public void run() {
                if (map.isEmpty()) return;
                java.util.Map<Location, Integer> toDamage = new java.util.HashMap<Location, Integer>();
                for (java.util.Map.Entry<Location, Barricade> e : map.entrySet()) {
                    Location l = e.getKey();
                    int near = 0;
                    for (org.bukkit.entity.Entity ent : l.getWorld().getNearbyEntities(l, dmgRange, dmgRange, dmgRange)) {
                        if (ent instanceof Monster) near++;
                    }
                    if (near > 0) toDamage.put(l, near * dmgPerMobPerTick);
                }
                for (java.util.Map.Entry<Location, Integer> d : toDamage.entrySet()) {
                    Barricade b = map.get(d.getKey());
                    if (b == null) continue;
                    b.damage(d.getValue());
                    if (b.isBroken()) {
                        Block block = d.getKey().getBlock();
                        block.setType(Material.AIR);
                        map.remove(d.getKey());
                        d.getKey().getWorld().playEffect(d.getKey(), Effect.STEP_SOUND, Material.OAK_FENCE);
                        d.getKey().getWorld().playSound(d.getKey(), Sound.BLOCK_WOOD_BREAK, 1.0f, 1.0f);
                    } else {
                        d.getKey().getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, d.getKey().clone().add(0.5, 0.9, 0.5), 2);
                    }
                }
            }
        }.runTaskTimer(plugin, 40L, ticks);
    }

    public void shutdown() { map.clear(); }

    public boolean tryUpgrade(Player p, Location loc) {
        Barricade b = map.get(loc);
        if (b == null) return false;
        int cur = b.getLevel();
        if (cur >= maxLevel) { p.sendMessage(Msg.prefix() + Msg.color("&7이미 최대 레벨입니다.")); return true; }
        int next = cur + 1;
        int cost = upgradeCost.containsKey(next) ? upgradeCost.get(next) : (1 + next);
        int has = countItem(p, Material.EMERALD);
        if (has < cost) { p.sendMessage(Msg.prefix() + Msg.color("&c업그레이드에 에메랄드가 부족합니다. 필요: " + cost)); return true; }
        removeItem(p, Material.EMERALD, cost);
        int newMax = baseHealth + healthPerLevel * (next - 1);
        b.setLevel(next, newMax);
        b.repairToMax();
        p.sendMessage(Msg.prefix() + Msg.color("&a바리케이드 레벨업! &7이제 레벨 " + next + " (체력 " + newMax + ")"));
        p.getWorld().playSound(loc, Sound.BLOCK_ANVIL_USE, 1.0f, 1.2f);
        return true;
    }

    private int countItem(Player p, Material m) {
        int total = 0;
        for (ItemStack it : p.getInventory().getContents()) { if (it != null && it.getType() == m) total += it.getAmount(); }
        return total;
    }

    private void removeItem(Player p, Material m, int amount) {
        int left = amount;
        for (int i = 0; i < p.getInventory().getSize(); i++) {
            ItemStack it = p.getInventory().getItem(i);
            if (it == null || it.getType() != m) continue;
            int a = it.getAmount();
            if (a <= left) { p.getInventory().setItem(i, null); left -= a; if (left == 0) break; }
            else { it.setAmount(a - left); p.getInventory().setItem(i, it); left = 0; break; }
        }
    }

    public double combinedDamageReduceNear(Location playerLoc) {
        int bestLevel = 0;
        for (java.util.Map.Entry<Location, Barricade> e : map.entrySet()) {
            if (!e.getKey().getWorld().equals(playerLoc.getWorld())) continue;
            double d = e.getKey().distance(playerLoc);
            if (d <= (dmgRange + 1.5)) { if (e.getValue().getLevel() > bestLevel) bestLevel = e.getValue().getLevel(); }
        }
        double reduce = dmgReducePerLevel * bestLevel;
        if (reduce > 0.5) reduce = 0.5;
        if (reduce < 0) reduce = 0;
        return reduce;
    }
}

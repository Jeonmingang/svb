package com.minkang.ultimate.sdefense.game;

import com.minkang.ultimate.sdefense.UltimateSurvivalDefense;
import com.minkang.ultimate.sdefense.barricade.BarricadeManager;
import com.minkang.ultimate.sdefense.reward.RewardManager;
import com.minkang.ultimate.sdefense.util.LocationUtil;
import com.minkang.ultimate.sdefense.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class GameManager {
    private final UltimateSurvivalDefense plugin;
    private final BarricadeManager barricadeManager;
    private final RewardManager rewardManager;
    private Arena arena;

    public GameManager(UltimateSurvivalDefense plugin, BarricadeManager barricadeManager, RewardManager rewardManager) {
        this.plugin = plugin;
        this.barricadeManager = barricadeManager;
        this.rewardManager = rewardManager;
    }

    public void loadArenaFromConfig() {
        FileConfiguration cfg = plugin.getConfig();
        String id = cfg.getString("arena.id", "default");
        String wname = cfg.getString("arena.world", "bskyblock_world");
        World w = LocationUtil.world(wname);
        if (w == null) {
            Msg.send(Bukkit.getConsoleSender(), "&c월드 '" + wname + "' 를 찾을 수 없습니다. 서버 월드 로드 상태를 확인하세요.");
            this.arena = null;
            return;
        }
        Location center = LocationUtil.parseXYZ(w, cfg.getString("arena.center", "0,64,0"));
        int radius = cfg.getInt("arena.radius", 40);
        Location lobby = LocationUtil.parseXYZ(w, cfg.getString("arena.lobby", "0,64,0"));
        java.util.List<String> list = cfg.getStringList("arena.spawnpoints");
        java.util.List<Location> spawns = new java.util.ArrayList<Location>();
        for (String s : list) {
            Location loc = LocationUtil.parseXYZ(w, s);
            if (loc != null) spawns.add(loc);
        }
        StageProvider provider = new StageProvider(cfg);
        this.arena = new Arena(plugin, id, w, center, radius, lobby, spawns, provider, barricadeManager, rewardManager);
    }

    public Arena getArena() { return arena; }

    public void shutdown() { if (arena != null && arena.isRunning()) arena.stop(); }

    public void start() { if (arena != null) arena.start(); }
    public void stop() { if (arena != null) arena.stop(); }
    public boolean join(Player p) { return (arena != null) && arena.join(p); }
    public void leave(Player p) { if (arena != null) arena.leave(p); }
}

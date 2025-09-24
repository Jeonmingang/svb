package com.minkang.ultimate.sdefense.game;

import com.minkang.ultimate.sdefense.UltimateSurvivalDefense;
import com.minkang.ultimate.sdefense.barricade.BarricadeManager;
import com.minkang.ultimate.sdefense.util.LocationUtil;
import com.minkang.ultimate.sdefense.reward.RewardManager;
import com.minkang.ultimate.sdefense.util.Msg;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class Arena {

    public final String id;
    public final World world;
    public Location center;
    public int radius;
    public Location lobby;
    public final List<Location> spawns = new ArrayList<Location>();

    private final Set<UUID> participants = new HashSet<UUID>();
    private final Set<UUID> survivors = new HashSet<UUID>();
    private final Set<UUID> aliveMobs = new HashSet<UUID>();

    private final StageProvider stageProvider;
    private final BarricadeManager barricadeManager;
    private final RewardManager rewardManager;
    private final UltimateSurvivalDefense plugin;

    private boolean running = false;
    private int stageNumber = 0;
    private SpawnTask spawnTask;
    private int stageTimeLeftSec = 0;
    private org.bukkit.scheduler.BukkitTask stageTimerTask;

    private final int spawnPerTick;
    private final int tickInterval;
    private final int nextStageDelaySec;

    private final Random random = new Random();

    public Arena(UltimateSurvivalDefense plugin, String id, World world, Location center, int radius, Location lobby,
                 List<Location> spawnpoints, StageProvider stageProvider, BarricadeManager barricadeManager, RewardManager rewardManager) {
        this.plugin = plugin;
        this.id = id;
        this.world = world;
        this.center = center;
        this.radius = radius;
        this.lobby = lobby;
        if (spawnpoints != null) spawns.addAll(spawnpoints);
        this.stageProvider = stageProvider;
        this.barricadeManager = barricadeManager;
        this.rewardManager = rewardManager;

        FileConfiguration cfg = plugin.getConfig();
        this.spawnPerTick = Math.max(1, cfg.getInt("wave.spawn_per_tick", 4));
        this.tickInterval  = Math.max(1, cfg.getInt("wave.tick_interval", 10));
        this.nextStageDelaySec = Math.max(1, cfg.getInt("wave.next_stage_delay_sec", 12));

        int actionbarTicks = cfg.getInt("ui.actionbar_interval_ticks", 20);
        new BukkitRunnable() {
            @Override public void run() {
                if (participants.isEmpty()) return;
                String msg = "&a스테이지: &f" + stageNumber +
                        "  &a생존자: &f" + survivors.size() + "/" + participants.size() +
                        "  &a남은 몹: &f" + aliveMobs.size() +
                        "  &a바리케이드: &f" + barricadeManager.countInWorld(world) + (stageTimeLeftSec>0 ? "  &a시간: &f" + formatTime(stageTimeLeftSec) : "");
                for (UUID u : participants) {
                    Player p = Bukkit.getPlayer(u);
                    if (p != null && p.isOnline()) {
                        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Msg.color(msg)));
                    }
                }
            }
        }.runTaskTimer(plugin, 40L, actionbarTicks);
    }

    public boolean isRunning() { return running; }
    public int getStageNumber() { return stageNumber; }
    public Set<UUID> getParticipants() { return Collections.unmodifiableSet(participants); }
    public Set<UUID> getAliveMobs() { return aliveMobs; }

    public boolean join(Player p) {
        if (participants.contains(p.getUniqueId())) {
            Msg.send(p, "&7이미 참가 중입니다.");
            return false;
        }
        participants.add(p.getUniqueId());
        survivors.add(p.getUniqueId());
        if (lobby != null) p.teleport(lobby);
        p.setGameMode(GameMode.SURVIVAL);
        p.setHealth(20.0);
        p.setFoodLevel(20);
        Msg.send(p, "&a경기에 참가했습니다. &7명령: /생존디펜스 도움말");
        return true;
    }

    public void leave(Player p) {
        boolean contained = participants.contains(p.getUniqueId());
        if (!contained) {
            Msg.send(p, "&7참가 상태가 아닙니다.");
            return;
        }
        participants.remove(p.getUniqueId());
        survivors.remove(p.getUniqueId());
        Msg.send(p, "&c경기에서 나갔습니다.");
        checkEndByWipe();
    }

    public void onPlayerDeath(Player p) {
        if (!participants.contains(p.getUniqueId())) return;
        survivors.remove(p.getUniqueId());
        p.setGameMode(GameMode.SPECTATOR);
        if (lobby != null) p.teleport(lobby);
        Msg.send(p, "&c사망했습니다. 관전자 모드로 전환됩니다.");
        checkEndByWipe();
    }

    private void checkEndByWipe() {
        if (running && survivors.isEmpty()) {
            broadcast("&c모든 참가자가 사망했습니다. 게임을 종료합니다.");
            stop();
        }
    }

    public void start() {
        if (running) {
            broadcast("&7이미 진행 중입니다.");
            return;
        }
        if (participants.isEmpty()) {
            broadcast("&7참가자가 없습니다. 시작 취소.");
            return;
        }
        running = true;
        survivors.clear();
        survivors.addAll(participants);
        stageNumber = 1;
        broadcast("&a게임 시작! &f스테이지 " + stageNumber + " 준비...");
        scheduleStage(stageNumber);
    }

    public void stop() {
        running = false;
        stageNumber = 0;
        for (UUID id : new HashSet<UUID>(aliveMobs)) {
            LivingEntity le = findMob(id);
            if (le != null) le.remove();
        }
        aliveMobs.clear();
        if (spawnTask != null) spawnTask.cancel();
        if (stageTimerTask != null) { stageTimerTask.cancel(); stageTimerTask = null; }
        stageTimeLeftSec = 0;
        broadcast("&c게임이 중지되었습니다.");
    }

    public void onMobDeath(UUID mobId) {
        if (!aliveMobs.contains(mobId)) return;
        aliveMobs.remove(mobId);
        if (running && aliveMobs.isEmpty() && (spawnTask == null || spawnTask.isFinished())) {
            broadcast("&a스테이지 " + stageNumber + " 클리어! &7다음 스테이지로 이동합니다...");
            // 보상 지급: 생존자에게
            java.util.List<org.bukkit.entity.Player> alivePlayers = new java.util.ArrayList<org.bukkit.entity.Player>();
            for (java.util.UUID u : new java.util.HashSet<java.util.UUID>(survivors)) {
                org.bukkit.entity.Player pl = org.bukkit.Bukkit.getPlayer(u);
                if (pl != null && pl.isOnline() && pl.getGameMode()==org.bukkit.GameMode.SURVIVAL) alivePlayers.add(pl);
            }
            rewardManager.giveRewards(stageNumber, alivePlayers);
            new BukkitRunnable() {
                @Override public void run() {
                    if (!running) return;
                    stageNumber++;
                    scheduleStage(stageNumber);
                }
            }.runTaskLater(plugin, nextStageDelaySec * 20L);
        }
    }

    private void scheduleStage(int number) {
        Stage stage = stageProvider.get(number);
        if (stage.totalCount() <= 0) {
            broadcast("&a축하합니다! 모든 스테이지를 클리어했습니다.");
            stop();
            return;
        }
        broadcast("&e스테이지 " + number + " 시작! &7총 몹 &f" + stage.totalCount());
        spawnTask = new SpawnTask(this, stage, spawnPerTick);
        spawnTask.runTaskTimer(plugin, 0L, tickInterval);

        // time limit (0 = disabled)
        int def = plugin.getConfig().getInt("stage_time.default_sec", 0);
        int limit = plugin.getConfig().getInt("stage_time.per_stage." + number, def);
        stageTimeLeftSec = Math.max(0, limit);
        if (stageTimerTask != null) { stageTimerTask.cancel(); stageTimerTask = null; }
        if (stageTimeLeftSec > 0) {
            stageTimerTask = new BukkitRunnable() {
                @Override public void run() {
                    if (!running) { cancel(); return; }
                    if (stageTimeLeftSec <= 0) {
                        cancel();
                        // 시간 초과 판정: 아직 몹이 남아있거나 스폰이 끝나지 않았으면 실패
                        if (!aliveMobs.isEmpty() || (spawnTask != null && !spawnTask.isFinished())) {
                            broadcast("&c시간 초과! 게임을 종료합니다.");
                            stop();
                        } else {
                            // 이미 클리어 상태면 다음 스테이지로 넘어가도록 onMobDeath 경로가 처리했을 것.
                        }
                        return;
                    }
                    stageTimeLeftSec--;
                }
            }.runTaskTimer(plugin, 20L, 20L);
        }
    }

    public void registerSpawned(LivingEntity e) {
        aliveMobs.add(e.getUniqueId());
        if (random.nextDouble() < 0.15) {
            world.playSound(e.getLocation(), Sound.ENTITY_ZOMBIE_AMBIENT, 0.5f, 1.0f);
        }
    }

    public boolean isParticipant(Player p) { return participants.contains(p.getUniqueId()); }
    public Location randomSpawn() { return spawns.isEmpty() ? center : spawns.get(random.nextInt(spawns.size())); }

    public LivingEntity findMob(UUID id) {
        for (LivingEntity e : world.getLivingEntities()) if (e.getUniqueId().equals(id)) return e;
        return null;
    }

    private String formatTime(int sec) {
        int m = sec / 60; int s = sec % 60;
        return (m<10?"0":"")+m+":"+(s<10?"0":"")+s;
    }

    public void broadcast(String msg) {
        String out = Msg.color(msg);
        for (UUID u : participants) {
            Player p = Bukkit.getPlayer(u);
            if (p != null && p.isOnline()) p.sendMessage(Msg.prefix() + out);
        }
        Bukkit.getConsoleSender().sendMessage(Msg.prefix() + out);
    }

    public void setCenter(Location loc) {
        this.center = loc;
        plugin.getConfig().set("arena.center", LocationUtil.toXYZ(loc));
        plugin.saveConfig();
    }

    public void setRadius(int r) {
        this.radius = r;
        plugin.getConfig().set("arena.radius", r);
        plugin.saveConfig();
    }

    public void setLobby(Location loc) {
        this.lobby = loc;
        plugin.getConfig().set("arena.lobby", LocationUtil.toXYZ(loc));
        plugin.saveConfig();
    }

    public void addSpawn(Location loc) {
        this.spawns.add(loc);
        java.util.List<String> list = plugin.getConfig().getStringList("arena.spawnpoints");
        list.add(LocationUtil.toXYZ(loc));
        plugin.getConfig().set("arena.spawnpoints", list);
        plugin.saveConfig();
    }
}

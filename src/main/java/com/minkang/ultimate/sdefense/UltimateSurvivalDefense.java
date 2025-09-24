package com.minkang.ultimate.sdefense;

import com.minkang.ultimate.sdefense.barricade.BarricadeManager;
import com.minkang.ultimate.sdefense.commands.SDefCommand;
import com.minkang.ultimate.sdefense.game.GameManager;
import com.minkang.ultimate.sdefense.game.TargetingTask;
import com.minkang.ultimate.sdefense.listener.BlockListener;
import com.minkang.ultimate.sdefense.listener.EntityListener;
import com.minkang.ultimate.sdefense.listener.InteractListener;
import com.minkang.ultimate.sdefense.listener.InventoryListener;
import com.minkang.ultimate.sdefense.listener.KoreanCommandListener;
import com.minkang.ultimate.sdefense.reward.RewardManager;
import com.minkang.ultimate.sdefense.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class UltimateSurvivalDefense extends JavaPlugin {

    private static UltimateSurvivalDefense instance;
    private GameManager gameManager;
    private BarricadeManager barricadeManager;
    private RewardManager rewardManager;

    public static UltimateSurvivalDefense inst() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.barricadeManager = new BarricadeManager(this);
        this.rewardManager = new RewardManager(this);
        this.gameManager = new GameManager(this, barricadeManager, rewardManager);
        this.gameManager.loadArenaFromConfig();

        // listeners
        Bukkit.getPluginManager().registerEvents(new EntityListener(gameManager, barricadeManager), this);
        Bukkit.getPluginManager().registerEvents(new BlockListener(barricadeManager, gameManager), this);
        Bukkit.getPluginManager().registerEvents(new InteractListener(barricadeManager, gameManager), this);
        Bukkit.getPluginManager().registerEvents(new InventoryListener(rewardManager), this);

        // Korean command listener
        if (getConfig().getBoolean("korean_commands.enabled", true)) {
            Bukkit.getPluginManager().registerEvents(new KoreanCommandListener(gameManager, barricadeManager), this);
        }

        // backup ASCII command
        if (getCommand("sdef") != null) {
            getCommand("sdef").setExecutor(new SDefCommand(gameManager, barricadeManager));
        }

        // periodic tasks
        int targetingTicks = getConfig().getInt("ui.targeting_refresh_ticks", 40);
        new TargetingTask(gameManager).runTaskTimer(this, 40L, targetingTicks);

        int barricadeDamageTicks = getConfig().getInt("ui.barricade_damage_ticks", 20);
        barricadeManager.startDamageTask(barricadeDamageTicks);

        getLogger().info(Msg.plain("&aUltimateSurvivalDefense v1.1.1 enabled."));
    }

    @Override
    public void onDisable() {
        if (gameManager != null) gameManager.shutdown();
        if (barricadeManager != null) barricadeManager.shutdown();
        getLogger().info(Msg.plain("&cUltimateSurvivalDefense disabled."));
    }

    public GameManager getGameManager() { return gameManager; }
    public RewardManager getRewardManager() {
        return rewardManager;
    }

    public BarricadeManager getBarricadeManager() { return barricadeManager; }
}

package com.minkang.ultimate.sdefense.mythic;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

public class MythicHook {

    private final boolean available;
    private Object apiHelper; // BukkitAPIHelper instance (if available)

    public MythicHook() {
        Plugin mm = Bukkit.getPluginManager().getPlugin("MythicMobs");
        boolean ok = false;
        if (mm != null && mm.isEnabled()) {
            try {
                Class<?> mmClass = Class.forName("io.lumine.xikage.mythicmobs.MythicMobs");
                Method inst = mmClass.getMethod("inst");
                Object mmInst = inst.invoke(null);
                Method getAPIHelper = mmClass.getMethod("getAPIHelper");
                apiHelper = getAPIHelper.invoke(mmInst);
                ok = (apiHelper != null);
            } catch (Throwable ignored) { ok = false; }
        }
        this.available = ok;
    }

    public boolean isAvailable() { return available; }

    public LivingEntity spawnMythic(String internalName, Location at) {
        if (!available || apiHelper == null) return null;
        try {
            Method spawn = apiHelper.getClass().getMethod("spawnMythicMob", String.class, Location.class);
            Object activeMob = spawn.invoke(apiHelper, internalName, at);
            if (activeMob == null) return null;
            try {
                Method getLiving = activeMob.getClass().getMethod("getLivingEntity");
                Object le = getLiving.invoke(activeMob);
                if (le instanceof LivingEntity) return (LivingEntity) le;
            } catch (Throwable ignored) {}
            try {
                Method getEntity = activeMob.getClass().getMethod("getEntity");
                Object abstractEnt = getEntity.invoke(activeMob);
                if (abstractEnt != null) {
                    Method getBukkit = abstractEnt.getClass().getMethod("getBukkitEntity");
                    Object bukkitEnt = getBukkit.invoke(abstractEnt);
                    if (bukkitEnt instanceof Entity && bukkitEnt instanceof LivingEntity) {
                        return (LivingEntity) bukkitEnt;
                    }
                }
            } catch (Throwable ignored) {}
        } catch (Throwable ignored) { return null; }
        return null;
    }
}

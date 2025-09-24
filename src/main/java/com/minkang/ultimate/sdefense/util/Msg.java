package com.minkang.ultimate.sdefense.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public final class Msg {
    private Msg() {}

    public static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static String prefix() {
        org.bukkit.plugin.Plugin p = org.bukkit.Bukkit.getPluginManager().getPlugin("UltimateSurvivalDefense");
        String def = "&6[생존디펜스]&f ";
        if (p == null) return color(def);
        return color(p.getConfig().getString("ui.prefix", def));
    }

    public static void send(CommandSender to, String msg) { to.sendMessage(prefix() + color(msg)); }
    public static String plain(String s) { return color(s); }
}

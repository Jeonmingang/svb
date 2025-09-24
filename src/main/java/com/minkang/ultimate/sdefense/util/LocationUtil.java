package com.minkang.ultimate.sdefense.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public final class LocationUtil {
    private LocationUtil(){}

    public static Location parseXYZ(World world, String csv) {
        if (world == null || csv == null) return null;
        String[] p = csv.split(",");
        if (p.length < 3) return null;
        double x = Double.parseDouble(p[0].trim());
        double y = Double.parseDouble(p[1].trim());
        double z = Double.parseDouble(p[2].trim());
        return new Location(world, x, y, z);
    }

    public static String toXYZ(Location loc) {
        if (loc == null) return "0,64,0";
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        return x + "," + y + "," + z;
    }

    public static World world(String name) {
        if (name == null) return null;
        return Bukkit.getWorld(name);
    }
}

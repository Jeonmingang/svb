package com.minkang.ultimate.sdefense.barricade;

import org.bukkit.Location;

import java.util.UUID;

public class Barricade {
    private final Location location;
    private final UUID owner;
    private int level;
    private int health;
    private int maxHealth;

    public Barricade(Location location, UUID owner, int level, int health, int maxHealth) {
        this.location = location;
        this.owner = owner;
        this.level = level;
        this.health = health;
        this.maxHealth = maxHealth;
    }

    public Location getLocation() { return location; }
    public UUID getOwner() { return owner; }
    public int getLevel() { return level; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }

    public void setLevel(int level, int newMax) { this.level = level; this.maxHealth = newMax; if (health > maxHealth) health = maxHealth; }
    public void repairToMax() { this.health = maxHealth; }
    public void damage(int amount) { int h = health - amount; if (h < 0) h = 0; health = h; }
    public boolean isBroken() { return health <= 0; }
}

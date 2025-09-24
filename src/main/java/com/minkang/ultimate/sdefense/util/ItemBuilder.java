package com.minkang.ultimate.sdefense.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemBuilder {
    private final ItemStack stack;

    public ItemBuilder(Material mat, int amount) { this.stack = new ItemStack(mat, amount); }

    public ItemBuilder name(String name) {
        ItemMeta m = stack.getItemMeta();
        m.setDisplayName(Msg.color(name));
        stack.setItemMeta(m);
        return this;
    }

    public ItemBuilder lore(List<String> lines) {
        ItemMeta m = stack.getItemMeta();
        java.util.List<String> out = new java.util.ArrayList<String>();
        for (String l : lines) out.add(Msg.color(l));
        m.setLore(out);
        stack.setItemMeta(m);
        return this;
    }

    public ItemBuilder flags(ItemFlag... flags) {
        ItemMeta m = stack.getItemMeta();
        for (ItemFlag f : flags) m.addItemFlags(f);
        stack.setItemMeta(m);
        return this;
    }

    public ItemStack build() { return stack; }
}

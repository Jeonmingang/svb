package com.minkang.ultimate.sdefense.game;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

import java.util.*;

public class StageProvider {
    private final FileConfiguration cfg;
    private final String mode;

    private final Map<Integer, Stage> fixed = new HashMap<Integer, Stage>();

    private final Map<EntityType, Integer> autoBaseVanilla = new EnumMap<EntityType, Integer>(EntityType.class);
    private final Map<EntityType, Integer> autoPerVanilla = new EnumMap<EntityType, Integer>(EntityType.class);

    private final Map<String, Integer> autoBaseMythic = new HashMap<String, Integer>();
    private final Map<String, Integer> autoPerMythic = new HashMap<String, Integer>();

    public StageProvider(FileConfiguration cfg) {
        this.cfg = cfg;
        this.mode = cfg.getString("stages.mode", "fixed").toLowerCase(Locale.ROOT);
        load();
    }

    private void load() {
        if ("fixed".equals(mode)) {
            ConfigurationSection list = cfg.getConfigurationSection("stages.list");
            if (list != null) {
                for (String k : list.getKeys(false)) {
                    int n;
                    try { n = Integer.parseInt(k); } catch (Exception e) { continue; }
                    Stage st = new Stage(n);
                    ConfigurationSection s = list.getConfigurationSection(k);
                    if (s != null) {
                        // 1) 중첩형: vanilla / mythic 하위 섹션
                        ConfigurationSection vsec = s.getConfigurationSection("vanilla");
                        if (vsec != null) {
                            for (String key : vsec.getKeys(false)) {
                                EntityType type = safeType(key);
                                if (type == null) continue;
                                int count = vsec.getInt(key, 0);
                                if (count > 0) st.vanilla().put(type, count);
                            }
                        }
                        ConfigurationSection msec = s.getConfigurationSection("mythic");
                        if (msec != null) {
                            for (String key : msec.getKeys(false)) {
                                int count = msec.getInt(key, 0);
                                if (count > 0) st.mythic().put(key, count);
                            }
                        }
                        // 2) 평면형: 바로 키가 나열된 경우
                        for (String key : s.getKeys(false)) {
                            if ("vanilla".equalsIgnoreCase(key) || "mythic".equalsIgnoreCase(key)) continue;
                            int count = s.getInt(key, -1);
                            if (count < 0) continue;
                            if (key.toUpperCase(Locale.ROOT).startsWith("MM:")) {
                                String id = key.substring(3);
                                if (id.length() > 0) st.mythic().put(id, count);
                            } else {
                                EntityType type = safeType(key);
                                if (type != null) st.vanilla().put(type, count);
                                else st.mythic().put(key, count);
                            }
                        }
                    }
                    fixed.put(n, st);
                }
            }
        } else {
            ConfigurationSection baseV = cfg.getConfigurationSection("stages.autoscale.vanilla.base");
            if (baseV != null) {
                for (String et : baseV.getKeys(false)) {
                    EntityType type = safeType(et);
                    if (type == null) continue;
                    autoBaseVanilla.put(type, baseV.getInt(et, 0));
                }
            }
            ConfigurationSection perV = cfg.getConfigurationSection("stages.autoscale.vanilla.per_stage");
            if (perV != null) {
                for (String et : perV.getKeys(false)) {
                    EntityType type = safeType(et);
                    if (type == null) continue;
                    autoPerVanilla.put(type, perV.getInt(et, 0));
                }
            }
            ConfigurationSection baseM = cfg.getConfigurationSection("stages.autoscale.mythic.base");
            if (baseM != null) {
                for (String id : baseM.getKeys(false)) {
                    autoBaseMythic.put(id, baseM.getInt(id, 0));
                }
            }
            ConfigurationSection perM = cfg.getConfigurationSection("stages.autoscale.mythic.per_stage");
            if (perM != null) {
                for (String id : perM.getKeys(false)) {
                    autoPerMythic.put(id, perM.getInt(id, 0));
                }
            }
        }
    }

    private EntityType safeType(String name) {
        try { return EntityType.valueOf(name.toUpperCase(Locale.ROOT)); }
        catch (Exception e) { return null; }
    }

    public Stage get(int number) {
        if ("fixed".equals(mode)) {
            Stage st = fixed.get(number);
            if (st != null) return st;
            if (fixed.isEmpty()) return new Stage(number);
            int max = 1;
            for (Integer k : fixed.keySet()) if (k > max) max = k;
            Stage last = fixed.get(max);
            Stage out = new Stage(number);
            double mul = 1.0 + ((number - max) * 0.20);
            for (java.util.Map.Entry<org.bukkit.entity.EntityType, Integer> e : last.vanilla().entrySet()) {
                out.vanilla().put(e.getKey(), Math.max(1, (int)Math.round(e.getValue() * mul)));
            }
            for (java.util.Map.Entry<String, Integer> e : last.mythic().entrySet()) {
                out.mythic().put(e.getKey(), Math.max(1, (int)Math.round(e.getValue() * mul)));
            }
            return out;
        } else {
            Stage out = new Stage(number);
            for (Map.Entry<EntityType, Integer> e : autoBaseVanilla.entrySet()) {
                int v = e.getValue() + (autoPerVanilla.containsKey(e.getKey()) ? autoPerVanilla.get(e.getKey()) : 0) * (number - 1);
                if (v > 0) out.vanilla().put(e.getKey(), v);
            }
            for (Map.Entry<String, Integer> e : autoBaseMythic.entrySet()) {
                int step = autoPerMythic.containsKey(e.getKey()) ? autoPerMythic.get(e.getKey()) : 0;
                int v = e.getValue() + step * (number - 1);
                if (v > 0) out.mythic().put(e.getKey(), v);
            }
            return out;
        }
    }
}

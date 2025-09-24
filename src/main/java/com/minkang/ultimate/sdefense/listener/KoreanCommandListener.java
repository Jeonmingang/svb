package com.minkang.ultimate.sdefense.listener;

import com.minkang.ultimate.sdefense.barricade.BarricadeManager;
import com.minkang.ultimate.sdefense.game.GameManager;
import com.minkang.ultimate.sdefense.util.LocationUtil;
import com.minkang.ultimate.sdefense.util.Msg;
import com.minkang.ultimate.sdefense.reward.RewardManager;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class KoreanCommandListener implements Listener {
    private final GameManager gm;
    private final BarricadeManager bm;
    private final Set<String> roots;

    public KoreanCommandListener(GameManager gm, BarricadeManager bm) {
        this.gm = gm;
        this.bm = bm;
        String[] cfg = com.minkang.ultimate.sdefense.UltimateSurvivalDefense.inst().getConfig().getStringList("korean_commands.roots").toArray(new String[0]);
        if (cfg.length == 0) cfg = new String[]{"생존디펜스", "디펜스"};
        this.roots = new HashSet<String>(Arrays.asList(cfg));
    }

    @EventHandler
    public void onPreprocess(PlayerCommandPreprocessEvent e) {
        String msg = e.getMessage().trim();
        if (!msg.startsWith("/")) return;
        String body = msg.substring(1).trim();
        String[] parts = body.split("\s+");
        if (parts.length == 0) return;
        String root = parts[0];
        if (!roots.contains(root)) return;

        e.setCancelled(true);
        Player p = e.getPlayer();
        String sub = (parts.length >= 2 ? parts[1] : "도움말");

        if ("도움말".equals(sub)) { help(p); return; }
        if ("참가".equals(sub)) { gm.join(p); return; }
        if ("퇴장".equals(sub)) { gm.leave(p); return; }
        if ("시작".equals(sub)) { if (!p.hasPermission("sdef.admin")) { Msg.send(p, "&c권한이 없습니다."); return; } gm.start(); return; }
        if ("중지".equals(sub)) { if (!p.hasPermission("sdef.admin")) { Msg.send(p, "&c권한이 없습니다."); return; } gm.stop(); return; }
        if ("센터".equals(sub)) {
            if (!p.hasPermission("sdef.admin")) { Msg.send(p, "&c권한이 없습니다."); return; }
            if (gm.getArena() == null) { Msg.send(p, "&c아레나가 로드되지 않았습니다."); return; }
            gm.getArena().setCenter(p.getLocation());
            Msg.send(p, "&a센터를 현재 위치로 설정했습니다.");
            return;
        }
        if ("반경".equals(sub)) {
            if (!p.hasPermission("sdef.admin")) { Msg.send(p, "&c권한이 없습니다."); return; }
            if (parts.length < 3) { Msg.send(p, "&7사용법: /" + root + " 반경 <숫자>"); return; }
            int r = 40;
            try { r = Integer.parseInt(parts[2]); } catch (Exception ignored) {}
            if (gm.getArena() == null) { Msg.send(p, "&c아레나가 로드되지 않았습니다."); return; }
            gm.getArena().setRadius(r);
            Msg.send(p, "&a반경을 " + r + " 으로 설정했습니다.");
            return;
        }
        if ("스폰추가".equals(sub)) {
            if (!p.hasPermission("sdef.admin")) { Msg.send(p, "&c권한이 없습니다."); return; }
            if (gm.getArena() == null) { Msg.send(p, "&c아레나가 로드되지 않았습니다."); return; }
            Location loc = p.getLocation().getBlock().getLocation();
            gm.getArena().addSpawn(loc);
            Msg.send(p, "&a스폰 포인트를 추가했습니다: &f" + LocationUtil.toXYZ(loc));
            return;
        }
        if ("로비".equals(sub)) {
            if (!p.hasPermission("sdef.admin")) { Msg.send(p, "&c권한이 없습니다."); return; }
            if (gm.getArena() == null) { Msg.send(p, "&c아레나가 로드되지 않았습니다."); return; }
            gm.getArena().setLobby(p.getLocation());
            Msg.send(p, "&a로비를 현재 위치로 설정했습니다.");
            return;
        }
        if ("바리키트".equals(sub)) {
            if (!p.hasPermission("sdef.admin")) { Msg.send(p, "&c권한이 없습니다."); return; }
            if (parts.length < 3) { Msg.send(p, "&7사용법: /" + root + " 바리키트 <플레이어> [개수]"); return; }
            Player t = p.getServer().getPlayer(parts[2]);
            int amt = 8;
            if (parts.length >= 4) { try { amt = Integer.parseInt(parts[3]); } catch (Exception ignored) {} }
            if (t == null) { Msg.send(p, "&c해당 플레이어를 찾을 수 없습니다."); return; }
            t.getInventory().addItem(bm.createItem(amt));
            Msg.send(p, "&a바리케이드 키트를 지급했습니다: " + t.getName() + " x" + amt);
            return;
        }

        if ("보상설정".equals(sub)) {
            if (!p.hasPermission("sdef.admin")) { Msg.send(p, "&c권한이 없습니다."); return; }
            if (parts.length < 3) { Msg.send(p, "&7사용법: /" + root + " 보상설정 <스테이지번호>"); return; }
            int st = 1; try { st = Integer.parseInt(parts[2]); } catch (Exception ignored) {}
            com.minkang.ultimate.sdefense.UltimateSurvivalDefense.inst().getRewardManager().openEditor(p, st);
            Msg.send(p, "&e보상 GUI를 열었습니다. 아이템을 넣고 창을 닫으면 저장됩니다.");
            return;
        }
        if ("제한시간".equals(sub)) {
            if (!p.hasPermission("sdef.admin")) { Msg.send(p, "&c권한이 없습니다."); return; }
            if (parts.length < 4) { Msg.send(p, "&7사용법: /" + root + " 제한시간 <스테이지번호> <초(0=해제)>"); return; }
            int st = 1; int sec = 0;
            try { st = Integer.parseInt(parts[2]); } catch (Exception ignored) {}
            try { sec = Integer.parseInt(parts[3]); } catch (Exception ignored) {}
            com.minkang.ultimate.sdefense.UltimateSurvivalDefense.inst().getConfig().set("stage_time.per_stage." + st, Math.max(0, sec));
            com.minkang.ultimate.sdefense.UltimateSurvivalDefense.inst().saveConfig();
            Msg.send(p, "&a스테이지 " + st + " 제한시간을 " + Math.max(0, sec) + "초로 설정했습니다.");
            return;
        }
        if ("제한시간초기화".equals(sub)) {
            if (!p.hasPermission("sdef.admin")) { Msg.send(p, "&c권한이 없습니다."); return; }
            if (parts.length < 3) { Msg.send(p, "&7사용법: /" + root + " 제한시간초기화 <스테이지번호>"); return; }
            int st = 1; try { st = Integer.parseInt(parts[2]); } catch (Exception ignored) {}
            com.minkang.ultimate.sdefense.UltimateSurvivalDefense.inst().getConfig().set("stage_time.per_stage." + st, null);
            com.minkang.ultimate.sdefense.UltimateSurvivalDefense.inst().saveConfig();
            Msg.send(p, "&a스테이지 " + st + " 제한시간 설정을 제거했습니다.");
            return;
        }

        Msg.send(p, "&7알 수 없는 하위 명령입니다. &f/" + root + " 도움말");
    }

    private void help(CommandSender s) {
        Msg.send(s, "&e/생존디펜스 참가 &7- 참가");
        Msg.send(s, "&e/생존디펜스 퇴장 &7- 퇴장");
        Msg.send(s, "&e/생존디펜스 시작 &7- &8(관리자) 시작");
        Msg.send(s, "&e/생존디펜스 중지 &7- &8(관리자) 중지");
        Msg.send(s, "&e/생존디펜스 센터 &7- &8(관리자) 센터 지정");
        Msg.send(s, "&e/생존디펜스 반경 <r> &7- &8(관리자) 반경 설정");
        Msg.send(s, "&e/생존디펜스 스폰추가 &7- &8(관리자) 몹 스폰포인트 추가");
        Msg.send(s, "&e/생존디펜스 로비 &7- &8(관리자) 로비 지정");
        Msg.send(s, "&e/생존디펜스 바리키트 <플레이어> [개수] &7- &8(관리자) 바리케이드 지급");
        Msg.send(s, "&e/생존디펜스 보상설정 <스테이지> &7- &8(관리자) 보상 편집 GUI 열기");
        Msg.send(s, "&e/생존디펜스 제한시간 <스테이지> <초> &7- &8(관리자) 스테이지별 제한시간 설정");
        Msg.send(s, "&e/생존디펜스 제한시간초기화 <스테이지> &7- &8(관리자) 제한시간 해제");
        Msg.send(s, "&7바리케이드는 전용 아이템으로 설치하며, &a에메랄드 우클릭&7으로 업그레이드합니다.");
    }
}

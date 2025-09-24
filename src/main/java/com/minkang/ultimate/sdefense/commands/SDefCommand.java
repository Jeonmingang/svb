package com.minkang.ultimate.sdefense.commands;

import com.minkang.ultimate.sdefense.barricade.BarricadeManager;
import com.minkang.ultimate.sdefense.game.GameManager;
import com.minkang.ultimate.sdefense.util.LocationUtil;
import com.minkang.ultimate.sdefense.util.Msg;
import com.minkang.ultimate.sdefense.UltimateSurvivalDefense;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class SDefCommand implements CommandExecutor {
    private final GameManager gm;
    private final BarricadeManager bm;

    public SDefCommand(GameManager gm, BarricadeManager bm) { this.gm = gm; this.bm = bm; }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
        if (args.length == 0) { help(s); return true; }
        String sub = args[0].toLowerCase();

        if ("join".equals(sub)) {
            if (!(s instanceof Player)) { Msg.send(s, "&7플레이어만 사용 가능합니다."); return true; }
            Player p = (Player) s; gm.join(p); return true;
        }
        if ("leave".equals(sub)) {
            if (!(s instanceof Player)) { Msg.send(s, "&7플레이어만 사용 가능합니다."); return true; }
            Player p = (Player) s; gm.leave(p); return true;
        }
        if ("start".equals(sub)) { if (!s.hasPermission("sdef.admin")) { Msg.send(s, "&c권한이 없습니다."); return true; } gm.start(); return true; }
        if ("stop".equals(sub)) { if (!s.hasPermission("sdef.admin")) { Msg.send(s, "&c권한이 없습니다."); return true; } gm.stop(); return true; }
        if ("setcenter".equals(sub)) {
            if (!s.hasPermission("sdef.admin")) { Msg.send(s, "&c권한이 없습니다."); return true; }
            if (!(s instanceof Player)) { Msg.send(s, "&7플레이어만 사용 가능합니다."); return true; }
            Player p = (Player) s;
            if (gm.getArena() == null) { Msg.send(s, "&c아레나가 로드되지 않았습니다."); return true; }
            gm.getArena().setCenter(p.getLocation()); Msg.send(s, "&a센터를 현재 위치로 설정했습니다."); return true;
        }
        if ("setradius".equals(sub)) {
            if (!s.hasPermission("sdef.admin")) { Msg.send(s, "&c권한이 없습니다."); return true; }
            if (args.length < 2) { Msg.send(s, "&7사용법: /sdef setradius <반경>"); return true; }
            int r = 40; try { r = Integer.parseInt(args[1]); } catch (Exception ignored) {}
            if (gm.getArena() == null) { Msg.send(s, "&c아레나가 로드되지 않았습니다."); return true; }
            gm.getArena().setRadius(r); Msg.send(s, "&a반경을 " + r + " 으로 설정했습니다."); return true;
        }
        if ("addspawn".equals(sub)) {
            if (!s.hasPermission("sdef.admin")) { Msg.send(s, "&c권한이 없습니다."); return true; }
            if (!(s instanceof Player)) { Msg.send(s, "&7플레이어만 사용 가능합니다."); return true; }
            Player p = (Player) s;
            if (gm.getArena() == null) { Msg.send(s, "&c아레나가 로드되지 않았습니다."); return true; }
            Location loc = p.getLocation().getBlock().getLocation();
            gm.getArena().addSpawn(loc); Msg.send(s, "&a스폰 포인트를 추가했습니다: &f" + LocationUtil.toXYZ(loc)); return true;
        }
        if ("lobby".equals(sub)) {
            if (!s.hasPermission("sdef.admin")) { Msg.send(s, "&c권한이 없습니다."); return true; }
            if (!(s instanceof Player)) { Msg.send(s, "&7플레이어만 사용 가능합니다."); return true; }
            Player p = (Player) s;
            if (gm.getArena() == null) { Msg.send(s, "&c아레나가 로드되지 않았습니다."); return true; }
            gm.getArena().setLobby(p.getLocation()); Msg.send(s, "&a로비를 현재 위치로 설정했습니다."); return true;
        }
        if ("givekit".equals(sub)) {
            if (!s.hasPermission("sdef.admin")) { Msg.send(s, "&c권한이 없습니다."); return true; }
            if (args.length < 2) { Msg.send(s, "&7사용법: /sdef givekit <플레이어> [개수]"); return true; }
            Player t = s.getServer().getPlayer(args[1]);
            int amt = 8;
            if (args.length >= 3) { try { amt = Integer.parseInt(args[2]); } catch (Exception ignored) {} }
            if (t == null) { Msg.send(s, "&c해당 플레이어를 찾을 수 없습니다."); return true; }
            t.getInventory().addItem(bm.createItem(amt)); Msg.send(s, "&a바리케이드 키트를 지급했습니다: " + t.getName() + " x" + amt); return true;
        }
        help(s); return true;
    }

    private void help(CommandSender s) {
        Msg.send(s, "&e/sdef join &7- 참가");
        Msg.send(s, "&e/sdef leave &7- 퇴장");
        Msg.send(s, "&e/sdef start &7- &8(관리자) 시작");
        Msg.send(s, "&e/sdef stop &7- &8(관리자) 중지");
        Msg.send(s, "&e/sdef setcenter &7- &8(관리자) 센터 지정");
        Msg.send(s, "&e/sdef setradius <r> &7- &8(관리자) 반경 설정");
        Msg.send(s, "&e/sdef addspawn &7- &8(관리자) 몹 스폰포인트 추가");
        Msg.send(s, "&e/sdef lobby &7- &8(관리자) 로비 지정");
        Msg.send(s, "&e/sdef givekit <플레이어> [개수] &7- &8(관리자) 바리케이드 지급");
        Msg.send(s, "&e/sdef rewardgui <stage> &7- &8(관리자) 보상 GUI 열기");
        Msg.send(s, "&e/sdef settime <stage> <sec> &7- &8(관리자) 제한시간 설정(0=해제)");
        Msg.send(s, "&7또는 한국어 명령: &f/생존디펜스 도움말");
    }
}

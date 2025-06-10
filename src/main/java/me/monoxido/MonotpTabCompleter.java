package me.monoxido;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class MonotpTabCompleter implements TabCompleter {
    private final WarpManager warpManager;

    // Constructor para inicializar WarpManager
    public MonotpTabCompleter(WarpManager warpManager) {
        this.warpManager = warpManager;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> suggestions = new ArrayList<>();
        String commandName = command.getName().toLowerCase();

        if (args.length == 1) {
            switch (commandName) {
                case "tpall":
                case "back":
                case "warps":
                    break;
                case "warp":
                case "warpall":
                case "delwarp":
                    for (String warpName : warpManager.getWarpNames()) {
                        if (warpName.toLowerCase().startsWith(args[0].toLowerCase())) {
                            suggestions.add(warpName);
                        }
                    }
                    break;
                case "setwarp":
                    break;
                case "tp":
                case "sudo":
                case "vanish":
                case "hat":
                case "god":
                case "fly":
                case "mutechat":
                case "blockcmds":
                    // Suggest online player names
                    for (org.bukkit.entity.Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
                        if (p.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                            suggestions.add(p.getName());
                        }
                    }
                    break;
                case "ec":
                case "enderchest":
                case "whois":
                    for (org.bukkit.entity.Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
                        if (p.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                            suggestions.add(p.getName());
                        }
                    }
                    break;
                case "nick":
                    if (args.length == 1) {
                        // Sugerir jugadores para el segundo argumento opcional
                        for (org.bukkit.entity.Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
                            suggestions.add(p.getName());
                        }
                    }
                    break;
                case "weather":
                    for (String w : new String[]{"clear", "rain", "storm"}) {
                        if (w.startsWith(args[0].toLowerCase())) suggestions.add(w);
                    }
                    break;
                case "gamemode":
                    for (String gm : new String[]{"survival", "creative", "adventure", "spectator", "0", "1", "2", "3"}) {
                        if (gm.startsWith(args[0].toLowerCase())) suggestions.add(gm);
                    }
                    for (org.bukkit.entity.Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
                        if (p.getName().toLowerCase().startsWith(args[0].toLowerCase())) suggestions.add(p.getName());
                    }
                    break;
                case "feed":
                case "heal":
                    if ("all".startsWith(args[0].toLowerCase())) suggestions.add("all");
                    for (org.bukkit.entity.Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
                        if (p.getName().toLowerCase().startsWith(args[0].toLowerCase())) suggestions.add(p.getName());
                    }
                    break;
                case "speed":
                    for (String s : new String[]{"1","2","3","4","5","6","7","8","9","10"}) {
                        if (s.startsWith(args[0].toLowerCase())) suggestions.add(s);
                    }
                    for (org.bukkit.entity.Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
                        if (p.getName().toLowerCase().startsWith(args[0].toLowerCase())) suggestions.add(p.getName());
                    }
                    break;
            }
        } else if (args.length == 2) {
            switch (commandName) {
                case "gamemode":
                case "feed":
                case "heal":
                    for (org.bukkit.entity.Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
                        if (p.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                            suggestions.add(p.getName());
                        }
                    }
                    break;
                case "mutechat":
                    if (args.length == 1) {
                        // /mutechat <tiempo> <motivo>
                        suggestions.add("30");
                        suggestions.add("60");
                        suggestions.add("120");
                    }
                    break;
                case "ec":
                case "enderchest":
                case "nick":
                    for (org.bukkit.entity.Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
                        if (p.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                            suggestions.add(p.getName());
                        }
                    }
                    break;
                case "speed":
                    for (org.bukkit.entity.Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
                        if (p.getName().toLowerCase().startsWith(args[1].toLowerCase())) suggestions.add(p.getName());
                    }
                    break;
            }
        }

        return suggestions;
    }
}

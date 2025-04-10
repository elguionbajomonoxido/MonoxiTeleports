package mt.monoxido;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class CommandHandler {
    private final WarpManager warpManager;
    private final TeleportManager teleportManager;
    private final WarpGUI warpGUI;
    private final JavaPlugin plugin;
    private final String prefix;

    public CommandHandler(JavaPlugin plugin, WarpManager warpManager, TeleportManager teleportManager, WarpGUI warpGUI) {
        this.plugin = plugin;
        this.warpManager = warpManager;
        this.teleportManager = teleportManager;
        this.warpGUI = warpGUI;
        this.prefix = ((MonoxiTeleports) plugin).getPrefix();
        registerCommands();
    }

    private void registerCommands() {
        // Registrar el comando "mtp"
        plugin.getCommand("mtp").setExecutor(new MTPCommand());
        plugin.getCommand("mtp").setTabCompleter(new MTPCommand());
    }

    private class MTPCommand implements CommandExecutor, TabCompleter {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(prefix + ChatColor.RED + "Este comando solo puede ser ejecutado por jugadores.");
                return false;
            }

            Player player = (Player) sender;

            if (args.length >= 1) {
                String subCommand = args[0].toLowerCase();

                switch (subCommand) {
                    case "tpall":
                        teleportManager.tpAll(player);
                        return true;
                    case "warp":
                        if (args.length == 2) {
                            String warpName = args[1];
                            teleportManager.teleportToWarp(player, warpName);
                            return true;
                        } else {
                            player.sendMessage(prefix + ChatColor.RED + "Uso correcto: /mtp warp <nombre>");
                            return false;
                        }
                    case "warpall":
                        if (args.length == 2) {
                            String warpName = args[1];
                            teleportManager.warpAll(player, warpName);
                            return true;
                        } else {
                            player.sendMessage(prefix + ChatColor.RED + "Uso correcto: /mtp warpall <nombre>");
                            return false;
                        }
                    case "back":
                        teleportManager.teleportBack(player);
                        return true;
                    case "gui":
                        warpGUI.openWarpMenu(player);
                        return true;
                    case "setwarp":
                        if (!player.hasPermission("monoxi.teleports.setwarp")) {
                            player.sendMessage(prefix + ChatColor.RED + "No tienes permiso para usar este comando.");
                            return false;
                        }
                        if (args.length == 2) {
                            String warpName = args[1];
                            warpManager.setWarp(player, warpName);
                            return true;
                        } else {
                            player.sendMessage(prefix + ChatColor.RED + "Uso correcto: /mtp setwarp <nombre>");
                            return false;
                        }
                    case "delwarp":
                        if (!player.hasPermission("monoxi.teleports.delwarp")) {
                            player.sendMessage(prefix + ChatColor.RED + "No tienes permiso para usar este comando.");
                            return false;
                        }
                        if (args.length == 2) {
                            String warpName = args[1];
                            warpManager.delWarp(player, warpName);
                            return true;
                        } else {
                            player.sendMessage(prefix + ChatColor.RED + "Uso correcto: /mtp delwarp <nombre>");
                            return false;
                        }
                    case "warps":
                        warpManager.listWarps(player);
                        return true;
                    default:
                        player.sendMessage(prefix + ChatColor.RED + "Comando desconocido. Uso correcto: /mtp <tpall|warp|warpall|back|gui|setwarp|delwarp|warps>");
                        return false;
                }
            } else {
                player.sendMessage(prefix + ChatColor.GOLD + "=== " + ChatColor.YELLOW + "MonoxiTeleports v" + ((MonoxiTeleports) plugin).version + ChatColor.GOLD + " ===");
                player.sendMessage(prefix + ChatColor.YELLOW + "/mtp tpall " + ChatColor.GRAY + "- " + ChatColor.WHITE + "Teletransporta a todos los jugadores");
                player.sendMessage(prefix + ChatColor.YELLOW + "/mtp warp <nombre> " + ChatColor.GRAY + "- " + ChatColor.WHITE + "Teletransporta a un warp");
                player.sendMessage(prefix + ChatColor.YELLOW + "/mtp warpall <nombre> " + ChatColor.GRAY + "- " + ChatColor.WHITE + "Teletransporta a todos a un warp");
                player.sendMessage(prefix + ChatColor.YELLOW + "/mtp back " + ChatColor.GRAY + "- " + ChatColor.WHITE + "Teletransporta a tu última ubicación");
                player.sendMessage(prefix + ChatColor.YELLOW + "/mtp gui " + ChatColor.GRAY + "- " + ChatColor.WHITE + "Abre el menú de warps");
                player.sendMessage(prefix + ChatColor.YELLOW + "/mtp setwarp <nombre> " + ChatColor.GRAY + "- " + ChatColor.WHITE + "Crea un nuevo warp");
                player.sendMessage(prefix + ChatColor.YELLOW + "/mtp delwarp <nombre> " + ChatColor.GRAY + "- " + ChatColor.WHITE + "Elimina un warp");
                player.sendMessage(prefix + ChatColor.YELLOW + "/mtp warps " + ChatColor.GRAY + "- " + ChatColor.WHITE + "Lista los warps disponibles");
                return false;
            }
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
            List<String> completions = new ArrayList<>();

            if (!(sender instanceof Player)) {
                return completions;
            }

            Player player = (Player) sender;

            if (args.length == 1) {
                // Completar subcomandos principales
                completions.add("tpall");
                completions.add("warp");
                completions.add("warpall");
                completions.add("back");
                completions.add("gui");
                
                if (player.hasPermission("monoxi.teleports.setwarp")) {
                    completions.add("setwarp");
                }
                
                if (player.hasPermission("monoxi.teleports.delwarp")) {
                    completions.add("delwarp");
                }
                
                completions.add("warps");
                
                // Filtrar según lo que el jugador ha escrito
                String input = args[0].toLowerCase();
                completions.removeIf(s -> !s.toLowerCase().startsWith(input));
            } else if (args.length == 2) {
                String subCommand = args[0].toLowerCase();
                
                switch (subCommand) {
                    case "warp":
                    case "warpall":
                    case "delwarp":
                        // Completar nombres de warps
                        completions.addAll(warpManager.getWarpNames());
                        break;
                }
                
                // Filtrar según lo que el jugador ha escrito
                String input = args[1].toLowerCase();
                completions.removeIf(s -> !s.toLowerCase().startsWith(input));
            }

            return completions;
        }
    }
} 
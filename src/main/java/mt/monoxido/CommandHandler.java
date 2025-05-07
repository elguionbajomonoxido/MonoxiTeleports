package mt.monoxido;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class CommandHandler {
    private final WarpManager warpManager;
    private final TeleportManager teleportManager;
    private final JavaPlugin plugin;
    private final String prefix;

    public CommandHandler(JavaPlugin plugin, WarpManager warpManager, TeleportManager teleportManager) {
        this.plugin = plugin;
        this.warpManager = warpManager;
        this.teleportManager = teleportManager;
        this.prefix = ((MonoxiTeleports) plugin).getPrefix();
        // Llamar al método registerCommands para registrar los comandos
        registerCommands();
    }

    private void registerCommands() {
        plugin.getCommand("tpall").setExecutor(new MTPCommand());
        plugin.getCommand("warp").setExecutor(new MTPCommand());
        plugin.getCommand("warpall").setExecutor(new MTPCommand());
        plugin.getCommand("back").setExecutor(new MTPCommand());
        plugin.getCommand("setwarp").setExecutor(new MTPCommand());
        plugin.getCommand("delwarp").setExecutor(new MTPCommand());
        plugin.getCommand("warps").setExecutor(new MTPCommand());

        plugin.getCommand("tpall").setTabCompleter(new MonotpTabCompleter(warpManager));
        plugin.getCommand("warp").setTabCompleter(new MonotpTabCompleter(warpManager));
        plugin.getCommand("warpall").setTabCompleter(new MonotpTabCompleter(warpManager));
        plugin.getCommand("back").setTabCompleter(new MonotpTabCompleter(warpManager));
        plugin.getCommand("setwarp").setTabCompleter(new MonotpTabCompleter(warpManager));
        plugin.getCommand("delwarp").setTabCompleter(new MonotpTabCompleter(warpManager));
        plugin.getCommand("warps").setTabCompleter(new MonotpTabCompleter(warpManager));
    }


    private class MTPCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            String commandName = command.getName().toLowerCase();

            switch (commandName) {
                case "tpall":
                    teleportManager.tpAll((Player) sender);
                    return true;
                case "warp":
                    if (args.length == 1) {
                        String warpName = args[0];
                        if (warpManager.warpExists(warpName)) {
                            teleportManager.teleportToWarp((Player) sender, warpName);
                            return true;
                        } else {
                            sender.sendMessage(prefix + ChatColor.RED + "El warp '" + warpName + "' no existe.");
                            return false;
                        }
                    } else {
                        sender.sendMessage(prefix + ChatColor.RED + "Uso correcto: /warp <nombre>");
                        return false;
                    }
                case "warpall":
                    if (args.length == 1) {
                        String warpName = args[0];
                        if (warpManager.warpExists(warpName)) {
                            teleportManager.warpAll((Player) sender, warpName);
                            return true;
                        } else {
                            sender.sendMessage(prefix + ChatColor.RED + "El warp '" + warpName + "' no existe.");
                            return false;
                        }
                    } else {
                        sender.sendMessage(prefix + ChatColor.RED + "Uso correcto: /warpall <nombre>");
                        return false;
                    }
                case "back":
                    teleportManager.teleportBack((Player) sender);
                    return true;
                case "setwarp":
                    if (!sender.hasPermission("monoxi.teleports.setwarp")) {
                        sender.sendMessage(prefix + ChatColor.RED + "No tienes permiso para usar este comando.");
                        return false;
                    }
                    if (args.length == 1) {
                        String warpName = args[0];
                        warpManager.setWarp((Player) sender, warpName);
                        return true;
                    } else {
                        sender.sendMessage(prefix + ChatColor.RED + "Uso correcto: /setwarp <nombre>");
                        return false;
                    }
                case "delwarp":
                    if (!sender.hasPermission("monoxi.teleports.delwarp")) {
                        sender.sendMessage(prefix + ChatColor.RED + "No tienes permiso para usar este comando.");
                        return false;
                    }
                    if (args.length == 1) {
                        String warpName = args[0];
                        warpManager.delWarp((Player) sender, warpName);
                        return true;
                    } else {
                        sender.sendMessage(prefix + ChatColor.RED + "Uso correcto: /delwarp <nombre>");
                        return false;
                    }
                case "warps":
                    warpManager.listWarps((Player) sender);
                    return true;
                default:
                    sender.sendMessage(prefix + ChatColor.RED + "Comando desconocido.");
                    return false;
            }
        }
    }
}

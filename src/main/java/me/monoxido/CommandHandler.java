package me.monoxido;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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
        this.prefix = ((MonoxiEssentials) plugin).getPrefix();
        // Llamar al método registerCommands para registrar los comandos
        registerCommands();
    }

    private void registerCommands() {
        // Lista completa de comandos para registrar executor
        String[] commands = {
                "tpall", "warp", "warpall", "back", "setwarp", "delwarp", "warps", "heal", "feed", "gamemode", "gm",
                "fly", "god", "day", "night", "weather", "speed", "flyspeed", "tp", "tph", "tphere", "tpo", "hat", "sudo", "vanish", "mutechat",
                "unmutechat", "mutelist", "blockcmds", "ec", "enderchest", "near", "whois", "nick", "iname", "ilore",
                /* eliminados: "spawn", "spawnall", "setspawn" */ "gamerulegui"
        };
        for (String cmd : commands) {
            if (plugin.getCommand(cmd) != null) {
                plugin.getCommand(cmd).setExecutor(new MECommand());
            }
        }
        // Lista de comandos con tab completer
        String[] tabCompleteCommands = {
                "tpall", "warp", "warpall", "back", "setwarp", "delwarp", "warps",
                "tp", "tph", "tphere", "tpo", "sudo", "vanish", "hat", "god", "fly", "mutechat", "blockcmds",
                "ec", "enderchest", "whois", "nick", "weather", "gamemode", "gm", "feed", "heal", "speed", "flyspeed"
                // eliminados: "spawn", "spawnall", "setspawn"
        };
        MonotpTabCompleter tabCompleter = new MonotpTabCompleter(warpManager);
        for (String cmd : tabCompleteCommands) {
            if (plugin.getCommand(cmd) != null) {
                plugin.getCommand(cmd).setTabCompleter(tabCompleter);
            }
        }
    }
    private class MECommand implements CommandExecutor {
        @Override

        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            // Solo permitir a operadores (op)
            if (!(sender instanceof Player) || !sender.isOp()) {
                sender.sendMessage(prefix + ChatColor.RED + "No tienes permiso para usar este comando. Solo operadores (op) pueden usarlo.");
                return false;
            }
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
                case "heal":
                    if (args.length == 0) {
                        Player player = (Player) sender;
                        player.setHealth(player.getMaxHealth());
                        player.setFoodLevel(20);
                        player.setFireTicks(0);
                        player.sendMessage(prefix + ChatColor.GREEN + "¡Has sido curado!");
                        return true;
                    } else if (args.length == 1) {
                        if (args[0].equalsIgnoreCase("all")) {
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                p.setHealth(p.getMaxHealth());
                                p.setFoodLevel(20);
                                p.setFireTicks(0);
                                p.sendMessage(prefix + ChatColor.GREEN + "¡Has sido curado por " + sender.getName() + "!");
                            }
                            sender.sendMessage(prefix + ChatColor.GREEN + "Todos los jugadores han sido curados.");
                            return true;
                        } else {
                            Player target = Bukkit.getPlayerExact(args[0]);
                            if (target != null) {
                                target.setHealth(target.getMaxHealth());
                                target.setFoodLevel(20);
                                target.setFireTicks(0);
                                target.sendMessage(prefix + ChatColor.GREEN + "¡Has sido curado por " + sender.getName() + "!");
                                sender.sendMessage(prefix + ChatColor.GREEN + "Has curado a " + target.getName() + ".");
                                return true;
                            } else {
                                sender.sendMessage(prefix + ChatColor.RED + "Jugador no encontrado.");
                                return false;
                            }
                        }
                    } else {
                        sender.sendMessage(prefix + ChatColor.RED + "Uso correcto: /heal [jugador|all]");
                        return false;
                    }
                case "feed":
                    if (args.length == 0) {
                        Player player = (Player) sender;
                        player.setFoodLevel(20);
                        player.setSaturation(20f);
                        player.sendMessage(prefix + ChatColor.GREEN + "¡Has sido alimentado!");
                        return true;
                    } else if (args.length == 1) {
                        if (args[0].equalsIgnoreCase("all")) {
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                p.setFoodLevel(20);
                                p.setSaturation(20f);
                                p.sendMessage(prefix + ChatColor.GREEN + "¡Has sido alimentado por " + sender.getName() + "!");
                            }
                            sender.sendMessage(prefix + ChatColor.GREEN + "Todos los jugadores han sido alimentados.");
                            return true;
                        } else {
                            Player target = Bukkit.getPlayerExact(args[0]);
                            if (target != null) {
                                target.setFoodLevel(20);
                                target.setSaturation(20f);
                                target.sendMessage(prefix + ChatColor.GREEN + "¡Has sido alimentado por " + sender.getName() + "!");
                                sender.sendMessage(prefix + ChatColor.GREEN + "Has alimentado a " + target.getName() + ".");
                                return true;
                            } else {
                                sender.sendMessage(prefix + ChatColor.RED + "Jugador no encontrado.");
                                return false;
                            }
                        }
                    } else {
                        sender.sendMessage(prefix + ChatColor.RED + "Uso correcto: /feed [jugador|all]");
                        return false;
                    }
                case "gamemode":
                case "gm":
                    if (args.length == 1 || args.length == 2) {
                        String mode = args[0].toLowerCase();
                        Player target = (args.length == 2) ? Bukkit.getPlayerExact(args[1]) : (Player) sender;
                        if (target == null) {
                            sender.sendMessage(prefix + ChatColor.RED + "Jugador no encontrado.");
                            return false;
                        }
                        switch (mode) {
                            case "0": case "survival":
                                target.setGameMode(org.bukkit.GameMode.SURVIVAL);
                                break;
                            case "1": case "creative":
                                target.setGameMode(org.bukkit.GameMode.CREATIVE);
                                break;
                            case "2": case "adventure":
                                target.setGameMode(org.bukkit.GameMode.ADVENTURE);
                                break;
                            case "3": case "spectator":
                                target.setGameMode(org.bukkit.GameMode.SPECTATOR);
                                break;
                            default:
                                sender.sendMessage(prefix + ChatColor.RED + "Modos: survival, creative, adventure, spectator");
                                return false;
                        }
                        target.sendMessage(prefix + ChatColor.GREEN + "¡Tu modo de juego ha sido cambiado a " + target.getGameMode().name().toLowerCase() + "!");
                        if (target != sender) {
                            sender.sendMessage(prefix + ChatColor.GREEN + "Modo de juego de " + target.getName() + " cambiado a " + target.getGameMode().name().toLowerCase() + ".");
                        }
                        return true;
                    } else {
                        sender.sendMessage(prefix + ChatColor.RED + "Uso correcto: /gamemode <modo> [jugador]");
                        return false;
                    }
                case "fly":
                    Player flyTarget = (args.length == 1) ? Bukkit.getPlayerExact(args[0]) : (Player) sender;
                    if (flyTarget == null) {
                        sender.sendMessage(prefix + ChatColor.RED + "Jugador no encontrado.");
                        return false;
                    }
                    boolean newFly = !flyTarget.getAllowFlight();
                    flyTarget.setAllowFlight(newFly);
                    flyTarget.sendMessage(prefix + ChatColor.GREEN + "¡Modo vuelo " + (newFly ? "activado" : "desactivado") + "!");
                    if (flyTarget != sender) {
                        sender.sendMessage(prefix + ChatColor.GREEN + "Modo vuelo de " + flyTarget.getName() + " " + (newFly ? "activado" : "desactivado") + ".");
                    }
                    return true;
                case "god":
                    Player godTarget = (args.length == 1) ? Bukkit.getPlayerExact(args[0]) : (Player) sender;
                    if (godTarget == null) {
                        sender.sendMessage(prefix + ChatColor.RED + "Jugador no encontrado.");
                        return false;
                    }
                    boolean newGod = !godTarget.isInvulnerable();
                    godTarget.setInvulnerable(newGod);
                    godTarget.setHealth(newGod ? godTarget.getMaxHealth() : Math.max(1, godTarget.getHealth()));
                    godTarget.sendMessage(prefix + ChatColor.GREEN + (newGod ? "¡Modo dios activado!" : "¡Modo dios desactivado!"));
                    if (godTarget != sender) {
                        sender.sendMessage(prefix + ChatColor.GREEN + "Modo dios de " + godTarget.getName() + (newGod ? " activado." : " desactivado."));
                    }
                    return true;
                case "day":
                    ((Player) sender).getWorld().setTime(1000);
                    sender.sendMessage(prefix + ChatColor.GREEN + "¡Ahora es de día!");
                    return true;
                case "night":
                    ((Player) sender).getWorld().setTime(13000);
                    sender.sendMessage(prefix + ChatColor.GREEN + "¡Ahora es de noche!");
                    return true;
                case "weather":
                    if (args.length == 1) {
                        String weather = args[0].toLowerCase();
                        org.bukkit.World world = ((Player) sender).getWorld();
                        switch (weather) {
                            case "clear":
                                world.setStorm(false);
                                world.setThundering(false);
                                sender.sendMessage(prefix + ChatColor.GREEN + "¡Clima despejado!");
                                return true;
                            case "rain":
                                world.setStorm(true);
                                world.setThundering(false);
                                sender.sendMessage(prefix + ChatColor.GREEN + "¡Ahora está lloviendo!");
                                return true;
                            case "storm":
                                world.setStorm(true);
                                world.setThundering(true);
                                sender.sendMessage(prefix + ChatColor.GREEN + "¡Ahora hay tormenta!");
                                return true;
                            default:
                                sender.sendMessage(prefix + ChatColor.RED + "Opciones: clear, rain, storm");
                                return false;
                        }
                    } else {
                        sender.sendMessage(prefix + ChatColor.RED + "Uso correcto: /weather <clear|rain|storm>");
                        return false;
                    }
                case "speed":
                    // /speed <1-10> [jugador] - solo velocidad caminando
                    if (args.length == 1 || args.length == 2) {
                        float speed;
                        try {
                            speed = Float.parseFloat(args[0]);
                        } catch (NumberFormatException e) {
                            sender.sendMessage(prefix + ChatColor.RED + "La velocidad debe ser un número.");
                            return false;
                        }
                        if (speed < 0.1f) speed = 0.1f;
                        if (speed > 10f) speed = 10f;
                        Player speedTarget = (args.length == 2) ? Bukkit.getPlayerExact(args[1]) : (Player) sender;
                        if (speedTarget == null) {
                            sender.sendMessage(prefix + ChatColor.RED + "Jugador no encontrado.");
                            return false;
                        }
                        speedTarget.setWalkSpeed(speed / 10f);
                        speedTarget.sendMessage(prefix + ChatColor.GREEN + "¡Tu velocidad de caminata ha sido cambiada a " + speed + "!");
                        if (speedTarget != sender) {
                            sender.sendMessage(prefix + ChatColor.GREEN + "Velocidad de caminata de " + speedTarget.getName() + " cambiada a " + speed + ".");
                        }
                        return true;
                    } else {
                        sender.sendMessage(prefix + ChatColor.RED + "Uso correcto: /speed <1-10> [jugador]");
                        return false;
                    }
                case "flyspeed":
                    // /flyspeed <1-10> [jugador] - solo velocidad de vuelo
                    if (args.length == 1 || args.length == 2) {
                        float speed;
                        try {
                            speed = Float.parseFloat(args[0]);
                        } catch (NumberFormatException e) {
                            sender.sendMessage(prefix + ChatColor.RED + "La velocidad debe ser un número.");
                            return false;
                        }
                        if (speed < 0.1f) speed = 0.1f;
                        if (speed > 10f) speed = 10f;
                        Player speedTarget = (args.length == 2) ? Bukkit.getPlayerExact(args[1]) : (Player) sender;
                        if (speedTarget == null) {
                            sender.sendMessage(prefix + ChatColor.RED + "Jugador no encontrado.");
                            return false;
                        }
                        speedTarget.setFlySpeed(speed / 10f);
                        speedTarget.sendMessage(prefix + ChatColor.GREEN + "¡Tu velocidad de vuelo ha sido cambiada a " + speed + "!");
                        if (speedTarget != sender) {
                            sender.sendMessage(prefix + ChatColor.GREEN + "Velocidad de vuelo de " + speedTarget.getName() + " cambiada a " + speed + ".");
                        }
                        return true;
                    } else {
                        sender.sendMessage(prefix + ChatColor.RED + "Uso correcto: /flyspeed <1-10> [jugador]");
                        return false;
                    }
                case "hat":
                    Player hatPlayer = (args.length == 1) ? Bukkit.getPlayerExact(args[0]) : (Player) sender;
                    if (hatPlayer == null) {
                        sender.sendMessage(prefix + ChatColor.RED + "Jugador no encontrado.");
                        return false;
                    }
                    ItemStack hand = hatPlayer.getInventory().getItemInMainHand();
                    if (hand == null || hand.getType().isAir()) {
                        sender.sendMessage(prefix + ChatColor.RED + "No tienes ningún objeto en la mano principal.");
                        return false;
                    }
                    ItemStack oldHelmet = hatPlayer.getInventory().getHelmet();
                    hatPlayer.getInventory().setHelmet(hand.clone());
                    if (oldHelmet != null && !oldHelmet.getType().isAir()) {
                        hatPlayer.getInventory().addItem(oldHelmet);
                    }
                    sender.sendMessage(prefix + ChatColor.GREEN + "¡Ahora llevas tu objeto en la cabeza!");
                    if (hatPlayer != sender) {
                        hatPlayer.sendMessage(prefix + ChatColor.GREEN + "¡Ahora llevas tu objeto en la cabeza por " + sender.getName() + "!");
                    }
                    return true;
                case "sudo":
                    if (args.length < 2) {
                        sender.sendMessage(prefix + ChatColor.RED + "Uso correcto: /sudo <jugador> <comando>");
                        return false;
                    }
                    Player sudoTarget = Bukkit.getPlayerExact(args[0]);
                    if (sudoTarget == null) {
                        sender.sendMessage(prefix + ChatColor.RED + "Jugador no encontrado.");
                        return false;
                    }
                    String sudoCmd = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
                    sudoTarget.performCommand(sudoCmd);
                    sender.sendMessage(prefix + ChatColor.GREEN + "Has forzado a " + sudoTarget.getName() + " a ejecutar: /" + sudoCmd);
                    return true;
                case "vanish":
                    Player vanishTarget = (args.length == 1) ? Bukkit.getPlayerExact(args[0]) : (Player) sender;
                    if (vanishTarget == null) {
                        sender.sendMessage(prefix + ChatColor.RED + "Jugador no encontrado.");
                        return false;
                    }
                    boolean vanished = vanishTarget.hasMetadata("vanished");
                    if (!vanished) {
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            if (!p.isOp()) p.hidePlayer(plugin, vanishTarget);
                        }
                        vanishTarget.setMetadata("vanished", new org.bukkit.metadata.FixedMetadataValue(plugin, true));
                        vanishTarget.sendMessage(prefix + ChatColor.GREEN + "¡Ahora eres invisible para los jugadores!");
                        if (vanishTarget != sender) sender.sendMessage(prefix + ChatColor.GREEN + vanishTarget.getName() + " ahora está invisible.");
                    } else {
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            p.showPlayer(plugin, vanishTarget);
                        }
                        vanishTarget.removeMetadata("vanished", plugin);
                        vanishTarget.sendMessage(prefix + ChatColor.GREEN + "¡Ya no eres invisible!");
                        if (vanishTarget != sender) sender.sendMessage(prefix + ChatColor.GREEN + vanishTarget.getName() + " ya no está invisible.");
                    }
                    return true;
                case "mutechat":
                    int seconds = 0;
                    String reason = "";
                    if (args.length >= 1) {
                        try {
                            seconds = Integer.parseInt(args[0]);
                        } catch (NumberFormatException ignored) {}
                    }
                    if (args.length >= 2) {
                        reason = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
                    }
                    MuteChatListener.mute(sender.getName(), seconds, reason);
                    sender.sendMessage(prefix + ChatColor.GREEN + "El chat global ha sido muteado" + (seconds > 0 ? " por " + seconds + " segundos" : "") + (reason.isEmpty() ? "." : ": " + reason));
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendMessage(prefix + ChatColor.RED + "El chat ha sido muteado por un administrador." + (reason.isEmpty() ? "" : " Motivo: " + reason));
                    }
                    return true;
                case "unmutechat":
                    MuteChatListener.unmute();
                    sender.sendMessage(prefix + ChatColor.GREEN + "El chat global ha sido desmuteado.");
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendMessage(prefix + ChatColor.YELLOW + "El chat ha sido desmuteado por un administrador.");
                    }
                    return true;
                case "mutelist":
                    if (MuteChatListener.isMuted()) {
                        sender.sendMessage(prefix + ChatColor.YELLOW + "El chat está muteado por: " + MuteChatListener.getMutedBy() + (MuteChatListener.getMuteReason().isEmpty() ? "" : " | Motivo: " + MuteChatListener.getMuteReason()) + (MuteChatListener.getMuteSeconds() > 0 ? " | Tiempo restante: " + MuteChatListener.getMuteSeconds() + "s" : ""));
                    } else {
                        sender.sendMessage(prefix + ChatColor.GREEN + "El chat no está muteado.");
                    }
                    return true;
                case "blockcmds":
                    BlockCmdsListener.toggleBlock();
                    sender.sendMessage(prefix + ChatColor.GREEN + "El uso de comandos ha sido " + (BlockCmdsListener.isBlocked() ? "bloqueado" : "desbloqueado") + " para los jugadores.");
                    return true;
                case "ec":
                case "enderchest":
                    Player ecTarget = (args.length == 1) ? Bukkit.getPlayerExact(args[0]) : (Player) sender;
                    if (ecTarget == null) {
                        sender.sendMessage(prefix + ChatColor.RED + "Jugador no encontrado.");
                        return false;
                    }
                    ((Player) sender).openInventory(ecTarget.getEnderChest());
                    sender.sendMessage(prefix + ChatColor.GREEN + "Enderchest de " + ecTarget.getName() + " abierto.");
                    return true;
                case "near":
                    Player nearPlayer = (Player) sender;
                    double radius = 100.0;
                    List<String> nearby = new ArrayList<>();
                    for (Player p : nearPlayer.getWorld().getPlayers()) {
                        if (p != nearPlayer && p.getLocation().distance(nearPlayer.getLocation()) <= radius) {
                            nearby.add(p.getName());
                        }
                    }
                    if (nearby.isEmpty()) {
                        sender.sendMessage(prefix + ChatColor.YELLOW + "No hay jugadores cerca (" + (int)radius + " bloques).");
                    } else {
                        sender.sendMessage(prefix + ChatColor.GREEN + "Jugadores cerca (" + (int)radius + " bloques): " + String.join(", ", nearby));
                    }
                    return true;
                case "whois":
                    if (args.length != 1) {
                        sender.sendMessage(prefix + ChatColor.RED + "Uso correcto: /whois <jugador>");
                        return false;
                    }
                    Player whoisTarget = Bukkit.getPlayerExact(args[0]);
                    if (whoisTarget == null) {
                        sender.sendMessage(prefix + ChatColor.RED + "Jugador no encontrado.");
                        return false;
                    }
                    sender.sendMessage(prefix + ChatColor.AQUA + "Información de " + whoisTarget.getName() + ":");
                    sender.sendMessage(ChatColor.GRAY + "UUID: " + whoisTarget.getUniqueId());
                    sender.sendMessage(ChatColor.GRAY + "IP: " + whoisTarget.getAddress().getAddress().getHostAddress());
                    sender.sendMessage(ChatColor.GRAY + "Mundo: " + whoisTarget.getWorld().getName());
                    sender.sendMessage(ChatColor.GRAY + "Ubicación: X=" + whoisTarget.getLocation().getBlockX() + " Y=" + whoisTarget.getLocation().getBlockY() + " Z=" + whoisTarget.getLocation().getBlockZ());
                    sender.sendMessage(ChatColor.GRAY + "Gamemode: " + whoisTarget.getGameMode().name());
                    sender.sendMessage(ChatColor.GRAY + "Salud: " + whoisTarget.getHealth() + "/" + whoisTarget.getMaxHealth());
                    sender.sendMessage(ChatColor.GRAY + "Comida: " + whoisTarget.getFoodLevel());
                    sender.sendMessage(ChatColor.GRAY + "Nivel: " + whoisTarget.getLevel());
                    return true;
                case "nick":
                    if (args.length < 1) {
                        sender.sendMessage(prefix + ChatColor.RED + "Uso correcto: /nick <nuevo-nick>");
                        return false;
                    }
                    Player nickTarget = (args.length == 2) ? Bukkit.getPlayerExact(args[1]) : (Player) sender;
                    if (nickTarget == null) {
                        sender.sendMessage(prefix + ChatColor.RED + "Jugador no encontrado.");
                        return false;
                    }
                    String newNick = args[0];
                    nickTarget.setDisplayName(ChatColor.translateAlternateColorCodes('&', newNick));
                    nickTarget.setPlayerListName(ChatColor.translateAlternateColorCodes('&', newNick));
                    sender.sendMessage(prefix + ChatColor.GREEN + "Nick de " + nickTarget.getName() + " cambiado a " + newNick);
                    if (nickTarget != sender) {
                        nickTarget.sendMessage(prefix + ChatColor.GREEN + "Tu nick ha sido cambiado a " + newNick + " por " + sender.getName());
                    }
                    return true;
                case "iname":
                    if (args.length < 1) {
                        sender.sendMessage(prefix + ChatColor.RED + "Uso correcto: /iname <nuevo-nombre>");
                        return false;
                    }
                    Player inamePlayer = (Player) sender;
                    if (inamePlayer.getInventory().getItemInMainHand() == null || inamePlayer.getInventory().getItemInMainHand().getType().isAir()) {
                        sender.sendMessage(prefix + ChatColor.RED + "No tienes ningún objeto en la mano principal.");
                        return false;
                    }
                    org.bukkit.inventory.ItemStack item = inamePlayer.getInventory().getItemInMainHand();
                    org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
                    String newName = String.join(" ", args);
                    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', newName));
                    item.setItemMeta(meta);
                    sender.sendMessage(prefix + ChatColor.GREEN + "Nombre del objeto cambiado a: " + newName);
                    return true;
                case "ilore":
                    if (args.length < 2) {
                        sender.sendMessage(prefix + ChatColor.RED + "Uso correcto: /ilore <set|add> <línea> <texto> o /ilore add <texto>");
                        return false;
                    }
                    Player ilorePlayer = (Player) sender;
                    if (ilorePlayer.getInventory().getItemInMainHand() == null || ilorePlayer.getInventory().getItemInMainHand().getType().isAir()) {
                        sender.sendMessage(prefix + ChatColor.RED + "No tienes ningún objeto en la mano principal.");
                        return false;
                    }
                    ItemStack loreItem = ilorePlayer.getInventory().getItemInMainHand();
                    org.bukkit.inventory.meta.ItemMeta loreMeta = loreItem.getItemMeta();
                    String action = args[0].toLowerCase();
                    if (action.equals("set") && args.length >= 3) {
                        int line;
                        try {
                            line = Integer.parseInt(args[1]) - 1;
                        } catch (NumberFormatException e) {
                            sender.sendMessage(prefix + ChatColor.RED + "La línea debe ser un número.");
                            return false;
                        }
                        String text = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
                        java.util.List<String> loreList = loreMeta.hasLore() ? loreMeta.getLore() : new java.util.ArrayList<>();
                        while (loreList.size() <= line) loreList.add("");
                        loreList.set(line, ChatColor.translateAlternateColorCodes('&', text));
                        loreMeta.setLore(loreList);
                        sender.sendMessage(prefix + ChatColor.GREEN + "Lore línea " + (line+1) + " establecida.");
                    } else if (action.equals("add")) {
                        String text = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
                        java.util.List<String> loreList = loreMeta.hasLore() ? loreMeta.getLore() : new java.util.ArrayList<>();
                        loreList.add(ChatColor.translateAlternateColorCodes('&', text));
                        loreMeta.setLore(loreList);
                        sender.sendMessage(prefix + ChatColor.GREEN + "Lore añadido al objeto.");
                    } else {
                        sender.sendMessage(prefix + ChatColor.RED + "Uso correcto: /ilore <set|add> <línea> <texto> o /ilore add <texto>");
                        return false;
                    }
                    loreItem.setItemMeta(loreMeta);
                    return true;
                default:
                    sender.sendMessage(prefix + ChatColor.RED + "Comando desconocido.");
                    return false;
            }
        }
    }
}

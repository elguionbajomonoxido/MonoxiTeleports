package me.monoxido;

import me.monoxido.commands.CommandHandler;
import me.monoxido.listener.BlockCmdsListener;
import me.monoxido.listener.MenuListener;
import me.monoxido.listener.MuteChatListener;
import me.monoxido.manager.TeleportEffects;
import me.monoxido.manager.TeleportManager;
import me.monoxido.manager.WarpManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class MonoxiEssentials extends JavaPlugin implements Listener {

    public String version;
    private WarpManager warpManager;
    private TeleportManager teleportManager;
    private TeleportEffects teleportEffects;
    private String prefix;
    private Location spawnLocation;

    public MonoxiEssentials() {
        // Hardcode the version or use another method to set the version
        this.version = "0.5"; // Example of hardcoding the version
        this.prefix = ChatColor.GOLD + "[" + ChatColor.YELLOW + "MonoxiEssentials" + ChatColor.GOLD + "] " + ChatColor.WHITE;
    }

    @Override
    public void onEnable() {
        // Guardar configuración por defecto si no existe
        saveDefaultConfig();

        // Inicializar los managers
        warpManager = new WarpManager(this);
        teleportEffects = new TeleportEffects(this, getConfig());
        teleportManager = new TeleportManager(warpManager, this, teleportEffects, getConfig());

        // Cargar la ubicación del spawn desde el WarpManager
        spawnLocation = warpManager.getSpawn();

        // Asegurarse de que la ubicación de spawn se cargue correctamente
        if (spawnLocation == null) {
            getLogger().warning("Spawn location is not set or could not be loaded.");
        }

        // Registrar eventos y comandos
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new MuteChatListener(), this);
        getServer().getPluginManager().registerEvents(new BlockCmdsListener(), this);
        // Registrar el listener de menús personalizados
        Bukkit.getPluginManager().registerEvents(new MenuListener(), this);
        getCommand("setspawn").setExecutor(new SetSpawnCommand());
        getCommand("spawn").setExecutor(new SpawnCommand());
        getCommand("spawnall").setExecutor(new SpawnAllCommand());

        // Registrar el comando de recarga
        registerReloadCommand();

        // Inicializar el gestor de comandos
        new CommandHandler(this, warpManager, teleportManager);

        // Mensaje de inicio
        Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.GOLD + "=== " + ChatColor.YELLOW + "MonoxiEssentials version: " + version + ChatColor.GOLD + " ===");
        Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.GREEN + "Plugin activado correctamente");
        Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.GREEN + "Desarrollado por Monoxido");
    }

    @Override
    public void onDisable() {
        // Guardar la ubicación del spawn en la configuración
        if (spawnLocation != null) {
            // Guardar la ubicación del spawn en el config.yml
            getConfig().set("spawn.world", spawnLocation.getWorld().getName());
            getConfig().set("spawn.x", spawnLocation.getX());
            getConfig().set("spawn.y", spawnLocation.getY());
            getConfig().set("spawn.z", spawnLocation.getZ());
            getConfig().set("spawn.yaw", spawnLocation.getYaw());
            getConfig().set("spawn.pitch", spawnLocation.getPitch());
            saveConfig();
        }
        // Guardar configuraciones actuales en config.yml
        saveConfig();

        // Guardar warps en la configuración al deshabilitar
        warpManager.saveWarps();
        Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.RED + "Plugin desactivado correctamente");
    }

    /**
     * Comando para recargar la configuración del plugin.
     */
    public void registerReloadCommand() {
        getCommand("me").setExecutor((sender, command, label, args) -> {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                reloadConfig();
                teleportEffects = new TeleportEffects(this, getConfig());
                teleportManager = new TeleportManager(warpManager, this, teleportEffects, getConfig());
                sender.sendMessage(prefix + ChatColor.GREEN + "Configuración recargada correctamente.");
                return true;
            }
            return false;
        });
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPlayedBefore() && spawnLocation != null) {
            player.teleport(spawnLocation);
        }
    }

    private class SetSpawnCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                spawnLocation = player.getLocation();
                sender.sendMessage(prefix + ChatColor.GREEN + "Spawn establecido correctamente.");
                return true;
            }
            sender.sendMessage(prefix + ChatColor.RED + "Este comando solo puede ser ejecutado por jugadores.");
            return false;
        }
    }

    private class SpawnCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (spawnLocation != null) {
                    player.teleport(spawnLocation);
                    sender.sendMessage(prefix + ChatColor.GREEN + "Teletransportado al spawn.");
                } else {
                    sender.sendMessage(prefix + ChatColor.RED + "El spawn no está configurado.");
                }
                return true;
            }
            sender.sendMessage(prefix + ChatColor.RED + "Este comando solo puede ser ejecutado por jugadores.");
            return false;
        }
    }

    private class SpawnAllCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (spawnLocation != null) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.teleport(spawnLocation);
                }
                sender.sendMessage(prefix + ChatColor.GREEN + "Todos los jugadores han sido teletransportados al spawn.");
                return true;
            }
            sender.sendMessage(prefix + ChatColor.RED + "El spawn no está configurado.");
            return false;
        }
    }

    public String getPrefix() {
        return prefix;
    }
}

package me.monoxido;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Administra todas las operaciones de teletransporte del plugin.
 * Esta clase se encarga de gestionar teletransportes entre jugadores, a warps,
 * y almacenar ubicaciones previas para el comando "back".
 */
public class TeleportManager {
    /** Mapa para almacenar la última ubicación de cada jugador */
    private final Map<Player, Location> lastLocation = new HashMap<>();
    /** Gestor de warps para acceder a las ubicaciones guardadas */
    private final WarpManager warpManager;
    /** Logger para registrar errores y advertencias */
    private final Logger logger;
    /** Gestor de efectos visuales para teletransportes */
    private final TeleportEffects teleportEffects;
    /** JavaPlugin principal */
    private final JavaPlugin plugin;
    private final int maxSimultaneousTeleports;
    private final int teleportBatchDelay;
    // Agregar el prefijo para los mensajes
    private static final String prefix = ChatColor.GOLD + "[" + ChatColor.YELLOW + "MonoxiEssentials" + ChatColor.GOLD + "] " + ChatColor.WHITE;
    /**
     * Construye un nuevo gestor de teletransportes.
     *
     * @param warpManager Gestor de warps a utilizar
     */
    public TeleportManager(WarpManager warpManager) {
        this.warpManager = warpManager;
        this.logger = Bukkit.getLogger();
        this.plugin = null;
        this.teleportEffects = null;
        this.maxSimultaneousTeleports = 5;
        this.teleportBatchDelay = 5;
    }

    /**
     * Constructor con soporte completo para logging y efectos visuales.
     *
     * @param warpManager Gestor de warps a utilizar
     * @param plugin Plugin desde el cual obtener el logger
     * @param teleportEffects Gestor de efectos visuales
     */
    public TeleportManager(WarpManager warpManager, JavaPlugin plugin, TeleportEffects teleportEffects, FileConfiguration config) {
        this.warpManager = warpManager;
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.teleportEffects = teleportEffects;
        this.maxSimultaneousTeleports = config.getInt("performance.max_simultaneous_teleports", 5);
        this.teleportBatchDelay = config.getInt("performance.teleport_batch_delay", 5);
    }

    /**
     * Teletransporta a todos los jugadores al jugador que ejecutó el comando.
     * Utiliza teletransporte escalonado para evitar lag en el servidor.
     *
     * @param player Jugador que ejecuta el comando
     * @throws IllegalStateException Si ocurre un error durante el teletransporte
     */
    public void tpAll(Player player) {
        if (player == null) {
            throw new IllegalArgumentException("El jugador no puede ser nulo");
        }

        try {
            final Location targetLocation = player.getLocation();
            List<Player> playersToTeleport = new ArrayList<>();

            // Construir lista de jugadores a teletransportar, excluyendo al que ejecuta el comando
            for (Player targetPlayer : Bukkit.getOnlinePlayers()) {
                if (!targetPlayer.equals(player)) {
                    playersToTeleport.add(targetPlayer);
                }
            }

            // Si hay pocos jugadores, teleportar directamente
            if (playersToTeleport.size() <= maxSimultaneousTeleports) {
                for (Player targetPlayer : playersToTeleport) {
                    teleportPlayerTo(targetPlayer, targetLocation);
                    targetPlayer.sendMessage(player.getName() + " te ha teletransportado hacia él.");
                }
                player.sendMessage(prefix + ChatColor.GREEN + "Has teleportado a todos hacia ti.");
            } else {
                // Teleportar en lotes para prevenir lag
                batchTeleport(playersToTeleport, targetLocation, player.getName() + " te ha teletransportado hacia él.", player);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error al ejecutar el comando tpAll", e);
            player.sendMessage("Error al teletransportar jugadores: " + e.getMessage());
            throw new IllegalStateException("Error al teletransportar a todos los jugadores", e);
        }
    }

    /**
     * Teletransporta a un jugador hacia un warp especificado.
     *
     * @param player Jugador a teletransportar
     * @param warpName Nombre del warp de destino
     * @throws IllegalArgumentException Si el warp no existe o está en otro mundo
     */
    public void teleportToWarp(Player player, String warpName) {
        if (player == null) {
            throw new IllegalArgumentException("El jugador no puede ser nulo");
        }

        if (warpName == null || warpName.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del warp no puede estar vacío");
        }

        try {
            if (warpManager.warpExists(warpName)) {
                Location warpLocation = warpManager.getWarpLocation(warpName);

                if (warpLocation == null || warpLocation.getWorld() == null) {
                    player.sendMessage("El warp existe pero su ubicación es inválida.");
                    return;
                }

                storeLastLocation(player);

                // Aplicar efectos visuales si están disponibles
                if (teleportEffects != null) {
                    teleportEffects.playTeleportEffects(player, warpLocation);
                }

                player.teleport(warpLocation);
                player.sendMessage(prefix + ChatColor.GREEN + "Te has teletransportado a " + ChatColor.YELLOW + warpName + ChatColor.GREEN + ".");
            } else {
                player.sendMessage(prefix + ChatColor.RED + "No existe un warp con ese nombre.");
                throw new IllegalArgumentException("El warp especificado no existe: " + warpName);
            }
        } catch (IllegalArgumentException e) {
            // Estas excepciones ya son gestionadas y el mensaje es enviado al jugador
            logger.log(Level.FINE, "Error controlado al teletransportar al jugador: {0}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error al teletransportar al jugador a un warp", e);
            player.sendMessage("Error al teletransportarte: " + e.getMessage());
            throw new IllegalStateException("Error al teletransportar al jugador al warp", e);
        }
    }

    /**
     * Teletransporta a todos los jugadores a un warp especificado.
     * Utiliza teletransporte escalonado para prevenir lag.
     *
     * @param player Jugador que ejecuta el comando
     * @param warpName Nombre del warp de destino
     * @throws IllegalArgumentException Si el warp no existe o está en otro mundo
     */
    public void warpAll(Player player, String warpName) {
        if (player == null) {
            throw new IllegalArgumentException("El jugador no puede ser nulo");
        }

        if (warpName == null || warpName.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del warp no puede estar vacío");
        }

        try {
            if (warpManager.warpExists(warpName)) {
                final Location warpLocation = warpManager.getWarpLocation(warpName);

                if (warpLocation == null || warpLocation.getWorld() == null) {
                    player.sendMessage("El warp existe pero su ubicación es inválida.");
                    return;
                }

                List<Player> playersToTeleport = new ArrayList<>();

                // Incluir a todos los jugadores en línea, sin importar el mundo
                for (Player targetPlayer : Bukkit.getOnlinePlayers()) {
                    playersToTeleport.add(targetPlayer);
                }

                if (playersToTeleport.isEmpty()) {
                    player.sendMessage("No hay jugadores para teletransportar.");
                    return;
                }

                // Si hay pocos jugadores, teleportar directamente
                if (playersToTeleport.size() <= maxSimultaneousTeleports) {
                    for (Player targetPlayer : playersToTeleport) {
                        teleportPlayerTo(targetPlayer, warpLocation);
                        targetPlayer.sendMessage(player.getName() + " te ha teletransportado a " + warpName + ".");
                    }
                    player.sendMessage(prefix + ChatColor.GREEN + "Has teleportado a todos a " + ChatColor.YELLOW + warpName + ChatColor.GREEN + ".");
                } else {
                    // Teleportar en lotes para prevenir lag
                    batchTeleport(playersToTeleport, warpLocation,
                            player.getName() + " te ha teletransportado a " + warpName + ".", player);
                }
            } else {
                player.sendMessage("No existe un warp con ese nombre.");
                throw new IllegalArgumentException("El warp especificado no existe: " + warpName);
            }
        } catch (IllegalArgumentException e) {
            // Estas excepciones ya son gestionadas y el mensaje es enviado al jugador
            logger.log(Level.FINE, "Error controlado al teletransportar a todos los jugadores: {0}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error al teletransportar a todos los jugadores a un warp", e);
            player.sendMessage("Error al teletransportar a todos: " + e.getMessage());
            throw new IllegalStateException("Error al teletransportar a todos los jugadores al warp", e);
        }
    }

    /**
     * Teletransporta al jugador a su última ubicación registrada.
     *
     * @param player Jugador a teletransportar
     * @throws IllegalStateException Si no hay ubicación previa guardada
     */
    public void teleportBack(Player player) {
        if (player == null) {
            throw new IllegalArgumentException("El jugador no puede ser nulo");
        }

        try {
            Location lastLoc = lastLocation.get(player);
            if (lastLoc != null) {
                if (lastLoc.getWorld() == null) {
                    player.sendMessage("Tu ubicación anterior es inválida.");
                    return;
                }

                // Guardar ubicación actual antes de teletransportar de vuelta
                Location currentLocation = player.getLocation();

                // Aplicar efectos visuales si están disponibles
                if (teleportEffects != null) {
                    teleportEffects.playTeleportEffects(player, lastLoc);
                }

                player.teleport(lastLoc);

                // Actualizar la última ubicación para poder volver a la posición actual
                lastLocation.put(player, currentLocation);

                player.sendMessage(prefix + ChatColor.GREEN + "Te has teletransportado de vuelta a tu última ubicación.");
            } else {
                player.sendMessage(prefix + ChatColor.RED + "No tienes una ubicación registrada para retornar.");
                throw new IllegalStateException("No hay ubicación previa registrada para el jugador");
            }
        } catch (IllegalStateException e) {
            // Esta excepción ya es gestionada y el mensaje es enviado al jugador
            logger.log(Level.FINE, "El jugador no tiene ubicación previa: {0}", player.getName());
            throw e;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error al teletransportar de vuelta al jugador", e);
            player.sendMessage(prefix + "Error al teletransportarte de vuelta: " + e.getMessage());
            throw new IllegalStateException("Error al teletransportar al jugador a su ubicación anterior", e);
        }
    }

    /**
     * Guarda la ubicación actual del jugador para futuros comandos "back" y para offline (/tpo).
     *
     * @param player Jugador cuya ubicación se guardará
     */
    public void storeLastLocation(Player player) {
        if (player == null) {
            throw new IllegalArgumentException("El jugador no puede ser nulo");
        }

        try {
            lastLocation.put(player, player.getLocation());
            // Guardar en config para soporte offline
            FileConfiguration config = plugin.getConfig();
            String path = "lastlocations." + player.getName().toLowerCase();
            Location loc = player.getLocation();
            config.set(path + ".world", loc.getWorld().getName());
            config.set(path + ".x", loc.getX());
            config.set(path + ".y", loc.getY());
            config.set(path + ".z", loc.getZ());
            config.set(path + ".yaw", loc.getYaw());
            config.set(path + ".pitch", loc.getPitch());
            plugin.saveConfig();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error al guardar la ubicación del jugador", e);
        }
    }

    /**
     * Limpia la última ubicación guardada para un jugador.
     * Útil cuando un jugador se desconecta para liberar memoria.
     *
     * @param player Jugador cuya ubicación se eliminará
     */
    public void clearLastLocation(Player player) {
        if (player == null) {
            return;
        }

        lastLocation.remove(player);
    }

    /**
     * Teletransporta a un jugador a una ubicación específica con efectos.
     *
     * @param player Jugador a teletransportar
     * @param location Ubicación destino
     */
    private void teleportPlayerTo(Player player, Location location) {
        storeLastLocation(player);

        if (teleportEffects != null) {
            teleportEffects.playLightweightTeleportEffects(player, location);
        }

        player.teleport(location);
    }

    /**
     * Teletransporta jugadores en lotes para evitar lag en el servidor.
     *
     * @param players Lista de jugadores a teleportar
     * @param destination Ubicación destino
     * @param message Mensaje a mostrar a cada jugador
     * @param initiator Jugador que inició el teletransporte
     */
    private void batchTeleport(List<Player> players, Location destination, String message, Player initiator) {
        final int totalPlayers = players.size();
        final int batches = (int) Math.ceil((double) totalPlayers / maxSimultaneousTeleports);

        new BukkitRunnable() {
            int currentBatch = 0;
            int processed = 0;

            @Override
            public void run() {
                if (currentBatch >= batches) {
                    initiator.sendMessage("Teletransporte masivo completado.");
                    this.cancel();
                    return;
                }

                int start = currentBatch * maxSimultaneousTeleports;
                int end = Math.min(start + maxSimultaneousTeleports, totalPlayers);

                for (int i = start; i < end; i++) {
                    Player player = players.get(i);
                    if (player.isOnline()) {
                        teleportPlayerTo(player, destination);
                        player.sendMessage(message);
                        processed++;
                    }
                }

                currentBatch++;
                initiator.sendMessage("Teletransporte: " + processed + "/" + totalPlayers + " jugadores procesados...");
            }
        }.runTaskTimer(plugin, 0L, teleportBatchDelay);
    }

    public void teleportToSpawn(Player player) {
        Location spawn = warpManager.getSpawn();
        if (spawn != null) {
            player.teleport(spawn);
            player.sendMessage(prefix + ChatColor.GREEN + "Teletransportado al spawn.");
        } else {
            player.sendMessage(prefix + ChatColor.RED + "El spawn no está configurado.");
        }
    }

    public void teleportAllToSpawn(CommandSender sender) {
        Location spawn = warpManager.getSpawn();
        if (spawn != null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.teleport(spawn);
                player.sendMessage(prefix + ChatColor.GREEN + "Has sido teletransportado al spawn por " + sender.getName() + ".");
            }
            sender.sendMessage(prefix + ChatColor.GREEN + "Todos los jugadores han sido teletransportados al spawn.");
        } else {
            sender.sendMessage(prefix + ChatColor.RED + "El spawn no está configurado.");
        }
    }
}

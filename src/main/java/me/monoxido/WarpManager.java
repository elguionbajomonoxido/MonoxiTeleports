package me.monoxido;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Administra la creación, eliminación y gestión de los warps del servidor.
 * Esta clase maneja todas las operaciones relacionadas con warps, incluyendo:
 * carga/guardado desde/hacia archivos de configuración, permisos, y manipulación de warps.
 */
public class WarpManager {
    private final Map<String, Location> warps = new HashMap<>();
    private final JavaPlugin plugin;
    private final String prefix;
    private Location spawnLocation;
    private String mysqlHost;
    private String mysqlDb;
    private String mysqlUser;
    private String mysqlPass;
    private int mysqlPort;
    private String mysqlUrl;
    /** Caché para los warps más utilizados */
    private final Map<String, CachedWarp> warpCache = new HashMap<>();
    /** Tamaño máximo para la caché */
    private static final int MAX_CACHE_SIZE = 10;

    /**
     * Construye un nuevo gestor de warps.
     *
     * @param plugin La instancia principal del plugin
     */
    public WarpManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.prefix = ((MonoxiEssentials) plugin).getPrefix();
        // Inicializar variables MySQL después de asignar plugin
        this.mysqlHost = plugin.getConfig().getString("mysql.host", "localhost");
        this.mysqlDb = plugin.getConfig().getString("mysql.database", "monoxi");
        this.mysqlUser = plugin.getConfig().getString("mysql.user", "root");
        this.mysqlPass = plugin.getConfig().getString("mysql.password", "");
        this.mysqlPort = plugin.getConfig().getInt("mysql.port", 3306);
        this.mysqlUrl = "jdbc:mysql://" + mysqlHost + ":" + mysqlPort + "/" + mysqlDb + "?useSSL=false&autoReconnect=true";
        loadWarps();
        createLastLocationTable();
    }

    /**
     * Carga los warps desde el archivo de configuración del plugin.
     * Este método lee todos los warps guardados y los almacena en memoria.
     *
     * @throws IllegalStateException Si ocurre un error al cargar los warps
     */
    public void loadWarps() {
        try {
            FileConfiguration config = plugin.getConfig();
            if (config.contains("warps")) {
                ConfigurationSection section = config.getConfigurationSection("warps");
                if (section != null) {
                    for (String warpName : section.getKeys(false)) {
                        String worldName = config.getString("warps." + warpName + ".world");
                        double x = config.getDouble("warps." + warpName + ".x");
                        double y = config.getDouble("warps." + warpName + ".y");
                        double z = config.getDouble("warps." + warpName + ".z");

                        if (worldName == null || Bukkit.getWorld(worldName) == null) {
                            // Agregar colores a los mensajes del logger
                            plugin.getLogger().warning(prefix + ChatColor.RED + "No se pudo cargar el warp " + warpName + ": mundo no encontrado");
                            continue;
                        }

                        Location location = new Location(Bukkit.getWorld(worldName), x, y, z);
                        warps.put(warpName, location);
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe(prefix + ChatColor.RED + "Error al cargar warps: " + e.getMessage());
            throw new IllegalStateException("No se pudieron cargar los warps correctamente", e);
        }
    }

    /**
     * Guarda todos los warps actuales en el archivo de configuración.
     *
     * @throws IllegalStateException Si ocurre un error al guardar los warps
     */
    public void saveWarps() {
        try {
            FileConfiguration config = plugin.getConfig();
            for (String warpName : warps.keySet()) {
                Location location = warps.get(warpName);
                config.set("warps." + warpName + ".world", location.getWorld().getName());
                config.set("warps." + warpName + ".x", location.getX());
                config.set("warps." + warpName + ".y", location.getY());
                config.set("warps." + warpName + ".z", location.getZ());
            }
            plugin.saveConfig();
        } catch (Exception e) {
            plugin.getLogger().severe(prefix + ChatColor.RED + "Error al guardar warps: " + e.getMessage());
            throw new IllegalStateException("No se pudieron guardar los warps correctamente", e);
        }
    }

    /**
     * Muestra al jugador una lista de todos los warps disponibles.
     *
     * @param player El jugador que solicita ver la lista de warps
     */
    public void listWarps(Player player) {
        if (warps.isEmpty()) {
            player.sendMessage(prefix + ChatColor.RED + "No hay warps disponibles.");
            return;
        }
        player.sendMessage(prefix + ChatColor.GOLD + "Warps disponibles:");
        for (String warpName : warps.keySet()) {
            player.sendMessage(ChatColor.YELLOW + "- " + warpName);
        }
    }

    /**
     * Crea un nuevo warp en la ubicación actual del jugador.
     *
     * @param player El jugador que crea el warp
     * @param warpName El nombre del nuevo warp
     * @throws IllegalArgumentException Si el nombre del warp es inválido
     */
    public void setWarp(Player player, String warpName) {
        if (warpName == null || warpName.trim().isEmpty()) {
            player.sendMessage(prefix + ChatColor.RED + "El nombre del warp no puede estar vacío");
            return;
        }

        try {
            Location location = player.getLocation();
            warps.put(warpName, location);

            // Actualizar la caché si existe
            if (warpCache.containsKey(warpName)) {
                warpCache.put(warpName, new CachedWarp(location, 0));
            }

            FileConfiguration config = plugin.getConfig();
            config.set("warps." + warpName + ".world", location.getWorld().getName());
            config.set("warps." + warpName + ".x", location.getX());
            config.set("warps." + warpName + ".y", location.getY());
            config.set("warps." + warpName + ".z", location.getZ());
            plugin.saveConfig();
            player.sendMessage(prefix + ChatColor.GREEN + "Warp " + ChatColor.YELLOW + warpName + ChatColor.GREEN + " creado correctamente.");
        } catch (Exception e) {
            plugin.getLogger().warning(prefix + ChatColor.RED + "Error al crear warp " + warpName + ": " + e.getMessage());
            player.sendMessage(prefix + ChatColor.RED + "Error al crear el warp: " + e.getMessage());
        }
    }

    /**
     * Elimina un warp existente.
     *
     * @param player El jugador que intenta eliminar el warp
     * @param warpName El nombre del warp a eliminar
     */
    public void delWarp(Player player, String warpName) {
        // Verificar si el jugador tiene permiso para eliminar warps
        if (!player.hasPermission("monoxi.teleports.delwarp")) {
            player.sendMessage(prefix + ChatColor.RED + "No tienes permiso para usar este comando.");
            return;
        }

        try {
            // Verificar si el warp existe
            if (warps.containsKey(warpName)) {
                // Eliminar el warp de la lista de warps
                warps.remove(warpName);

                // Eliminar de la caché si existe
                warpCache.remove(warpName);

                // Eliminar el warp de la configuración
                plugin.getConfig().set("warps." + warpName, null);
                plugin.saveConfig();

                // Confirmar que el warp ha sido eliminado
                player.sendMessage(prefix + ChatColor.GREEN + "El warp " + ChatColor.YELLOW + warpName + ChatColor.GREEN + " ha sido eliminado.");
            } else {
                // Si no existe el warp
                player.sendMessage(prefix + ChatColor.RED + "No existe un warp con ese nombre.");
            }
        } catch (Exception e) {
            plugin.getLogger().warning(prefix + ChatColor.RED + "Error al eliminar warp " + warpName + ": " + e.getMessage());
            player.sendMessage(prefix + ChatColor.RED + "Error al eliminar el warp: " + e.getMessage());
        }
    }

    /**
     * Obtiene la ubicación de un warp especificado.
     * Utiliza sistema de caché para warps frecuentemente utilizados.
     *
     * @param warpName El nombre del warp
     * @return La ubicación del warp o null si no existe
     */
    public Location getWarpLocation(String warpName) {
        // Verificar primero en la caché
        if (warpCache.containsKey(warpName)) {
            CachedWarp cachedWarp = warpCache.get(warpName);
            cachedWarp.incrementUseCount();
            return cachedWarp.getLocation();
        }

        // Si no está en caché, buscar en la colección principal
        Location location = warps.get(warpName);

        // Si se encontró, agregarlo a la caché
        if (location != null) {
            addToCache(warpName, location);
        }

        return location;
    }

    /**
     * Verifica si existe un warp con el nombre especificado.
     *
     * @param warpName El nombre del warp a verificar
     * @return true si el warp existe, false en caso contrario
     */
    public boolean warpExists(String warpName) {
        return warps.containsKey(warpName);
    }

    /**
     * Obtiene los nombres de todos los warps disponibles.
     *
     * @return Conjunto con los nombres de todos los warps
     */
    public Set<String> getWarpNames() {
        return warps.keySet();
    }

    /**
     * Agrega un warp a la caché.
     * Si la caché está llena, elimina el warp menos utilizado.
     *
     * @param warpName El nombre del warp
     * @param location La ubicación del warp
     */
    private void addToCache(String warpName, Location location) {
        // Si la caché está llena, eliminar el menos usado
        if (warpCache.size() >= MAX_CACHE_SIZE) {
            String leastUsed = null;
            int minUses = Integer.MAX_VALUE;

            for (Map.Entry<String, CachedWarp> entry : warpCache.entrySet()) {
                if (entry.getValue().getUseCount() < minUses) {
                    minUses = entry.getValue().getUseCount();
                    leastUsed = entry.getKey();
                }
            }

            if (leastUsed != null) {
                warpCache.remove(leastUsed);
            }
        }

        // Agregar el nuevo warp a la caché
        warpCache.put(warpName, new CachedWarp(location, 1));
    }

    /**
     * Clase interna para representar un warp en caché con su contador de uso.
     */
    private static class CachedWarp {
        private final Location location;
        private int useCount;

        /**
         * Crea un nuevo warp en caché.
         *
         * @param location La ubicación del warp
         * @param useCount El contador inicial de uso
         */
        public CachedWarp(Location location, int useCount) {
            this.location = location;
            this.useCount = useCount;
        }

        /**
         * Obtiene la ubicación del warp.
         *
         * @return La ubicación del warp
         */
        public Location getLocation() {
            return location;
        }

        /**
         * Obtiene el contador de uso del warp.
         *
         * @return El número de veces que se ha utilizado este warp
         */
        public int getUseCount() {
            return useCount;
        }

        /**
         * Incrementa el contador de uso del warp.
         */
        public void incrementUseCount() {
            useCount++;
        }
    }

    public void setSpawn(Location location) {
        this.spawnLocation = location;
        FileConfiguration config = plugin.getConfig();
        config.set("spawn.world", location.getWorld().getName());
        config.set("spawn.x", location.getX());
        config.set("spawn.y", location.getY());
        config.set("spawn.z", location.getZ());
        config.set("spawn.yaw", location.getYaw());
        config.set("spawn.pitch", location.getPitch());
        plugin.saveConfig();
    }

    public Location getSpawn() {
        if (spawnLocation == null) {
            FileConfiguration config = plugin.getConfig();
            String worldName = config.getString("spawn.world");
            if (worldName != null && Bukkit.getWorld(worldName) != null) {
                spawnLocation = new Location(
                    Bukkit.getWorld(worldName),
                    config.getDouble("spawn.x"),
                    config.getDouble("spawn.y"),
                    config.getDouble("spawn.z"),
                    (float) config.getDouble("spawn.yaw"),
                    (float) config.getDouble("spawn.pitch")
                );
            } else {
                plugin.getLogger().warning("El mundo especificado para el spawn no existe o no está cargado.");
            }
        }
        return spawnLocation;
    }

    // Método para obtener la conexión a la base de datos MySQL
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(mysqlUrl, mysqlUser, mysqlPass);
    }

    // Crear tabla si no existe (llamar en el constructor)
    private void createLastLocationTable() {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "CREATE TABLE IF NOT EXISTS lastlocations ("
                + "player VARCHAR(40) PRIMARY KEY,"
                + "world VARCHAR(40), x DOUBLE, y DOUBLE, z DOUBLE, yaw FLOAT, pitch FLOAT)")
        ) {
            ps.execute();
        } catch (SQLException e) {
            plugin.getLogger().warning(prefix + ChatColor.RED + "No se pudo crear la tabla lastlocations en MySQL: " + e.getMessage());
        }
    }

    // Guardar lastlocation en MySQL
    public void storeLastLocation(Player player) {
        if (player == null) return;
        Location loc = player.getLocation();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "REPLACE INTO lastlocations (player, world, x, y, z, yaw, pitch) VALUES (?, ?, ?, ?, ?, ?, ?)")
        ) {
            ps.setString(1, player.getName().toLowerCase());
            ps.setString(2, loc.getWorld().getName());
            ps.setDouble(3, loc.getX());
            ps.setDouble(4, loc.getY());
            ps.setDouble(5, loc.getZ());
            ps.setFloat(6, loc.getYaw());
            ps.setFloat(7, loc.getPitch());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning(prefix + ChatColor.RED + "No se pudo guardar lastlocation en MySQL: " + e.getMessage());
            // Fallback: guardar en config
            FileConfiguration config = plugin.getConfig();
            String path = "lastlocations." + player.getName().toLowerCase();
            config.set(path + ".world", loc.getWorld().getName());
            config.set(path + ".x", loc.getX());
            config.set(path + ".y", loc.getY());
            config.set(path + ".z", loc.getZ());
            config.set(path + ".yaw", loc.getYaw());
            config.set(path + ".pitch", loc.getPitch());
            plugin.saveConfig();
        }
    }

    // Obtener lastlocation desde MySQL
    public Location getLastLocation(String playerName) {
        if (playerName == null || playerName.isEmpty()) return null;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "SELECT world, x, y, z, yaw, pitch FROM lastlocations WHERE player = ?")
        ) {
            ps.setString(1, playerName.toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String worldName = rs.getString("world");
                    double x = rs.getDouble("x");
                    double y = rs.getDouble("y");
                    double z = rs.getDouble("z");
                    float yaw = rs.getFloat("yaw");
                    float pitch = rs.getFloat("pitch");
                    if (worldName == null || Bukkit.getWorld(worldName) == null) return null;
                    return new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning(prefix + ChatColor.RED + "No se pudo leer lastlocation de MySQL: " + e.getMessage());
            // Fallback: leer de config
            FileConfiguration config = plugin.getConfig();
            String path = "lastlocations." + playerName.toLowerCase();
            if (!config.contains(path + ".world")) return null;
            String worldName = config.getString(path + ".world");
            double x = config.getDouble(path + ".x");
            double y = config.getDouble(path + ".y");
            double z = config.getDouble(path + ".z");
            float yaw = (float) config.getDouble(path + ".yaw");
            float pitch = (float) config.getDouble(path + ".pitch");
            if (worldName == null || Bukkit.getWorld(worldName) == null) return null;
            return new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
        }
        return null;
    }
}

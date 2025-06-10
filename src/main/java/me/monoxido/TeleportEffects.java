package me.monoxido;

import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Clase para manejar efectos visuales y sonoros durante los teletransportes.
 * Proporciona métodos para crear efectos en la ubicación del jugador
 * antes y después de teletransportarse.
 */
public class TeleportEffects {

    private final JavaPlugin plugin;
    private boolean enableEffects;
    private boolean enableSounds;

    /**
     * Construye una nueva instancia de TeleportEffects.
     *
     * @param plugin La instancia principal del plugin
     */
    public TeleportEffects(JavaPlugin plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.enableEffects = config.getBoolean("effects.enable_particles", true);
        this.enableSounds = config.getBoolean("effects.enable_sounds", true);
    }

    /**
     * Establece si los efectos visuales están habilitados.
     *
     * @param enableEffects true para habilitar, false para deshabilitar
     */
    public void setEnableEffects(boolean enableEffects) {
        this.enableEffects = enableEffects;
    }

    /**
     * Establece si los efectos de sonido están habilitados.
     *
     * @param enableSounds true para habilitar, false para deshabilitar
     */
    public void setEnableSounds(boolean enableSounds) {
        this.enableSounds = enableSounds;
    }

    /**
     * Ejecuta efectos visuales y sonoros en la ubicación de origen
     * y destino del teletransporte.
     *
     * @param player El jugador que se teletransporta
     * @param destinationLocation La ubicación de destino
     */
    public void playTeleportEffects(Player player, Location destinationLocation) {
        if (!enableEffects && !enableSounds) return;

        // Guardar la ubicación de origen
        final Location originLocation = player.getLocation().clone();

        // Ejecutar efectos antes del teletransporte
        playDepartureEffects(originLocation);

        // Ejecutar efectos después del teletransporte, ligeramente retrasados
        new BukkitRunnable() {
            @Override
            public void run() {
                playArrivalEffects(destinationLocation);
            }
        }.runTaskLater(plugin, 2L); // 2 ticks = 0.1 segundos
    }

    /**
     * Ejecuta efectos visuales cuando un jugador se teletransporta desde una ubicación.
     *
     * @param location La ubicación de origen
     */
    private void playDepartureEffects(Location location) {
        World world = location.getWorld();

        if (enableEffects) {
            // Efecto de círculo de partículas expandiéndose
            for (int i = 0; i < 2; i++) {
                final int iteration = i;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        double radius = 0.8 + (iteration * 0.5);
                        for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 8) {
                            double x = Math.cos(angle) * radius;
                            double z = Math.sin(angle) * radius;
                            Location particleLoc = location.clone().add(x, 0.1, z);
                            world.spawnParticle(Particle.PORTAL, particleLoc, 5, 0.05, 0.05, 0.05, 0.05);
                        }
                    }
                }.runTaskLater(plugin, i * 3L);
            }

            // Efecto de humo
            world.playEffect(location, Effect.SMOKE, 0);
        }

        if (enableSounds) {
            // Sonido de teletransporte de salida
            world.playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1.0f);
        }
    }

    /**
     * Ejecuta efectos visuales cuando un jugador llega a una ubicación.
     *
     * @param location La ubicación de destino
     */
    private void playArrivalEffects(Location location) {
        World world = location.getWorld();

        if (enableEffects) {
            // Efecto de partículas en espiral hacia arriba
            for (int i = 0; i < 3; i++) {
                final int iteration = i;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        double y = iteration * 0.3;
                        for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 8) {
                            double radius = 0.8 - (iteration * 0.2);
                            double x = Math.cos(angle) * radius;
                            double z = Math.sin(angle) * radius;
                            Location particleLoc = location.clone().add(x, y, z);
                            world.spawnParticle(Particle.END_ROD, particleLoc, 1, 0, 0, 0, 0);
                        }
                    }
                }.runTaskLater(plugin, i * 2L);
            }
        }

        if (enableSounds) {
            // Sonido de teletransporte de llegada
            world.playSound(location, Sound.BLOCK_BEACON_POWER_SELECT, 0.5f, 1.2f);
        }
    }

    /**
     * Método especial para teleportaciones masivas que utiliza menos partículas
     * para evitar lag cuando muchos jugadores se teletransportan a la vez.
     *
     * @param player El jugador que se teletransporta
     * @param destinationLocation La ubicación de destino
     */
    public void playLightweightTeleportEffects(Player player, Location destinationLocation) {
        if (!enableEffects && !enableSounds) return;

        World world = player.getWorld();
        Location originLocation = player.getLocation();

        if (enableEffects) {
            // Efecto mínimo de partículas
            world.spawnParticle(Particle.PORTAL, originLocation.clone().add(0, 1, 0), 10, 0.3, 0.5, 0.3, 0.05);

            // Efecto mínimo en el destino, ligeramente retrasado
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (destinationLocation.getWorld() != null) {
                        destinationLocation.getWorld().spawnParticle(Particle.END_ROD,
                                destinationLocation.clone().add(0, 1, 0), 10, 0.3, 0.5, 0.3, 0.05);
                    }
                }
            }.runTaskLater(plugin, 2L);
        }

        if (enableSounds) {
            // Sonidos simples
            world.playSound(originLocation, Sound.ENTITY_ENDERMAN_TELEPORT, 0.3f, 1.5f);

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (destinationLocation.getWorld() != null) {
                        destinationLocation.getWorld().playSound(destinationLocation,
                                Sound.ENTITY_ENDERMAN_TELEPORT, 0.3f, 1.2f);
                    }
                }
            }.runTaskLater(plugin, 2L);
        }
    }
}

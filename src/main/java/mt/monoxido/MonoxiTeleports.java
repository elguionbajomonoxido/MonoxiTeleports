package mt.monoxido;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.ChatColor;

public final class MonoxiTeleports extends JavaPlugin {
    PluginDescriptionFile pdfFile = this.getDescription();
    public String version;
    public String version2;
    private WarpManager warpManager;
    private TeleportManager teleportManager;
    private WarpGUI warpGUI;
    private CommandHandler commandHandler;
    private TeleportEffects teleportEffects;
    private String prefix;

    public MonoxiTeleports() {
        this.version = this.pdfFile.getVersion();
        this.prefix = ChatColor.GOLD + "[" + ChatColor.YELLOW + "MonoxiTeleports" + ChatColor.GOLD + "] " + ChatColor.RESET;
    }

    @Override
    public void onEnable() {
        // Guardar configuración por defecto si no existe
        saveDefaultConfig();
        
        // Inicializar los managers
        warpManager = new WarpManager(this);
        teleportEffects = new TeleportEffects(this);
        teleportManager = new TeleportManager(warpManager, this, teleportEffects);
        warpGUI = new WarpGUI(warpManager, teleportManager);
        
        // Inicializar el gestor de comandos
        commandHandler = new CommandHandler(this, warpManager, teleportManager, warpGUI);

        // Registrar los eventos para la GUI
        getServer().getPluginManager().registerEvents(warpGUI, this);
        
        // Mensaje de inicio
        getLogger().info(ChatColor.GOLD + "=== " + ChatColor.YELLOW + "MonoxiTeleports version: " + version + ChatColor.GOLD + " ===");
        getLogger().info(ChatColor.GREEN + "Plugin activado correctamente");
        getLogger().info(ChatColor.GREEN + "Desarrollado por Monoxido");
    }

    @Override
    public void onDisable() {
        // Guardar warps en la configuración al deshabilitar
        warpManager.saveWarps();
        getLogger().info(ChatColor.RED + "Plugin desactivado correctamente");
    }

    public String getPrefix() {
        return prefix;
    }
}

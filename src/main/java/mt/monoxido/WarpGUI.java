package mt.monoxido;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class WarpGUI implements Listener {
    private final WarpManager warpManager;
    private final TeleportManager teleportManager;
    private static final String INVENTORY_TITLE = "Selecciona un Warp";

    public WarpGUI(WarpManager warpManager, TeleportManager teleportManager) {
        this.warpManager = warpManager;
        this.teleportManager = teleportManager;
    }

    // Método para abrir el menú gráfico de warps
    public void openWarpMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 9, INVENTORY_TITLE);

        for (String warpName : warpManager.getWarpNames()) {
            ItemStack warpItem = new ItemStack(Material.PAPER);
            ItemMeta meta = warpItem.getItemMeta();
            meta.setDisplayName(warpName);
            warpItem.setItemMeta(meta);

            inventory.addItem(warpItem);
        }

        player.openInventory(inventory);
    }

    // Manejo del clic en el inventario (GUI)
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryView view = event.getView();
        String inventoryTitle = view.getTitle();
        if (INVENTORY_TITLE.equals(inventoryTitle)) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem != null && clickedItem.getType() == Material.PAPER) {
                String warpName = clickedItem.getItemMeta().getDisplayName();
                if (warpManager.warpExists(warpName)) {
                    teleportManager.teleportToWarp(player, warpName);
                } else {
                    player.sendMessage("Este warp no existe.");
                }
            }
        }
    }
} 
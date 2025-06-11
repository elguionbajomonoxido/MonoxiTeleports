package me.monoxido.listener;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MenuListener implements Listener {
    public static class CustomMenuHolder implements org.bukkit.inventory.InventoryHolder {
        private final String id;
        public CustomMenuHolder(String id) { this.id = id; }
        public String getId() { return id; }
        @Override public org.bukkit.inventory.Inventory getInventory() { return null; }
    }
    public static final String ENCHANT_MENU_ID = "enchant_menu";

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        org.bukkit.inventory.Inventory inv = event.getInventory();
        org.bukkit.inventory.InventoryHolder holder = inv.getHolder();
        // ENCHANT GUI
        if (holder instanceof CustomMenuHolder && ENCHANT_MENU_ID.equals(((CustomMenuHolder) holder).getId())) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() != Material.ENCHANTED_BOOK) return;
            ItemMeta meta = clicked.getItemMeta();
            if (meta == null || !meta.hasDisplayName()) return;
            String enchKey = ChatColor.stripColor(meta.getDisplayName());
            Enchantment ench = null;
            for (Enchantment e : Enchantment.values()) {
                if (e.getKey().getKey().equalsIgnoreCase(enchKey)) {
                    ench = e;
                    break;
                }
            }
            if (ench == null) {
                player.sendMessage(ChatColor.RED + "Error: Encantamiento no encontrado.");
                return;
            }
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item == null || item.getType().isAir()) {
                player.sendMessage(ChatColor.RED + "No tienes ningún objeto en la mano principal.");
                return;
            }
            item.addUnsafeEnchantment(ench, 255);
            player.sendMessage(ChatColor.GREEN + "Encantamiento " + ench.getKey().getKey() + " nivel 255 añadido al objeto en mano.");
            player.closeInventory();
            return;
        }
    }

    // El método enchantgui ya no es usado, puedes eliminarlo si no se usa en otros lados.
}

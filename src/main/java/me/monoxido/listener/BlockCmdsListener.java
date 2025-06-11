package me.monoxido.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class BlockCmdsListener implements Listener {
    private static boolean blocked = false;
    public static boolean toggleBlock() { blocked = !blocked; return blocked; }
    public static boolean isBlocked() { return blocked; }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (blocked && !event.getPlayer().isOp()) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cEl uso de comandos está bloqueado por un administrador.");
        }
    }
}

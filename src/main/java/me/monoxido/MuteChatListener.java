package me.monoxido;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class MuteChatListener implements Listener {
    private static boolean muted = false;
    private static String mutedBy = "";
    private static String muteReason = "";
    private static long muteEnd = 0L;

    public static boolean toggleMute() { muted = !muted; return muted; }
    public static boolean isMuted() { return muted; }
    public static void mute(String by, int seconds, String reason) {
        muted = true;
        mutedBy = by;
        muteReason = reason;
        muteEnd = (seconds > 0) ? (System.currentTimeMillis() + seconds * 1000L) : 0L;
    }
    public static void unmute() {
        muted = false;
        mutedBy = "";
        muteReason = "";
        muteEnd = 0L;
    }
    public static String getMutedBy() { return mutedBy; }
    public static String getMuteReason() { return muteReason; }
    public static int getMuteSeconds() {
        if (muteEnd == 0L) return 0;
        long left = (muteEnd - System.currentTimeMillis()) / 1000L;
        return (int) Math.max(left, 0);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (muted && !event.getPlayer().isOp()) {
            if (muteEnd > 0 && System.currentTimeMillis() > muteEnd) {
                unmute();
                return;
            }
            event.setCancelled(true);
            String msg = "§cEl chat está muteado por un administrador.";
            if (!muteReason.isEmpty()) msg += " Motivo: " + muteReason;
            if (getMuteSeconds() > 0) msg += " Tiempo restante: " + getMuteSeconds() + "s";
            event.getPlayer().sendMessage(msg);
        }
    }
}

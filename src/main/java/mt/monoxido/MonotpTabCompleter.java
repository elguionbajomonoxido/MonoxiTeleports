package mt.monoxido;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class MonotpTabCompleter implements TabCompleter {
    private final WarpManager warpManager;

    public MonotpTabCompleter(WarpManager warpManager) {
        this.warpManager = warpManager;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> suggestions = new ArrayList<>();

        // Verificar si el jugador está escribiendo el primer argumento de monotp
        if (args.length == 1) {
            if ("tpall".startsWith(args[0].toLowerCase())) {
                suggestions.add("tpall");
            }
            if ("warp".startsWith(args[0].toLowerCase())) {
                suggestions.add("warp");
            }
            if ("warpall".startsWith(args[0].toLowerCase())) {
                suggestions.add("warpall");
            }
            if ("back".startsWith(args[0].toLowerCase())) {
                suggestions.add("back");
            }
        }

        // Verificar si el jugador está escribiendo el segundo argumento para "warp" o "warpall"
        if (args.length == 2) {
            if ("warp".equalsIgnoreCase(args[0]) || "warpall".equalsIgnoreCase(args[0])) {
                for (String warpName : warpManager.getWarpNames()) {
                    if (warpName.startsWith(args[1].toLowerCase())) {
                        suggestions.add(warpName);
                    }
                }
            }
        }

        return suggestions;
    }
} 
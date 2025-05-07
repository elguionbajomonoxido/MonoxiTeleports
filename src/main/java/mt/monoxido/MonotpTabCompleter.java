package mt.monoxido;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class MonotpTabCompleter implements TabCompleter {
    private final WarpManager warpManager;

    // Constructor para inicializar WarpManager
    public MonotpTabCompleter(WarpManager warpManager) {
        this.warpManager = warpManager;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> suggestions = new ArrayList<>();
        String commandName = command.getName().toLowerCase();

        if (args.length == 1) {
            switch (commandName) {
                case "tpall":
                case "back":
                case "warps":
                    // No specific tab completion for these commands
                    break;
                case "warp":
                case "warpall":
                case "delwarp":
                    // Suggest warp names
                    for (String warpName : warpManager.getWarpNames()) {
                        if (warpName.toLowerCase().startsWith(args[0].toLowerCase())) {
                            suggestions.add(warpName);
                        }
                    }
                    break;
                case "setwarp":
                    // Suggest nothing for setwarp (user provides a new name)
                    break;
            }
        }

        return suggestions;
    }
}

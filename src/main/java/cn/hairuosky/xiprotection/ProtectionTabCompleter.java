package cn.hairuosky.xiprotection;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class ProtectionTabCompleter implements TabCompleter {

    private final XiProtection plugin;

    public ProtectionTabCompleter(XiProtection plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            // 第一个参数: set 或 reload
            suggestions.add("reload");
            suggestions.add("set");
            suggestions.add("editor");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            // 第二个参数: 世界名
            for (World world : Bukkit.getWorlds()) {
                suggestions.add(world.getName());
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            // 第三个参数: 设置项
            String[] settings = {
                    "anti-break", "anti-place", "always-sun", "always-rain",
                    "always-day", "always-night", "anti-fire", "anti-shear",
                    "keep-full-hunger", "anti-pvp", "prevent-explosion",
                    "keep-full-health", "prevent-treading", "prevent-throwables",
                    "keep-items-enabled", "banned-commands-enabled",
                    "prevent-eating", "prevent-drinking", "prevent-potion-effects-enabled",
                    "keep-potion-effects-enabled"
            };
            for (String setting : settings) {
                suggestions.add(setting);
            }
        } else if (args.length == 4 && args[0].equalsIgnoreCase("set")) {
            // 第四个参数: 值
            suggestions.add("true");
            suggestions.add("false");
        }

        return suggestions;
    }
}

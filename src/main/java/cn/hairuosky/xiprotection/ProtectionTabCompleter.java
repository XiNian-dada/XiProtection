package cn.hairuosky.xiprotection;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ProtectionTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        // 使用局部变量而不是字段
        if (args.length == 1) {
            // 第一个参数: reload、set、editor
            return Arrays.asList("reload", "set", "editor");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            // 第二个参数: 世界名
            List<String> worlds = new ArrayList<>();
            Bukkit.getWorlds().forEach(world -> worlds.add(world.getName()));
            return worlds;
        } else if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            // 第三个参数: 设置项
            return Arrays.asList(
                    "anti-break", "anti-place", "always-sun", "always-rain",
                    "always-day", "always-night", "anti-fire", "anti-shear",
                    "keep-full-hunger", "anti-pvp", "prevent-explosion",
                    "keep-full-health", "prevent-treading", "prevent-throwables",
                    "keep-items-enabled", "banned-commands-enabled",
                    "prevent-eating", "prevent-drinking", "prevent-potion-effects-enabled",
                    "keep-potion-effects-enabled"
            );
        } else if (args.length == 4 && args[0].equalsIgnoreCase("set")) {
            // 第四个参数: true/false
            return Arrays.asList("true", "false");
        }

        return Collections.emptyList(); // 返回空列表表示无补全项
    }
}

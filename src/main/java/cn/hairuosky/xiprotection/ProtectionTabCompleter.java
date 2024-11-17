package cn.hairuosky.xiprotection;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProtectionTabCompleter implements TabCompleter {

    private final XiProtection plugin; // 主插件类

    public ProtectionTabCompleter(XiProtection plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            // 第一个参数: reload、set、editor、add、remove
            return Arrays.asList("reload", "set", "editor", "add", "remove");
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove"))) {
            // 第二个参数: 世界名
            List<String> worlds = new ArrayList<>();
            plugin.getServer().getWorlds().forEach(world -> worlds.add(world.getName()));
            return worlds;
        } else if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            // 第三个参数: 设置项
            return Arrays.asList(
                    "enable", "anti-break", "anti-place", "always-sun", "always-rain",
                    "always-day", "always-night", "anti-fire", "anti-shear",
                    "keep-full-hunger", "anti-pvp", "prevent-explosion",
                    "keep-full-health", "prevent-treading", "prevent-throwables",
                    "keep-items-enabled", "banned-commands-enabled",
                    "prevent-eating", "prevent-drinking", "prevent-potion-effects-enabled",
                    "keep-potion-effects-enabled"
            );
        } else if (args.length == 3 && args[0].equalsIgnoreCase("add")) {
            // 第三个参数: keep-items、keep-potion-effects、prevent-potion-effects
            return Arrays.asList("keep-items", "keep-potion-effects", "prevent-potion-effects");
        } else if (args.length == 3 && args[0].equalsIgnoreCase("remove")) {
            // 第三个参数: keep-items、keep-potion-effects、prevent-potion-effects
            return Arrays.asList("keep-items", "keep-potion-effects", "prevent-potion-effects");
        } else if (args.length == 4 && args[0].equalsIgnoreCase("add")) {
            // 添加的第四个参数
            if (args[2].equalsIgnoreCase("keep-potion-effects") || args[2].equalsIgnoreCase("prevent-potion-effects")) {
                // 补全药水效果
                return getAllPotionEffects();
            } else if (args[2].equalsIgnoreCase("keep-items")) {
                // 补全物品
                return getAllItems();
            }
        } else if (args.length == 4 && args[0].equalsIgnoreCase("remove")) {
            String worldName = args[1];
            World world = plugin.getServer().getWorld(worldName);
            if (world == null) return Collections.emptyList();

            FileConfiguration config = plugin.getWorldConfig(world);
            if (config == null) return Collections.emptyList();

            if (args[2].equalsIgnoreCase("keep-items")) {
                // 获取已配置的 keep-items
                List<Map<String, Object>> items = (List<Map<String, Object>>) config.getList("keep-items", Collections.emptyList());
                List<String> itemNames = new ArrayList<>();
                for (Map<String, Object> item : items) {
                    itemNames.add((String) item.get("item"));
                }
                return itemNames;
            } else if (args[2].equalsIgnoreCase("keep-potion-effects")) {
                // 获取已配置的 keep-potion-effects
                List<Map<String, Object>> effects = (List<Map<String, Object>>) config.getList("keep-potion-effects", Collections.emptyList());
                List<String> effectNames = new ArrayList<>();
                for (Map<String, Object> effect : effects) {
                    effectNames.add((String) effect.get("effect"));
                }
                return effectNames;
            } else if (args[2].equalsIgnoreCase("prevent-potion-effects")) {
                // 获取已配置的 prevent-potion-effects
                return config.getStringList("prevent-potion-effects");
            }
        } else if (args.length == 5 && args[0].equalsIgnoreCase("add") && args[2].equalsIgnoreCase("keep-potion-effects")) {
            // 第五个参数: 药水效果等级
            return Arrays.asList("1", "2", "3", "4", "5");
        }

        return Collections.emptyList(); // 默认返回空列表
    }

    /**
     * 获取所有药水效果
     */
    private List<String> getAllPotionEffects() {
        return Arrays.stream(PotionEffectType.values())
                .filter(effect -> effect != null && effect.getName() != null)
                .map(PotionEffectType::getName)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有物品
     */
    private List<String> getAllItems() {
        return Arrays.stream(Material.values())
                .filter(Material::isItem) // 确保仅包括物品
                .map(Material::name)
                .collect(Collectors.toList());
    }
}

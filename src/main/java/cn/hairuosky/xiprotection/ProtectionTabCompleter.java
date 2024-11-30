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
                    "keep-potion-effects-enabled",// 新增的 prevent-interactions 配置项
                    "prevent-interactions.furnace", "prevent-interactions.chest",
                    "prevent-interactions.ender_chest", "prevent-interactions.shulker_box",
                    "prevent-interactions.button", "prevent-interactions.lever",
                    "prevent-interactions.trapdoor", "prevent-interactions.door",
                    "prevent-interactions.pressure_plate", "prevent-interactions.dispenser",
                    "prevent-interactions.dropper", "prevent-interactions.hopper",
                    "prevent-interactions.barrel", "prevent-interactions.brewing_stand",
                    "prevent-interactions.beacon", "prevent-interactions.enchanting_table",
                    "prevent-interactions.anvil", "prevent-interactions.cartography_table",
                    "prevent-interactions.grindstone", "prevent-interactions.lectern",
                    "prevent-interactions.smithing_table", "prevent-interactions.stonecutter",
                    "prevent-interactions.jukebox", "prevent-interactions.note_block",
                    "prevent-interactions.fence_gate", "prevent-interactions.composter",
                    "prevent-interactions.beehive", "prevent-interactions.bee_nest",
                    "prevent-interactions.daylight_detector", "prevent-interactions.tripwire_hook",
                    "prevent-interactions.bed", "prevent-interactions.bell",
                    "prevent-interactions.cauldron"
            );
        } else if (args.length == 4 && args[0].equalsIgnoreCase("set")) {
            return Arrays.asList("true","false");

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
            // 移除的第四个参数: 动态补全已有配置的物品/效果
            String worldName = args[1];
            World world = plugin.getServer().getWorld(worldName);
            if (world == null) return Collections.emptyList();

            FileConfiguration config = plugin.getWorldConfig(world);
            if (config == null) return Collections.emptyList();

            if (args[2].equalsIgnoreCase("keep-items")) {
                return getConfigItems(config, "protect.keep-items", "item");
            } else if (args[2].equalsIgnoreCase("keep-potion-effects")) {
                return getConfigItems(config, "protect.keep-potion-effects", "effect");
            } else if (args[2].equalsIgnoreCase("prevent-potion-effects")) {
                return config.getStringList("protect.prevent-potion-effects");
            }
        } else if (args.length == 5 && args[0].equalsIgnoreCase("add") && args[2].equalsIgnoreCase("keep-potion-effects")) {
            // 第五个参数: 药水效果等级
            return Arrays.asList("1", "2", "3", "4", "5");
        }

        return Collections.emptyList(); // 默认返回空列表
    }
    /**
     * 从配置文件中获取指定路径下的所有物品/效果名
     *
     * @param config 配置文件
     * @param path   配置路径
     * @param key    数据键名（例如 "item" 或 "effect"）
     * @return 列表结果
     */
    private List<String> getConfigItems(FileConfiguration config, String path, String key) {
        List<Map<String, Object>> entries = (List<Map<String, Object>>) config.getList(path, Collections.emptyList());
        if (entries == null) return Collections.emptyList();
        return entries.stream()
                .map(entry -> (String) entry.get(key))
                .filter(item -> item != null && !item.isEmpty())
                .collect(Collectors.toList());
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

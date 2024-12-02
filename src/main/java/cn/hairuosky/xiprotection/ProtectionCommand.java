package cn.hairuosky.xiprotection;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

public class ProtectionCommand implements CommandExecutor {

    private final XiProtection plugin;
    private final ProtectionGUI protectionGUI;

    public ProtectionCommand(XiProtection plugin) {
        this.plugin = plugin;
        this.protectionGUI = new ProtectionGUI(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 检查指令格式
        if (args.length < 1) {
            sender.sendMessage(plugin.getLanguageText("command-usage","用法： /xiprotection <set|reload|editor>"));
            return false;
        }

        String action = args[0].toLowerCase();

        switch (action) {
            case "reload":
                reloadPlugin(sender);
                return true;

            case "editor":
                openEditor(sender);
                return true;

            case "add":
            case "remove":
                return handleAddRemoveCommand(sender, args, action);

            case "set":
                return handleSetCommand(sender, args);

            default:
                sender.sendMessage(plugin.getLanguageText("command-unknown-action", "未知指令：{action}.").replace("{action}", action));
                return false;
        }
    }

    private void reloadPlugin(CommandSender sender) {
        plugin.reloadWorldConfigs();
        sender.sendMessage(plugin.getLanguageText("command-reload", "插件已重载。"));
    }

    private void openEditor(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            protectionGUI.openWorldSelectionMenu(player); // 使用 ProtectionGUI 打开菜单
        } else {
            sender.sendMessage(plugin.getLanguageText("command-only-player", "只有玩家可以执行这个指令。"));
        }
    }

    private boolean handleAddRemoveCommand(CommandSender sender, String[] args, String action) {
        if (args.length < 3) {
            sender.sendMessage(plugin.getLanguageText("command-usage-add-remove", "用法: /xiprotection <add|remove> <world> <setting> [details]"));
            return false;
        }

        String worldName = args[1];
        String setting = args[2];
        World world = getWorld(sender, worldName);
        if (world == null) return false;

        FileConfiguration config = plugin.getWorldConfig(world);
        if (config == null) return false;

        switch (setting.toLowerCase()) {
            case "keep-potion-effects":
            case "prevent-potion-effects":
                handlePotionEffectModification(config, sender, args, action, setting);
                break;
            case "keep-items":
                handleItemModification(config, sender, args, action);
                break;
            default:
                sender.sendMessage(plugin.getLanguageText("command-unknown-setting", "未知设置：{setting}。").replace("{setting}", setting));
                return false;
        }

        plugin.saveWorldConfig(world);
        return true;
    }

    private boolean handleSetCommand(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(plugin.getLanguageText("command-usage-set", "用法: /xiprotection set <world> <setting> <value>"));
            return false;
        }

        String worldName = args[1];
        String setting = args[2];
        String value = args[3].toLowerCase();

        World world = getWorld(sender, worldName);
        if (world == null) return false;

        FileConfiguration config = plugin.getWorldConfig(world);
        if (config == null) return false;

        if (applySetting(config, setting, value)) {
            plugin.saveWorldConfig(world);
            sender.sendMessage(plugin.getLanguageText("command-save-done", "已将 {world} 的 {setting} 设置为 {value}。")
                    .replace("{world}", worldName)
                    .replace("{setting}", setting)
                    .replace("{value}", value));

            // 返回新的设置值
            boolean newValue = config.getBoolean("protect." + setting);
            sender.sendMessage(plugin.getLanguageText("command-new-value", "当前设置：{value}。").replace("{value}", String.valueOf(newValue)));
            return true;
        } else {
            sender.sendMessage(plugin.getLanguageText("command-unknown-setting", "未知设置：{setting}。").replace("{setting}", setting));
            return false;
        }
    }

    // 获取世界
    private World getWorld(CommandSender sender, String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            sender.sendMessage(plugin.getLanguageText("command-world-not-found", "未找到该世界 {world}。").replace("{world}", worldName));
        }
        return world;
    }

    // 应用设置
    private boolean applySetting(FileConfiguration config, String setting, String value) {
        Map<String, String> settingMap = getSettingMap();
        String configPath = settingMap.get(setting.toLowerCase());
        if (configPath != null) {
            config.set(configPath, Boolean.parseBoolean(value));
            return true;
        }
        return false;
    }

    // 获取设置映射
    private Map<String, String> getSettingMap() {
        Map<String, String> map = new HashMap<>();
        map.put("enable", "enable");
        map.put("anti-break", "protect.anti-break");
        map.put("anti-place", "protect.anti-place");
        map.put("always-sun", "protect.always-sun");
        map.put("always-rain", "protect.always-rain");
        map.put("always-day", "protect.always-day");
        map.put("always-night", "protect.always-night");
        map.put("anti-fire", "protect.anti-fire");
        map.put("anti-shear", "protect.anti-shear");
        map.put("keep-full-hunger", "protect.keep-full-hunger");
        map.put("anti-pvp", "protect.anti-pvp");
        map.put("prevent-explosion", "protect.prevent-explosion");
        map.put("keep-full-health", "protect.keep-full-health");
        map.put("prevent-treading", "protect.prevent-treading");
        map.put("prevent-throwables", "protect.prevent-throwables");
        map.put("keep-items-enabled", "protect.keep-items-enabled");
        map.put("banned-commands-enabled", "protect.banned-commands-enabled");
        map.put("prevent-eating", "protect.prevent-eating");
        map.put("prevent-drinking", "protect.prevent-drinking");
        map.put("prevent-potion-effects-enabled", "protect.prevent-potion-effects-enabled");
        map.put("keep-potion-effects-enabled", "protect.keep-potion-effects-enabled");
        map.put("prevent-interactions.furnace", "protect.prevent-interactions.furnace");
        map.put("prevent-interactions.chest", "protect.prevent-interactions.chest");
        map.put("prevent-interactions.ender_chest", "protect.prevent-interactions.ender_chest");
        map.put("prevent-interactions.shulker_box", "protect.prevent-interactions.shulker_box");
        map.put("prevent-interactions.button", "protect.prevent-interactions.button");
        map.put("prevent-interactions.lever", "protect.prevent-interactions.lever");
        map.put("prevent-interactions.trapdoor", "protect.prevent-interactions.trapdoor");
        map.put("prevent-interactions.door", "protect.prevent-interactions.door");
        map.put("prevent-interactions.pressure_plate", "protect.prevent-interactions.pressure_plate");
        map.put("prevent-interactions.dispenser", "protect.prevent-interactions.dispenser");
        map.put("prevent-interactions.dropper", "protect.prevent-interactions.dropper");
        map.put("prevent-interactions.hopper", "protect.prevent-interactions.hopper");
        map.put("prevent-interactions.barrel", "protect.prevent-interactions.barrel");
        map.put("prevent-interactions.brewing_stand", "protect.prevent-interactions.brewing_stand");
        map.put("prevent-interactions.beacon", "protect.prevent-interactions.beacon");
        map.put("prevent-interactions.enchanting_table", "protect.prevent-interactions.enchanting_table");
        map.put("prevent-interactions.anvil", "protect.prevent-interactions.anvil");
        map.put("prevent-interactions.cartography_table", "protect.prevent-interactions.cartography_table");
        map.put("prevent-interactions.grindstone", "protect.prevent-interactions.grindstone");
        map.put("prevent-interactions.lectern", "protect.prevent-interactions.lectern");
        map.put("prevent-interactions.smithing_table", "protect.prevent-interactions.smithing_table");
        map.put("prevent-interactions.stonecutter", "protect.prevent-interactions.stonecutter");
        map.put("prevent-interactions.jukebox", "protect.prevent-interactions.jukebox");
        map.put("prevent-interactions.note_block", "protect.prevent-interactions.note_block");
        map.put("prevent-interactions.fence_gate", "protect.prevent-interactions.fence_gate");
        map.put("prevent-interactions.composter", "protect.prevent-interactions.composter");
        map.put("prevent-interactions.beehive", "protect.prevent-interactions.beehive");
        map.put("prevent-interactions.bee_nest", "protect.prevent-interactions.bee_nest");
        map.put("prevent-interactions.daylight_detector", "protect.prevent-interactions.daylight_detector");
        map.put("prevent-interactions.tripwire_hook", "protect.prevent-interactions.tripwire_hook");
        map.put("prevent-interactions.bed", "protect.prevent-interactions.bed");
        map.put("prevent-interactions.bell", "protect.prevent-interactions.bell");
        map.put("prevent-interactions.cauldron", "protect.prevent-interactions.cauldron");
        // 可以继续添加更多映射...
        return map;
    }

    private void handlePotionEffectModification(FileConfiguration config, CommandSender sender, String[] args, String action, String setting) {
        if (args.length < 4) {
            sender.sendMessage(plugin.getLanguageText("command-usage-potion", "用法: /xiprotection <add|remove> <world> " + setting + " <effect> [level]"));
            return;
        }

        String effect = args[3].toUpperCase(); // 药水效果名称
        String fullPath = "protect." + setting; // 操作路径

        if (action.equalsIgnoreCase("add") && setting.equals("keep-potion-effects")) {
            if (args.length < 5) {
                sender.sendMessage(plugin.getLanguageText("command-usage-potion-level", "用法: /xiprotection add <world> keep-potion-effects <effect> <level>"));
                return;
            }

            int level = Integer.parseInt(args[4]);

            // 使用通用方法处理添加操作
            Map<String, Object> newEffect = new LinkedHashMap<>();
            newEffect.put("effect", effect);
            newEffect.put("level", level);
            modifyConfigList(config, fullPath, action, "effect", newEffect);

            sender.sendMessage(plugin.getLanguageText("command-add-potion", "已添加 {effect} 效果（等级 {level}）到 {setting}。")
                    .replace("{effect}", effect).replace("{level}", String.valueOf(level)).replace("{setting}", setting));
        } else if (action.equalsIgnoreCase("add")) {
            // 处理 prevent-potion-effects
            modifyConfigList(config, fullPath, action, "effect", effect);

            sender.sendMessage(plugin.getLanguageText("command-add-potion", "已添加 {effect} 到 {setting}。")
                    .replace("{effect}", effect).replace("{setting}", setting));
        } else if (action.equalsIgnoreCase("remove")) {
            if (setting.equals("keep-potion-effects")) {
                List<Map<String, Object>> effects = getConfigItems(config, fullPath);

                // 检查输入的药水效果是否存在
                boolean effectExists = effects.stream()
                        .anyMatch(e -> effect.equalsIgnoreCase((String) e.get("effect")));
                if (!effectExists) {
                    sender.sendMessage(plugin.getLanguageText("command-effect-not-found", "未找到 {effect} 效果于 {setting} 中。")
                            .replace("{effect}", effect).replace("{setting}", setting));
                    return;
                }

                // 使用通用方法处理移除操作
                modifyConfigList(config, fullPath, action, "effect", effect);
            } else {
                // 处理 prevent-potion-effects
                modifyConfigList(config, fullPath, action, "effect", effect);
            }

            sender.sendMessage(plugin.getLanguageText("command-remove-potion", "已从 {setting} 移除 {effect}。")
                    .replace("{effect}", effect).replace("{setting}", setting));
        }
    }



    private void handleItemModification(FileConfiguration config, CommandSender sender, String[] args, String action) {
        if (args.length < 4 || (action.equalsIgnoreCase("add") && args.length < 5)) {
            sender.sendMessage(plugin.getLanguageText("command-usage-items", "用法: /xiprotection <add|remove> <world> keep-items <item> [quantity]"));
            return;
        }

        String item = args[3].toUpperCase();
        String fullPath = "protect.keep-items";

        if (action.equalsIgnoreCase("add")) {
            if (args.length < 5) {
                sender.sendMessage(plugin.getLanguageText("command-usage-add-item", "用法: /xiprotection add <world> keep-items <item> <quantity>"));
                return;
            }

            int quantity = Integer.parseInt(args[4]);

            // 使用通用方法处理添加物品操作
            Map<String, Object> newItem = new HashMap<>();
            newItem.put("item", item);
            newItem.put("quantity", quantity);
            modifyConfigList(config, fullPath, action, "item", newItem);

            sender.sendMessage(plugin.getLanguageText("command-add-item", "已添加 {item}（数量 {quantity}）到 keep-items。")
                    .replace("{item}", item).replace("{quantity}", String.valueOf(quantity)));
        } else if (action.equalsIgnoreCase("remove")) {
            List<Map<String, Object>> items = getConfigItems(config, fullPath);

            // 检查物品是否存在
            boolean itemExists = items.stream()
                    .anyMatch(i -> item.equalsIgnoreCase((String) i.get("item")));
            if (!itemExists) {
                sender.sendMessage(plugin.getLanguageText("command-item-not-found", "未找到物品 {item} 于 keep-items 中。")
                        .replace("{item}", item));
                return;
            }

            // 使用通用方法处理移除物品操作
            modifyConfigList(config, fullPath, action, "item", item);

            sender.sendMessage(plugin.getLanguageText("command-remove-item", "已从 keep-items 移除 {item}。")
                    .replace("{item}", item));
        }
    }
    private List<Map<String, Object>> getConfigItems(FileConfiguration config, String path) {
        List<Map<String, Object>> items = new ArrayList<>();
        for (Object obj : config.getList(path, new ArrayList<>())) {
            if (obj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) obj;
                items.add(map);
            }
        }
        return items;
    }

    private void modifyConfigList(FileConfiguration config, String path, String action, String key, Object value) {
        List<Map<String, Object>> items = getConfigItems(config, path);

        if (action.equalsIgnoreCase("add")) {
            // 添加新条目
            Map<String, Object> newItem = new HashMap<>();
            newItem.put(key, value);
            items.add(newItem);
        } else if (action.equalsIgnoreCase("remove")) {
            // 移除条目
            items.removeIf(item -> value.equals(item.get(key)));
        }

        // 更新配置
        config.set(path, items);
    }



}

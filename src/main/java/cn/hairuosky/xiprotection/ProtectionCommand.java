package cn.hairuosky.xiprotection;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            //sender.sendMessage("用法: /xiprotection <set|reload|editor> ...");
            return false;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            // 重载插件
            plugin.reloadWorldConfigs();
            sender.sendMessage(plugin.getLanguageText("command-reload","插件已重载。"));
            //sender.sendMessage("插件配置已重载。");
            return true;
        }

        if (args[0].equalsIgnoreCase("editor")) {
            // 打开 GUI 编辑器
            if (sender instanceof Player) {
                Player player = (Player) sender;
                protectionGUI.openWorldSelectionMenu(player); // 使用 ProtectionGUI 打开菜单
                return true;
            } else {
                sender.sendMessage(plugin.getLanguageText("command-only-player","只有玩家可以执行这个指令。"));
                //sender.sendMessage("只有玩家可以使用此命令。");
                return false;
            }
        }
        if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove")) {
            if (args.length < 3) {
                sender.sendMessage(plugin.getLanguageText("command-usage-add-remove", "用法: /xiprotection <add|remove> <world> <setting> [details]"));
                return false;
            }

            String action = args[0]; // "add" 或 "remove"
            String worldName = args[1];
            String setting = args[2];

            // 获取世界
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                sender.sendMessage(plugin.getLanguageText("command-world-not-found", "未找到该世界 {world}。").replace("{world}", worldName));
                return false;
            }

            // 获取世界配置
            FileConfiguration config = plugin.getWorldConfig(world);
            if (config == null) {
                sender.sendMessage(plugin.getLanguageText("command-world-config-not-found", "未找到该世界的配置：{world}。").replace("{world}", worldName));
                return false;
            }

            switch (setting.toLowerCase()) {
                case "keep-potion-effects":
                    handlePotionEffectModification(config, sender, args, action, "keep-potion-effects");
                    break;
                case "prevent-potion-effects":
                    handlePotionEffectModification(config, sender, args, action, "prevent-potion-effects");
                    break;
                case "keep-items":
                    handleItemModification(config, sender, args, action);
                    break;
                default:
                    sender.sendMessage(plugin.getLanguageText("command-unknown-setting", "未知设置：{setting}。").replace("{setting}", setting));
                    return false;
            }

            // 保存配置
            plugin.saveWorldConfig(world);
            sender.sendMessage(plugin.getLanguageText("command-save-done", "已更新 {world} 的 {setting} 设置。")
                    .replace("{world}", worldName).replace("{setting}", setting));
            return true;
        }


        // 处理 set 指令
        if (args.length < 4 || !args[0].equalsIgnoreCase("set")) {
            //sender.sendMessage("用法: /xiprotection set <world> <setting> <value>");
            sender.sendMessage(plugin.getLanguageText("command-usage-set","用法: /xiprotection set <world> <setting> <value>。"));
            return false;
        }

        String worldName = args[1];
        String setting = args[2];
        String value = args[3].toLowerCase();

        // 获取世界
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            sender.sendMessage(plugin.getLanguageText("command-world-not-found","未找到该世界 {world}。").replace("{world}",worldName));
            //sender.sendMessage("未找到该世界: " + worldName);
            return false;
        }

        // 获取世界配置
        FileConfiguration config = plugin.getWorldConfig(world);
        if (config == null) {
            //sender.sendMessage("未找到该世界的配置: " + worldName);
            sender.sendMessage(plugin.getLanguageText("command-world-config-not-found","未找到该世界的配置：{world}。").replace("{world}",worldName));
            return false;
        }

        // 设置值
        switch (setting.toLowerCase()) {
            case "enable":
                config.set("enable", Boolean.parseBoolean(value));
            case "anti-break":
                config.set("protect.anti-break", Boolean.parseBoolean(value));
                break;
            case "anti-place":
                config.set("protect.anti-place", Boolean.parseBoolean(value));
                break;
            case "always-sun":
                config.set("protect.always-sun", Boolean.parseBoolean(value));
                break;
            case "always-rain":
                config.set("protect.always-rain", Boolean.parseBoolean(value));
                break;
            case "always-day":
                config.set("protect.always-day", Boolean.parseBoolean(value));
                break;
            case "always-night":
                config.set("protect.always-night", Boolean.parseBoolean(value));
                break;
            case "anti-fire":
                config.set("protect.anti-fire", Boolean.parseBoolean(value));
                break;
            case "anti-shear":
                config.set("protect.anti-shear", Boolean.parseBoolean(value));
                break;
            case "keep-full-hunger":
                config.set("protect.keep-full-hunger", Boolean.parseBoolean(value));
                break;
            case "anti-pvp":
                config.set("protect.anti-pvp", Boolean.parseBoolean(value));
                break;
            case "prevent-explosion":
                config.set("protect.prevent-explosion", Boolean.parseBoolean(value));
                break;
            case "keep-full-health":
                config.set("protect.keep-full-health", Boolean.parseBoolean(value));
                break;
            case "prevent-treading":
                config.set("protect.prevent-treading", Boolean.parseBoolean(value));
                break;
            case "prevent-throwables":
                config.set("protect.prevent-throwables", Boolean.parseBoolean(value));
                break;
            case "keep-items-enabled":
                config.set("protect.keep-items-enabled", Boolean.parseBoolean(value));
                break;
            case "banned-commands-enabled":
                config.set("protect.banned-commands-enabled", Boolean.parseBoolean(value));
                break;
            case "prevent-eating":
                config.set("protect.prevent-eating", Boolean.parseBoolean(value));
                break;
            case "prevent-drinking":
                config.set("protect.prevent-drinking", Boolean.parseBoolean(value));
                break;
            case "prevent-potion-effects-enabled":
                config.set("protect.prevent-potion-effects-enabled", Boolean.parseBoolean(value));
                break;
            case "keep-potion-effects-enabled":
                config.set("protect.keep-potion-effects-enabled", Boolean.parseBoolean(value));
                break;
            default:
                sender.sendMessage(plugin.getLanguageText("command-unknown-setting","未知设置：{setting}。").replace("{setting}",setting));
                //sender.sendMessage("未知设置: " + setting);
                return false;
        }
        Bukkit.getLogger().info(plugin.getLanguageText("command-save-settings","正在保存设置到 {world}。").replace("{world}",world.getName()));
        //Bukkit.getLogger().info("正在保存配置到 " + world.getName());
        // 保存配置
        plugin.saveWorldConfig(world);
        //sender.sendMessage("已将 " + worldName + " 的 " + setting + " 设置为 " + value);
        sender.sendMessage(plugin.getLanguageText("command-save-done","已将 {world} 的 {setting} 设置为 {value}。").replace("{world}",worldName).replace("{setting}",setting).replace("{value}",value));
        FileConfiguration updatedConfig = plugin.getWorldConfig(world);
        boolean newValue = updatedConfig.getBoolean("protect." + setting);
        sender.sendMessage(plugin.getLanguageText("command-new-value","当前设置：{value}。").replace("{value}",String.valueOf(newValue)));
        //sender.sendMessage("当前设置: " + newValue);

        return true;
    }

//TODO 以下的操作的语言文件还没有写出去
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

            // 获取配置中的列表
            List<Map<String, Object>> effects = new ArrayList<>();
            for (Object obj : config.getList(fullPath, new ArrayList<>())) {
                if (obj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>) obj;
                    effects.add(map);
                }
            }

            // 添加新效果
            Map<String, Object> newEffect = new HashMap<>();
            newEffect.put("effect", effect);
            newEffect.put("level", level);
            effects.add(newEffect);
            config.set(fullPath, effects);

            sender.sendMessage(plugin.getLanguageText("command-add-potion", "已添加 {effect} 效果（等级 {level}）到 {setting}。")
                    .replace("{effect}", effect).replace("{level}", String.valueOf(level)).replace("{setting}", setting));
        } else if (action.equalsIgnoreCase("add")) {
            // 处理 prevent-potion-effects
            List<String> effects = config.getStringList(fullPath);
            effects.add(effect);
            config.set(fullPath, effects);

            sender.sendMessage(plugin.getLanguageText("command-add-potion", "已添加 {effect} 到 {setting}。")
                    .replace("{effect}", effect).replace("{setting}", setting));
        } else if (action.equalsIgnoreCase("remove")) {
            if (setting.equals("keep-potion-effects")) {
                // 获取配置中的列表
                List<Map<String, Object>> effects = new ArrayList<>();
                for (Object obj : config.getList(fullPath, new ArrayList<>())) {
                    if (obj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> map = (Map<String, Object>) obj;
                        effects.add(map);
                    }
                }

                // 移除效果
                effects.removeIf(e -> effect.equalsIgnoreCase((String) e.get("effect")));
                config.set(fullPath, effects);
            } else {
                // 处理 prevent-potion-effects
                List<String> effects = config.getStringList(fullPath);
                effects.remove(effect);
                config.set(fullPath, effects);
            }

            sender.sendMessage(plugin.getLanguageText("command-remove-potion", "已从 {setting} 移除 {effect}。")
                    .replace("{effect}", effect).replace("{setting}", setting));
        }
    }


    private void handleItemModification(FileConfiguration config, CommandSender sender, String[] args, String action) {
        if (args.length < 5) {
            sender.sendMessage(plugin.getLanguageText("command-usage-items", "用法: /xiprotection <add|remove> <world> keep-items <item> <quantity>"));
            return;
        }

        String item = args[3].toUpperCase();
        int quantity = Integer.parseInt(args[4]);
        String fullPath = "protect.keep-items";

        if (action.equalsIgnoreCase("add")) {
            // 获取配置中的列表
            List<Map<String, Object>> items = new ArrayList<>();
            for (Object obj : config.getList(fullPath, new ArrayList<>())) {
                if (obj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>) obj;
                    items.add(map);
                }
            }

            // 添加新物品
            Map<String, Object> newItem = new HashMap<>();
            newItem.put("item", item);
            newItem.put("quantity", quantity);
            items.add(newItem);
            config.set(fullPath, items);

            sender.sendMessage(plugin.getLanguageText("command-add-item", "已添加 {item}（数量 {quantity}）到 keep-items。")
                    .replace("{item}", item).replace("{quantity}", String.valueOf(quantity)));
        } else if (action.equalsIgnoreCase("remove")) {
            // 获取配置中的列表
            List<Map<String, Object>> items = new ArrayList<>();
            for (Object obj : config.getList(fullPath, new ArrayList<>())) {
                if (obj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>) obj;
                    items.add(map);
                }
            }

            // 移除物品
            items.removeIf(i -> item.equalsIgnoreCase((String) i.get("item")));
            config.set(fullPath, items);

            sender.sendMessage(plugin.getLanguageText("command-remove-item", "已从 keep-items 移除 {item}。").replace("{item}", item));
        }
    }

}

package cn.hairuosky.xiprotection;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

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
            sender.sendMessage("用法: /xiprotection <set|reload|editor> ...");
            return false;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            // 重载插件
            plugin.reloadWorldConfigs();
            sender.sendMessage("插件配置已重载。");
            return true;
        }

        if (args[0].equalsIgnoreCase("editor")) {
            // 打开 GUI 编辑器
            if (sender instanceof Player) {
                Player player = (Player) sender;
                protectionGUI.openWorldSelectionMenu(player); // 使用 ProtectionGUI 打开菜单
                return true;
            } else {
                sender.sendMessage("只有玩家可以使用此命令。");
                return false;
            }
        }

        // 处理 set 指令
        if (args.length < 4 || !args[0].equalsIgnoreCase("set")) {
            sender.sendMessage("用法: /xiprotection set <world> <setting> <value>");
            return false;
        }

        String worldName = args[1];
        String setting = args[2];
        String value = args[3].toLowerCase();

        // 获取世界
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            sender.sendMessage("未找到该世界: " + worldName);
            return false;
        }

        // 获取世界配置
        FileConfiguration config = plugin.getWorldConfig(world);
        if (config == null) {
            sender.sendMessage("未找到该世界的配置: " + worldName);
            return false;
        }

        // 设置值
        switch (setting.toLowerCase()) {
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
                sender.sendMessage("未知设置: " + setting);
                return false;
        }
        Bukkit.getLogger().info("正在保存配置到 " + world.getName());
        // 保存配置
        plugin.saveWorldConfig(world);
        sender.sendMessage("已将 " + worldName + " 的 " + setting + " 设置为 " + value);
        FileConfiguration updatedConfig = plugin.getWorldConfig(world);
        boolean newValue = updatedConfig.getBoolean("protect." + setting);
        sender.sendMessage("当前设置: " + newValue);

        return true;
    }

    private void openWorldSelectionMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, "选择世界");

        for (World world : Bukkit.getWorlds()) {
            ItemStack item = new ItemStack(Material.GRASS_BLOCK);
            item.getItemMeta().setDisplayName(world.getName()); // 设置物品名称为世界名称
            inventory.addItem(item);
        }

        player.openInventory(inventory);
    }
}

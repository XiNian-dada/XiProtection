package cn.hairuosky.xiprotection;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ProtectionGUI implements Listener {

    private final XiProtection plugin;

    public ProtectionGUI(XiProtection plugin) {
        this.plugin = plugin;
    }

    public void openWorldSelectionMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, "选择世界");

        FileConfiguration config = plugin.getConfig();
        // 获取所有已加载的世界
        for (World world : Bukkit.getWorlds()) {
            String worldName = world.getName();
            // 从配置中获取物品名称，如果没有则使用默认值
            String materialName = config.getString("gui.world." + worldName, "GRASS_BLOCK").toUpperCase(); // 确保为大写
            Material material = Material.getMaterial(materialName);
            plugin.getLogger().info("World found: " + worldName);
            plugin.getLogger().info("Material name for " + worldName + ": " + materialName);

            if (material != null) {
                ItemStack item = new ItemStack(material);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    // 设置物品名称为世界名称
                    meta.setDisplayName(worldName);
                    item.setItemMeta(meta);
                }
                inventory.addItem(item);
            }
        }

        player.openInventory(inventory);
    }



    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("选择世界")) {
            event.setCancelled(true); // 取消点击事件以防止关闭

            Player player = (Player) event.getWhoClicked();
            ItemStack currentItem = event.getCurrentItem();

            if (currentItem == null || currentItem.getType() == Material.AIR) {
                return; // 如果点击的是空白项，则直接返回
            }

            ItemMeta meta = currentItem.getItemMeta();
            if (meta != null) {
                String worldName = meta.getDisplayName(); // 获取物品名称

                if (worldName != null) { // 确保名称不为 null
                    World world = Bukkit.getWorld(worldName);
                    if (world != null) {
                        openSettingsMenu(player, world);
                    } else {
                        player.sendMessage("未找到世界: " + worldName);
                    }
                }
            }
        }
    }


    private void openSettingsMenu(Player player, World world) {
        Inventory settingsInventory = Bukkit.createInventory(null, 27, "配置选项 - " + world.getName());

        FileConfiguration config = plugin.getConfig();

        settingsInventory.setItem(0, createToggleItem(Material.getMaterial(config.getString("option.anti-break", "DIAMOND_PICKAXE")), config.getString("option.anti-break-name", "Anti Break"), getConfigValue(world, "anti-break")));
        settingsInventory.setItem(1, createToggleItem(Material.getMaterial(config.getString("option.anti-place", "DIAMOND_PICKAXE")), config.getString("option.anti-place-name", "Anti Place"), getConfigValue(world, "anti-place")));
        settingsInventory.setItem(2, createToggleItem(Material.getMaterial(config.getString("option.always-sun", "GOLDEN_SWORD")), config.getString("option.always-sun-name", "Always Sun"), getConfigValue(world, "always-sun")));
        settingsInventory.setItem(3, createToggleItem(Material.getMaterial(config.getString("option.always-rain", "WATER_BUCKET")), config.getString("option.always-rain-name", "Always Rain"), getConfigValue(world, "always-rain")));
        settingsInventory.setItem(4, createToggleItem(Material.getMaterial(config.getString("option.always-day", "GOLDEN_SWORD")), config.getString("option.always-day-name", "Always Day"), getConfigValue(world, "always-day")));
        settingsInventory.setItem(5, createToggleItem(Material.getMaterial(config.getString("option.always-night", "GOLDEN_SWORD")), config.getString("option.always-night-name", "Always Night"), getConfigValue(world, "always-night")));
        settingsInventory.setItem(6, createToggleItem(Material.getMaterial(config.getString("option.anti-fire", "FIRE")), config.getString("option.anti-fire-name", "Anti Fire"), getConfigValue(world, "anti-fire")));
        settingsInventory.setItem(7, createToggleItem(Material.getMaterial(config.getString("option.anti-shear", "SHEARS")), config.getString("option.anti-shear-name", "Anti Shear"), getConfigValue(world, "anti-shear")));
        settingsInventory.setItem(8, createToggleItem(Material.getMaterial(config.getString("option.keep-full-hunger", "COOKED_BEEF")), config.getString("option.keep-full-hunger-name", "Keep Full Hunger"), getConfigValue(world, "keep-full-hunger")));
        settingsInventory.setItem(9, createToggleItem(Material.getMaterial(config.getString("option.anti-pvp", "DIAMOND_SWORD")), config.getString("option.anti-pvp-name", "Anti PvP"), getConfigValue(world, "anti-pvp")));
        settingsInventory.setItem(10, createToggleItem(Material.getMaterial(config.getString("option.prevent-explosion", "TNT")), config.getString("option.prevent-explosion-name", "Prevent Explosion"), getConfigValue(world, "prevent-explosion")));
        settingsInventory.setItem(11, createToggleItem(Material.getMaterial(config.getString("option.keep-full-health", "GOLDEN_APPLE")), config.getString("option.keep-full-health-name", "Keep Full Health"), getConfigValue(world, "keep-full-health")));
        settingsInventory.setItem(12, createToggleItem(Material.getMaterial(config.getString("option.prevent-treading", "GRASS_BLOCK")), config.getString("option.prevent-treading-name", "Prevent Treading"), getConfigValue(world, "prevent-treading")));
        settingsInventory.setItem(13, createToggleItem(Material.getMaterial(config.getString("option.prevent-throwables", "ENDER_PEARL")), config.getString("option.prevent-throwables-name", "Prevent Throwables"), getConfigValue(world, "prevent-throwables")));
        settingsInventory.setItem(14, createToggleItem(Material.getMaterial(config.getString("option.keep-items-enabled", "CHEST")), config.getString("option.keep-items-enabled-name", "Keep Items Enabled"), getConfigValue(world, "keep-items-enabled")));
        settingsInventory.setItem(15, createToggleItem(Material.getMaterial(config.getString("option.banned-commands-enabled", "BARRIER")), config.getString("option.banned-commands-enabled-name", "Banned Commands Enabled"), getConfigValue(world, "banned-commands-enabled")));
        settingsInventory.setItem(16, createToggleItem(Material.getMaterial(config.getString("option.prevent-eating", "COOKED_BEEF")), config.getString("option.prevent-eating-name", "Prevent Eating"), getConfigValue(world, "prevent-eating")));
        settingsInventory.setItem(17, createToggleItem(Material.getMaterial(config.getString("option.prevent-drinking", "POTION")), config.getString("option.prevent-drinking-name", "Prevent Drinking"), getConfigValue(world, "prevent-drinking")));
        settingsInventory.setItem(18, createToggleItem(Material.getMaterial(config.getString("option.prevent-potion-effects-enabled", "POTION")), config.getString("option.prevent-potion-effects-enabled-name", "Prevent Potion Effects Enabled"), getConfigValue(world, "prevent-potion-effects-enabled")));
        settingsInventory.setItem(19, createToggleItem(Material.getMaterial(config.getString("option.keep-potion-effects-enabled", "POTION")), config.getString("option.keep-potion-effects-enabled-name", "Keep Potion Effects Enabled"), getConfigValue(world, "keep-potion-effects-enabled")));

        player.openInventory(settingsInventory);
    }


    private ItemStack createToggleItem(Material material, String name, boolean value) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            // 设置物品名称为功能名称加上状态
            meta.setDisplayName(name + ": " + (value ? "开启" : "关闭"));
            item.setItemMeta(meta); // 将 ItemMeta 应用到 ItemStack
        }

        return item;
    }


    private boolean getConfigValue(World world, String setting) {
        FileConfiguration config = plugin.getWorldConfig(world);
        return config.getBoolean("protect." + setting, false);
    }

    @EventHandler
    public void onSettingsInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().startsWith("配置选项 - ")) {
            event.setCancelled(true);

            Player player = (Player) event.getWhoClicked();
            String worldName = event.getView().getTitle().replace("配置选项 - ", "");
            World world = Bukkit.getWorld(worldName);

            if (world != null) {
                toggleSetting(player, event.getSlot(), world);
            }
        }
    }

    private void toggleSetting(Player player, int slot, World world) {
        FileConfiguration config = plugin.getWorldConfig(world);
        String setting;

        switch (slot) {
            case 0:
                setting = "anti-break";
                break;
            case 1:
                setting = "anti-place";
                break;
            case 2:
                setting = "always-sun";
                break;
            case 3:
                setting = "always-rain";
                break;
            case 4:
                setting = "always-day";
                break;
            case 5:
                setting = "always-night";
                break;
            case 6:
                setting = "anti-fire";
                break;
            case 7:
                setting = "anti-shear";
                break;
            case 8:
                setting = "keep-full-hunger";
                break;
            case 9:
                setting = "anti-pvp";
                break;
            case 10:
                setting = "prevent-explosion";
                break;
            case 11:
                setting = "keep-full-health";
                break;
            case 12:
                setting = "prevent-treading";
                break;
            case 13:
                setting = "prevent-throwables";
                break;
            case 14:
                setting = "keep-items-enabled";
                break;
            case 15:
                setting = "banned-commands-enabled";
                break;
            case 16:
                setting = "prevent-eating";
                break;
            case 17:
                setting = "prevent-drinking";
                break;
            case 18:
                setting = "prevent-potion-effects-enabled";
                break;
            case 19:
                setting = "keep-potion-effects-enabled";
                break;
            default:
                player.sendMessage("未知设置项！");
                return;
        }

        boolean currentValue = config.getBoolean("protect." + setting, false);
        config.set("protect." + setting, !currentValue); // 切换值
        plugin.saveWorldConfig(world); // 保存配置
        player.sendMessage(setting + " 已设置为 " + (!currentValue ? "开启" : "关闭"));
        // 重新打开设置菜单
        openSettingsMenu(player, world);
    }

}

package cn.hairuosky.xiprotection;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Objects;

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

        // 读取并设置功能项
        for (String key : Objects.requireNonNull(config.getConfigurationSection("gui.option")).getKeys(false)) {
            String itemPath = "gui.option." + key + ".item";
            String namePath = "gui.option." + key + ".name";
            int slot = config.getInt("gui.option." + key + ".slot", -1);  // 默认值为 -1
            List<Integer> slots = config.getIntegerList("gui.option." + key + ".slots");  // 读取 slots 列表

            Material material = Material.getMaterial(config.getString(itemPath, "BARRIER"));
            String displayName = ChatColor.translateAlternateColorCodes('&',config.getString(namePath, key));
            String function = config.getString("gui.option." + key + ".function", "");

            // 转换颜色符号(&)为Minecraft的颜色代码
            displayName = ChatColor.translateAlternateColorCodes('&', displayName);

            // 如果是装饰物品，不显示开启/关闭状态
            if (function.equals("decorate")) {
                ItemStack itemStack = new ItemStack(material);
                ItemMeta meta = itemStack.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(displayName);  // 保持原名称，不带 "开启/关闭"
                    itemStack.setItemMeta(meta);
                }

                // 如果 slots 存在，填充多个位置
                if (!slots.isEmpty()) {
                    for (int s : slots) {
                        settingsInventory.setItem(s, itemStack);
                    }
                } else if (slot != -1) {
                    settingsInventory.setItem(slot, itemStack);
                }
            } else {
                // 普通功能项，带上开启/关闭状态
                ItemStack itemStack = createToggleItem(material, displayName, getConfigValue(world, key));

                // 如果 slots 存在，填充多个位置
                if (!slots.isEmpty()) {
                    for (int s : slots) {
                        settingsInventory.setItem(s, itemStack);
                    }
                } else if (slot != -1) {
                    // 如果只有一个 slot，则填充单个位置
                    settingsInventory.setItem(slot, itemStack);
                }
            }
        }

        player.openInventory(settingsInventory);
    }

    private ItemStack createToggleItem(Material material, String name, boolean value) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            // 根据状态设置颜色和加粗样式
            ChatColor color = value ? ChatColor.GREEN : ChatColor.RED;
            String status = (value ? "开启" : "关闭");

            // 设置加粗样式
            meta.setDisplayName(name + ": " + color + ChatColor.BOLD + status);

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
            event.setCancelled(true);  // 防止物品被移除

            Player player = (Player) event.getWhoClicked();
            String worldName = event.getView().getTitle().replace("配置选项 - ", "");
            World world = Bukkit.getWorld(worldName);

            if (world != null) {
                ItemStack clickedItem = event.getCurrentItem();
                if (clickedItem == null || clickedItem.getType() == Material.AIR) {
                    return;  // 空点击则返回
                }

                ItemMeta meta = clickedItem.getItemMeta();
                if (meta != null && meta.hasDisplayName()) {
                    String itemName = meta.getDisplayName();

                    // 从配置中获取 item 的 function
                    String function = "";
                    FileConfiguration config = plugin.getConfig();
                    for (String key : Objects.requireNonNull(config.getConfigurationSection("gui.option")).getKeys(false)) {
                        String name = config.getString("gui.option." + key + ".name");
                        if (name != null && name.equals(itemName)) {
                            function = config.getString("gui.option." + key + ".function", "");
                            break;
                        }
                    }

                    // 如果 function 是 "decorate"，则不执行任何操作
                    if (function.equals("decorate")) {
                        return;
                    }

                    // 否则继续执行正常的设置切换逻辑
                    toggleSetting(player, event.getSlot(), world);
                }
            }
        }
    }

    private void toggleSetting(Player player, int slot, World world) {
        FileConfiguration worldConfig = plugin.getWorldConfig(world);
        FileConfiguration mainConfig = plugin.getConfig();
        ConfigurationSection optionSection = mainConfig.getConfigurationSection("gui.option");

        if (optionSection == null) {
            player.sendMessage("未找到配置节 'gui.option'。请检查 config.yml 文件。");
            return;
        }

        // 遍历主配置文件中的 GUI 配置节
        for (String key : optionSection.getKeys(false)) {
            int configSlot = mainConfig.getInt("gui.option." + key + ".slot");

            if (configSlot == slot) {
                boolean currentValue = worldConfig.getBoolean("protect." + key, false);
                worldConfig.set("protect." + key, !currentValue); // 切换值
                plugin.saveWorldConfig(world); // 保存世界配置文件
                player.sendMessage(key + " 已设置为 " + (!currentValue ? "开启" : "关闭"));
                // 重新打开设置菜单
                openSettingsMenu(player, world);
                return;
            }
        }

        player.sendMessage("未知设置项！");
    }




}

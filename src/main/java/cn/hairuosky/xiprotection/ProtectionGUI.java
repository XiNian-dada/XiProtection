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
                String worldName = meta.getDisplayName();

                World world = Bukkit.getWorld(worldName); // 如果世界名不存在，Bukkit.getWorld 会返回 null
                if (world != null) {
                    openSettingsMenu_1(player, world); // 打开第一页设置菜单
                } else {
                    player.sendMessage("未找到世界: " + worldName);
                }
            }
        }
    }

    // 打开第一页设置菜单
    private void openSettingsMenu_1(Player player, World world) {
        FileConfiguration config = plugin.getConfig();
        Inventory settingsInventory = Bukkit.createInventory(null, config.getInt("gui.size", 54), "配置选项 - " + world.getName() + " 第1页");

        // 读取第一页的配置并设置物品项
        for (String key : Objects.requireNonNull(config.getConfigurationSection("gui.page-1")).getKeys(false)) {
            String itemPath = "gui.page-1." + key + ".item";
            String namePath = "gui.page-1." + key + ".name";
            int slot = config.getInt("gui.page-1." + key + ".slot", -1);  // 默认值为 -1
            List<Integer> slots = config.getIntegerList("gui.page-1." + key + ".slots");  // 读取 slots 列表

            Material material = Material.getMaterial(config.getString(itemPath, "BARRIER"));
            if (material == null) {
                material = Material.BARRIER; // 使用默认物品
            }

            String displayName = ChatColor.translateAlternateColorCodes('&', config.getString(namePath, key));
            String function = config.getString("gui.page-1." + key + ".function", "");

            displayName = ChatColor.translateAlternateColorCodes('&', displayName);

            if (function.equals("decorate")) {
                ItemStack itemStack = new ItemStack(material);
                ItemMeta meta = itemStack.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(displayName);  // 保持原名称，不带 "开启/关闭"
                    itemStack.setItemMeta(meta);
                }

                if (!slots.isEmpty()) {
                    for (int s : slots) {
                        settingsInventory.setItem(s, itemStack);
                    }
                } else if (slot != -1) {
                    settingsInventory.setItem(slot, itemStack);
                }
            } else {
                ItemStack itemStack = createToggleItem(material, displayName, getConfigValue(world, function));

                if (!slots.isEmpty()) {
                    for (int s : slots) {
                        settingsInventory.setItem(s, itemStack);
                    }
                } else if (slot != -1) {
                    settingsInventory.setItem(slot, itemStack);
                }
            }
        }

        // 设置翻页按钮
        setPaginationButtons(settingsInventory, 1);

        player.openInventory(settingsInventory);
    }

    private void openSettingsMenu_2(Player player, World world) {
        FileConfiguration config = plugin.getConfig();
        Inventory settingsInventory = Bukkit.createInventory(null, config.getInt("gui.size", 54), "配置选项 - " + world.getName() + " 第2页");

        // 读取第二页的配置并设置物品项
        for (String key : Objects.requireNonNull(config.getConfigurationSection("gui.page-2")).getKeys(false)) {
            String itemPath = "gui.page-2." + key + ".item";
            String namePath = "gui.page-2." + key + ".name";
            int slot = config.getInt("gui.page-2." + key + ".slot", -1);  // 默认值为 -1
            List<Integer> slots = config.getIntegerList("gui.page-2." + key + ".slots");  // 读取 slots 列表

            Material material = Material.getMaterial(config.getString(itemPath, "BARRIER"));
            if (material == null) {
                material = Material.BARRIER; // 使用默认物品
            }

            String displayName = ChatColor.translateAlternateColorCodes('&', config.getString(namePath, key));
            String function = config.getString("gui.page-2." + key + ".function", "");

            displayName = ChatColor.translateAlternateColorCodes('&', displayName);

            if (function.equals("decorate")) {
                ItemStack itemStack = new ItemStack(material);
                ItemMeta meta = itemStack.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(displayName);  // 保持原名称，不带 "开启/关闭"
                    itemStack.setItemMeta(meta);
                }

                if (!slots.isEmpty()) {
                    for (int s : slots) {
                        settingsInventory.setItem(s, itemStack);
                    }
                } else if (slot != -1) {
                    settingsInventory.setItem(slot, itemStack);
                }
            } else {
                ItemStack itemStack = createToggleItem(material, displayName, getConfigValue(world, function));

                if (!slots.isEmpty()) {
                    for (int s : slots) {
                        settingsInventory.setItem(s, itemStack);
                    }
                } else if (slot != -1) {
                    settingsInventory.setItem(slot, itemStack);
                }
            }
        }

        // 设置翻页按钮
        setPaginationButtons(settingsInventory, 2);

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

    private void setPaginationButtons(Inventory inventory, int page) {
        FileConfiguration config = plugin.getConfig();

        // 获取翻页按钮的配置
        String nextPageItem = config.getString("gui.page-" + page + ".next-page.item", "ARROW");
        String previousPageItem = config.getString("gui.page-" + page + ".previous_page.item", "ARROW");

        int nextPageSlot = config.getInt("gui.page-" + page + ".next-page.slot", 53);
        int previousPageSlot = config.getInt("gui.page-" + page + ".previous_page.slot", 45);

        String nextPageName = ChatColor.translateAlternateColorCodes('&', config.getString("gui.page-" + page + ".next-page.name", "&e下一页"));
        String previousPageName = ChatColor.translateAlternateColorCodes('&', config.getString("gui.page-" + page + ".previous_page.name", "&7上一页"));

        // 创建翻页按钮
        ItemStack nextPage = new ItemStack(Material.getMaterial(nextPageItem));
        ItemMeta nextMeta = nextPage.getItemMeta();
        if (nextMeta != null) {
            nextMeta.setDisplayName(nextPageName);
            nextPage.setItemMeta(nextMeta);
        }

        ItemStack previousPage = new ItemStack(Material.getMaterial(previousPageItem));
        ItemMeta previousMeta = previousPage.getItemMeta();
        if (previousMeta != null) {
            previousMeta.setDisplayName(previousPageName);
            previousPage.setItemMeta(previousMeta);
        }

        // 设置翻页按钮
        if (page == 1) {
            inventory.setItem(nextPageSlot, nextPage); // 设置下一页按钮
        } else if (page == 2) {
            inventory.setItem(previousPageSlot, previousPage); // 设置上一页按钮
        }
    }



    @EventHandler
    public void onSettingsInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().startsWith("配置选项 - ")) {
            event.setCancelled(true);  // 防止物品被移除

            Player player = (Player) event.getWhoClicked();
            String worldName = event.getView().getTitle().replace("配置选项 - ", "").split(" ")[0];
            World world = Bukkit.getWorld(worldName);

            if (world != null) {
                ItemStack clickedItem = event.getCurrentItem();
                if (clickedItem == null || clickedItem.getType() == Material.AIR) {
                    return;  // 空点击则返回
                }

                ItemMeta meta = clickedItem.getItemMeta();
                if (meta != null && meta.hasDisplayName()) {
                    String itemName = meta.getDisplayName();

                    // 处理翻页按钮的点击
                    if (itemName.contains("下一页")) {
                        openSettingsMenu_2(player, world); // 转到第二页
                    } else if (itemName.contains("上一页")) {
                        openSettingsMenu_1(player, world); // 返回第一页
                    } else {
                        // 否则执行设置切换
                        toggleSetting(player, event.getSlot(), world, event.getView().getTitle());
                    }
                }
            }
        }
    }

    private void toggleSetting(Player player, int slot, World world, String menuTitle) {
        FileConfiguration worldConfig = plugin.getWorldConfig(world);
        FileConfiguration mainConfig = plugin.getConfig();

        // 判断当前页面是第一页还是第二页
        int page = menuTitle.contains("第2页") ? 2 : 1;

        // 遍历当前页的配置
        ConfigurationSection pageSection = mainConfig.getConfigurationSection("gui.page-" + page);
        if (pageSection == null) {
            player.sendMessage("无法找到当前页面的配置！");
            return;
        }

        for (String key : pageSection.getKeys(false)) {
            ConfigurationSection itemSection = pageSection.getConfigurationSection(key);
            if (itemSection == null) continue;

            int configSlot = itemSection.getInt("slot", -1);
            String function = itemSection.getString("function", "");

            // 如果点击的槽位匹配，则切换该设置
            if (configSlot == slot && !function.isEmpty()) {
                boolean currentValue = worldConfig.getBoolean("protect." + function, false);
                worldConfig.set("protect." + function, !currentValue); // 切换值
                plugin.saveWorldConfig(world); // 保存世界配置文件
                player.sendMessage(function + " 已设置为 " + (!currentValue ? "开启" : "关闭"));

                // 重新打开当前页面的设置菜单
                if (page == 1) {
                    openSettingsMenu_1(player, world);
                } else {
                    openSettingsMenu_2(player, world);
                }
                return;
            }
        }

        player.sendMessage("未找到匹配的设置项！");
    }

}

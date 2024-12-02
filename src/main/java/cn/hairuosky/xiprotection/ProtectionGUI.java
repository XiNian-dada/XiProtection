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

import java.util.*;

public class ProtectionGUI implements Listener {

    private final XiProtection plugin;
    private final Map<UUID, Integer> playerPageMap = new HashMap<>(); // 存储玩家的当前页面
    private String world_title;
    private String setting_title;
    public ProtectionGUI(XiProtection plugin) {
        this.plugin = plugin;
    }

    public void openWorldSelectionMenu(Player player) {
        FileConfiguration config = plugin.getConfig();
        world_title = config.getString("gui.world-menu-title","选择世界");
        Inventory inventory = Bukkit.createInventory(null, 27, world_title);

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
        if (event.getView().getTitle().equals(world_title)) {
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
        // 设置玩家当前页面为第一页
        playerPageMap.put(player.getUniqueId(), 1);

        FileConfiguration config = plugin.getConfig();
        setting_title = config.getString("gui.setting-menu-title","配置选项 - {world} - 第 {page} 页")
                .replace("{world}",world.getName()).replace("{page}","1");
        Inventory settingsInventory = Bukkit.createInventory(null, 54, setting_title);

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

            settingMenuHandler(world, settingsInventory, slot, slots, material, displayName, function);
        }

        // 设置翻页按钮
        setPaginationButtons(settingsInventory, 1);

        player.openInventory(settingsInventory);
    }


    private void openSettingsMenu_2(Player player, World world) {
        // 设置玩家当前页面为第二页
        playerPageMap.put(player.getUniqueId(), 2);

        FileConfiguration config = plugin.getConfig();
        setting_title = config.getString("gui.setting-menu-title","配置选项 - {world} - 第 {page} 页")
                .replace("{world}",world.getName()).replace("{page}","1");
        Inventory settingsInventory = Bukkit.createInventory(null, 54, setting_title);

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

            settingMenuHandler(world, settingsInventory, slot, slots, material, displayName, function);
        }

        // 设置翻页按钮
        setPaginationButtons(settingsInventory, 2);

        player.openInventory(settingsInventory);
    }


    private void settingMenuHandler(World world, Inventory settingsInventory, int slot, List<Integer> slots, Material material, String displayName, String function) {
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
        ItemStack nextPage = new ItemStack(Objects.requireNonNull(Material.getMaterial(nextPageItem)));
        ItemMeta nextMeta = nextPage.getItemMeta();
        if (nextMeta != null) {
            nextMeta.setDisplayName(nextPageName);
            nextPage.setItemMeta(nextMeta);
        }

        ItemStack previousPage = new ItemStack(Objects.requireNonNull(Material.getMaterial(previousPageItem)));
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
//TODO 检测硬编码需要修改，否则可能导致修改标题后无效。
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
                    // 获取物品的 function 配置
                    String function = getItemFunction(event.getSlot(), (Player)event.getViewers());

                    if ("next-page".equals(function)) {
                        openSettingsMenu_2(player, world); // 转到第二页
                    } else if ("previous-page".equals(function)) {
                        openSettingsMenu_1(player, world); // 返回第一页
                    } else {
                        // 否则执行设置切换
                        toggleSetting(player, event.getSlot(), world);
                    }
                }
            }
        }
    }

    private String getItemFunction(int slot, Player player) {
        FileConfiguration config = plugin.getConfig();

        // 从 Map 中获取玩家的当前页面
        int page = playerPageMap.getOrDefault(player.getUniqueId(), 1);  // 默认是第1页

        ConfigurationSection pageSection = config.getConfigurationSection("gui.page-" + page);
        if (pageSection != null) {
            for (String key : pageSection.getKeys(false)) {
                ConfigurationSection itemSection = pageSection.getConfigurationSection(key);
                if (itemSection != null && itemSection.contains("slot") && itemSection.getInt("slot") == slot) {
                    return itemSection.getString("function", "");
                }
            }
        }
        return ""; // 如果没有找到对应的 function，返回空字符串
    }

    private void toggleSetting(Player player, int slot, World world) {
        FileConfiguration worldConfig = plugin.getWorldConfig(world);
        FileConfiguration mainConfig = plugin.getConfig();

        // 获取玩家当前页面
        int page = playerPageMap.getOrDefault(player.getUniqueId(), 1); // 默认是第1页

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

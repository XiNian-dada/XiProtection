package com.leeinx.xiprotection;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;


/*
* 测试记录：
* Listeners & Functions 全部可用
* Commands 可用
* 理论上已经完全完事了
*
*
*  */

public class XiProtection extends JavaPlugin {
    private ProtectionListener protectionListener;
    private final Map<World, FileConfiguration> worldConfigs = new HashMap<>();
    private final Map<String, String> languageMap = new HashMap<>();
    private int updateInterval; // 更新间隔
    private int itemCheckInterval; // 物品检查间隔
    private int effectCheckInterval; // 效果检查间隔
    private int updateTaskId;
    private int itemCheckTaskId;
    private int effectCheckTaskId;
    public String prefix;
    private boolean debugModeSwitch;
    private Set<Material> foodItems;
    private Set<Material> drinkItems;
    @Override
    public void onEnable() {
        try {
            loadLanguages(); // 加载语言文件
            saveDefaultConfig(); // 创建默认配置文件
            loadGlobalConfig(); // 加载全局配置

            initializeProtectionListener();
            // 注册命令和补全
            if (this.getCommand("xiprotection") != null) {
                this.getCommand("xiprotection").setExecutor(new ProtectionCommand(this));
                this.getCommand("xiprotection").setTabCompleter(new ProtectionTabCompleter(this));
            } else {
                getLogger().warning("命令 'xiprotection' 未找到，请检查 plugin.yml 配置");
            }
            createWorldConfigFiles();
            loadWorldConfigs(); // 加载世界配置
            scheduleTasks(); // 安排定时任务
            
            getLogger().info("XiProtection 插件已成功启用");
        } catch (Exception e) {
            getLogger().severe("插件启动失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadGlobalConfig() {
        updateInterval = getConfig().getInt("update-interval", 600); // 默认30秒
        itemCheckInterval = getConfig().getInt("item-check-interval", 1200); // 默认60秒
        effectCheckInterval = getConfig().getInt("effect-check-interval", 1200); // 默认60秒
        prefix = ChatColor.translateAlternateColorCodes('&',getConfig().getString("prefix","[Xiprotection]"));
        debugModeSwitch = getConfig().getBoolean("debug",false);
        loadConsumablesFromConfig();
    }

    private void initializeProtectionListener() {
        protectionListener = new ProtectionListener(this);
        Bukkit.getPluginManager().registerEvents(protectionListener, this);
        Bukkit.getPluginManager().registerEvents(new ProtectionGUI(this), this);

    }

    private void scheduleTasks() {
        // 取消现有的任务（如果存在）
        cancelTasks();

        // 定时任务更新天气和时间
        updateTaskId = Bukkit.getScheduler().runTaskTimerAsynchronously(this, protectionListener::updateWorldSettings, 0L, updateInterval).getTaskId();
        itemCheckTaskId = Bukkit.getScheduler().runTaskTimerAsynchronously(this, this::checkAllPlayersItems, 0L, itemCheckInterval).getTaskId();
        effectCheckTaskId = Bukkit.getScheduler().runTaskTimerAsynchronously(this, this::checkAllPlayersEffects, 0L, effectCheckInterval).getTaskId();
    }

    private void cancelTasks() {
        // 取消所有任务
        if (updateTaskId != 0) {
            Bukkit.getScheduler().cancelTask(updateTaskId);
            updateTaskId = 0;
        }
        if (itemCheckTaskId != 0) {
            Bukkit.getScheduler().cancelTask(itemCheckTaskId);
            itemCheckTaskId = 0;
        }
        if (effectCheckTaskId != 0) {
            Bukkit.getScheduler().cancelTask(effectCheckTaskId);
            effectCheckTaskId = 0;
        }
    }

    private void checkAllPlayersItems() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            protectionListener.maintainItems(player);
        }
    }

    private void checkAllPlayersEffects() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            protectionListener.handlePotionEffects(player);
        }
    }

    @Override
    public void onDisable() {
        // 取消所有任务
        cancelTasks();
        getLogger().info("XiProtection 插件已成功禁用");
    }

    private void createWorldConfigFiles() {
        // 创建数据文件夹
        File dataFolder = getDataFolder();
        if (!dataFolder.exists()) {
            if (dataFolder.mkdirs()) {
                getLogger().info("数据文件夹创建成功");
            } else {
                getLogger().severe("无法创建数据文件夹！");
                return; // 如果创建失败，不继续执行
            }
        }

        // 创建世界配置文件的目录
        File worldsFolder = new File(dataFolder, "worlds");
        if (!worldsFolder.exists()) {
            if (worldsFolder.mkdirs()) {
                getLogger().info("世界配置文件目录创建成功");
            } else {
                getLogger().severe("无法创建世界配置文件目录！");
                return; // 如果创建失败，不继续执行
            }
        }

        // 为每个世界创建配置文件
        for (World world : Bukkit.getWorlds()) {
            File worldConfigFile = new File(worldsFolder, world.getName() + ".yml");
            if (!worldConfigFile.exists()) {
                try {
                    copyDefaultConfig(worldConfigFile);
                    getLogger().info("已为世界 " + world.getName() + " 创建配置文件");
                } catch (IOException e) {
                    getLogger().severe("无法为世界 " + world.getName() + " 创建配置文件，错误：" + e.getMessage());
                }
            }
        }
    }


    private void copyDefaultConfig(File worldConfigFile) throws IOException {
        InputStream in = getResource("world.yml");
        if (in == null) {
            throw new FileNotFoundException("默认配置文件 world.yml 未找到！");
        }

        try (InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            YamlConfiguration yamlConfig = YamlConfiguration.loadConfiguration(reader);
            yamlConfig.save(worldConfigFile);
        } catch (IOException e) {
            throw e; // 重新抛出异常以便于上层调用处理
        }
    }

    private void loadWorldConfigs() {
        File worldsFolder = new File(getDataFolder(), "worlds"); // 指向新的目录
        for (World world : Bukkit.getWorlds()) {
            File worldConfigFile = new File(worldsFolder, world.getName() + ".yml");
            if (worldConfigFile.exists()) {
                FileConfiguration config = YamlConfiguration.loadConfiguration(worldConfigFile);
                worldConfigs.put(world, config);
                getLogger().info("已成功加载 " + world.getName() + " 的配置文件");
            } else {
                getLogger().warning("未找到世界 " + world.getName() + " 的配置文件！");
                // 为没有配置文件的世界创建一个默认配置
                FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "worlds" + File.separator + world.getName() + ".yml"));
                worldConfigs.put(world, defaultConfig);
                // 尝试创建配置文件
                try {
                    copyDefaultConfig(worldConfigFile);
                    getLogger().info("已为世界 " + world.getName() + " 创建配置文件");
                } catch (IOException e) {
                    getLogger().severe("无法为世界 " + world.getName() + " 创建配置文件，错误：" + e.getMessage());
                }
            }
        }
    }




    public FileConfiguration getWorldConfig(World world) {
        FileConfiguration config = worldConfigs.get(world);
        if (config == null) {
            // 如果配置不存在，尝试创建并加载
            File worldsFolder = new File(getDataFolder(), "worlds");
            File worldConfigFile = new File(worldsFolder, world.getName() + ".yml");
            if (!worldConfigFile.exists()) {
                try {
                    copyDefaultConfig(worldConfigFile);
                    getLogger().info("已为世界 " + world.getName() + " 创建配置文件");
                } catch (IOException e) {
                    getLogger().severe("无法为世界 " + world.getName() + " 创建配置文件，错误：" + e.getMessage());
                }
            }
            config = YamlConfiguration.loadConfiguration(worldConfigFile);
            worldConfigs.put(world, config);
        }
        return config;
    }

    public void saveWorldConfig(World world) {
        FileConfiguration config = worldConfigs.get(world);

        if (config != null) {
            // 获取保存路径
            File file = new File(new File(getDataFolder(), "worlds"), world.getName() + ".yml");

            try {
                // 保存配置
                config.save(file);

                // 输出保存后的日志，确认文件保存成功
                getLogger().info("成功保存世界 " + world.getName() + " 的配置文件");

            } catch (IOException e) {
                // 失败时记录详细的错误信息
                getLogger().severe("无法保存世界 " + world.getName() + " 的配置文件！");
                e.printStackTrace();  // 输出堆栈信息
            }
        } else {
            // 记录找不到配置文件的错误
            getLogger().severe("未找到世界 " + world.getName() + " 的配置文件！");
        }
    }


    public void debugPrint(String text, int importance) {
        if (debugModeSwitch) {
            switch (importance) {
                case 1:
                    getLogger().info(text);
                    break; // 添加 break 语句
                case 2:
                    getLogger().warning(text);
                    break; // 添加 break 语句
                case 3:
                    getLogger().severe(text);
                    break; // 添加 break 语句
                default:
                    getLogger().info("Unknown importance level: " + importance); // 可选：处理未知重要性
            }
        }
    }
    private void loadLanguages() {
        File languageFile = new File(getDataFolder(), "languages.yml");
        if (!languageFile.exists()) {
            saveResource("languages.yml", false); // 复制默认语言文件到数据文件夹
        }

        FileConfiguration languageConfig = YamlConfiguration.loadConfiguration(languageFile);
        for (String key : languageConfig.getKeys(false)) {
            languageMap.put(key, languageConfig.getString(key));
        }
    }

    public String getLanguageText(String key, String defaultValue) {
        String text = languageMap.get(key);
        String message = text != null ? text : defaultValue;
        return ChatColor.translateAlternateColorCodes('&', prefix + message);
    }

    public String getOnEnableText(String key, String defaultValue) {
        String text = languageMap.get(key);
        return text != null ? text : defaultValue;
    }
    public void reloadWorldConfigs() {
        try {
            // 注销旧的监听器
            HandlerList.unregisterAll(protectionListener);
            worldConfigs.clear(); // 清空当前加载的配置
            loadWorldConfigs();   // 重新加载配置文件
            reloadConfig();
            // 重新加载全局配置
            loadGlobalConfig();
            loadLanguages();
            // 重新注册监听器
            protectionListener = new ProtectionListener(this);
            Bukkit.getPluginManager().registerEvents(protectionListener, this);

            // 重新启用定时任务
            scheduleTasks();
            
            getLogger().info("配置重载成功");
        } catch (Exception e) {
            getLogger().severe("配置重载失败：" + e.getMessage());
            e.printStackTrace();
        }
    }
    private void loadConsumablesFromConfig() {
        foodItems = new HashSet<>();
        drinkItems = new HashSet<>();

        List<String> foodList = getConfig().getStringList("consumables.food");
        List<String> drinkList = getConfig().getStringList("consumables.drink");

        for (String item : foodList) {
            Material material = Material.matchMaterial(item.toUpperCase());
            if (material != null) {
                foodItems.add(material);
            } else {
                getLogger().warning(getOnEnableText("invalid-consumable-item", "物品 {item} 不是可消耗物品。 ").replace("{item}",item));
            }
        }

        for (String item : drinkList) {
            Material material = Material.matchMaterial(item.toUpperCase());
            if (material != null) {
                drinkItems.add(material);
            } else {
                getLogger().warning(getOnEnableText("invalid-drinkable-item","物品 {item} 不是可饮用物品。").replace("{item}",item));
            }
        }
    }

    public boolean isFood(Material material) {
        return foodItems.contains(material);
    }

    public boolean isDrink(Material material) {
        return drinkItems.contains(material);
    }
}

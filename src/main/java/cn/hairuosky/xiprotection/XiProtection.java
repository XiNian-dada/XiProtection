package cn.hairuosky.xiprotection;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

//TODO 语言文件放出会乱码
//TODO 测试呗
//TODO 颜色显示似乎有些问题，前缀的颜色首先有很大问题，其次是有些文字的颜色应该被单独强调，不能整句都是一个颜色
public final class XiProtection extends JavaPlugin {
    private ProtectionListener protectionListener;
    private final Map<World, FileConfiguration> worldConfigs = new HashMap<>();
    private Map<String, String> languageMap = new HashMap<>();
    private int updateInterval; // 更新间隔
    private int itemCheckInterval; // 物品检查间隔
    private int effectCheckInterval; // 效果检查间隔
    private int updateTaskId;
    private int itemCheckTaskId;
    private int effectCheckTaskId;
    public String prefix;
    private boolean debugModeSwitch;
    @Override
    public void onEnable() {
        loadLanguages(); // 加载语言文件
        saveDefaultConfig(); // 创建默认配置文件
        loadGlobalConfig(); // 加载全局配置
        initializeProtectionListener();
        // 注册命令和补全
        Objects.requireNonNull(this.getCommand("xiprotection")).setExecutor(new ProtectionCommand(this));
        Objects.requireNonNull(this.getCommand("xiprotection")).setTabCompleter(new ProtectionTabCompleter(this));
        createWorldConfigFiles();
        loadWorldConfigs(); // 加载世界配置
        scheduleTasks(); // 安排定时任务
    }

    private void loadGlobalConfig() {
        updateInterval = getConfig().getInt("update-interval", 600); // 默认30秒
        itemCheckInterval = getConfig().getInt("item-check-interval", 1200); // 默认60秒
        effectCheckInterval = getConfig().getInt("effect-check-interval", 1200); // 默认60秒
        prefix = getConfig().getString("prefix","[Xiprotection]");
        debugModeSwitch = getConfig().getBoolean("debug",true);
    }

    private void initializeProtectionListener() {
        protectionListener = new ProtectionListener(this);
        Bukkit.getPluginManager().registerEvents(protectionListener, this);
        Bukkit.getPluginManager().registerEvents(new ProtectionGUI(this), this);

    }

    private void scheduleTasks() {
        // 取消现有的任务（如果存在）
        if (updateTaskId != 0) {
            Bukkit.getScheduler().cancelTask(updateTaskId);
        }
        if (itemCheckTaskId != 0) {
            Bukkit.getScheduler().cancelTask(itemCheckTaskId);
        }
        if (effectCheckTaskId != 0) {
            Bukkit.getScheduler().cancelTask(effectCheckTaskId);
        }

        // 定时任务更新天气和时间
        updateTaskId = Bukkit.getScheduler().runTaskTimer(this, protectionListener::updateWorldSettings, 0L, updateInterval).getTaskId();
        itemCheckTaskId = Bukkit.getScheduler().runTaskTimer(this, this::checkAllPlayersItems, 0L, itemCheckInterval).getTaskId();
        effectCheckTaskId = Bukkit.getScheduler().runTaskTimer(this, this::checkAllPlayersEffects, 0L, effectCheckInterval).getTaskId();
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
        if (updateTaskId != 0) {
            Bukkit.getScheduler().cancelTask(updateTaskId);
        }
        if (itemCheckTaskId != 0) {
            Bukkit.getScheduler().cancelTask(itemCheckTaskId);
        }
        if (effectCheckTaskId != 0) {
            Bukkit.getScheduler().cancelTask(effectCheckTaskId);
        }
    }

    private void createWorldConfigFiles() {
        // 创建数据文件夹
        File dataFolder = getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
            getLogger().info(getOnEnableText("datafolder-create","数据文件夹创建成功：{datafolder}！").replace("{datafolder}", dataFolder.getAbsolutePath()));
            //getLogger().info("数据文件夹创建成功: " + dataFolder.getAbsolutePath());
        } else {
            getLogger().info(getOnEnableText("datafolder-exist","数据文件夹已存在：{datafolder}！").replace("{datafolder}", dataFolder.getAbsolutePath()));
            //getLogger().info("数据文件夹已存在: " + dataFolder.getAbsolutePath());
        }

        // 创建世界配置文件的目录
        File worldsFolder = new File(dataFolder, "worlds");
        if (!worldsFolder.exists()) {
            worldsFolder.mkdirs();
            getLogger().info(getOnEnableText("worlds-folder-create","世界配置文件目录创建成功：{worlds-folder}！").replace("{worlds-folder}",worldsFolder.getAbsolutePath()));
            //getLogger().info("世界配置文件目录创建成功: " + worldsFolder.getAbsolutePath());
        } else {
            getLogger().info(getOnEnableText("worlds-folder-exist","世界配置文件目录已存在： {worlds-folder}！").replace("{worlds-folder}",worldsFolder.getAbsolutePath()));
            //getLogger().info("世界配置文件目录已存在: " + worldsFolder.getAbsolutePath());
        }

        for (World world : Bukkit.getWorlds()) {
            File worldConfigFile = new File(worldsFolder, world.getName() + ".yml");
            if (!worldConfigFile.exists()) {
                try {
                    copyDefaultConfig(worldConfigFile);
                    getLogger().info(getOnEnableText("world-config-create","已为世界 {world} 创建配置文件：{path}！").replace("{world}",world.getName()).replace("{path}", worldConfigFile.getAbsolutePath()));
                    //getLogger().info("已创建配置文件: " + worldConfigFile.getAbsolutePath());
                } catch (IOException e) {
                    getLogger().info(getOnEnableText("world-config-cannot-create","无法为世界 {world} 创建配置文件，错误：{error}！").replace("{world}",world.getName()).replace("{error}", e.getMessage()));
                    //getLogger().severe("无法创建配置文件: " + world.getName() + ".yml，错误: " + e.getMessage());
                }
            } else {
                getLogger().info(getOnEnableText("world-config-exist","世界 {world} 的配置文件已存在，正在读取！").replace("{world}",world.getName()));
                //getLogger().info("配置文件已存在: " + worldConfigFile.getAbsolutePath());
            }
        }
    }

    private void copyDefaultConfig(File worldConfigFile) throws IOException {
        try (InputStream in = getResource("world.yml");
             InputStreamReader reader = new InputStreamReader(in)) {
            YamlConfiguration yamlConfig = YamlConfiguration.loadConfiguration(reader);
            yamlConfig.save(worldConfigFile);
            getLogger().info(getOnEnableText("copy-config-success","默认配置已成功复制至 {worlds-folder}！").replace("{worlds-folder}",worldConfigFile.getAbsolutePath()));
            //getLogger().info("默认配置已复制到: " + worldConfigFile.getAbsolutePath());
        } catch (IOException e) {
            getLogger().severe(getOnEnableText("copy-config-fail","复制默认配置时出错 {error}！").replace("{error}",e.getMessage()));
            //getLogger().severe("复制默认配置时出错: " + e.getMessage());
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
                getLogger().info(getOnEnableText("load-config-success","已成功加载 {world} 的配置文件！").replace("{world}",world.getName()));
                //getLogger().info("已加载配置文件: " + worldConfigFile.getAbsolutePath());
            } else {
                getLogger().warning(getOnEnableText("load-config-fail","未找到世界 {world} 的配置文件！").replace("{world}",world.getName()));
                //getLogger().warning("未找到配置文件: " + worldConfigFile.getAbsolutePath());
            }
        }
    }



    public FileConfiguration getWorldConfig(World world) {
        return worldConfigs.get(world);
    }

    public void saveWorldConfig(World world) {
        FileConfiguration config = worldConfigs.get(world);
        if (config != null) {
            // 指定保存路径为 worlds 文件夹
            File file = new File(new File(getDataFolder(), "worlds"), world.getName() + ".yml");
            try {
                config.save(file);
                getLogger().info(getOnEnableText("save-config-success","成功保存世界 {world} 的配置文件！").replace("{world}",world.getName()));
            } catch (IOException e) {
                getLogger().severe(getOnEnableText("save-config-fail","无法保存世界 {world} 的配置文件！").replace("{world}",world.getName()));
                //getLogger().severe("无法保存配置文件: " + file.getName());
            }
        } else {
            getLogger().severe(getOnEnableText("config-not-found","未找到世界 {world} 的配置文件，请检查控制台和对应文件夹！").replace("{world}",world.getName()));
            //getLogger().warning("未找到配置文件: " + world.getName());
        }
    }

    public void debugPrint(String text, int importance) {
        if (debugModeSwitch) {
            switch (importance) {
                case 1:
                    getLogger().info(prefix + text);
                    break; // 添加 break 语句
                case 2:
                    getLogger().warning(prefix + text);
                    break; // 添加 break 语句
                case 3:
                    getLogger().severe(prefix + text);
                    break; // 添加 break 语句
                default:
                    getLogger().info(prefix + "Unknown importance level: " + importance); // 可选：处理未知重要性
            }
        }
    }
    private void loadLanguages() {
        File languageFile = new File(getDataFolder(), "languages.yml");
        if (!languageFile.exists()) {
            saveResource("languages.yml", false); // 复制默认语言文件到数据文件夹
            getLogger().info("Languages file has been created: " + languageFile.getAbsolutePath());
        }

        FileConfiguration languageConfig = YamlConfiguration.loadConfiguration(languageFile);
        for (String key : languageConfig.getKeys(false)) {
            languageMap.put(key, languageConfig.getString(key));
        }

        getLogger().info("Loading languages file: " + languageFile.getAbsolutePath());
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
        // 注销旧的监听器
        HandlerList.unregisterAll(protectionListener);
        worldConfigs.clear(); // 清空当前加载的配置
        loadWorldConfigs();   // 重新加载配置文件

        // 重新加载全局配置
        loadGlobalConfig();

        // 重新注册监听器
        protectionListener = new ProtectionListener(this);
        Bukkit.getPluginManager().registerEvents(protectionListener, this);

        // 重新启用定时任务
        scheduleTasks();
    }
}

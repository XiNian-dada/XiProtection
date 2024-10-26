package cn.hairuosky.xiprotection;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public final class XiProtection extends JavaPlugin {
    private ProtectionListener protectionListener;
    @Override
    public void onEnable() {
        // 注册事件监听器
        protectionListener = new ProtectionListener(this);
        Bukkit.getPluginManager().registerEvents(protectionListener, this);
        createWorldConfigFiles();

        // 定时任务更新天气和时间
        Bukkit.getScheduler().runTaskTimer(this, protectionListener::updateWorldSettings, 0L, 20L); // 每秒更新一次
        Bukkit.getScheduler().runTaskTimer(this, () -> checkAllPlayersItems(protectionListener), 0L, 1200L); // 每 60 秒
    }
    private void checkAllPlayersItems(ProtectionListener protectionListener) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            protectionListener.maintainItems(player);
        }
    }
    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void createWorldConfigFiles() {
        // 获取插件数据目录
        File dataFolder = getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        // 遍历所有世界
        for (World world : Bukkit.getWorlds()) {
            File worldConfigFile = new File(dataFolder, world.getName() + ".yml");
            if (!worldConfigFile.exists()) {
                try {
                    // 从内置模板复制配置文件
                    copyDefaultConfig(worldConfigFile);
                } catch (IOException e) {
                    getLogger().severe("无法创建配置文件: " + world.getName() + ".yml");
                }
            }
        }
    }

    private void copyDefaultConfig(File worldConfigFile) throws IOException {
        // 创建输出流
        try (InputStream in = getResource("world.yml"); // 确保模板文件在资源文件夹中
             InputStreamReader reader = new InputStreamReader(in)) {
            // 使用 YAML 读取模板
            YamlConfiguration yamlConfig = YamlConfiguration.loadConfiguration(reader);
            yamlConfig.save(worldConfigFile); // 保存到目标文件
        }
    }

    public FileConfiguration getWorldConfig(World world) {
        // 获取世界的配置文件
        File worldConfigFile = new File(getDataFolder(), world.getName() + ".yml");
        if (worldConfigFile.exists()) {
            return YamlConfiguration.loadConfiguration(worldConfigFile);
        }
        return null;
    }
}

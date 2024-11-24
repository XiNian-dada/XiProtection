package cn.hairuosky.xiprotection;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import java.util.Arrays;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProtectionListener implements Listener {
    private final XiProtection plugin;

    public ProtectionListener(XiProtection plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        FileConfiguration config = plugin.getWorldConfig(world);

        if (player.hasPermission("xiprotection.bypass.*") || player.hasPermission("xiprotection.bypass.break")) return;

        if (config != null && config.getBoolean("enable") && config.getBoolean("protect.anti-break")) {
            event.setCancelled(true);
            player.sendMessage(plugin.getLanguageText("cannot-break","你无法在这个世界破坏方块！"));
            //player.sendMessage("你无法在这个世界破坏方块。");
        }
    }


    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        FileConfiguration config = plugin.getWorldConfig(world);

        if (player.hasPermission("xiprotection.bypass.*") || player.hasPermission("xiprotection.bypass.place")) return;

        if (config != null && config.getBoolean("enable") && config.getBoolean("protect.anti-place")) {
            event.setCancelled(true);
            player.sendMessage(plugin.getLanguageText("cannot-place","你无法在这个世界放置方块！"));
            //player.sendMessage("你无法在这个世界放置方块。");
        }
    }
    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        World world = event.getBlock().getWorld();
        FileConfiguration config = plugin.getWorldConfig(world);

        if (config != null && config.getBoolean("enable") && config.getBoolean("protect.anti-fire")) {
            event.setCancelled(true);
        }
    }
    // 定时检查并保持天气和时间状态
    public void updateWorldSettings() {
        for (World world : Bukkit.getWorlds()) {
            FileConfiguration config = plugin.getWorldConfig(world);
            if (config != null && config.getBoolean("enable")) {
                // 控制时间
                boolean alwaysDay = config.getBoolean("protect.always-day");
                boolean alwaysNight = config.getBoolean("protect.always-night");

                if (alwaysDay && alwaysNight) {
                    notifyOps(world, plugin.getLanguageText(
                            "time-settings-not-proper",
                            "警告：世界设置为同时永远白天和永远黑夜，这会导致冲突。请检查配置！"
                    ));
                } else if (alwaysDay) {
                    world.setTime(6000); // 正午12点
                } else if (alwaysNight) {
                    world.setTime(18000); // 午夜12点
                }

                // 控制天气
                boolean alwaysRain = config.getBoolean("protect.always-rain");
                boolean alwaysSun = config.getBoolean("protect.always-sun");

                if (alwaysRain && alwaysSun) {
                    notifyOps(world, plugin.getLanguageText(
                            "weather-settings-not-proper",
                            "警告：世界设置为同时永远下雨和永远晴天，这会导致冲突。请检查配置！"
                    ));
                } else if (alwaysRain) {
                    world.setStorm(true); // 设置为下雨
                } else if (alwaysSun) {
                    world.setStorm(false); // 设置为晴天
                }
            }
        }
    }

    @EventHandler
    public void onShear(PlayerShearEntityEvent event) {
        Player player = event.getPlayer();
        World world = event.getEntity().getWorld();
        FileConfiguration config = plugin.getWorldConfig(world);

        if (player.hasPermission("xiprotection.bypass.*") || player.hasPermission("xiprotection.bypass.shear")) return;

        if (config != null && config.getBoolean("enable") && config.getBoolean("protect.anti-shear")) {
            event.setCancelled(true);
            if (event.getEntity().getType().equals(EntityType.SHEEP)) {
                player.sendMessage(plugin.getLanguageText("cannot-shear","你不能在这个世界剪羊毛！"));
                //player.sendMessage("你无法在这个世界剪羊毛。");
            }
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        Player player = (Player) event.getEntity();
        World world = player.getWorld();
        FileConfiguration config = plugin.getWorldConfig(world);

        if (player.hasPermission("xiprotection.bypass.*") || player.hasPermission("xiprotection.bypass.hunger")) return;

        if (config != null && config.getBoolean("enable") && config.getBoolean("protect.keep-full-hunger")) {
            event.setFoodLevel(20); // 设置为满饱食度
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            Player victim = (Player) event.getEntity();
            World world = attacker.getWorld();
            FileConfiguration config = plugin.getWorldConfig(world);

            if (attacker.hasPermission("xiprotection.bypass.*") || attacker.hasPermission("xiprotection.bypass.pvp")) return;

            if (config != null && config.getBoolean("enable") && config.getBoolean("protect.anti-pvp")) {
                event.setCancelled(true);
                String text = plugin.getLanguageText("cannot-pvp","你不能在这个世界中进行PVP！");
                attacker.sendMessage(text);
                victim.sendMessage(text);
            }
        }
    }

    @EventHandler
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        World world = event.getEntity().getWorld();
        FileConfiguration config = plugin.getWorldConfig(world);

        if (config != null && config.getBoolean("enable") && config.getBoolean("protect.prevent-explosion")) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            World world = player.getWorld();
            FileConfiguration config = plugin.getWorldConfig(world);

            // 权限检查
            if (player.hasPermission("xiprotection.bypass.*") || player.hasPermission("xiprotection.bypass.health")) {
                return; // 如果有权限，直接返回
            }

            // 检查配置
            if (config != null && config.getBoolean("enable") && config.getBoolean("protect.keep-full-health")) {
                event.setCancelled(true); // 取消伤害

                // 安全地获取最大生命值
                AttributeInstance healthAttribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                if (healthAttribute != null) {
                    player.setHealth(healthAttribute.getValue()); // 恢复满生命值
                } else {
                    plugin.getLogger().warning("Player " + player.getName() + " does not have a GENERIC_MAX_HEALTH attribute.");
                    player.setHealth(20.0); // 回退到默认值（20.0是Minecraft默认最大生命值）
                }
            }
        }
    }


    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        World world = event.getLocation().getWorld();
        FileConfiguration config = plugin.getWorldConfig(world);

        if (config != null && config.getBoolean("enable") && config.getBoolean("protect.prevent-explosion")) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onBlockChange(EntityChangeBlockEvent event) {
        World world = event.getBlock().getWorld();
        FileConfiguration config = plugin.getWorldConfig(world);
        Entity entity = event.getEntity();

        // 检查配置
        if (config != null && config.getBoolean("enable") && config.getBoolean("protect.prevent-treading")) {
            // 如果耕地被踩踏
            if (event.getBlock().getType() == Material.FARMLAND) {
                // 处理生物
                if (entity instanceof Player) {
                    Player player = (Player) entity;
                    // 权限检查：如果是玩家并且有权限，直接返回
                    if (player.hasPermission("xiprotection.bypass.*") || player.hasPermission("xiprotection.bypass.treading")) {
                        return; // 如果有权限，直接返回
                    }
                    // 取消事件并发送消息
                    event.setCancelled(true);
                    player.sendMessage(plugin.getLanguageText("cannot-tread-farmland","你不能在这个世界中踩踏任何耕地！保护粮食好吗！"));
                    //player.sendMessage("在这个世界中，耕地不能被踩踏。");
                } else {
                    // 对于非玩家实体，直接取消事件
                    event.setCancelled(true);
                    //plugin.getLogger().info(entity.getType() + " 尝试踩踏耕地。");
                    plugin.debugPrint(entity.getType() + "尝试踩踏耕地",1);
                }
            }
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        // 仅检查特定类型的投掷物
        if (event.getEntity() instanceof ThrownPotion || event.getEntity() instanceof Snowball) {
            World world = event.getEntity().getWorld();
            FileConfiguration config = plugin.getWorldConfig(world);

            // 检查投掷物的射手是否是玩家
            if (event.getEntity().getShooter() instanceof Player) {
                Player player = (Player) event.getEntity().getShooter();

                // 权限检查
                if (player.hasPermission("xiprotection.bypass.*") || player.hasPermission("xiprotection.bypass.throwables")) {
                    return; // 如果有权限，直接返回
                }

                // 检查配置并取消事件
                if (config != null && config.getBoolean("enable") && config.getBoolean("protect.prevent-throwables")) {
                    event.setCancelled(true);
                    player.sendMessage(plugin.getLanguageText("cannot-use-projectile", "你不能在这个世界中使用任何投掷物！"));
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        //TODO 压力板检测无效
        //TODO 拌线勾可正常使用，但是无法右键
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        // 如果没有点击任何方块，直接返回
        if (block == null) return;

        World world = player.getWorld();
        FileConfiguration config = plugin.getWorldConfig(world);

        // 权限检查
        if (player.hasPermission("xiprotection.bypass.*") || player.hasPermission("xiprotection.bypass.interactions")) {
            return; // 如果有权限，直接返回
        }

        if (config != null && config.getBoolean("enable")) {
            // 获取交互类型设置
            ConfigurationSection interactConfig = config.getConfigurationSection("protect.prevent-interactions");

            if (interactConfig != null) {
                String blockTypeName = block.getType().name().toLowerCase();

                // 通用检查：直接从配置获取是否禁止
                if (interactConfig.getBoolean(blockTypeName, false)) {
                    cancelInteraction(player, blockTypeName, event);
                    return;
                }

                // 判断版本
                String version = getServerVersion();
                boolean isLegacy = version.startsWith("v1_12"); // 判断是否为 1.12.x 或以下

                BlockState state = block.getState();

                // 高版本特有的方块检查（仅在非低版本情况下处理）
                if (!isLegacy) {
                    Material[] highVersionMaterials = {
                            Material.BELL, Material.COMPOSTER, Material.BEEHIVE, Material.BEE_NEST, Material.STONECUTTER,
                            Material.CARTOGRAPHY_TABLE, Material.GRINDSTONE, Material.SMITHING_TABLE, Material.BARREL,
                            Material.DAYLIGHT_DETECTOR
                    };

                    for (Material material : highVersionMaterials) {
                        if (block.getType() == material && interactConfig.getBoolean(material.name().toLowerCase(), false)) {
                            cancelInteraction(player, material.name().toLowerCase(), event);
                            return;
                        }
                    }
                }

                // 活版门、门、栅栏门
                if (block.getType().name().contains("TRAPDOOR") && interactConfig.getBoolean("trapdoor", false)) {
                    cancelInteraction(player, "trapdoor", event);
                    return;
                }
                if (block.getType().name().contains("DOOR") && interactConfig.getBoolean("door", false)) {
                    cancelInteraction(player, "door", event);
                    return;
                }
                if (block.getType().name().contains("FENCE_GATE") && interactConfig.getBoolean("fence_gate", false)) {
                    cancelInteraction(player, "fence_gate", event);
                    return;
                }

                // 按钮、拉杆、绊线钩
                if (state.getBlockData() instanceof org.bukkit.block.data.type.Switch) {
                    if (block.getType().name().contains("BUTTON") && interactConfig.getBoolean("button", false)) {
                        cancelInteraction(player, "button", event);
                        return;
                    }
                    if (block.getType() == Material.LEVER && interactConfig.getBoolean("lever", false)) {
                        cancelInteraction(player, "lever", event);
                        return;
                    }
                    if (!isLegacy && block.getType() == Material.TRIPWIRE_HOOK && interactConfig.getBoolean("tripwire_hook", false)) {
                        cancelInteraction(player, "tripwire_hook", event);
                        return;
                    }
                }

                // 容器方块
                Material[] containerBlocks = {
                        Material.FURNACE, Material.CHEST, Material.ENDER_CHEST, Material.SHULKER_BOX,
                        Material.DISPENSER, Material.DROPPER, Material.HOPPER, Material.BARREL
                };

                for (Material containerBlock : containerBlocks) {
                    if (block.getType() == containerBlock && interactConfig.getBoolean(containerBlock.name().toLowerCase(), false)) {
                        cancelInteraction(player, containerBlock.name().toLowerCase(), event);
                        return;
                    }
                }

                // 其他特殊方块检查
                Material[] specialBlocks = {
                        Material.BREWING_STAND, Material.BEACON, Material.ENCHANTING_TABLE, Material.ANVIL,
                        Material.LECTERN, Material.CAULDRON
                };

                for (Material specialBlock : specialBlocks) {
                    if (block.getType() == specialBlock && interactConfig.getBoolean(specialBlock.name().toLowerCase(), false)) {
                        cancelInteraction(player, specialBlock.name().toLowerCase(), event);
                        return;
                    }
                }

                // 音乐方块和床
                if (block.getType() == Material.NOTE_BLOCK && interactConfig.getBoolean("note_block", false)) {
                    cancelInteraction(player, "note_block", event);
                    return;
                }
                if (block.getType() == Material.JUKEBOX && interactConfig.getBoolean("jukebox", false)) {
                    cancelInteraction(player, "jukebox", event);
                    return;
                }
                if (block.getType().name().contains("BED") && interactConfig.getBoolean("bed", false)) {
                    cancelInteraction(player, "bed", event);
                }
            }
        }
    }

    // 取消交互并发送消息的方法
    private void cancelInteraction(Player player, String blockName, PlayerInteractEvent event) {
        event.setCancelled(true);
        sendCannotInteractMessage(player, blockName);
    }



    /**
     * 获取服务器版本。
     */
    private String getServerVersion() {
        String packageName = plugin.getServer().getClass().getPackage().getName();
        return packageName.substring(packageName.lastIndexOf('.') + 1);
    }

    void maintainItems(Player player) {
        World world = player.getWorld();
        FileConfiguration config = plugin.getWorldConfig(world);

        // 权限检查
        if (player.hasPermission("xiprotection.bypass.*") || player.hasPermission("xiprotection.bypass.keep-items")) {
            return; // 如果有权限，直接返回
        }

        if (config != null && config.getBoolean("enable") && config.getBoolean("protect.keep-items-enabled")) {
            List<Map<?, ?>> itemsToKeep = config.getMapList("protect.keep-items"); // 获取物品列表

            for (Map<?, ?> item : itemsToKeep) {
                String itemName = (String) item.get("item");
                Integer quantity = (Integer) item.get("quantity"); // 使用 Integer 以处理 null

                if (itemName != null && quantity != null) {
                    Material material = Material.getMaterial(itemName);
                    if (material != null) {
                        // 检查当前玩家的背包中是否缺少物品
                        int currentQuantity = getItemCount(player, material);
                        if (currentQuantity < quantity) {
                            int missingQuantity = quantity - currentQuantity;
                            ItemStack itemStack = new ItemStack(material, missingQuantity);
                            player.getInventory().addItem(itemStack);

                            // 替换占位符并发送消息
                            player.sendMessage(plugin.getLanguageText(
                                            "maintain-items",
                                            "你的背包中缺少了 {quantity} 个 {item}，已经为您自动添加！")
                                    .replace("{quantity}", String.valueOf(missingQuantity))
                                    .replace("{item}", itemName));
                        }
                    }
                }
            }
        }
    }


    // 辅助方法：统计背包中某种物品的数量
    private int getItemCount(Player player, Material material) {
        int count = 0;
        for (ItemStack itemStack : player.getInventory()) {
            if (itemStack != null && itemStack.getType() == material) {
                count += itemStack.getAmount();
            }
        }
        return count;
    }


    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        FileConfiguration config = plugin.getWorldConfig(world);

        // 权限检查
        if (player.hasPermission("xiprotection.bypass.*") || player.hasPermission("xiprotection.bypass.banned-commands")) {
            return; // 如果有权限，直接返回
        }

        // 检查开关
        if (config != null && config.getBoolean("enable") && config.getBoolean("protect.banned-commands-enabled")) {
            List<String> bannedCommands = config.getStringList("protect.banned-commands");

            String command = event.getMessage().toLowerCase();
            plugin.getLogger().info("玩家输入的命令: " + command);
            plugin.getLogger().info("禁用命令列表: " + bannedCommands);

            for (String banned : bannedCommands) {
                if (command.startsWith(banned.toLowerCase())) {
                    player.sendMessage(plugin.getLanguageText("cannot-use-specific-command","你不能使用这个指令！"));
                    //player.sendMessage("你不能使用这个命令。"); // 提示信息
                    event.setCancelled(true); // 取消命令执行
                    break;
                }
            }
        }
    }
    @EventHandler
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        Material material = item.getType();

        // 判断是吃东西还是喝东西
        if (plugin.isFood(material)) {
            handleConsume(event, "eating", "cannot-eat", plugin.getLanguageText("cannot-eat","你不能在这个世界中吃东西！"), "protect.prevent-eating");
        } else if (plugin.isDrink(material)) {
            handleConsume(event, "drinking", "cannot-drink", plugin.getLanguageText("cannot-drink","你不能在这个世界中喝东西！"), "protect.prevent-drinking");
        }
    }

    private void handleConsume(PlayerItemConsumeEvent event, String action, String langKey, String defaultMsg, String configKey) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        FileConfiguration config = plugin.getWorldConfig(world);

        // 权限检查
        if (player.hasPermission("xiprotection.bypass.*") || player.hasPermission("xiprotection.bypass." + action)) {
            return; // 如果有权限，直接返回
        }

        // 检查配置并取消事件
        if (config != null && config.getBoolean("enable") && config.getBoolean(configKey)) {
            player.sendMessage(plugin.getLanguageText(langKey, defaultMsg));
            event.setCancelled(true);
        }
    }

    public void handlePotionEffects(Player player) {
        World world = player.getWorld();
        FileConfiguration config = plugin.getWorldConfig(world);
        int duration = plugin.getConfig().getInt("effect-check-interval", 600);

        // 日志：记录方法调用和基本信息
        plugin.getLogger().info("Handling potion effects for player: " + player.getName() + " in world: " + world.getName());

        // 权限检查
        if (player.hasPermission("xiprotection.bypass.*") || player.hasPermission("xiprotection.bypass.effects")) {
            plugin.getLogger().info("Player " + player.getName() + " has bypass permission. Skipping potion effect handling.");
            return;
        }

        if (config != null) {
            // 检查是否启用功能
            boolean isEnabled = config.getBoolean("enable", false);
            if (!isEnabled) {
                plugin.getLogger().info("Potion effect handling is disabled in world: " + world.getName());
                return;
            }

            boolean preventEffectsEnabled = config.getBoolean("protect.prevent-potion-effects-enabled", false);
            boolean keepEffectsEnabled = config.getBoolean("protect.keep-potion-effects-enabled", false);

            plugin.getLogger().info("Potion effects settings: preventEffectsEnabled=" + preventEffectsEnabled + ", keepEffectsEnabled=" + keepEffectsEnabled);

            Set<String> preventEffectsSet = new HashSet<>(config.getStringList("protect.prevent-potion-effects"));

            // 日志：记录屏蔽效果列表
            plugin.getLogger().info("Effects to prevent: " + preventEffectsSet);

            // 处理保持的药水效果
            if (keepEffectsEnabled) {
                List<Map<?, ?>> keepEffectsList = config.getMapList("protect.keep-potion-effects");
                plugin.getLogger().info("Effects to keep: " + keepEffectsList);

                for (Map<?, ?> effectMap : keepEffectsList) {
                    String effectName = (String) effectMap.get("effect");
                    Integer level = (Integer) effectMap.get("level");

                    // 添加药水效果
                    PotionEffectType effectType = PotionEffectType.getByName(effectName);
                    if (effectType != null && level != null) {
                        player.addPotionEffect(new PotionEffect(effectType, duration + 100, level - 1, true, false));
                        plugin.getLogger().info("Applied potion effect: " + effectName + ", level: " + level);
                    } else {
                        plugin.getLogger().warning("Failed to apply potion effect: " + effectName + ". Please check the configuration.");
                    }
                }
            }

            // 屏蔽药水效果
            if (preventEffectsEnabled) {
                plugin.getLogger().info("Checking active potion effects for removal.");
                for (PotionEffect potionEffect : player.getActivePotionEffects()) {
                    String potionEffectName = potionEffect.getType().getName();
                    boolean isPrevented = preventEffectsSet.contains(potionEffectName);

                    if (isPrevented) {
                        player.removePotionEffect(potionEffect.getType());
                        plugin.getLogger().info("Removed potion effect: " + potionEffectName);
                    } else {
                        plugin.getLogger().info("Potion effect not prevented: " + potionEffectName);
                    }
                }
            }
        } else {
            plugin.getLogger().warning("Configuration for world " + world.getName() + " not found. Please check plugin setup.");
            notifyOps(world, "配置未找到，请检查插件设置。");
        }
    }


    /**
     * 动态发送不能交互的消息
     *
     * @param player 玩家
     * @param type   物品类型（语言键）
     */
    private void sendCannotInteractMessage(Player player, String type) {
        String defaultMessage = "你不能使用这个物品！"; // 默认消息
        String messageKey = "cannot-use-" + type;
        player.sendMessage(plugin.getLanguageText(messageKey, defaultMessage));
    }

    private void notifyOps(World world, String message) {
        boolean hasOp = false;

        // 首先检查是否有 OP 玩家
        for (Player player : world.getPlayers()) {
            if (player.isOp()) {
                player.sendMessage(message);
                hasOp = true;
            }
        }

        // 如果没有 OP 玩家，发送给所有普通玩家
        if (!hasOp) {
            String warningMessage = "请联系服务器管理员: " + message;
            for (Player player : world.getPlayers()) {
                player.sendMessage(warningMessage);
            }
        }
    }




}

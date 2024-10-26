package cn.hairuosky.xiprotection;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

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
            player.sendMessage("你无法在这个世界破坏方块。");
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
            player.sendMessage("你无法在这个世界放置方块。");
        }
    }
    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        World world = event.getBlock().getWorld();
        FileConfiguration config = plugin.getWorldConfig(world);

        if (config != null && config.getBoolean("enable") && config.getBoolean("protect.anti-fire")) {
            event.setCancelled(true);
            event.getIgnitingEntity().sendMessage("你无法在这个世界引燃火焰。");
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
                    notifyOps(world, "警告：世界设置为同时永远白天和永远黑夜，这会导致冲突。请检查配置。");
                } else if (alwaysDay && !alwaysNight) {
                    world.setTime(6000); // 正午12点
                } else if (alwaysNight && !alwaysDay) {
                    world.setTime(18000); // 午夜12点
                }

                // 控制天气
                boolean alwaysRain = config.getBoolean("protect.always-rain");
                boolean alwaysSun = config.getBoolean("protect.always-sun");

                if (alwaysRain && alwaysSun) {
                    notifyOps(world, "警告：世界设置为同时永远下雨和永远晴天，这会导致冲突。请检查配置。");
                } else if (alwaysRain && !alwaysSun) {
                    world.setStorm(true);
                } else if (alwaysSun && !alwaysRain) {
                    world.setStorm(false);
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
                player.sendMessage("你无法在这个世界剪羊毛。");
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
                attacker.sendMessage("在这个世界中禁止 PvP。");
                victim.sendMessage("在这个世界中禁止 PvP。");
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
            if (player.hasPermission("xiprotection.bypass.*") || player.hasPermission("xiprotection.bypass.health")) return;

            if (config != null && config.getBoolean("enable") && config.getBoolean("protect.keep-full-health")) {
                event.setCancelled(true); // 取消伤害
                player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()); // 恢复满生命值
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

        // 权限检查
        if (event.getEntity() instanceof Player && ((Player) event.getEntity()).hasPermission("xiprotection.bypass.*") ||
                ((Player) event.getEntity()).hasPermission("xiprotection.bypass.treading")) {
            return; // 如果有权限，直接返回
        }

        if (config != null && config.getBoolean("enable") && config.getBoolean("protect.prevent-treading")) {
            if (event.getBlock().getType() == Material.FARMLAND) {
                event.setCancelled(true);
                event.getEntity().sendMessage("在这个世界中，耕地不能被踩踏。");
            }
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity() instanceof ThrownPotion || event.getEntity() instanceof Snowball) {
            World world = event.getEntity().getWorld();
            FileConfiguration config = plugin.getWorldConfig(world);

            // 权限检查
            if (event.getEntity().getShooter() instanceof Player &&
                    ((Player) event.getEntity().getShooter()).hasPermission("xiprotection.bypass.*") ||
                    ((Player) event.getEntity().getShooter()).hasPermission("xiprotection.bypass.throwables")) {
                return; // 如果有权限，直接返回
            }

            if (config != null && config.getBoolean("enable") && config.getBoolean("protect.prevent-throwables")) {
                event.setCancelled(true);
                if (event.getEntity().getShooter() instanceof Player) {
                    Player player = (Player) event.getEntity().getShooter();
                    player.sendMessage("在这个世界中，禁止使用投掷物。");
                }
            }
        }
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

            if (itemsToKeep != null) { // 确保 itemsToKeep 不是 null
                for (Map<?, ?> item : itemsToKeep) {
                    String itemName = (String) item.get("item");
                    Integer quantity = (Integer) item.get("quantity"); // 使用 Integer 以处理 null

                    if (itemName != null && quantity != null) {
                        Material material = Material.getMaterial(itemName);
                        if (material != null) {
                            ItemStack itemStack = new ItemStack(material, quantity);
                            if (!player.getInventory().contains(itemStack.getType(), quantity)) {
                                player.getInventory().addItem(itemStack);
                                player.sendMessage("你背包中缺少" + quantity + "个" + itemName + "，已为你添加。");
                            }
                        }
                    }
                }
            }
        }
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
            for (String banned : bannedCommands) {
                if (command.startsWith(banned.toLowerCase())) {
                    player.sendMessage("你不能使用这个命令。"); // 提示信息
                    event.setCancelled(true); // 取消命令执行
                    break;
                }
            }
        }
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

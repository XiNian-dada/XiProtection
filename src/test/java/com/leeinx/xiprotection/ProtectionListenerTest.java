package com.leeinx.xiprotection;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mockito.Mockito;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ProtectionListenerTest {

    private ProtectionListener listener;
    
    @Mock
    private XiProtection plugin;
    
    @Mock
    private World world;
    
    @Mock
    private Block block;
    
    @Mock
    private Sheep sheep;
    
    @Mock
    private Player player;
    
    @Mock
    private FileConfiguration pluginConfig;

    @BeforeEach
    public void setUp() {
        // 模拟插件配置
        when(plugin.getConfig()).thenReturn(pluginConfig);
        when(pluginConfig.getInt(anyString(), anyInt())).thenReturn(600);
        
        // 模拟世界配置
        FileConfiguration worldConfig = mock(FileConfiguration.class);
        when(plugin.getWorldConfig(world)).thenReturn(worldConfig);
        when(worldConfig.getBoolean("enable")).thenReturn(true);
        
        // 模拟插件方法
        when(plugin.isFood(any())).thenReturn(false);
        when(plugin.isDrink(any())).thenReturn(false);
        when(plugin.getLanguageText(anyString(), anyString())).thenAnswer(invocation -> {
            String defaultValue = invocation.getArgument(1);
            return defaultValue;
        });
        
        // 移除对plugin.getServer()的模拟，因为这个方法可能是final的
        
        // 初始化监听器
        listener = new ProtectionListener(plugin);
    }
    
    @AfterEach
    public void tearDown() {
        // 强制验证 Mockito 使用情况，这有助于发现是哪个测试留下了烂摊子
        // 如果是上一个测试没跑完导致的，这行代码会抛出异常告诉你具体位置
        Mockito.validateMockitoUsage();
        // 清理测试环境
    }


    // 测试方块破坏限制
    @Test
    public void testBlockBreakRestriction() {
        BlockBreakEvent event = mock(BlockBreakEvent.class);
        FileConfiguration worldConfig = mock(org.bukkit.configuration.file.FileConfiguration.class);
        
        when(event.getPlayer()).thenReturn(player);
        when(player.getWorld()).thenReturn(world);
        when(player.hasPermission("xiprotection.bypass.*")).thenReturn(false);
        when(player.hasPermission("xiprotection.bypass.break")).thenReturn(false);
        when(plugin.getWorldConfig(world)).thenReturn(worldConfig);
        when(worldConfig.getBoolean("enable")).thenReturn(true);
        when(worldConfig.getBoolean("protect.anti-break")).thenReturn(true);

        // 执行事件处理
        listener.onBlockBreak(event);

        // 验证事件被取消
        verify(event, times(1)).setCancelled(true);
    }

    // 测试方块破坏权限
    @Test
    public void testBlockBreakPermission() {
        BlockBreakEvent event = mock(BlockBreakEvent.class);
        
        when(event.getPlayer()).thenReturn(player);
        when(player.getWorld()).thenReturn(world);
        when(player.hasPermission("xiprotection.bypass.break")).thenReturn(true);

        // 执行事件处理
        listener.onBlockBreak(event);

        // 验证事件未被取消
        verify(event, never()).setCancelled(anyBoolean());
    }

    // 测试方块放置限制
    @Test
    public void testBlockPlaceRestriction() {
        BlockPlaceEvent event = mock(BlockPlaceEvent.class);
        FileConfiguration worldConfig = mock(org.bukkit.configuration.file.FileConfiguration.class);
        
        when(event.getPlayer()).thenReturn(player);
        when(player.getWorld()).thenReturn(world);
        when(player.hasPermission("xiprotection.bypass.*")).thenReturn(false);
        when(player.hasPermission("xiprotection.bypass.place")).thenReturn(false);
        when(plugin.getWorldConfig(world)).thenReturn(worldConfig);
        when(worldConfig.getBoolean("enable")).thenReturn(true);
        when(worldConfig.getBoolean("protect.anti-place")).thenReturn(true);

        // 执行事件处理
        listener.onBlockPlace(event);

        // 验证事件被取消
        verify(event, times(1)).setCancelled(true);
    }

    // 测试方块放置权限
    @Test
    public void testBlockPlacePermission() {
        BlockPlaceEvent event = mock(BlockPlaceEvent.class);
        
        when(event.getPlayer()).thenReturn(player);
        when(player.getWorld()).thenReturn(world);
        when(player.hasPermission("xiprotection.bypass.place")).thenReturn(true);

        // 执行事件处理
        listener.onBlockPlace(event);

        // 验证事件未被取消
        verify(event, never()).setCancelled(anyBoolean());
    }



    // 测试饥饿管理
    @Test
    public void testHungerManagement() {
        FoodLevelChangeEvent event = mock(FoodLevelChangeEvent.class);
        when(event.getEntity()).thenReturn(player);
        when(player.getWorld()).thenReturn(world);

        // 模拟权限检查
        when(player.hasPermission("xiprotection.bypass.*")).thenReturn(false);
        when(player.hasPermission("xiprotection.bypass.hunger")).thenReturn(false);

        // 模拟配置检查
        FileConfiguration worldConfig = mock(org.bukkit.configuration.file.FileConfiguration.class);
        when(plugin.getWorldConfig(world)).thenReturn(worldConfig);
        when(worldConfig.getBoolean("enable")).thenReturn(true);
        when(worldConfig.getBoolean("protect.keep-full-hunger")).thenReturn(true);

        // 执行事件处理
        listener.onFoodLevelChange(event);

        // 验证饥饿值被设置为满
        verify(event, times(1)).setFoodLevel(20);
    }

    // 测试生命值管理
    @Test
    public void testHealthManagement() {
        EntityDamageEvent event = mock(EntityDamageEvent.class);
        when(event.getEntity()).thenReturn(player);
        when(player.getWorld()).thenReturn(world);

        // 模拟权限检查
        when(player.hasPermission("xiprotection.bypass.*")).thenReturn(false);
        when(player.hasPermission("xiprotection.bypass.health")).thenReturn(false);

        // 模拟配置检查
        FileConfiguration worldConfig = mock(org.bukkit.configuration.file.FileConfiguration.class);
        when(plugin.getWorldConfig(world)).thenReturn(worldConfig);
        when(worldConfig.getBoolean("enable")).thenReturn(true);
        when(worldConfig.getBoolean("protect.keep-full-health")).thenReturn(true);

        // 模拟属性
        org.bukkit.attribute.AttributeInstance healthAttribute = mock(org.bukkit.attribute.AttributeInstance.class);
        when(healthAttribute.getValue()).thenReturn(20.0);
        when(player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH)).thenReturn(healthAttribute);

        // 执行事件处理
        listener.onPlayerDamage(event);

        // 验证事件被取消
        verify(event, times(1)).setCancelled(true);
    }

    // 测试 PvP 限制
    @Test
    public void testPvpRestriction() {
        Player victim = mock(Player.class);
        EntityDamageByEntityEvent event = mock(EntityDamageByEntityEvent.class);
        when(event.getDamager()).thenReturn(player);
        when(event.getEntity()).thenReturn(victim);
        when(player.getWorld()).thenReturn(world);

        // 模拟权限检查
        when(player.hasPermission("xiprotection.bypass.*")).thenReturn(false);
        when(player.hasPermission("xiprotection.bypass.pvp")).thenReturn(false);

        // 模拟配置检查
        FileConfiguration worldConfig = mock(org.bukkit.configuration.file.FileConfiguration.class);
        when(plugin.getWorldConfig(world)).thenReturn(worldConfig);
        when(worldConfig.getBoolean("enable")).thenReturn(true);
        when(worldConfig.getBoolean("protect.anti-pvp")).thenReturn(true);

        // 执行事件处理
        listener.onPlayerDamage(event);

        // 验证事件被取消
        verify(event, times(1)).setCancelled(true);
    }

    // 测试 PvP 权限
    @Test
    public void testPvpPermission() {
        Player victim = mock(Player.class);
        EntityDamageByEntityEvent event = mock(EntityDamageByEntityEvent.class);
        when(event.getDamager()).thenReturn(player);
        when(event.getEntity()).thenReturn(victim);
        when(player.getWorld()).thenReturn(world);

        // 模拟拥有权限
        when(player.hasPermission("xiprotection.bypass.pvp")).thenReturn(true);

        // 执行事件处理
        listener.onPlayerDamage(event);

        // 验证事件未被取消
        verify(event, never()).setCancelled(anyBoolean());
    }

    // 测试耕地保护
    @Test
    public void testFarmlandProtection() {
        EntityChangeBlockEvent event = mock(EntityChangeBlockEvent.class);
        when(event.getBlock()).thenReturn(block);
        when(block.getWorld()).thenReturn(world);
        when(block.getType()).thenReturn(Material.FARMLAND);
        when(event.getEntity()).thenReturn(player);

        // 模拟权限检查
        when(player.hasPermission("xiprotection.bypass.*")).thenReturn(false);
        when(player.hasPermission("xiprotection.bypass.treading")).thenReturn(false);

        // 模拟配置检查
        FileConfiguration worldConfig = mock(org.bukkit.configuration.file.FileConfiguration.class);
        when(plugin.getWorldConfig(world)).thenReturn(worldConfig);
        when(worldConfig.getBoolean("enable")).thenReturn(true);
        when(worldConfig.getBoolean("protect.prevent-treading")).thenReturn(true);

        // 执行事件处理
        listener.onBlockChange(event);

        // 验证事件被取消
        verify(event, times(1)).setCancelled(true);
    }

    // 测试耕地保护权限
    @Test
    public void testFarmlandProtectionPermission() {
        EntityChangeBlockEvent event = mock(EntityChangeBlockEvent.class);
        when(event.getBlock()).thenReturn(block);
        when(block.getWorld()).thenReturn(world);
        when(block.getType()).thenReturn(Material.FARMLAND);
        when(event.getEntity()).thenReturn(player);

        // 模拟拥有权限
        when(player.hasPermission("xiprotection.bypass.treading")).thenReturn(true);

        // 执行事件处理
        listener.onBlockChange(event);

        // 验证事件未被取消
        verify(event, never()).setCancelled(anyBoolean());
    }

    // 测试投掷物限制
    @Test
    public void testProjectileRestriction() {
        ThrownPotion potion = mock(ThrownPotion.class);
        ProjectileLaunchEvent event = mock(ProjectileLaunchEvent.class);
        when(event.getEntity()).thenReturn(potion);
        when(potion.getWorld()).thenReturn(world);
        when(potion.getShooter()).thenReturn(player);

        // 模拟权限检查
        when(player.hasPermission("xiprotection.bypass.*")).thenReturn(false);
        when(player.hasPermission("xiprotection.bypass.throwables")).thenReturn(false);

        // 模拟配置检查
        FileConfiguration worldConfig = mock(org.bukkit.configuration.file.FileConfiguration.class);
        when(plugin.getWorldConfig(world)).thenReturn(worldConfig);
        when(worldConfig.getBoolean("enable")).thenReturn(true);
        when(worldConfig.getBoolean("protect.prevent-throwables")).thenReturn(true);

        // 执行事件处理
        listener.onProjectileLaunch(event);

        // 验证事件被取消
        verify(event, times(1)).setCancelled(true);
    }

    // 测试投掷物权限
    @Test
    public void testProjectilePermission() {
        Snowball snowball = mock(Snowball.class);
        ProjectileLaunchEvent event = mock(ProjectileLaunchEvent.class);
        when(event.getEntity()).thenReturn(snowball);
        when(snowball.getWorld()).thenReturn(world);
        when(snowball.getShooter()).thenReturn(player);

        // 模拟拥有权限
        when(player.hasPermission("xiprotection.bypass.throwables")).thenReturn(true);

        // 执行事件处理
        listener.onProjectileLaunch(event);

        // 验证事件未被取消
        verify(event, never()).setCancelled(anyBoolean());
    }

    // 测试爆炸限制
    @Test
    public void testExplosionRestriction() {
        ExplosionPrimeEvent event = mock(ExplosionPrimeEvent.class);
        when(event.getEntity()).thenReturn(player);
        when(player.getWorld()).thenReturn(world);

        // 模拟配置检查
        FileConfiguration worldConfig = mock(org.bukkit.configuration.file.FileConfiguration.class);
        when(plugin.getWorldConfig(world)).thenReturn(worldConfig);
        when(worldConfig.getBoolean("enable")).thenReturn(true);
        when(worldConfig.getBoolean("protect.prevent-explosion")).thenReturn(true);

        // 执行事件处理
        listener.onExplosionPrime(event);

        // 验证事件被取消
        verify(event, times(1)).setCancelled(true);
    }



    // 测试药水效果管理
    @Test
    public void testPotionEffectsManagement() {
        // 模拟玩家和世界
        when(player.getWorld()).thenReturn(world);
        when(world.getName()).thenReturn("test_world");
        
        // 模拟配置检查
        when(plugin.getWorldConfig(world)).thenReturn(mock(org.bukkit.configuration.file.FileConfiguration.class));
        when(plugin.getWorldConfig(world).getBoolean("enable")).thenReturn(true);
        when(plugin.getWorldConfig(world).getBoolean("protect.keep-potion-effects-enabled")).thenReturn(true);
        when(plugin.getWorldConfig(world).getMapList("protect.keep-potion-effects")).thenReturn(java.util.Collections.emptyList());
        
        // 模拟插件配置
        when(plugin.getConfig()).thenReturn(mock(org.bukkit.configuration.file.FileConfiguration.class));
        when(plugin.getConfig().getInt("effect-check-interval", 600)).thenReturn(600);

        // 执行方法
        listener.handlePotionEffects(player);

        // 验证方法执行完成（没有异常）
        // 由于这是一个void方法，我们只能验证它执行完成而没有抛出异常
    }
}

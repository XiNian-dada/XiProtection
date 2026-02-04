package com.leeinx.xiprotection;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
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

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ProtectionCommandTest {

    @Mock
    private XiProtection plugin;
    
    @Mock
    private CommandSender sender;
    
    @Mock
    private Command command;
    
    @Mock
    private FileConfiguration pluginConfig;
    
    private ProtectionCommand protectionCommand;

    @BeforeEach
    public void setUp() {
        // 模拟插件配置
        when(plugin.getConfig()).thenReturn(pluginConfig);
        when(pluginConfig.getString(anyString(), anyString())).thenAnswer(invocation -> {
            String defaultValue = invocation.getArgument(1);
            return defaultValue;
        });
        
        // 初始化命令执行器
        protectionCommand = new ProtectionCommand(plugin);
        
        // 模拟必要的依赖
        when(plugin.getLanguageText(anyString(), anyString())).thenAnswer(invocation -> {
            String defaultValue = invocation.getArgument(1);
            return defaultValue;
        });
        
        // 模拟重载配置方法
        doNothing().when(plugin).reloadWorldConfigs();
    }
    
    @AfterEach
    public void tearDown() {
        // 清理测试环境
    }


    @Test
    public void testCommandReload() {
        // 测试重载命令
        String[] args = {"reload"};
        
        // 执行命令
        boolean result = protectionCommand.onCommand(sender, command, "xiprotection", args);
        
        // 验证命令执行成功
        assertTrue(result, "重载命令应该执行成功");
        verify(plugin, times(1)).reloadWorldConfigs();
        verify(sender, times(1)).sendMessage(anyString());
    }

    @Test
    public void testCommandEditor() {
        // 测试编辑器命令
        String[] args = {"editor"};
        
        // 执行命令
        boolean result = protectionCommand.onCommand(sender, command, "xiprotection", args);
        
        // 验证命令执行成功
        assertTrue(result, "编辑器命令应该执行成功");
        verify(sender, times(1)).sendMessage(anyString());
    }

    @Test
    public void testCommandUnknownAction() {
        // 测试未知命令
        String[] args = {"unknown"};
        
        // 执行命令
        boolean result = protectionCommand.onCommand(sender, command, "xiprotection", args);
        
        // 验证命令执行失败
        assertFalse(result, "未知命令应该执行失败");
        verify(sender, times(1)).sendMessage(anyString());
    }

    @Test
    public void testCommandMissingArguments() {
        // 测试缺少参数的命令
        String[] args = {};
        
        // 执行命令
        boolean result = protectionCommand.onCommand(sender, command, "xiprotection", args);
        
        // 验证命令执行失败
        assertFalse(result, "缺少参数的命令应该执行失败");
        verify(sender, times(1)).sendMessage(anyString());
    }

    @Test
    public void testCommandWithPlayerSender() {
        // 测试使用玩家作为命令发送者
        String[] args = {"reload"};
        
        // 模拟玩家对象
        org.bukkit.entity.Player player = mock(org.bukkit.entity.Player.class);
        
        // 执行命令
        boolean result = protectionCommand.onCommand(player, command, "xiprotection", args);
        
        // 验证命令执行成功
        assertTrue(result, "命令应该执行成功");
        verify(plugin, times(1)).reloadWorldConfigs();
        verify(player, times(1)).sendMessage(anyString());
    }

}



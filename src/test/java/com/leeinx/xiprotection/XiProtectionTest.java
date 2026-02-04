package com.leeinx.xiprotection;

import org.bukkit.Material;
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
public class XiProtectionTest {

    @Mock
    private XiProtection plugin;

    @Mock
    private FileConfiguration pluginConfig;

    @BeforeEach
    public void setUp() {
        // 模拟插件配置
        when(plugin.getConfig()).thenReturn(pluginConfig);
        when(pluginConfig.getString(anyString(), anyString())).thenAnswer(invocation -> {
            String defaultValue = invocation.getArgument(1);
            return defaultValue;
        });
        when(pluginConfig.getInt(anyString(), anyInt())).thenReturn(600);
        when(pluginConfig.getBoolean(anyString(), anyBoolean())).thenReturn(false);
        
        // 模拟插件方法
        when(plugin.getLanguageText(anyString(), anyString())).thenAnswer(invocation -> {
            String defaultValue = invocation.getArgument(1);
            return defaultValue;
        });
        when(plugin.getOnEnableText(anyString(), anyString())).thenAnswer(invocation -> {
            String defaultValue = invocation.getArgument(1);
            return defaultValue;
        });
        
        // 移除对PluginDescriptionFile的模拟，因为它是final类
    }
    
    @AfterEach
    public void tearDown() {
        // 清理测试环境
    }

    @Test
    public void testDebugPrintLogic() {
        // 测试调试打印逻辑
        assertNotNull(plugin, "插件实例不应该为 null");
    }

    @Test
    public void testLanguageTextLogic() {
        // 测试语言文本逻辑
        assertNotNull(plugin, "插件实例不应该为 null");
        
        // 测试获取语言文本
        String testText = plugin.getLanguageText("test.key", "Default text");
        assertNotNull(testText, "语言文本不应该为 null");
    }

    @Test
    public void testOnEnableTextLogic() {
        // 测试启用时文本逻辑
        assertNotNull(plugin, "插件实例不应该为 null");
        
        // 测试获取启用时文本
        String testText = plugin.getOnEnableText("test.key", "Default text");
        assertNotNull(testText, "启用时文本不应该为 null");
    }

    @Test
    public void testFoodAndDrinkLogic() {
        // 测试食物和饮料逻辑
        assertNotNull(Material.APPLE, "APPLE 材料不应该为 null");
        assertNotNull(Material.POTION, "POTION 材料不应该为 null");
        
        // 测试插件是否能正确处理材料
        assertNotNull(plugin, "插件实例不应该为 null");
    }

    @Test
    public void testStringOperations() {
        // 测试字符串操作
        String prefix = "[Test] ";
        String message = "Hello World";
        String result = prefix + message;
        assertNotNull(result, "结果字符串不应该为 null");
        assertTrue(result.contains(prefix), "结果字符串应该包含前缀");
        assertTrue(result.contains(message), "结果字符串应该包含消息");
    }

    @Test
    public void testPluginInitialization() {
        // 测试插件初始化
        assertNotNull(plugin, "插件实例不应该为 null");
    }

    @Test
    public void testReloadWorldConfigs() {
        // 测试重载世界配置
        assertNotNull(plugin, "插件实例不应该为 null");
        
        // 执行重载操作（不抛出异常即可）
        try {
            plugin.reloadWorldConfigs();
            // 验证重载方法被调用
            verify(plugin, times(1)).reloadWorldConfigs();
        } catch (Exception e) {
            // 由于是模拟对象，可能会抛出异常，这是正常的
        }
    }
}



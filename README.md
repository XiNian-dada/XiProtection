# XiProtection 插件 | XiProtection Plugin

![GitHub All Releases](https://img.shields.io/github/downloads/XiNian-dada/XiProtection/total) ![GitHub Repo stars](https://img.shields.io/github/stars/XiNian-dada/XiProtection?style=social)

## 简介 | Introduction

XiProtection 是一个用于 Minecraft 服务器的插件，旨在为玩家提供丰富的保护功能。通过本插件，您可以控制玩家在特定世界中的行为，从而创建更加安全和受控的游戏环境。

XiProtection is a plugin for Minecraft servers designed to provide players with a rich set of protection features. With this plugin, you can control player actions in specific worlds, creating a safer and more controlled gaming environment.

## 作者 | Author
- **GitHub**: [XiNian-dada](https://github.com/XiNian-dada)

## CI/CD 流程 | CI/CD Process

本项目使用 GitHub Actions 实现自动化构建和发布流程：

1. **构建与测试**: 当代码推送到 `main` 或 `master` 分支时，自动运行 Maven 构建和测试
2. **版本号管理**: 从 POM 文件中自动获取版本号，确保版本号统一管理
3. **自动发布**: 当构建成功且版本号对应的 Tag 不存在时，自动创建 Tag 并发布新版本
4. **质量检查**: 对 Pull Request 进行代码质量检查

This project uses GitHub Actions for automated build and release processes:

1. **Build and Test**: Automatically run Maven build and tests when code is pushed to `main` or `master` branch
2. **Version Management**: Automatically retrieve version number from POM file to ensure unified version management
3. **Auto Release**: Automatically create Tag and release new version when build is successful and the corresponding Tag does not exist
4. **Quality Check**: Perform code quality checks for Pull Requests

## 功能 | Features

### 1. 方块破坏与放置限制 | Block Break and Place Restrictions
- **禁止破坏方块**：玩家无法在特定世界中破坏方块，保护建筑安全。
- **禁止放置方块**：玩家无法在特定世界中放置方块，保持环境整洁。

### 2. 火焰控制 | Fire Control
- **禁止引燃火焰**：玩家无法在特定世界中引发火焰，防止火灾。

### 3. 剪羊毛限制 | Shearing Restrictions
- **禁止剪羊毛**：玩家无法在特定世界中剪羊毛，保护羊群。

### 4. 饥饿与生命值管理 | Hunger and Health Management
- **保持满饱食度**：玩家的饥饿值始终保持在满值，无需担心饥饿。
- **保持满生命值**：玩家受到伤害时会自动恢复到满生命值，确保安全。

### 5. PvP 管理 | PvP Management
- **禁止 PvP**：玩家无法在特定世界中进行 PvP 战斗，创造和平环境。

### 6. 爆炸控制 | Explosion Control
- **禁止爆炸**：玩家无法引发爆炸，确保世界安全和结构完整。

### 7. 耕地保护 | Farmland Protection
- **禁止踩踏耕地**：防止玩家和生物踩踏耕地，保护农作物生长。

### 8. 投掷物限制 | Throwable Restrictions
- **禁止使用投掷物**：玩家无法在特定世界中使用投掷物，避免混乱。

### 9. 物品保持 | Item Maintenance
- **保持特定物品**：自动为玩家补充特定物品，确保他们的库存始终完备。

### 10. 禁止命令 | Banned Commands
- **自定义禁用命令**：您可以设置特定命令在特定世界中被禁止使用，维护秩序。

### 11. 时间与天气控制 | Time and Weather Control
- **控制时间**：可以设置世界始终为白天或黑夜。
- **控制天气**：可以设置世界始终为晴天或雨天。

### 12. 交互限制 | Interaction Restrictions
- **禁止特定交互**：可以限制玩家与特定方块的交互，如箱子、熔炉等。

### 13. 药水效果管理 | Potion Effect Management
- **管理药水效果**：可以控制玩家的药水效果，确保公平游戏环境。

## 安装 | Installation

1. **下载插件**：从 GitHub Releases 页面下载最新版本的 XiProtection 插件。
2. **安装插件**：将下载的 JAR 文件放入您的 Minecraft 服务器的 `plugins` 文件夹中。
3. **重启服务器**：重启服务器以加载插件。
4. **配置插件**：插件会自动生成默认配置文件，您可以根据需要修改。

## 配置 | Configuration

### 全局配置 (config.yml)
- `update-interval`：时间和天气更新间隔（默认：600 刻，即 30 秒）
- `item-check-interval`：物品检查间隔（默认：1200 刻，即 60 秒）
- `effect-check-interval`：效果检查间隔（默认：1200 刻，即 60 秒）
- `prefix`：插件前缀（默认：[XiProtection]）
- `debug`：调试模式（默认：false）
- `consumables`：可消耗物品配置

### 世界配置 (worlds/{worldName}.yml)
每个世界都有独立的配置文件，包含以下选项：
- `enable`：是否启用世界保护（默认：true）
- `protect.anti-break`：是否禁止破坏方块
- `protect.anti-place`：是否禁止放置方块
- `protect.anti-fire`：是否禁止引燃火焰
- `protect.anti-shear`：是否禁止剪羊毛
- `protect.keep-full-hunger`：是否保持满饱食度
- `protect.keep-full-health`：是否保持满生命值
- `protect.anti-pvp`：是否禁止 PvP
- `protect.prevent-explosion`：是否禁止爆炸
- `protect.prevent-treading`：是否禁止踩踏耕地
- `protect.prevent-throwables`：是否禁止使用投掷物
- `protect.always-day`：是否始终为白天
- `protect.always-night`：是否始终为黑夜
- `protect.always-rain`：是否始终下雨
- `protect.always-sun`：是否始终晴天
- `protect.prevent-interactions`：是否禁止特定交互
- `protect.banned-commands`：禁止的命令列表

## 命令 | Commands

### 主命令
- **/xiprotection**：显示插件帮助信息
- **/xiprotection reload**：重载插件配置
- **/xiprotection editor**：打开配置编辑器
- **/xiprotection add <world> <protection>**：为特定世界添加保护
- **/xiprotection remove <world> <protection>**：为特定世界移除保护

### 命令别名
- **/xip**：主命令的别名

## 权限 | Permissions

### 绕过保护权限
- `xiprotection.bypass.*`：允许玩家绕过所有保护限制
- `xiprotection.bypass.break`：允许破坏方块
- `xiprotection.bypass.place`：允许放置方块
- `xiprotection.bypass.fire`：允许引燃火焰
- `xiprotection.bypass.shear`：允许剪羊毛
- `xiprotection.bypass.hunger`：允许修改饥饿值
- `xiprotection.bypass.pvp`：允许进行 PvP
- `xiprotection.bypass.health`：允许受到伤害
- `xiprotection.bypass.treading`：允许踩踏耕地
- `xiprotection.bypass.throwables`：允许使用投掷物
- `xiprotection.bypass.keep-items`：允许保持物品
- `xiprotection.bypass.banned-commands`：允许使用被禁止的命令
- `xiprotection.bypass.interactions`：允许与特定方块交互

### 命令权限
- `xiprotection.command.*`：允许使用所有插件命令
- `xiprotection.command.reload`：允许使用重载命令
- `xiprotection.command.editor`：允许使用编辑器命令
- `xiprotection.command.add`：允许使用添加保护命令
- `xiprotection.command.remove`：允许使用移除保护命令

## 性能优化 | Performance Optimization

### 1. 异步处理
- **定时任务异步执行**：时间和天气更新、物品检查、效果检查等定时任务都在异步线程中执行，避免阻塞主线程。

### 2. 缓存机制
- **配置文件缓存**：世界配置文件在插件启动时加载并缓存，减少文件 I/O 操作。
- **权限检查优化**：权限检查结果会被 Bukkit 缓存，提高重复检查的效率。

### 3. 代码优化
- **减少重复代码**：使用辅助方法处理权限检查和配置检查，减少代码重复。
- **优化事件处理**：事件处理方法简洁高效，只执行必要的检查和操作。
- **减少日志输出**：调试模式默认关闭，减少不必要的日志输出。

### 4. 内存管理
- **合理使用集合**：使用适合的数据结构存储配置和状态信息。
- **避免内存泄漏**：插件禁用时会取消所有定时任务，释放资源。

## 测试 | Testing

### 测试框架
- **JUnit 5**：用于单元测试
- **Mockito**：用于模拟对象
- **MockBukkit**：用于模拟 Bukkit 环境

### 测试内容
- **插件启动与禁用**：测试插件是否能正常启动和禁用。
- **配置加载与保存**：测试配置文件是否能正常加载和保存。
- **世界配置管理**：测试世界配置的加载、保存和重载功能。
- **命令执行**：测试插件命令是否能正常执行。
- **权限检查**：测试权限检查功能是否正常工作。

### 运行测试
```bash
mvn test
```

## 版本管理 | Version Management

插件使用 Maven 进行版本管理，版本号统一在 `pom.xml` 文件中定义。当代码推送到 GitHub 的 `main` 或 `master` 分支时，CI/CD 工作流会自动：

1. 从 POM 文件中获取版本号
2. 构建和测试插件
3. 如果版本号对应的 Tag 不存在，创建 Tag 并发布新版本
4. 生成详细的 Release Notes

## 问题反馈 | Issue Reporting

如果您在使用插件时遇到任何问题，请在 GitHub 上提交 Issue，包含以下信息：

1. 插件版本
2. 服务器版本
3. 问题描述
4. 复现步骤
5. 错误日志（如果有）

## 贡献 | Contribution

欢迎各位开发者贡献代码和建议，共同改进插件。您可以：

1. Fork 仓库
2. 创建分支
3. 提交更改
4. 发起 Pull Request

## 许可证 | License

本插件采用 MIT 许可证，详见 LICENSE 文件。

## 联系我们 | Contact Us
如有任何问题或建议，请在 GitHub 上提交问题或联系开发者。

For any questions or suggestions, please submit an issue on GitHub or contact the developer.

---

感谢您使用 XiProtection 插件！祝您游戏愉快！

Thank you for using the XiProtection plugin! Enjoy your gaming experience!

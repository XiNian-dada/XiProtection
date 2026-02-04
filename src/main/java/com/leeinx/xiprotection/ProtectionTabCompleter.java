package com.leeinx.xiprotection;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProtectionTabCompleter implements TabCompleter {

    private final XiProtection plugin; // 主插件类
    private final List<String> potionEffectsCache;
    private final List<String> itemsCache;

    public ProtectionTabCompleter(XiProtection plugin) {
        this.plugin = plugin;
        this.potionEffectsCache = getAllPotionEffects();
        this.itemsCache = getAllItems();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        plugin.debugPrint("Tab completion request: " + Arrays.toString(args), 1);

        if (args.length == 1) {
            return getFirstLevelCommands();
        } else if (args.length == 2) {
            return getSecondLevelCommands(args[0]);
        } else if (args.length == 3) {
            return getThirdLevelCommands(args[0]);
        } else if (args.length == 4) {
            return getFourthLevelCommands(args[0], args[1], args[2]);
        } else if (args.length == 5) {
            return getFifthLevelCommands(args[0], args[2]);
        }

        return Collections.emptyList(); // 默认返回空列表
    }

    private List<String> getFirstLevelCommands() {
        return Arrays.asList("reload", "set", "editor", "add", "remove");
    }

    private List<String> getSecondLevelCommands(String firstArg) {
        switch (firstArg.toLowerCase()) {
            case "set":
            case "add":
            case "remove":
                return getWorldNames();
            default:
                return Collections.emptyList();
        }
    }

    private List<String> getThirdLevelCommands(String firstArg) {
        switch (firstArg.toLowerCase()) {
            case "set":
                return getSetOptions();
            case "add":
            case "remove":
                return Arrays.asList("keep-items", "keep-potion-effects", "prevent-potion-effects");
            default:
                return Collections.emptyList();
        }
    }

    private List<String> getFourthLevelCommands(String firstArg, String secondArg, String thirdArg) {
        switch (firstArg.toLowerCase()) {
            case "set":
                return Arrays.asList("true", "false");
            case "add":
                return getAddOptions(secondArg, thirdArg);
            case "remove":
                return getRemoveOptions(secondArg, thirdArg);
            default:
                return Collections.emptyList();
        }
    }

    private List<String> getFifthLevelCommands(String firstArg, String thirdArg) {
        if (firstArg.equalsIgnoreCase("add") && thirdArg.equalsIgnoreCase("keep-potion-effects")) {
            return Arrays.asList("1", "2", "3", "4", "5");
        }
        return Collections.emptyList();
    }

    private List<String> getWorldNames() {
        List<String> worlds = new ArrayList<>();
        plugin.getServer().getWorlds().forEach(world -> worlds.add(world.getName()));
        if (worlds.isEmpty()) {
            plugin.getLogger().warning("No worlds found.");
        }
        return worlds;
    }

    private List<String> getSetOptions() {
        return Arrays.asList(
                "enable", "anti-break", "anti-place", "always-sun", "always-rain",
                "always-day", "always-night", "anti-fire", "anti-shear",
                "keep-full-hunger", "anti-pvp", "prevent-explosion",
                "keep-full-health", "prevent-treading", "prevent-throwables",
                "keep-items-enabled", "banned-commands-enabled",
                "prevent-eating", "prevent-drinking", "prevent-potion-effects-enabled",
                "keep-potion-effects-enabled",
                "prevent-interactions.furnace", "prevent-interactions.chest",
                "prevent-interactions.ender_chest", "prevent-interactions.shulker_box",
                "prevent-interactions.button", "prevent-interactions.lever",
                "prevent-interactions.trapdoor", "prevent-interactions.door",
                "prevent-interactions.pressure_plate", "prevent-interactions.dispenser",
                "prevent-interactions.dropper", "prevent-interactions.hopper",
                "prevent-interactions.barrel", "prevent-interactions.brewing_stand",
                "prevent-interactions.beacon", "prevent-interactions.enchanting_table",
                "prevent-interactions.anvil", "prevent-interactions.cartography_table",
                "prevent-interactions.grindstone", "prevent-interactions.lectern",
                "prevent-interactions.smithing_table", "prevent-interactions.stonecutter",
                "prevent-interactions.jukebox", "prevent-interactions.note_block",
                "prevent-interactions.fence_gate", "prevent-interactions.composter",
                "prevent-interactions.beehive", "prevent-interactions.bee_nest",
                "prevent-interactions.daylight_detector", "prevent-interactions.tripwire_hook",
                "prevent-interactions.bed", "prevent-interactions.bell",
                "prevent-interactions.cauldron"
        );
    }

    private List<String> getAddOptions(String worldName, String option) {
        World world = plugin.getServer().getWorld(worldName);
        if (world == null) {
            plugin.getLogger().warning("World not found: " + worldName);
            return Collections.emptyList();
        }

        switch (option.toLowerCase()) {
            case "keep-potion-effects":
            case "prevent-potion-effects":
                return potionEffectsCache;
            case "keep-items":
                return itemsCache;
            default:
                return Collections.emptyList();
        }
    }

    private List<String> getRemoveOptions(String worldName, String option) {
        World world = plugin.getServer().getWorld(worldName);
        if (world == null) {
            plugin.getLogger().warning("World not found: " + worldName);
            return Collections.emptyList();
        }

        FileConfiguration config = plugin.getWorldConfig(world);
        if (config == null) {
            plugin.getLogger().warning("No configuration found for world: " + worldName);
            return Collections.emptyList();
        }

        switch (option.toLowerCase()) {
            case "keep-items":
                return getConfigItems(config, "protect.keep-items", "item");
            case "keep-potion-effects":
                return getConfigItems(config, "protect.keep-potion-effects", "effect");
            case "prevent-potion-effects":
                return config.getStringList("protect.prevent-potion-effects");
            default:
                return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> getConfigItems(FileConfiguration config, String path, String key) {
        List<Map<String, Object>> entries = (List<Map<String, Object>>) config.getList(path, Collections.emptyList());
        return entries.stream()
                .map(entry -> (String) entry.get(key))
                .filter(item -> item != null && !item.isEmpty())
                .collect(Collectors.toList());
    }

    private List<String> getAllPotionEffects() {
        return Arrays.stream(PotionEffectType.values())
                .map(PotionEffectType::getName)
                .collect(Collectors.toList());
    }

    private List<String> getAllItems() {
        return Arrays.stream(Material.values())
                .filter(Material::isItem) // 确保仅包括物品
                .map(Material::name)
                .collect(Collectors.toList());
    }
}

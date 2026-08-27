/**
 * File: Config.java
 * Author: Maxime66410
 * Created: 2026-08-23
 * Last Modified: 2026-08-27
 */
package org.furranystudio.colorapocalypse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.furranystudio.colorapocalypse.settings.BoolSetting;
import org.furranystudio.colorapocalypse.settings.IntSetting;
import org.furranystudio.colorapocalypse.settings.SettingsRegistry;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

// Hand-rolled JSON config (no ForgeConfigSpec, so it works identically on Forge and Fabric).
// Stored at <gamedir>/config/colorapocalypse.json, same style as the other JSON files this
// mod already loads (block_colors.json etc).
public final class Config {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static final ConfigValue<Integer> DESTRUCTION_RADIUS = new ConfigValue<>("destructionRadius", 2);
    public static final ConfigValue<Boolean> AUTO_TIMER_ENABLED = new ConfigValue<>("autoTimerEnabled", true);
    public static final ConfigValue<Boolean> MOB_KILL_ENABLED = new ConfigValue<>("mobKillEnabled", true);
    public static final ConfigValue<Boolean> ITEM_DESTROY_ENABLED = new ConfigValue<>("itemDestroyEnabled", true);
    public static final ConfigValue<Boolean> INVENTORY_ITEM_DESTROY_ENABLED = new ConfigValue<>("inventoryItemDestroyEnabled", false);
    public static final ConfigValue<Integer> EASY_INTERVAL_MINUTES = new ConfigValue<>("easyIntervalMinutes", 5);
    public static final ConfigValue<Integer> NORMAL_INTERVAL_MINUTES = new ConfigValue<>("normalIntervalMinutes", 3);
    public static final ConfigValue<Integer> HARD_INTERVAL_MINUTES = new ConfigValue<>("hardIntervalMinutes", 1);

    private static final ConfigValue<?>[] VALUES = {
        DESTRUCTION_RADIUS, AUTO_TIMER_ENABLED, MOB_KILL_ENABLED, ITEM_DESTROY_ENABLED,
        INVENTORY_ITEM_DESTROY_ENABLED, EASY_INTERVAL_MINUTES, NORMAL_INTERVAL_MINUTES, HARD_INTERVAL_MINUTES
    };

    private Config() {
    }

    public static void registerSettings() {
        SettingsRegistry.register("destructionRadius", new IntSetting(DESTRUCTION_RADIUS, 1, 32));
        SettingsRegistry.register("autoTimerEnabled", new BoolSetting(AUTO_TIMER_ENABLED));
        SettingsRegistry.register("mobKillEnabled", new BoolSetting(MOB_KILL_ENABLED));
        SettingsRegistry.register("itemDestroyEnabled", new BoolSetting(ITEM_DESTROY_ENABLED));
        SettingsRegistry.register("inventoryItemDestroyEnabled", new BoolSetting(INVENTORY_ITEM_DESTROY_ENABLED));
        SettingsRegistry.register("easyIntervalMinutes", new IntSetting(EASY_INTERVAL_MINUTES, 1, 1440));
        SettingsRegistry.register("normalIntervalMinutes", new IntSetting(NORMAL_INTERVAL_MINUTES, 1, 1440));
        SettingsRegistry.register("hardIntervalMinutes", new IntSetting(HARD_INTERVAL_MINUTES, 1, 1440));
    }

    // Loads the config file if present, falls back to defaults for anything missing/invalid,
    // then writes it back out so the file always reflects every known key.
    public static void load() {
        Path path = configPath();
        if (Files.exists(path)) {
            try {
                String json = Files.readString(path, StandardCharsets.UTF_8);
                JsonObject root = JsonParser.parseString(json).getAsJsonObject();
                for (ConfigValue<?> value : VALUES) {
                    applyIfPresent(value, root);
                }
            } catch (IOException | RuntimeException e) {
                Colorapocalypse.LOGGER.error("[ColorApocalypse] Failed to read {}, using defaults.", path, e);
            }
        }
        save();
    }

    @SuppressWarnings("unchecked")
    private static void applyIfPresent(ConfigValue<?> value, JsonObject root) {
        if (!root.has(value.key())) {
            return;
        }
        Object defaultValue = value.defaultValue();
        if (defaultValue instanceof Integer) {
            ((ConfigValue<Integer>) value).setRaw(root.get(value.key()).getAsInt());
        } else if (defaultValue instanceof Boolean) {
            ((ConfigValue<Boolean>) value).setRaw(root.get(value.key()).getAsBoolean());
        }
    }

    public static void save() {
        JsonObject root = new JsonObject();
        for (ConfigValue<?> value : VALUES) {
            Object current = value.get();
            if (current instanceof Integer i) {
                root.addProperty(value.key(), i);
            } else if (current instanceof Boolean b) {
                root.addProperty(value.key(), b);
            }
        }

        Path path = configPath();
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(root), StandardCharsets.UTF_8);
        } catch (IOException e) {
            Colorapocalypse.LOGGER.error("[ColorApocalypse] Failed to write {}", path, e);
        }
    }

    private static Path configPath() {
        return Platform.getGameDir().resolve("config").resolve("colorapocalypse.json");
    }
}

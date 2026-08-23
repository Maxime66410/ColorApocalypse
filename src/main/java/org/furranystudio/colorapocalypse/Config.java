package org.furranystudio.colorapocalypse;

import net.minecraftforge.common.ForgeConfigSpec;
import org.furranystudio.colorapocalypse.settings.BoolSetting;
import org.furranystudio.colorapocalypse.settings.IntSetting;
import org.furranystudio.colorapocalypse.settings.SettingsRegistry;

public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.IntValue DESTRUCTION_RADIUS = BUILDER
            .comment("Radius (in chunks) around each player within which blocks are destroyed when a color is eliminated.")
            .defineInRange("destructionRadius", 2, 1, 32);

    public static final ForgeConfigSpec.BooleanValue AUTO_TIMER_ENABLED = BUILDER
            .comment("Whether the wheel draws automatically over time (Peaceful is always excluded).")
            .define("autoTimerEnabled", true);

    public static final ForgeConfigSpec.BooleanValue MOB_KILL_ENABLED = BUILDER
            .comment("Whether mobs of the eliminated color are killed alongside its blocks.")
            .define("mobKillEnabled", true);

    public static final ForgeConfigSpec.BooleanValue ITEM_DESTROY_ENABLED = BUILDER
            .comment("Whether item drops of the eliminated color are destroyed alongside its blocks.")
            .define("itemDestroyEnabled", true);

    public static final ForgeConfigSpec.BooleanValue INVENTORY_ITEM_DESTROY_ENABLED = BUILDER
            .comment("Whether items of the eliminated color are also removed from every online player's inventory and equipment.")
            .define("inventoryItemDestroyEnabled", false);

    public static final ForgeConfigSpec.IntValue EASY_INTERVAL_MINUTES = BUILDER
            .comment("Minutes between automatic draws on Easy.")
            .defineInRange("easyIntervalMinutes", 5, 1, 1440);

    public static final ForgeConfigSpec.IntValue NORMAL_INTERVAL_MINUTES = BUILDER
            .comment("Minutes between automatic draws on Normal.")
            .defineInRange("normalIntervalMinutes", 3, 1, 1440);

    public static final ForgeConfigSpec.IntValue HARD_INTERVAL_MINUTES = BUILDER
            .comment("Minutes between automatic draws on Hard.")
            .defineInRange("hardIntervalMinutes", 1, 1, 1440);

    static final ForgeConfigSpec SPEC = BUILDER.build();

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
}

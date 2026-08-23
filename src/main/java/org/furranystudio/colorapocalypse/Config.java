package org.furranystudio.colorapocalypse;

import net.minecraftforge.common.ForgeConfigSpec;
import org.furranystudio.colorapocalypse.settings.IntSetting;
import org.furranystudio.colorapocalypse.settings.SettingsRegistry;

public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.IntValue DESTRUCTION_RADIUS = BUILDER
            .comment("Radius (in chunks) around each player within which blocks are destroyed when a color is eliminated.")
            .defineInRange("destructionRadius", 4, 1, 32);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static void registerSettings() {
        SettingsRegistry.register("destructionRadius", new IntSetting(DESTRUCTION_RADIUS, 1, 32));
    }
}

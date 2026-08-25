/**
 * File: IntSetting.java
 * Author: Maxime66410
 * Created: 2026-08-23
 * Last Modified: 2026-08-24
 */
package org.furranystudio.colorapocalypse.settings;

import net.minecraftforge.common.ForgeConfigSpec;

public final class IntSetting implements SettingsRegistry.Setting {

    private final ForgeConfigSpec.IntValue value;
    private final int min;
    private final int max;

    public IntSetting(ForgeConfigSpec.IntValue value, int min, int max) {
        this.value = value;
        this.min = min;
        this.max = max;
    }

    @Override
    public String get() {
        return String.valueOf(value.get());
    }

    @Override
    public String trySet(String rawValue) {
        int parsed;
        try {
            parsed = Integer.parseInt(rawValue);
        } catch (NumberFormatException e) {
            return "'" + rawValue + "' is not a valid integer.";
        }
        if (parsed < min || parsed > max) {
            return "must be between " + min + " and " + max + " (got " + parsed + ").";
        }
        value.set(parsed);
        return null;
    }
}

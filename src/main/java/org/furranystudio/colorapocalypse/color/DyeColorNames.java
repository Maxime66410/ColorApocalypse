package org.furranystudio.colorapocalypse.color;

import net.minecraft.world.item.DyeColor;

/** Finds the first {@code DyeColor} whose name matches a token in an id, e.g. "dark_brown" -> BROWN. */
final class DyeColorNames {

    private DyeColorNames() {
    }

    static DyeColor firstMatch(String name) {
        for (String token : name.split("_")) {
            for (DyeColor color : DyeColor.values()) {
                if (color.getName().equalsIgnoreCase(token)) {
                    return color;
                }
            }
        }
        return null;
    }
}

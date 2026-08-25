/**
 * File: ItemColorResolver.java
 * Author: Maxime66410
 * Created: 2026-08-24
 * Last Modified: 2026-08-24
 */
package org.furranystudio.colorapocalypse.color;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;

/**
 * Resolves the DyeColor an item "belongs to", if any. {@code item_colors.json} wins first then block items (wool, concrete, dyed banners...) fall back to their block's resolved color ({@link ColorBlockRegistry}).
 * Then plain items fall back to a color name found in their own id (dyes: "red_dye" -> RED), then tools/armor/ingots fall back to their material (iron/gold/diamond/netherite/copper/leather/chainmail/stone/wood, etc "iron_pickaxe" and "raw_iron" both -> LIGHT_GRAY).
 * Anything else has no color.
 */
public final class ItemColorResolver {

    private static final ItemColorOverrides OVERRIDES = ItemColorOverrides.load();

    private static final Map<String, DyeColor> MATERIAL_COLORS = Map.ofEntries(
        Map.entry("wooden", DyeColor.BROWN),
        Map.entry("leather", DyeColor.BROWN),
        Map.entry("stone", DyeColor.GRAY),
        Map.entry("chainmail", DyeColor.LIGHT_GRAY),
        Map.entry("iron", DyeColor.LIGHT_GRAY),
        Map.entry("gold", DyeColor.YELLOW),
        Map.entry("golden", DyeColor.YELLOW),
        Map.entry("diamond", DyeColor.LIGHT_BLUE),
        Map.entry("netherite", DyeColor.BLACK),
        Map.entry("copper", DyeColor.ORANGE)
    );

    private ItemColorResolver() {
    }

    public static DyeColor resolve(Item item) {
        String itemId = String.valueOf(ForgeRegistries.ITEMS.getKey(item));

        DyeColor override = OVERRIDES.get(itemId);
        if (override != null) {
            return override;
        }

        if (item instanceof BlockItem blockItem) {
            DyeColor blockColor = ColorBlockRegistry.getColorFor(blockItem.getBlock());
            if (blockColor != null) {
                return blockColor;
            }
        }

        int colonIndex = itemId.indexOf(':');
        String path = colonIndex >= 0 ? itemId.substring(colonIndex + 1) : itemId;

        DyeColor nameColor = DyeColorNames.firstMatch(path);
        if (nameColor != null) {
            return nameColor;
        }

        for (String token : path.split("_")) {
            DyeColor materialColor = MATERIAL_COLORS.get(token);
            if (materialColor != null) {
                return materialColor;
            }
        }
        return null;
    }
}

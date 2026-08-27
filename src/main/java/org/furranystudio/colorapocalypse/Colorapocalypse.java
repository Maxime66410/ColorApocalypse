/**
 * File: Colorapocalypse.java
 * Author: Maxime66410
 * Created: 2026-08-23
 * Last Modified: 2026-08-27
 */
package org.furranystudio.colorapocalypse;

import com.mojang.logging.LogUtils;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import org.furranystudio.colorapocalypse.color.ColorBlockRegistry;
import org.furranystudio.colorapocalypse.color.ItemColorReport;
import org.slf4j.Logger;

import java.util.List;

// Shared mod identity and init logic, called from each loader's own entrypoint
// (org.furranystudio.colorapocalypse.forge / .fabric).
public final class Colorapocalypse {

    public static final String MODID = "colorapocalypse";
    public static final Logger LOGGER = LogUtils.getLogger();

    private Colorapocalypse() {
    }

    // Common (non-loader-specific) init: config, color classification, reports. Each loader's
    // bootstrap calls this once, after Platform.init() and Config.registerSettings().
    public static void commonSetup() {
        Config.load();
        ColorBlockRegistry.build();

        LOGGER.info("[ColorApocalypse] Block color classification:");
        for (DyeColor color : DyeColor.values()) {
            List<Block> blocks = ColorBlockRegistry.getBlocksFor(color);
            LOGGER.info(" - {} ({} blocks): {}", color.getName(), blocks.size(), blocks);
        }

        ItemColorReport.write();
    }
}

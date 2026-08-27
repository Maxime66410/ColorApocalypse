/**
 * File: ItemColorReport.java
 * Author: Maxime66410
 * Created: 2026-08-24
 * Last Modified: 2026-08-24
 */
package org.furranystudio.colorapocalypse.color;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import org.furranystudio.colorapocalypse.Colorapocalypse;
import org.furranystudio.colorapocalypse.Platform;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.TreeSet;

/** Writes a report of every item that {@link ItemColorResolver} could NOT assign a color to. */
public final class ItemColorReport {

    private static final Path REPORT_PATH = Platform.getGameDir()
        .resolve("colorapocalypse").resolve("cache").resolve("item_color_report.txt");

    private ItemColorReport() {
    }

    public static void write() {
        int total = 0;
        TreeSet<String> unresolved = new TreeSet<>();

        for (Item item : BuiltInRegistries.ITEM) {
            total++;
            DyeColor color = ItemColorResolver.resolve(item);
            if (color == null) {
                unresolved.add(String.valueOf(BuiltInRegistries.ITEM.getKey(item)));
            }
        }

        int resolved = total - unresolved.size();

        StringBuilder report = new StringBuilder();
        report.append("ColorApocalypse item color report\n");
        report.append("Edit src/main/resources/assets/colorapocalypse/config/item_colors.json to fix any of this.\n\n");
        report.append("Resolved: ").append(resolved).append(" / ").append(total).append(" items\n\n");
        report.append("Unresolved (no color assigned): ").append(unresolved.size()).append('\n');
        for (String itemId : unresolved) {
            report.append(" - ").append(itemId).append('\n');
        }

        try {
            Files.createDirectories(REPORT_PATH.getParent());
            Files.writeString(REPORT_PATH, report.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            Colorapocalypse.LOGGER.error("[ColorApocalypse] Failed to write {}", REPORT_PATH, e);
        }

        Colorapocalypse.LOGGER.info("[ColorApocalypse] Item color classification: {} / {} items resolved, see {}",
            resolved, total, REPORT_PATH);
    }
}

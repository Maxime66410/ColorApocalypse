package org.furranystudio.colorapocalypse.color;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;
import org.furranystudio.colorapocalypse.Colorapocalypse;

import java.awt.Color;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Buckets every registered block into a {@link DyeColor}. The primary source is
 * {@code /colorapocalypse/block_colors.json}, a hand-maintained file meant to be edited directly
 * to fix misclassifications. Any block it doesn't list falls back to a guess based on the
 * block's declared {@link MapColor} (nearest {@link DyeColor} by hue/saturation/value), so
 * naturally/biome-colored blocks like grass or leaves still get a reasonable default without
 * anyone having to list every block by hand.
 * <p>
 * Built once, at mod setup, from a full registry scan. Also writes a report of new/divergent
 * blocks to the game directory, so the JSON file can be kept up to date over time.
 */
public final class ColorBlockRegistry {

    private static final Path REPORT_PATH = FMLPaths.GAMEDIR.get()
        .resolve("colorapocalypse").resolve("cache").resolve("block_color_report.txt");

    private static final Map<DyeColor, List<Block>> BLOCKS_BY_COLOR = new EnumMap<>(DyeColor.class);

    private ColorBlockRegistry() {
    }

    public static void build() {
        for (DyeColor color : DyeColor.values()) {
            BLOCKS_BY_COLOR.put(color, new ArrayList<>());
        }

        Map<Block, DyeColor> guessedColorByBlock = new LinkedHashMap<>();
        for (Block block : ForgeRegistries.BLOCKS) {
            MapColor mapColor = resolveMapColor(block);
            if (mapColor == null || mapColor == MapColor.NONE) {
                continue;
            }
            guessedColorByBlock.put(block, closestDyeColor(mapColor));
        }

        BlockColorOverrides overrides = BlockColorOverrides.load();

        Map<String, DyeColor> newBlocks = new TreeMap<>();
        Map<String, String> divergences = new TreeMap<>();

        for (Map.Entry<Block, DyeColor> entry : guessedColorByBlock.entrySet()) {
            Block block = entry.getKey();
            DyeColor guessed = entry.getValue();
            String blockId = String.valueOf(ForgeRegistries.BLOCKS.getKey(block));

            DyeColor override = overrides.get(blockId);
            if (override == null) {
                newBlocks.put(blockId, guessed);
                BLOCKS_BY_COLOR.get(guessed).add(block);
            } else {
                if (override != guessed) {
                    divergences.put(blockId, "json=" + override.getName() + ", guess=" + guessed.getName());
                }
                BLOCKS_BY_COLOR.get(override).add(block);
            }
        }

        writeReport(newBlocks, divergences);
    }

    public static List<Block> getBlocksFor(DyeColor color) {
        return BLOCKS_BY_COLOR.getOrDefault(color, List.of());
    }

    public static Map<DyeColor, List<Block>> getAll() {
        return BLOCKS_BY_COLOR;
    }

    private static void writeReport(Map<String, DyeColor> newBlocks, Map<String, String> divergences) {
        StringBuilder report = new StringBuilder();
        report.append("ColorApocalypse block color report\n");
        report.append("Edit src/main/resources/colorapocalypse/block_colors.json to fix any of this.\n\n");

        report.append("New blocks (not in block_colors.json, using MapColor guess): ").append(newBlocks.size()).append('\n');
        for (Map.Entry<String, DyeColor> entry : newBlocks.entrySet()) {
            report.append(" - ").append(entry.getKey()).append(" -> ").append(entry.getValue().getName()).append('\n');
        }

        report.append("\nDivergences (block_colors.json vs MapColor guess differ): ").append(divergences.size()).append('\n');
        for (Map.Entry<String, String> entry : divergences.entrySet()) {
            report.append(" - ").append(entry.getKey()).append(" : ").append(entry.getValue()).append('\n');
        }

        try {
            Files.createDirectories(REPORT_PATH.getParent());
            Files.writeString(REPORT_PATH, report.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            Colorapocalypse.LOGGER.error("[ColorApocalypse] Failed to write {}", REPORT_PATH, e);
        }

        if (!newBlocks.isEmpty() || !divergences.isEmpty()) {
            Colorapocalypse.LOGGER.warn("[ColorApocalypse] {} new block(s) and {} divergence(s) vs block_colors.json, see {}",
                newBlocks.size(), divergences.size(), REPORT_PATH);
        }
    }

    private static MapColor resolveMapColor(Block block) {
        try {
            return block.defaultBlockState().getMapColor(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
        } catch (Exception e) {
            // A handful of blocks compute their MapColor from real level/neighbor data and
            // can't be resolved outside a world; just skip them rather than crashing setup.
            return null;
        }
    }

    // Hue only matters once a color actually carries some saturation; weighting it by
    // saturation (instead of comparing raw RGB) is what keeps neutral grays away from
    // saturated hues, and keeps dark-but-vivid colors (like foliage) away from black.
    private static final double HUE_WEIGHT = 6.0;
    private static final double SATURATION_WEIGHT = 0.5;
    private static final double VALUE_WEIGHT = 0.5;

    private static DyeColor closestDyeColor(MapColor mapColor) {
        float[] targetHsv = toHsv(mapColor.calculateARGBColor(MapColor.Brightness.NORMAL));

        DyeColor best = DyeColor.WHITE;
        double bestDistance = Double.MAX_VALUE;
        for (DyeColor color : DyeColor.values()) {
            float[] candidateHsv = toHsv(color.getMapColor().calculateARGBColor(MapColor.Brightness.NORMAL));
            double distance = hsvDistanceSquared(targetHsv, candidateHsv);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = color;
            }
        }
        return best;
    }

    private static float[] toHsv(int argb) {
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        return Color.RGBtoHSB(r, g, b, null);
    }

    private static double hsvDistanceSquared(float[] hsv1, float[] hsv2) {
        float hue1 = hsv1[0] * 360f;
        float hue2 = hsv2[0] * 360f;
        float saturation1 = hsv1[1];
        float saturation2 = hsv2[1];
        float value1 = hsv1[2];
        float value2 = hsv2[2];

        float hueDiff = Math.abs(hue1 - hue2);
        if (hueDiff > 180f) {
            hueDiff = 360f - hueDiff;
        }
        float normalizedHueDiff = hueDiff / 180f;
        float hueWeightFactor = (saturation1 + saturation2) / 2f;
        double weightedHueDiff = normalizedHueDiff * hueWeightFactor;

        double saturationDiff = saturation1 - saturation2;
        double valueDiff = value1 - value2;

        return HUE_WEIGHT * weightedHueDiff * weightedHueDiff
            + SATURATION_WEIGHT * saturationDiff * saturationDiff
            + VALUE_WEIGHT * valueDiff * valueDiff;
    }
}

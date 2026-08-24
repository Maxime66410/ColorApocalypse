package org.furranystudio.colorapocalypse.color;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.world.item.DyeColor;
import org.furranystudio.colorapocalypse.Colorapocalypse;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/** Loads {@code block_colors.json} the hand-edited overrides that win over the MapColor guess. */
public final class BlockColorOverrides {

    private static final String RESOURCE_PATH = "/assets/colorapocalypse/config/block_colors.json";
    // Strips /* ... */ block comments before parsing, since standard JSON (and Gson, even in lenient mode) doesn't support comments at all, or maybe i'm a dumb ?
    private static final Pattern BLOCK_COMMENT = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);

    private final Map<String, DyeColor> colorByBlockId;

    private BlockColorOverrides(Map<String, DyeColor> colorByBlockId) {
        this.colorByBlockId = colorByBlockId;
    }

    public static BlockColorOverrides load() {
        Map<String, DyeColor> byBlockId = new HashMap<>();

        try (InputStream stream = openResource()) {
            if (stream == null) {
                Colorapocalypse.LOGGER.warn("[ColorApocalypse] {} not found, using MapColor guesses only.", RESOURCE_PATH);
                return new BlockColorOverrides(byBlockId);
            }

            String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            json = BLOCK_COMMENT.matcher(json).replaceAll("");

            Gson gson = new Gson();
            Map<String, List<String>> raw = gson.fromJson(json, new TypeToken<Map<String, List<String>>>() {}.getType());

            for (Map.Entry<String, List<String>> entry : raw.entrySet()) {
                DyeColor color = parseDyeColor(entry.getKey());
                if (color == null) {
                    Colorapocalypse.LOGGER.warn("[ColorApocalypse] Unknown color key '{}' in {}, ignoring.", entry.getKey(), RESOURCE_PATH);
                    continue;
                }
                for (String blockId : entry.getValue()) {
                    byBlockId.put(blockId, color);
                }
            }
        } catch (Exception e) {
            Colorapocalypse.LOGGER.error("[ColorApocalypse] Failed to load {}, using MapColor guesses only.", RESOURCE_PATH, e);
            return new BlockColorOverrides(new HashMap<>());
        }

        return new BlockColorOverrides(byBlockId);
    }

    public DyeColor get(String blockId) {
        return colorByBlockId.get(blockId);
    }

    public boolean contains(String blockId) {
        return colorByBlockId.containsKey(blockId);
    }

    private static InputStream openResource() {
        return BlockColorOverrides.class.getResourceAsStream(RESOURCE_PATH);
    }

    private static DyeColor parseDyeColor(String name) {
        for (DyeColor color : DyeColor.values()) {
            if (color.getName().equals(name)) {
                return color;
            }
        }
        return null;
    }
}

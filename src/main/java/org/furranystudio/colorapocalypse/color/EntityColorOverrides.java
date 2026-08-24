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

/** Loads {@code entity_colors.json}, the hand-curated color assignments for mobs with no inherent color. */
public final class EntityColorOverrides {

    private static final String RESOURCE_PATH = "/assets/colorapocalypse/config/entity_colors.json";
    private static final Pattern BLOCK_COMMENT = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);

    private final Map<String, DyeColor> colorByEntityId;

    private EntityColorOverrides(Map<String, DyeColor> colorByEntityId) {
        this.colorByEntityId = colorByEntityId;
    }

    public static EntityColorOverrides load() {
        Map<String, DyeColor> byEntityId = new HashMap<>();

        try (InputStream stream = openResource()) {
            if (stream == null) {
                Colorapocalypse.LOGGER.warn("[ColorApocalypse] {} not found, no manually-assigned mob colors.", RESOURCE_PATH);
                return new EntityColorOverrides(byEntityId);
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
                for (String entityId : entry.getValue()) {
                    byEntityId.put(entityId, color);
                }
            }
        } catch (Exception e) {
            Colorapocalypse.LOGGER.error("[ColorApocalypse] Failed to load {}, no manually-assigned mob colors.", RESOURCE_PATH, e);
            return new EntityColorOverrides(new HashMap<>());
        }

        return new EntityColorOverrides(byEntityId);
    }

    public DyeColor get(String entityId) {
        return colorByEntityId.get(entityId);
    }

    private static InputStream openResource() {
        return EntityColorOverrides.class.getResourceAsStream(RESOURCE_PATH);
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

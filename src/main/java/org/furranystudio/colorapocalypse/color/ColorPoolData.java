/**
 * File: ColorPoolData.java
 * Author: Maxime66410
 * Created: 2026-08-23
 * Last Modified: 2026-08-24
 */
package org.furranystudio.colorapocalypse.color;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.furranystudio.colorapocalypse.Colorapocalypse;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Persistent (saved with the world) pool of {@link DyeColor}s still eligible to be drawn.
 * A color is removed permanently once drawn, until {@link #reset()} is called.
 */
public final class ColorPoolData extends SavedData {

    public static final SavedDataType<ColorPoolData> TYPE = new SavedDataType<>(
        Identifier.fromNamespaceAndPath(Colorapocalypse.MODID, "color_pool"),
        ColorPoolData::createDefault,
        buildCodec(),
        DataFixTypes.LEVEL
    );

    private final Set<DyeColor> remaining;

    private ColorPoolData(Set<DyeColor> remaining) {
        this.remaining = remaining;
    }

    private static ColorPoolData createDefault() {
        return new ColorPoolData(EnumSet.allOf(DyeColor.class));
    }

    private static Codec<ColorPoolData> buildCodec() {
        return DyeColor.CODEC.listOf().xmap(
            list -> {
                EnumSet<DyeColor> set = EnumSet.noneOf(DyeColor.class);
                set.addAll(list);
                return new ColorPoolData(set);
            },
            data -> List.copyOf(data.remaining)
        );
    }

    public Set<DyeColor> getRemaining() {
        return Set.copyOf(remaining);
    }

    /**
     * Draws and permanently removes a random color from the pool.
     *
     * @return the drawn color, or {@code null} if the pool is empty
     */
    public DyeColor draw(Random random) {
        if (remaining.isEmpty()) {
            return null;
        }
        List<DyeColor> options = List.copyOf(remaining);
        DyeColor drawn = options.get(random.nextInt(options.size()));
        remaining.remove(drawn);
        setDirty();
        return drawn;
    }

    public void reset() {
        remaining.clear();
        remaining.addAll(EnumSet.allOf(DyeColor.class));
        setDirty();
    }
}

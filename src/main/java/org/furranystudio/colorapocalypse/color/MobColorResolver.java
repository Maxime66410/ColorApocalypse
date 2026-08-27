/**
 * File: MobColorResolver.java
 * Author: Maxime66410
 * Created: 2026-08-24
 * Last Modified: 2026-08-24
 */
package org.furranystudio.colorapocalypse.color;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.fish.TropicalFish;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.panda.Panda;
import net.minecraft.world.entity.animal.parrot.Parrot;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

/**
 * Resolves the DyeColor a mob "belongs to", if any. Sheep, wolves, llamas, tropical fish and shulkers have a real per-instance color read straight off the entity. Cats, cows, frogs, horses, pandas, rabbits and parrots have variants instead of a color,
 * we just take the first color name found in the variant's id (example "dark_brown" -> BROWN, "red_blue" -> RED), so plenty of their variants (tabby, siamese, temperate...) simply won't match anything.
 * Every other mob only counts if it's listed in {@code entity_colors.json}.
 */
public final class MobColorResolver {

    private static final EntityColorOverrides OVERRIDES = EntityColorOverrides.load();

    private MobColorResolver() {
    }

    public static DyeColor resolve(Entity entity) {
        return switch (entity) {
            case Sheep sheep -> sheep.getColor();
            case Wolf wolf -> wolf.isTame() ? wolf.getCollarColor() : DyeColor.GRAY;
            case Llama llama -> resolveLlamaColor(llama);
            case TropicalFish fish -> fish.getBaseColor();
            case Shulker shulker -> shulker.getColor() != null ? shulker.getColor() : DyeColor.PURPLE;
            case Horse horse -> DyeColorNames.firstMatch(horse.getVariant().name());
            case Rabbit rabbit -> DyeColorNames.firstMatch(rabbit.getVariant().name());
            case Panda panda -> DyeColorNames.firstMatch(panda.getVariant().name());
            case Parrot parrot -> DyeColorNames.firstMatch(parrot.getVariant().name());
            case Cat cat -> colorFromHolder(cat.getVariant());
            case Cow cow -> colorFromHolder(cow.getVariant());
            case Frog frog -> colorFromHolder(frog.getVariant());
            default -> OVERRIDES.get(String.valueOf(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType())));
        };
    }

    private static DyeColor colorFromHolder(Holder<?> variant) {
        return variant.unwrapKey().map(key -> DyeColorNames.firstMatch(key.identifier().getPath())).orElse(null);
    }

    private static DyeColor resolveLlamaColor(Llama llama) {
        ItemStack carpet = llama.getItemBySlot(EquipmentSlot.BODY);
        if (carpet.isEmpty()) {
            return DyeColor.WHITE;
        }

        String itemId = String.valueOf(BuiltInRegistries.ITEM.getKey(carpet.getItem()));
        String path = itemId.substring(itemId.indexOf(':') + 1);
        if (!path.endsWith("_carpet")) {
            return DyeColor.WHITE;
        }

        String colorName = path.substring(0, path.length() - "_carpet".length());
        for (DyeColor color : DyeColor.values()) {
            if (color.getName().equals(colorName)) {
                return color;
            }
        }
        return DyeColor.WHITE;
    }
}

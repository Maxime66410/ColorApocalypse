package org.furranystudio.colorapocalypse.color;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.DyeColor;

import java.util.ArrayList;
import java.util.List;

/** Discards every loaded item drop whose resolved color matches the eliminated one. */
public final class ItemDestroyer {

    private ItemDestroyer() {
    }

    public static int destroy(DyeColor color, MinecraftServer server) {
        int destroyed = 0;

        for (ServerLevel level : server.getAllLevels()) {
            List<ItemEntity> toDestroy = new ArrayList<>();
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof ItemEntity itemEntity
                    && ItemColorResolver.resolve(itemEntity.getItem().getItem()) == color) {
                    toDestroy.add(itemEntity);
                }
            }
            for (ItemEntity itemEntity : toDestroy) {
                itemEntity.discard();
            }
            destroyed += toDestroy.size();
        }

        return destroyed;
    }
}

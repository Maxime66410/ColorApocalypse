package org.furranystudio.colorapocalypse.color;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;

import java.util.ArrayList;
import java.util.List;

/** Kills every loaded mob whose resolved color matches the eliminated one. */
public final class MobDestroyer {

    private MobDestroyer() {
    }

    public static int kill(DyeColor color, MinecraftServer server) {
        int killed = 0;

        for (ServerLevel level : server.getAllLevels()) {
            List<LivingEntity> toKill = new ArrayList<>();
            for (Entity entity : level.getAllEntities()) {
                if (!(entity instanceof LivingEntity living) || living instanceof Player) {
                    continue;
                }
                if (MobColorResolver.resolve(living) == color) {
                    toKill.add(living);
                }
            }
            for (LivingEntity living : toKill) {
                living.kill(level);
            }
            killed += toKill.size();
        }

        return killed;
    }
}

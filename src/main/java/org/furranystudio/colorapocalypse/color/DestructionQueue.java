/**
 * File: DestructionQueue.java
 * Author: Maxime66410
 * Created: 2026-08-23
 * Last Modified: 2026-08-24
 */
package org.furranystudio.colorapocalypse.color;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraftforge.event.TickEvent;
import org.furranystudio.colorapocalypse.Colorapocalypse;
import org.furranystudio.colorapocalypse.Config;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

/**
 * Destroys the blocks of an eliminated color within {@link Config#DESTRUCTION_RADIUS} of each
 * online player. Spread across ticks (time-budgeted, main thread only) and skips whole empty
 * chunk sections instead of scanning every block.
 */
public final class DestructionQueue {

    private static final long TIME_BUDGET_NANOS = 8_000_000L; // 8ms/tick

    private static final Deque<PendingChunk> QUEUE = new ArrayDeque<>();
    private static Set<Block> targetBlocks = Set.of();

    private static PendingChunk currentChunk;
    private static ChunkAccess currentChunkAccess;
    private static int cursorSectionY;
    private static boolean sectionEntered;
    private static int cursorX;
    private static int cursorYLocal;
    private static int cursorZ;

    private DestructionQueue() {
    }

    public static void register() {
        TickEvent.ServerTickEvent.Post.BUS.addListener(event -> tick());
    }

    public static boolean isActive() {
        return currentChunk != null || !QUEUE.isEmpty();
    }

    /**
     * Snapshots, for every online player across every dimension, the currently loaded chunks
     * within the configured radius, and queues them up for destruction over the next ticks.
     *
     * @return the number of chunks queued
     */
    public static int start(DyeColor color, MinecraftServer server) {
        targetBlocks = new HashSet<>(ColorBlockRegistry.getBlocksFor(color));

        QUEUE.clear();
        currentChunk = null;
        int radius = Config.DESTRUCTION_RADIUS.get();

        for (ServerLevel level : server.getAllLevels()) {
            Set<Long> queuedInLevel = new HashSet<>();
            for (ServerPlayer player : level.players()) {
                ChunkPos center = ChunkPos.containing(player.blockPosition());
                ChunkPos.rangeClosed(center, radius).forEach(chunkPos -> {
                    if (queuedInLevel.add(chunkPos.pack()) && level.hasChunk(chunkPos.x(), chunkPos.z())) {
                        QUEUE.add(new PendingChunk(level, chunkPos.x(), chunkPos.z()));
                    }
                });
            }
        }

        return QUEUE.size();
    }

    private static void tick() {
        if (!isActive()) {
            return;
        }

        long deadline = System.nanoTime() + TIME_BUDGET_NANOS;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        while (System.nanoTime() < deadline) {
            if (currentChunk == null) {
                if (!startNextChunk()) {
                    break; // queue empty, nothing left to do
                }
                continue;
            }

            if (!sectionEntered && !enterNextSection()) {
                continue; // chunk finished, or section skipped, loop back around
            }

            processPosition(pos);
            advancePositionCursor();
        }

        if (!isActive()) {
            Colorapocalypse.LOGGER.info("[ColorApocalypse] Destruction complete.");
        }
    }

    private static boolean startNextChunk() {
        currentChunk = QUEUE.poll();
        if (currentChunk == null) {
            return false;
        }
        currentChunkAccess = currentChunk.level().getChunk(currentChunk.chunkX(), currentChunk.chunkZ());
        cursorSectionY = currentChunk.level().getMinSectionY();
        sectionEntered = false;
        return true;
    }

    // True once positioned at a section worth scanning, false if skipped or chunk is done.
    private static boolean enterNextSection() {
        ServerLevel level = currentChunk.level();
        if (cursorSectionY > level.getMaxSectionY()) {
            currentChunk = null;
            currentChunkAccess = null;
            return false;
        }

        LevelChunkSection section = currentChunkAccess.getSection(level.getSectionIndexFromSectionY(cursorSectionY));
        if (section.hasOnlyAir() || !section.maybeHas(state -> targetBlocks.contains(state.getBlock()))) {
            cursorSectionY++;
            return false;
        }

        cursorX = 0;
        cursorZ = 0;
        cursorYLocal = 0;
        sectionEntered = true;
        return true;
    }

    private static void processPosition(BlockPos.MutableBlockPos pos) {
        ServerLevel level = currentChunk.level();
        pos.set(
            (currentChunk.chunkX() << 4) + cursorX,
            (cursorSectionY << 4) + cursorYLocal,
            (currentChunk.chunkZ() << 4) + cursorZ
        );

        BlockState state = level.getBlockState(pos);
        if (!state.isAir() && targetBlocks.contains(state.getBlock())) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
        }
    }

    private static void advancePositionCursor() {
        cursorYLocal++;
        if (cursorYLocal < 16) {
            return;
        }
        cursorYLocal = 0;
        cursorZ++;
        if (cursorZ < 16) {
            return;
        }
        cursorZ = 0;
        cursorX++;
        if (cursorX < 16) {
            return;
        }
        // section done, move on to the next one
        cursorSectionY++;
        sectionEntered = false;
    }

    private record PendingChunk(ServerLevel level, int chunkX, int chunkZ) {
    }
}

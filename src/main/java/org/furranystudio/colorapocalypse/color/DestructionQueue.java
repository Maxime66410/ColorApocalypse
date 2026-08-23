package org.furranystudio.colorapocalypse.color;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import org.furranystudio.colorapocalypse.Colorapocalypse;
import org.furranystudio.colorapocalypse.Config;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

/**
 * Destroys the blocks of an eliminated color, restricted to currently loaded chunks within
 * {@link Config#DESTRUCTION_RADIUS} of each online player (computed once, when {@link #start}
 * is called). All the actual world mutation happens on the main server thread — level writes
 * aren't thread-safe — but spread across ticks so a large area doesn't freeze the server in a
 * single tick.
 */
public final class DestructionQueue {

    private static final int CHUNKS_PER_TICK = 4;

    private static final Deque<PendingChunk> QUEUE = new ArrayDeque<>();
    private static Set<Block> targetBlocks = Set.of();

    private DestructionQueue() {
    }

    public static void register() {
        TickEvent.ServerTickEvent.Post.BUS.addListener(event -> tick());
    }

    public static boolean isActive() {
        return !QUEUE.isEmpty();
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
        if (QUEUE.isEmpty()) {
            return;
        }

        for (int i = 0; i < CHUNKS_PER_TICK && !QUEUE.isEmpty(); i++) {
            processChunk(QUEUE.poll());
        }

        if (QUEUE.isEmpty()) {
            Colorapocalypse.LOGGER.info("[ColorApocalypse] Destruction complete.");
        }
    }

    private static void processChunk(PendingChunk chunk) {
        ServerLevel level = chunk.level();
        int baseX = chunk.chunkX() << 4;
        int baseZ = chunk.chunkZ() << 4;
        int minY = level.getMinY();
        int height = level.getHeight();

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = minY; y < minY + height; y++) {
                    pos.set(baseX + x, y, baseZ + z);
                    BlockState state = level.getBlockState(pos);
                    if (!state.isAir() && targetBlocks.contains(state.getBlock())) {
                        level.removeBlock(pos, false);
                    }
                }
            }
        }
    }

    private record PendingChunk(ServerLevel level, int chunkX, int chunkZ) {
    }
}

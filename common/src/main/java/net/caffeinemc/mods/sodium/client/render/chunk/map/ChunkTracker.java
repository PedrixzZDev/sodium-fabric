package net.caffeinemc.mods.sodium.client.render.chunk.map;

import it.unimi.dsi.fastutil.longs.*;
import net.minecraft.world.level.ChunkPos;

public class ChunkTracker implements ClientChunkEventListener {
    private final Long2IntMap chunkStatus = new Long2IntOpenHashMap();
    private final LongSet chunkReady = new LongOpenHashSet();

    private final LongSet unloadQueue = new LongOpenHashSet();
    private final LongSet loadQueue = new LongOpenHashSet();

    public ChunkTracker() {}

    @Override
    public void updateMapCenter(int chunkX, int chunkZ) {}

    @Override
    public void updateLoadDistance(int loadDistance) {}

    @Override
    public void onChunkStatusAdded(int x, int z, int flags) {
        long key = ChunkPos.asLong(x, z);
        int prev = chunkStatus.getOrDefault(key, 0);
        int cur = prev | flags;

        if (prev == cur) return;

        chunkStatus.put(key, cur);
        updateNeighbors(x, z);
    }

    @Override
    public void onChunkStatusRemoved(int x, int z, int flags) {
        long key = ChunkPos.asLong(x, z);
        int prev = chunkStatus.getOrDefault(key, 0);
        int cur = prev & ~flags;

        if (prev == cur) return;

        if (cur == 0) {
            chunkStatus.remove(key);
        } else {
            chunkStatus.put(key, cur);
        }
        updateNeighbors(x, z);
    }

    private void updateNeighbors(int x, int z) {
        for (int ox = -1; ox <= 1; ox++) {
            for (int oz = -1; oz <= 1; oz++) {
                updateMerged(ox + x, oz + z);
            }
        }
    }

    private void updateMerged(int x, int z) {
        long key = ChunkPos.asLong(x, z);
        int flags = chunkStatus.getOrDefault(key, 0);

        for (int ox = -1; ox <= 1; ox++) {
            for (int oz = -1; oz <= 1; oz++) {
                flags &= chunkStatus.getOrDefault(ChunkPos.asLong(ox + x, oz + z), 0);
            }
        }

        if (flags == ChunkStatus.FLAG_ALL) {
            if (chunkReady.add(key) && !unloadQueue.remove(key)) {
                loadQueue.add(key);
            }
        } else {
            if (chunkReady.remove(key) && !loadQueue.remove(key)) {
                unloadQueue.add(key);
            }
        }
    }

    public LongCollection getReadyChunks() {
        return LongSets.unmodifiable(chunkReady);
    }

    public void forEachEvent(ChunkEventHandler loadEventHandler, ChunkEventHandler unloadEventHandler) {
        forEachChunk(unloadQueue, unloadEventHandler);
        unloadQueue.clear();

        forEachChunk(loadQueue, loadEventHandler);
        loadQueue.clear();
    }

    public static void forEachChunk(LongCollection queue, ChunkEventHandler handler) {
        queue.forEach(pos -> {
            int x = ChunkPos.getX(pos);
            int z = ChunkPos.getZ(pos);
            handler.apply(x, z);
        });
    }

    public interface ChunkEventHandler {
        void apply(int x, int z);
    }
}

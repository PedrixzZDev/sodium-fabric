package net.caffeinemc.mods.sodium.client.render.chunk.map;

import it.unimi.dsi.fastutil.longs.*;
import net.minecraft.world.level.ChunkPos;

public class ChunkTracker implements ClientChunkEventListener {
    private final Long2IntOpenHashMap chunkStatus = new Long2IntOpenHashMap();
    private final LongOpenHashSet chunkReady = new LongOpenHashSet();

    private final LongSet unloadQueue = new LongOpenHashSet();
    private final LongSet loadQueue = new LongOpenHashSet();

    public ChunkTracker() {

    }

    @Override
    public void updateMapCenter(int chunkX, int chunkZ) {

    }

    @Override
    public void updateLoadDistance(int loadDistance) {

    }

    @Override
    public void onChunkStatusAdded(int x, int z, int flags) {
        var key = ChunkPos.asLong(x, z);

        var prev = this.chunkStatus.get(key);
        var cur = prev | flags;

        if (prev == cur) {
            return;
        }

        this.chunkStatus.put(key, cur);

        this.updateNeighbors(x, z);
    }

    @Override
    public void onChunkStatusRemoved(int x, int z, int flags) {
        var key = ChunkPos.asLong(x, z);

        var prev = this.chunkStatus.get(key);
        int cur = prev & ~flags;

        if (prev == cur) {
            return;
        }

        if (cur == this.chunkStatus.defaultReturnValue()) {
            this.chunkStatus.remove(key);
        } else {
            this.chunkStatus.put(key, cur);
        }

        this.updateNeighbors(x, z);
    }

    private void updateNeighbors(int x, int z) {
        for (int ox = -1; ox <= 1; ox++) {
            for (int oz = -1; oz <= 1; oz++) {
                this.updateMerged(ox + x, oz + z);
            }
        }
    }

    private void updateMerged(int x, int z) {
    long key = ChunkPos.asLong(x, z);
    int flags = chunkStatus.get(key);

    // Crie um cache local para armazenar os resultados
    int[][] neighborFlags = new int[3][3];
    for (int ox = -1; ox <= 1; ox++) {
        for (int oz = -1; oz <= 1; oz++) {
            neighborFlags[ox + 1][oz + 1] = chunkStatus.get(ChunkPos.asLong(ox + x, oz + z));
        }
    }

    // Reduza o número de operações
    for (int i = 0; i < 3; i++) {
        for (int j = 0; j < 3; j++) {
            flags &= neighborFlags[i][j];
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
        return LongSets.unmodifiable(this.chunkReady);
    }

    public void forEachEvent(ChunkEventHandler loadEventHandler, ChunkEventHandler unloadEventHandler) {
        forEachChunk(this.unloadQueue, unloadEventHandler);
        this.unloadQueue.clear();

        forEachChunk(this.loadQueue, loadEventHandler);
        this.loadQueue.clear();
    }

    public static void forEachChunk(LongCollection queue, ChunkEventHandler handler) {
        var iterator = queue.iterator();

        while (iterator.hasNext()) {
            var pos = iterator.nextLong();

            var x = ChunkPos.getX(pos);
            var z = ChunkPos.getZ(pos);

            handler.apply(x, z);
        }
    }

    public interface ChunkEventHandler {
        void apply(int x, int z);
    }
}

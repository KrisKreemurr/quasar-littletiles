package team.creative.littletiles.server.level.little;

import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ChunkLevel;
import it.unimi.dsi.fastutil.longs.LongIterators;
import it.unimi.dsi.fastutil.longs.LongIterator;
import java.util.function.LongConsumer;
import net.minecraft.util.TriState;
import java.util.concurrent.Executor;

import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

public class LittleDistanceManager extends DistanceManager {
    
    protected LittleDistanceManager(Executor exe, Executor exe2) {
        super(null, null, null); // never runs: created with CreativeHackery.allocateInstance
    }
    
    @Override
    protected boolean isChunkToRemove(long pos) {
        return false;
    }
    
    @Override
    protected ChunkHolder getChunk(long pos) {
        return null;
    }
    
    @Override
    protected ChunkHolder updateChunkScheduling(long pos, int oldLevel, ChunkHolder holder, int newLevel) {
        return null;
    }
    
    @Override
    public boolean runAllUpdates(ChunkMap map) {
        return true;
    }
    
    
    
    
    
    
    
    @Override
    public void addPlayer(SectionPos pos, ServerPlayer player) {}
    
    @Override
    public void removePlayer(SectionPos pos, ServerPlayer player) {
        
    }
    
    @Override
    public boolean inEntityTickingRange(long pos) {
        return true;
    }
    
    @Override
    public boolean inBlockTickingRange(long pos) {
        return true;
    }
    
    @Override
    public void updateSimulationDistance(int distance) {}
    
    @Override
    public int getNaturalSpawnChunkCount() {
        return 0;
    }
    
    @Override
    public TriState hasPlayersNearby(long pos) {
        return TriState.FALSE;
    }
    
    // Quasar MC: newer DistanceManager methods. This stub is created without its constructor running,
    // so anything the sub-level could call must be overridden here.
    @Override
    public void forEachEntityTickingChunk(LongConsumer consumer) {}
    
    @Override
    public LongIterator getSpawnCandidateChunks() {
        return LongIterators.EMPTY_ITERATOR;
    }
    
    @Override
    public int getChunkLevel(long pos, boolean simulation) {
        return ChunkLevel.byStatus(FullChunkStatus.ENTITY_TICKING);
    }
    
    @Override
    protected void updatePlayerTickets(int viewDistance) {}
    
    @Override
    public String getDebugStatus() {
        return "";
    }
    
    
    
    @Override
    public boolean hasTickets() {
        return false;
    }
    
    
}

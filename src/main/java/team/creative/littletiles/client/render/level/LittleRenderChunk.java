package team.creative.littletiles.client.render.level;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nullable;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.primitives.Doubles;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.MeshData.SortState;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.CrashReport;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.AddSectionGeometryEvent.AdditionalSectionRenderer;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.type.list.Tuple;
import team.creative.creativecore.common.util.type.map.ChunkLayerMap;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.render.cache.buffer.BufferCollection;
import team.creative.littletiles.client.render.entity.LittleLevelRenderManager;
import team.creative.littletiles.client.render.level.transition.LevelTransition.SectionAdditional;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;
import team.creative.littletiles.client.render.mc.SectionCompilerResultsExtender;
import team.creative.littletiles.common.level.little.LittleSubLevel;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

@OnlyIn(Dist.CLIENT)
public class LittleRenderChunk implements RenderChunkExtender {

    public LittleLevelRenderManager manager;
    public SectionPos section;
    public BlockPos pos;
    private AABB bb;
    public AtomicBoolean considered;
    private RebuildTask lastRebuildTask;
    private ResortTransparencyTask lastResortTransparencyTask;
    private Set<BlockEntity> globalBlockEntities;
    private boolean dirty;
    private SectionPos[] neighbors;
    private boolean playerChanged;
    private SectionAdditional additional;
    public ChunkLayerMap<BufferCollection> lastUploaded;
    private volatile int queued;
    public LittleRenderChunk(LittleLevelRenderManager manager, SectionPos pos) {}

    public LittleSubLevel level() { return null; }

    private boolean doesChunkExistAt(SectionPos pos) { return false; }

    public boolean hasAllNeighbors() { return false; }

    public SectionAdditional getAdditional() { return null; }

    public void setAdditional(SectionAdditional uploader) {}

    public ChunkLayerMap<BufferCollection> getLastUploaded() { return null; }

    public void setLastUploaded(ChunkLayerMap<BufferCollection> uploaded) {}

    public int getQueued() { return 0; }

    public void setQueued(int queued) {}

    public AABB getBB() { return null; }


    protected double getDistToPlayerSqr() { return 0D; }


    private void reset() {}

    public void releaseBuffers() {}

    public void setDirty(boolean playerChanged) {}

    public void setNotDirty() {}

    public boolean isDirty() { return false; }

    public void markReadyForUpdate(boolean playerChanged) {}

    public boolean isDirtyFromPlayer() { return false; }

    public boolean resortTransparency(ChunkSectionLayer layer) { return false; }

    protected boolean cancelTasks() { return false; }

    public CompileTask createCompileTask(RenderRegionCache cache) { return null; }

    public void compileASync(RenderRegionCache cache) {}

    public void compile(RenderRegionCache cache) {}

    public void updateGlobalBlockEntities(Collection<BlockEntity> blockEntities) {}

    public SortState getTransparencyState() { return null; }

    public void setTransparencyState(SortState state) {}

    public void setHasBlock(ChunkSectionLayer layer) {}

    public boolean isEmpty(ChunkSectionLayer layer) { return false; }

    public VertexSorting createVertexSorting(double x, double y, double z) { return null; }

    public static enum SectionTaskResult {

        SUCCESSFUL, CANCELLED;
    }

    public abstract class CompileTask implements Comparable<CompileTask> {

        protected double distAtCreation;
        protected AtomicBoolean isCancelled;
        public boolean isHighPriority;
        public CompileTask(double distAtCreation, boolean isHighPriority) {}

        public abstract CompletableFuture<SectionTaskResult> doTask(SectionBufferBuilderPack p_112853_);

        public abstract void cancel();

        public abstract String name();

        public int compareTo(CompileTask other) { return 0; }

        protected CompileTask() {}

    }

    class RebuildTask extends CompileTask {

        private List<AdditionalSectionRenderer> additionalRenderers;

        public String name() { return null; }

        public CompletableFuture<SectionTaskResult> doTask(SectionBufferBuilderPack pack) { return null; }

        public void cancel() {}

        protected RebuildTask() {}

    }

    @OnlyIn(Dist.CLIENT)
    class ResortTransparencyTask extends CompileTask {


        public String name() { return null; }

        public CompletableFuture<SectionTaskResult> doTask(SectionBufferBuilderPack pack) { return null; }

        public void cancel() {}

        protected ResortTransparencyTask() {}

    }

    protected LittleRenderChunk() {}

}


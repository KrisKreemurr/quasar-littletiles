package team.creative.littletiles.client.render.cache.build;

import java.util.ArrayList;
import java.util.List;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.client.render.box.QuadGeneratorContext;
import team.creative.creativecore.common.level.LevelAccessorFake;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.type.list.SingletonList;
import team.creative.creativecore.common.util.type.list.Tuple;
import team.creative.creativecore.common.util.type.map.ChunkLayerMap;
import team.creative.creativecore.common.util.type.map.ChunkLayerMapList;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.render.cache.buffer.BufferCache;
import team.creative.littletiles.client.render.cache.pipeline.LittleRenderPipeline;
import team.creative.littletiles.client.render.cache.pipeline.LittleRenderPipelineType;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;
import team.creative.littletiles.client.render.overlay.LittleTilesProfilerOverlay;
import team.creative.littletiles.client.render.tile.LittleRenderBox;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.mod.sable.SableManager;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

@OnlyIn(Dist.CLIENT)
public class RenderingThread extends Thread {

    public static volatile int CURRENT_RENDERING_INDEX;
    private static ChunkLayerMap<BufferCache> EMPTY_HOLDERS;
    public static volatile List<RenderingThread> THREADS;
    public static Minecraft MC;
    public static RenderingBlockQueue QUEUE;
    public static synchronized void initThreads(int count) {}

    private static void emptyQueue() {}

    public static synchronized void unload() {}

    public static synchronized boolean queue(BETiles be, boolean hasPos, long pos) { return false; }

    public static int queueSize() { return 0; }

    public static synchronized void reload() {}

    public RenderingThread() {}

    private LevelAccessorFake fakeAccess;
    private ChunkLayerMap<BufferCache> buffers;
    public boolean active;
    private volatile boolean requiresReload;
    private LittleRenderPipeline[] pipelines;
    private QuadGeneratorContext quadContext;
    public <T extends LittleRenderPipeline> T get(LittleRenderPipelineType<T> type) { return null; }

    public void run() {}

    public void stopImmediately() {}

    public void finishWithError(RenderingBlockContext data) {}

    public void unqueue(RenderingBlockContext data) {}

    public boolean finish(RenderingBlockContext data, ChunkLayerMap<BufferCache> buffers, int renderState, boolean force) { return false; }

    public static class BlockNotYetLoadedException extends RuntimeException {

    }

    public static class RemovedBlockEntityException extends Exception {

        public RemovedBlockEntityException(String arg0) { super(arg0); }

        protected RemovedBlockEntityException() {}

    }

    public static class RenderingException extends Exception {

        public RenderingException(String arg0) { super(arg0); }

        protected RenderingException() {}

    }

    public static class RenderingBlockedException extends RenderingException {

        public RenderingBlockedException() { super(""); }

    }

}


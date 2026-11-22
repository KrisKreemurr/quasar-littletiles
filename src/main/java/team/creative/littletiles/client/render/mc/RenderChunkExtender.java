package team.creative.littletiles.client.render.mc;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL15C;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.MeshData.SortState;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.world.phys.Vec3;
import team.creative.creativecore.common.util.type.list.Tuple;
import team.creative.creativecore.common.util.type.map.ChunkLayerMap;
import team.creative.littletiles.client.render.cache.LayeredBufferCache;
import team.creative.littletiles.client.render.cache.buffer.BufferCollection;
import team.creative.littletiles.client.render.cache.buffer.ChunkBufferDownloader.SimpleChunkBufferDownloader;
import team.creative.littletiles.client.render.level.transition.LevelTransition.SectionAdditional;
import team.creative.littletiles.client.render.cache.buffer.ChunkBufferUploader;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

public interface RenderChunkExtender {


    public void markReadyForUpdate(boolean playerChanged);

    public default VertexSorting createVertexSorting(Vec3 vec) { return null; }

    public VertexSorting createVertexSorting(double x, double y, double z);

    public boolean isEmpty(ChunkSectionLayer layer);

    public SortState getTransparencyState();

    public void setTransparencyState(SortState state);

    public void setHasBlock(ChunkSectionLayer layer);

    public int getQueued();

    public void setQueued(int queued);

    public default boolean isBuilding() { return false; }

    public ChunkLayerMap<BufferCollection> getLastUploaded();

    public void setLastUploaded(ChunkLayerMap<BufferCollection> uploaded);

    public SectionAdditional getAdditional();

    public void setAdditional(SectionAdditional uploader);

    public default void prepareUpload() {}

    public default void beforeCompileEnds(@Nullable Function<ChunkSectionLayer, ChunkBufferUploader> builderSupplier, Function<ChunkSectionLayer, BufferCollection> bufferSupplier) {}

    public default void uploaded(ChunkSectionLayer layer, BufferCollection buffers) {}

    public default void backToRAM() {}

    public default ByteBuffer downloadUploadedData(VertexBufferExtender buffer, long offset, int size) { return null; }

    public default boolean appendRenderData(Iterable<? extends LayeredBufferCache> blocks) { return false; }

    public default void startBuilding() {}

    public default void endBuilding() {}

}


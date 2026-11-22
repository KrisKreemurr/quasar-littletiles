package team.creative.littletiles.client.render.cache.pipeline;

import java.util.List;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.type.list.SingletonList;
import team.creative.creativecore.common.util.type.list.Tuple;
import team.creative.creativecore.common.util.type.map.ChunkLayerMap;
import team.creative.creativecore.common.util.type.map.ChunkLayerMapList;
import team.creative.littletiles.api.client.IFakeRenderingBlock;
import team.creative.littletiles.client.render.cache.buffer.BufferCache;
import team.creative.littletiles.client.render.cache.buffer.BufferHolder;
import team.creative.littletiles.client.render.cache.build.RenderingBlockContext;
import team.creative.littletiles.client.render.tile.LittleRenderBox;
import team.creative.littletiles.common.level.little.LittleSubLevel;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

@OnlyIn(Dist.CLIENT)
public class LittleRenderPipelineForge extends LittleRenderPipeline {

    private ChunkLayerMap<ByteBufferBuilder> byteBuilder;
    private ChunkLayerMap<IntArrayList> indexes;
    private MutableBlockPos modelOffset;
    private ByteBufferBuilder getOrCreate(ChunkSectionLayer layer) { return null; }

    private void clearIndexes() {}


    public void reload() {}

    public void release() {}

}


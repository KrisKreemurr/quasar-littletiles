package team.creative.littletiles.client.render.entity;

import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import javax.annotation.Nullable;
import org.joml.Matrix4f;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelDataManager;
import net.neoforged.neoforge.common.extensions.ILevelExtension;
import team.creative.littletiles.client.render.cache.build.RenderingLevelHandler;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.entity.LittleEntity;
import team.creative.littletiles.common.level.little.LittleSubLevel;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

@OnlyIn(Dist.CLIENT)
public abstract class LittleEntityRenderManager<T extends LittleEntity> {

    public static Minecraft mc;
    public Boolean isInSight;
    public boolean needsFullRenderChunkUpdate;
    protected Set<BlockEntity> globalBlockEntities;
    private Int2ObjectMap<BlockDestructionProgress> destroyingBlocks;
    private Long2ObjectMap<SortedSet<BlockDestructionProgress>> destructionProgress;
    private int ticks;
    public T entity;
    public LittleEntityRenderManager(T entity) {}

    public LittleSubLevel getLevel() { return null; }

    public abstract RenderChunkExtender getRenderChunk(long pos);

    public abstract boolean isSmall();

    public abstract RenderingLevelHandler renderingHandler();

    public void setupRender(Camera camera, @Nullable Frustum frustum, boolean capturedFrustum, boolean spectator) {}

    public void unload() {}

    public void allChanged() {}

    public Iterable<Long2ObjectMap.Entry<SortedSet<BlockDestructionProgress>>> getDestructions() { return null; }

    public SortedSet<BlockDestructionProgress> getDestructionProgress(BlockPos pos) { return null; }

    public void clientTick() {}

    public void destroyBlockProgress(int id, BlockPos pos, int progress) {}

    private void removeProgress(BlockDestructionProgress destruction) {}

    public abstract void compileSections(Camera camera);

    protected MultiBufferSource prepareBlockEntity(PoseStack pose, LittleSubLevel level, BlockPos pos, MultiBufferSource bufferSource) { return null; }

    protected abstract void renderAllBlockEntities(PoseStack pose, Frustum frustum, Vec3 cam, float frameTime, MultiBufferSource bufferSource);

    protected void renderBlockEntity(BlockEntity blockentity, PoseStack pose, Frustum frustum, Vec3 cam, float frameTime, MultiBufferSource bufferSource) {}

    public void renderBlockEntitiesAndDestruction(PoseStack pose, Frustum frustum, Vec3 cam, float frameTime, MultiBufferSource bufferSource) {}

    public void renderGlobalEntities(PoseStack pose, Frustum frustum, Vec3 cam, float frameTime, MultiBufferSource bufferSource) {}

    public abstract void resortTransparency(ChunkSectionLayer layer, double x, double y, double z);

    public abstract void renderChunkLayer(ChunkSectionLayer layer, PoseStack pose, double x, double y, double z, Matrix4f projectionMatrix, Uniform offset);

    public void onLightUpdate(LightLayer layer, SectionPos pos) {}

    public void blockChanged(BlockGetter level, BlockPos pos, BlockState actualState, BlockState setState, int updateType) {}

    protected void setBlockDirty(BlockPos pos, boolean playerChanged) {}

    public void setBlocksDirty(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {}

    public void setBlockDirty(BlockPos pos, BlockState actualState, BlockState setState) {}

    public void setSectionDirtyWithNeighbors(int x, int y, int z) {}

    public void setSectionDirty(int x, int y, int z) {}

    protected abstract void setSectionDirty(int x, int y, int z, boolean playerChanged);

    public abstract boolean shouldRender(boolean sodiumLayer);

}


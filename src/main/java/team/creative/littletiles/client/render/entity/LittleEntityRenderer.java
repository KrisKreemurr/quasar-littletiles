package team.creative.littletiles.client.render.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import com.mojang.blaze3d.vertex.PoseStack;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.common.util.type.list.Tuple;
import team.creative.creativecore.common.util.type.map.ChunkLayerMapList;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.render.cache.build.RenderingBlockContext;
import team.creative.littletiles.client.render.level.LittleItemRenderer;
import team.creative.littletiles.client.render.tile.LittleRenderBox;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.entity.LittleEntity;

/** Quasar MC (26.3): draws animated structures (doors, sliding parts, any LittleTiles sub-level entity).
 * <p>
 * The original compiled every sub-level into its own chunk sections on a render thread. Here each block of the
 * structure is meshed once into a vertex list (same face/texture cutting as the world mesher) and cached until it
 * changes; every frame the lists are replayed through {@link SubmitNodeCollector#submitCustomGeometry} with the
 * structure's current transform (its origin pose), lit like an entity. */
public class LittleEntityRenderer extends EntityRenderer<LittleEntity, LittleEntityRenderer.State> {

    public static class State extends EntityRenderState {
        public LittleEntity entity;
    }

    private static final class Mesh {
        float[] solid;
        float[] translucent;
    }

    private static final Map<BETiles, Mesh> MESHES = new WeakHashMap<>();

    public static void invalidate(BETiles be) {
        synchronized (MESHES) {
            MESHES.remove(be);
        }
    }

    public LittleEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(LittleEntity entity, State state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.entity = entity;
    }

    @Override
    public boolean shouldRender(LittleEntity entity, Frustum frustum, double camX, double camY, double camZ, float partialTick) {
        if (!entity.hasLoaded())
            return false;
        return entity.shouldRender(camX, camY, camZ) && frustum.isVisible(entity.getBoundingBox().inflate(0.5D));
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        LittleEntity entity = state.entity;
        if (entity == null || !entity.hasLoaded() || entity.getSubLevel() == null)
            return;
        try {
            Vec3 cam = camera.pos;
            PoseStack world = copy(pose);
            // The dispatcher moved the pose to the entity; go back to camera-relative world space, then apply the
            // structure's transform exactly like the original (origin pose setup returns the camera in its space).
            world.translate(-(state.x - cam.x), -(state.y - cam.y), -(state.z - cam.z));
            submitStructure(entity, world, cam, state.partialTick, state.lightCoords, collector);
        } catch (Throwable e) {
            if (!logged) {
                logged = true;
                LittleTiles.LOGGER.error("Failed to render animated LittleTiles structure (logged once)", e);
            }
        }
    }

    /** Submits every block of the structure's sub-level. {@code world} is the space the structure's origin lives in, {@code cam} the camera position in
     * that space (zero for GUI previews). Also used by the blueprint previews. */
    public static void submitStructure(LittleEntity entity, PoseStack world, Vec3 cam, float partialTick, int light, SubmitNodeCollector collector) {
        world = copy(world);
        Vec3 local = entity.getOrigin().pose(partialTick).setup(world, cam);
        for (ChunkAccess chunk : entity.getSubLevel().chunks()) {
            if (!(chunk instanceof LevelChunk levelChunk))
                continue;
            for (BlockEntity be : levelChunk.getBlockEntities().values()) {
                if (!(be instanceof BETiles tiles))
                    continue;
                Mesh mesh = mesh(tiles);
                if (mesh == null)
                    continue;
                BlockPos pos = be.getBlockPos();
                if (mesh.solid != null && mesh.solid.length > 0) {
                    PoseStack at = at(world, pos, local);
                    float[] data = mesh.solid;
                    collector.submitCustomGeometry(at, Sheets.cutoutBlockItemSheet(), (p, consumer) -> LittleItemRenderer.replay(data, p, consumer, light,
                        net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY));
                }
                if (mesh.translucent != null && mesh.translucent.length > 0) {
                    PoseStack at = at(world, pos, local);
                    float[] data = mesh.translucent;
                    collector.submitCustomGeometry(at, Sheets.translucentBlockItemSheet(), (p, consumer) -> LittleItemRenderer.replay(data, p, consumer, light,
                        net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY));
                }
            }
        }
    }

    private static boolean logged;

    private static PoseStack copy(PoseStack pose) {
        PoseStack copy = new PoseStack();
        copy.last().pose().set(pose.last().pose());
        copy.last().normal().set(pose.last().normal());
        return copy;
    }

    private static PoseStack at(PoseStack world, BlockPos pos, Vec3 cam) {
        PoseStack at = copy(world); // every submission keeps its own pose
        at.translate(pos.getX() - cam.x, pos.getY() - cam.y, pos.getZ() - cam.z);
        return at;
    }

    private static Mesh mesh(BETiles be) {
        synchronized (MESHES) {
            Mesh mesh = MESHES.get(be);
            if (mesh != null)
                return mesh;
        }
        Mesh mesh = build(be);
        synchronized (MESHES) {
            MESHES.put(be, mesh);
        }
        return mesh;
    }

    private static Mesh build(BETiles be) {
        Mesh mesh = new Mesh();
        Int2ObjectMap<ChunkLayerMapList<LittleRenderBox>> boxes;
        synchronized (be) {
            be.render.startBuildingCache();
            try {
                RenderingBlockContext data = new RenderingBlockContext(be, true, 0, null);
                data.beforeBuilding();
                boxes = be.render.getRenderingBoxes(data);
                data.clearQuadBuilding();
            } finally {
                be.render.unsetBlocked();
            }
        }
        if (boxes == null || boxes.isEmpty())
            return mesh;
        List<RenderBox> solid = new ArrayList<>(), translucent = new ArrayList<>();
        for (ChunkLayerMapList<LittleRenderBox> map : boxes.values())
            for (Tuple<ChunkSectionLayer, List<LittleRenderBox>> tuple : map.tuples())
                (tuple.key == ChunkSectionLayer.TRANSLUCENT ? translucent : solid).addAll(tuple.value);
        mesh.solid = LittleItemRenderer.buildBoxes(solid);
        mesh.translucent = LittleItemRenderer.buildBoxes(translucent);
        return mesh;
    }
}

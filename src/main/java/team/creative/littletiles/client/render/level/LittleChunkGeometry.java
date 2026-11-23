package team.creative.littletiles.client.render.level;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3fc;

import com.mojang.blaze3d.vertex.VertexConsumer;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.RandomSource;
import team.creative.creativecore.common.util.type.list.Tuple;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.client.event.AddSectionGeometryEvent;
import net.neoforged.neoforge.common.NeoForge;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.vec.Vec3f;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.type.map.ChunkLayerMapList;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.render.cache.build.RenderingBlockContext;
import team.creative.littletiles.client.render.tile.LittleRenderBox;
import team.creative.littletiles.common.block.entity.BETiles;

/** Quasar MC (26.3): draws LittleTiles blocks as part of the chunk mesh.
 * <p>
 * Replaces LittleTiles' own render thread + buffer cache (parked). When a section is rebuilt, NeoForge's
 * {@link AddSectionGeometryEvent} lets us add geometry to the same buffers vanilla blocks use, so tiles are drawn,
 * sorted and culled exactly like terrain (and whatever Quasium does to terrain applies to them too).
 * <p>
 * Per tile box and visible face we take the block's real model quads for that side and cut them to the face's polygon,
 * keeping the texture position-locked like the original (a tile shows the part of the texture it covers). */
public final class LittleChunkGeometry {

    private LittleChunkGeometry() {}

    private static final Facing[] FACINGS = Facing.VALUES;

    public static void init() {
        NeoForge.EVENT_BUS.addListener(LittleChunkGeometry::onAddSectionGeometry);
    }

    private static void onAddSectionGeometry(AddSectionGeometryEvent event) {
        Level level = event.getLevel();
        if (level == null)
            return;
        BlockPos origin = event.getSectionOrigin();
        LevelChunk chunk = level.getChunkSource().getChunkNow(SectionPos.blockToSectionCoord(origin.getX()), SectionPos.blockToSectionCoord(origin.getZ()));
        if (chunk == null)
            return;
        int minY = origin.getY();
        int maxY = minY + 16;
        List<BETiles> tiles = null;
        for (BlockEntity be : chunk.getBlockEntities().values()) {
            if (!(be instanceof BETiles t))
                continue;
            int y = be.getBlockPos().getY();
            if (y < minY || y >= maxY)
                continue;
            if (tiles == null)
                tiles = new ArrayList<>();
            tiles.add(t);
        }
        if (tiles == null)
            return;
        final List<BETiles> found = tiles;
        event.addRenderer(context -> {
            Mesher mesher = new Mesher(context);
            for (BETiles be : found) {
                try {
                    mesher.block(be);
                } catch (Throwable e) {
                    LittleTiles.LOGGER.error("Failed to mesh LittleTiles block at {}", be.getBlockPos(), e);
                }
            }
        });
    }

    /** One section build. Not thread-safe; one instance per build. */
    private static final class Mesher {

        private final AddSectionGeometryEvent.SectionRenderingContext context;
        private final BlockAndTintGetter region;
        private final Minecraft mc = Minecraft.getInstance();
        private final List<BlockStateModelPart> parts = new ArrayList<>();
        private final List<Vec3f[]> polygons = new ArrayList<>();
        private final RandomSource random = RandomSource.create();
        private final BlockPos.MutableBlockPos lightPos = new BlockPos.MutableBlockPos();
        private final BlockPos.MutableBlockPos samplePos = new BlockPos.MutableBlockPos();
        private final boolean smooth = Minecraft.getInstance().options.ambientOcclusion().get();
        /** Per-section cache of (light, occludes) per sampled block, keyed by BlockPos.asLong. */
        private final it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap samples = new it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap();
        // per-face state for vertex(): world block of the tile, flat fallback light, whether smoothing applies
        private BlockPos curPos;
        private int curFlat;
        private boolean curSmooth;
        private int curEmission;
        private Facing curFacing;

        Mesher(AddSectionGeometryEvent.SectionRenderingContext context) {
            this.context = context;
            this.region = context.getRegion();
        }

        void block(BETiles be) {
            if (be.isRemoved() || be.getLevel() == null)
                return;
            Int2ObjectMap<ChunkLayerMapList<LittleRenderBox>> boxes;
            synchronized (be) { // the original synchronises box creation with grid changes the same way
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
                return;

            BlockPos pos = be.getBlockPos();
            float ox = SectionPos.sectionRelative(pos.getX());
            float oy = SectionPos.sectionRelative(pos.getY());
            float oz = SectionPos.sectionRelative(pos.getZ());

            for (ChunkLayerMapList<LittleRenderBox> map : boxes.values())
                for (Tuple<ChunkSectionLayer, List<LittleRenderBox>> tuple : map.tuples()) {
                    ChunkSectionLayer layer = tuple.key;
                    VertexConsumer consumer = null;
                    for (LittleRenderBox box : tuple.value) {
                        if (box.state == null)
                            continue;
                        if (consumer == null)
                            consumer = context.getOrCreateChunkBuffer(layer);
                        box(consumer, layer, box, pos, ox, oy, oz);
                    }
                }
        }

        private void box(VertexConsumer consumer, ChunkSectionLayer layer, LittleRenderBox box, BlockPos pos, float ox, float oy, float oz) {
            BlockState state = box.state;
            BlockStateModel model = mc.getModelManager().getBlockStateModelSet().get(state);
            parts.clear();
            random.setSeed(state.getSeed(pos));
            model.collectParts(random, parts);
            if (parts.isEmpty())
                return;

            int boxColor = box.color == -1 ? ColorUtils.WHITE : box.color;
            // A see-through tile colour moves the whole block to the translucent layer (see LittleTile.canRenderInLayer),
            // so in that case every quad of the model belongs here; otherwise only quads made for this layer.
            boolean takeAllQuads = ColorUtils.isTransparent(boxColor);

            for (Facing facing : FACINGS) {
                polygons.clear();
                box.quasar$collectFacePolygons(facing, polygons);
                if (polygons.isEmpty())
                    continue;
                Direction dir = facing.toVanilla();
                int light = box.emissive ? LightCoordsUtil.FULL_BRIGHT : light(pos, facing, polygons);
                curPos = pos;
                curFlat = light;
                curSmooth = smooth && !box.emissive && state.getLightEmission() == 0;
                for (BlockStateModelPart part : parts) {
                    quads(consumer, layer, takeAllQuads, part.getQuads(dir), dir, facing, state, pos, boxColor, light, ox, oy, oz);
                    quads(consumer, layer, takeAllQuads, part.getQuads(null), dir, facing, state, pos, boxColor, light, ox, oy, oz);
                }
            }
        }

        private void quads(VertexConsumer consumer, ChunkSectionLayer layer, boolean takeAll, List<BakedQuad> quads, Direction dir, Facing facing, BlockState state,
                BlockPos pos, int boxColor, int light, float ox, float oy, float oz) {
            for (int i = 0; i < quads.size(); i++) {
                BakedQuad quad = quads.get(i);
                if (quad.direction() != dir)
                    continue;
                if (!takeAll && quad.materialInfo().layer() != layer)
                    continue;
                for (Vec3f[] polygon : polygons)
                    emit(consumer, quad, polygon, facing, state, pos, boxColor, light, ox, oy, oz);
            }
        }

        /** Light for a face: faces lying on the block's outer boundary take the neighbour's light (like vanilla faces),
         * faces inside the block take the light of the block itself. */
        private int light(BlockPos pos, Facing facing, List<Vec3f[]> polygons) {
            Vec3f first = polygons.get(0)[0];
            float along = first.get(facing.axis);
            boolean boundary = facing.positive ? along >= 0.999F : along <= 0.001F;
            if (boundary)
                lightPos.setWithOffset(pos, facing.toVanilla());
            else
                lightPos.set(pos);
            if (region instanceof BlockAndLightGetter getter)
                return LightCoordsUtil.getLightCoords(getter, lightPos);
            return LightCoordsUtil.FULL_BRIGHT;
        }

        private void emit(VertexConsumer consumer, BakedQuad quad, Vec3f[] polygon, Facing facing, BlockState state, BlockPos pos, int boxColor, int light, float ox,
                float oy, float oz) {
            Axis one = facing.one();
            Axis two = facing.two();

            // Quad rectangle on the face plane and an affine (a, b) -> (u, v) map from three of its corners.
            float minA = Float.MAX_VALUE, minB = Float.MAX_VALUE, maxA = -Float.MAX_VALUE, maxB = -Float.MAX_VALUE;
            for (int k = 0; k < 4; k++) {
                Vector3fc p = quad.position(k);
                float a = get(p, one), b = get(p, two);
                minA = Math.min(minA, a);
                maxA = Math.max(maxA, a);
                minB = Math.min(minB, b);
                maxB = Math.max(maxB, b);
            }
            if (maxA - minA < 1.0E-6F || maxB - minB < 1.0E-6F)
                return;
            float[] uvMap = uvMap(quad, one, two);
            if (uvMap == null)
                return;

            Vec3f[] shape = polygon;
            if (minA > 0.0001F || minB > 0.0001F || maxA < 0.9999F || maxB < 0.9999F) {
                shape = clip(polygon, one, two, minA, minB, maxA, maxB);
                if (shape == null)
                    return;
            }

            int color = multiply(boxColor, tint(quad, state, pos));
            color = shade(color, quad, facing);
            int emission = quad.materialInfo().lightEmission();
            int quadLight = emission > 0 ? LightCoordsUtil.lightCoordsWithEmission(light, emission) : light;
            curEmission = emission;
            boolean saveSmooth = curSmooth;
            if (emission > 0)
                curSmooth = false;
            curFacing = facing;
            Direction normal = facing.toVanilla();
            float nx = normal.getStepX(), ny = normal.getStepY(), nz = normal.getStepZ();

            // Fan -> quads (0, i, i+1, i+2); an odd remainder becomes a quad with a repeated last vertex.
            int index = 1;
            while (index < shape.length - 2) {
                vertex(consumer, shape[0], one, two, uvMap, color, quadLight, nx, ny, nz, ox, oy, oz);
                vertex(consumer, shape[index], one, two, uvMap, color, quadLight, nx, ny, nz, ox, oy, oz);
                vertex(consumer, shape[index + 1], one, two, uvMap, color, quadLight, nx, ny, nz, ox, oy, oz);
                vertex(consumer, shape[index + 2], one, two, uvMap, color, quadLight, nx, ny, nz, ox, oy, oz);
                index += 2;
            }
            if (index < shape.length - 1) {
                vertex(consumer, shape[0], one, two, uvMap, color, quadLight, nx, ny, nz, ox, oy, oz);
                vertex(consumer, shape[index], one, two, uvMap, color, quadLight, nx, ny, nz, ox, oy, oz);
                vertex(consumer, shape[index + 1], one, two, uvMap, color, quadLight, nx, ny, nz, ox, oy, oz);
                vertex(consumer, shape[index + 1], one, two, uvMap, color, quadLight, nx, ny, nz, ox, oy, oz);
            }
            curSmooth = saveSmooth;
        }

        private void vertex(VertexConsumer consumer, Vec3f vec, Axis one, Axis two, float[] uvMap, int color, int light, float nx, float ny, float nz,
                float ox, float oy, float oz) {
            float a = vec.get(one);
            float b = vec.get(two);
            float u = uvMap[0] * a + uvMap[1] * b + uvMap[2];
            float v = uvMap[3] * a + uvMap[4] * b + uvMap[5];
            if (curSmooth) {
                long lit = smoothLight(vec, one, two);
                light = (int) lit;
                float ao = Float.intBitsToFloat((int) (lit >>> 32));
                if (ao < 0.999F) {
                    int r = (int) (((color >> 16) & 0xFF) * ao);
                    int g = (int) (((color >> 8) & 0xFF) * ao);
                    int bl = (int) ((color & 0xFF) * ao);
                    color = (color & 0xFF000000) | (r << 16) | (g << 8) | bl;
                }
            }
            consumer.addVertex(ox + vec.x, oy + vec.y, oz + vec.z, color, u, v, 0, light, nx, ny, nz);
        }

        /** Smooth lighting + ambient occlusion for one vertex (vanilla style, but continuous so tile corners inside a block
         * blend too). The vertex is pushed a hair out of its face; the 2x2 blocks around it on that plane are sampled with
         * bilinear weights: open blocks contribute light, view-blocking blocks darken (0.2 like vanilla AO). At a block
         * corner this is exactly vanilla's 4-block average. Returns ao float bits << 32 | packed light. */
        private long smoothLight(Vec3f vec, Axis one, Axis two) {
            Facing f = curFacing;
            Axis n = f.axis;
            double px = curPos.getX() + vec.x, py = curPos.getY() + vec.y, pz = curPos.getZ() + vec.z;
            double push = f.positive ? 0.01 : -0.01;
            double pa = coord(px, py, pz, one) - 0.5, pb = coord(px, py, pz, two) - 0.5;
            int ca = (int) Math.floor(pa), cb = (int) Math.floor(pb);
            int cn = (int) Math.floor(coord(px, py, pz, n) + push);
            float fa = (float) (pa - ca), fb = (float) (pb - cb);
            float aoSum = 0, wSum = 0, blockSum = 0, skySum = 0;
            for (int i = 0; i < 2; i++)
                for (int j = 0; j < 2; j++) {
                    float w = (i == 0 ? 1 - fa : fa) * (j == 0 ? 1 - fb : fb);
                    if (w <= 0)
                        continue;
                    set(samplePos, one, ca + i, two, cb + j, n, cn);
                    long sample = sample(samplePos);
                    boolean occludes = ((sample >>> 32) & 1) != 0;
                    boolean skip = ((sample >>> 32) & 2) != 0;
                    aoSum += w * (occludes ? 0.2F : 1F);
                    if (!occludes && !skip) {
                        int packed = (int) sample;
                        blockSum += w * (packed & 0xFFFF);
                        skySum += w * ((packed >>> 16) & 0xFFFF);
                        wSum += w;
                    }
                }
            int light;
            if (wSum <= 0.0001F)
                light = curFlat;
            else {
                int blk = Math.round(blockSum / wSum);
                int sky = Math.round(skySum / wSum);
                light = (sky << 16) | blk;
            }
            return ((long) Float.floatToIntBits(aoSum) << 32) | (light & 0xFFFFFFFFL);
        }

        /** (occludes ? 1 : 0) << 32 | packed light for a block, cached for the section build. */
        private long sample(BlockPos.MutableBlockPos p) {
            long key = p.asLong();
            long cached = samples.getOrDefault(key, Long.MIN_VALUE);
            if (cached != Long.MIN_VALUE)
                return cached;
            BlockState state = region.getBlockState(p);
            boolean occludes = state.isSolidRender() && !(state.getBlock() instanceof team.creative.littletiles.common.block.mc.BlockTile);
            int light = 0;
            long flags = occludes ? 1L : 0L;
            if (!occludes) {
                light = region instanceof BlockAndLightGetter getter ? LightCoordsUtil.getLightCoords(getter, p) : LightCoordsUtil.FULL_BRIGHT;
                if (light == 0 && state.getBlock() instanceof team.creative.littletiles.common.block.mc.BlockTile)
                    flags = 2L; // dark inside a (light-blocking) tile block: no information, ignore for light
            }
            long value = (flags << 32) | (light & 0xFFFFFFFFL);
            samples.put(key, value);
            return value;
        }

        private static double coord(double x, double y, double z, Axis axis) {
            return switch (axis) {
                case X -> x;
                case Y -> y;
                case Z -> z;
            };
        }

        private static void set(BlockPos.MutableBlockPos p, Axis a, int va, Axis b, int vb, Axis c, int vc) {
            int x = 0, y = 0, z = 0;
            switch (a) { case X -> x = va; case Y -> y = va; case Z -> z = va; }
            switch (b) { case X -> x = vb; case Y -> y = vb; case Z -> z = vb; }
            switch (c) { case X -> x = vc; case Y -> y = vc; case Z -> z = vc; }
            p.set(x, y, z);
        }

        /** Solves u = ua*a + ub*b + uc and v = va*a + vb*b + vc from three non-collinear corners of the quad. */
        private static float[] uvMap(BakedQuad quad, Axis one, Axis two) {
            int[][] triples = { { 0, 1, 2 }, { 0, 1, 3 }, { 0, 2, 3 }, { 1, 2, 3 } };
            for (int[] t : triples) {
                Vector3fc p0 = quad.position(t[0]), p1 = quad.position(t[1]), p2 = quad.position(t[2]);
                float a0 = get(p0, one), b0 = get(p0, two);
                float a1 = get(p1, one) - a0, b1 = get(p1, two) - b0;
                float a2 = get(p2, one) - a0, b2 = get(p2, two) - b0;
                float det = a1 * b2 - a2 * b1;
                if (Math.abs(det) < 1.0E-8F)
                    continue;
                long uv0 = quad.packedUV(t[0]), uv1 = quad.packedUV(t[1]), uv2 = quad.packedUV(t[2]);
                float u0 = UVPair.unpackU(uv0), v0 = UVPair.unpackV(uv0);
                float du1 = UVPair.unpackU(uv1) - u0, dv1 = UVPair.unpackV(uv1) - v0;
                float du2 = UVPair.unpackU(uv2) - u0, dv2 = UVPair.unpackV(uv2) - v0;
                float ua = (du1 * b2 - du2 * b1) / det;
                float ub = (du2 * a1 - du1 * a2) / det;
                float va = (dv1 * b2 - dv2 * b1) / det;
                float vb = (dv2 * a1 - dv1 * a2) / det;
                return new float[] { ua, ub, u0 - ua * a0 - ub * b0, va, vb, v0 - va * a0 - vb * b0 };
            }
            return null;
        }

        /** Sutherland-Hodgman clip of a planar polygon to [minA, maxA] x [minB, maxB] on the face axes (vertices are
         * interpolated in 3D, so sloped faces stay on their plane). */
        private static Vec3f[] clip(Vec3f[] polygon, Axis one, Axis two, float minA, float minB, float maxA, float maxB) {
            List<Vec3f> current = new ArrayList<>(List.of(polygon));
            current = clipEdge(current, one, minA, true);
            current = clipEdge(current, one, maxA, false);
            current = clipEdge(current, two, minB, true);
            current = clipEdge(current, two, maxB, false);
            if (current.size() < 3)
                return null;
            return current.toArray(new Vec3f[0]);
        }

        private static List<Vec3f> clipEdge(List<Vec3f> input, Axis axis, float limit, boolean keepAbove) {
            List<Vec3f> output = new ArrayList<>(input.size() + 2);
            int n = input.size();
            for (int i = 0; i < n; i++) {
                Vec3f current = input.get(i);
                Vec3f previous = input.get((i + n - 1) % n);
                boolean currentIn = inside(current.get(axis), limit, keepAbove);
                boolean previousIn = inside(previous.get(axis), limit, keepAbove);
                if (currentIn) {
                    if (!previousIn)
                        output.add(intersect(previous, current, axis, limit));
                    output.add(current);
                } else if (previousIn)
                    output.add(intersect(previous, current, axis, limit));
            }
            return output;
        }

        private static boolean inside(float value, float limit, boolean keepAbove) {
            return keepAbove ? value >= limit - 1.0E-6F : value <= limit + 1.0E-6F;
        }

        private static Vec3f intersect(Vec3f from, Vec3f to, Axis axis, float limit) {
            float d = to.get(axis) - from.get(axis);
            float t = Math.abs(d) < 1.0E-9F ? 0 : (limit - from.get(axis)) / d;
            return new Vec3f(from.x + (to.x - from.x) * t, from.y + (to.y - from.y) * t, from.z + (to.z - from.z) * t);
        }

        private int tint(BakedQuad quad, BlockState state, BlockPos pos) {
            if (!quad.materialInfo().isTinted())
                return -1;
            BlockTintSource source = mc.getBlockColors().getTintSource(state, quad.materialInfo().tintIndex());
            if (source == null)
                return -1;
            return 0xFF000000 | source.colorInWorld(state, region, pos);
        }

        /** Vanilla-style directional shading (top brightest, bottom darkest). */
        private static int shade(int color, BakedQuad quad, Facing facing) {
            Direction dir = quad.materialInfo().shadeDirectionOverride();
            if (dir == null)
                dir = facing.toVanilla();
            float shade = switch (dir) {
                case DOWN -> 0.5F;
                case UP -> 1.0F;
                case NORTH, SOUTH -> 0.8F;
                case WEST, EAST -> 0.6F;
            };
            int r = (int) (((color >> 16) & 0xFF) * shade);
            int g = (int) (((color >> 8) & 0xFF) * shade);
            int b = (int) ((color & 0xFF) * shade);
            return (color & 0xFF000000) | (r << 16) | (g << 8) | b;
        }

        private static int multiply(int first, int second) {
            if (second == -1)
                return first;
            int a = (((first >>> 24) & 0xFF) * ((second >>> 24) & 0xFF)) / 255;
            int r = (((first >> 16) & 0xFF) * ((second >> 16) & 0xFF)) / 255;
            int g = (((first >> 8) & 0xFF) * ((second >> 8) & 0xFF)) / 255;
            int b = ((first & 0xFF) * (second & 0xFF)) / 255;
            return (a << 24) | (r << 16) | (g << 8) | b;
        }

        private static float get(Vector3fc p, Axis axis) {
            return switch (axis) {
                case X -> p.x();
                case Y -> p.y();
                case Z -> p.z();
            };
        }
    }
}

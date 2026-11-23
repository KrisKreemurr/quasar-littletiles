package team.creative.littletiles.client.render.level;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.joml.Vector3f;
import org.joml.Vector3fc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.MapCodec;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.vec.Vec3f;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.api.common.tool.ILittlePlacer;
import team.creative.littletiles.client.render.tile.LittleRenderBox;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;

/** Quasar MC (26.3): 3D item icons for anything holding tiles (premade structures, multi tiles, picked tile groups).
 * <p>
 * Registered as the special item model {@code littletiles:tiles}; the item JSON supplies a block-shaped base model for the
 * display transforms. The tile group of a stack is turned into a vertex list once (same face/texture cutting as the chunk
 * mesher) and replayed every frame through {@link SubmitNodeCollector#submitCustomGeometry}. */
public final class LittleItemRenderer implements SpecialModelRenderer<LittleItemRenderer.Mesh> {

    /** What a renderer draws: the stack's tile group (fitted to a block), the tool's selected block (small cube in the
     * corner of the tool icon: chisel, glove) or a block ingredient's fill level. */
    public enum Mode {
        TILES,
        ELEMENT,
        INGREDIENT
    }

    public static final LittleItemRenderer INSTANCE = new LittleItemRenderer(Mode.TILES);
    public static final LittleItemRenderer ELEMENT = new LittleItemRenderer(Mode.ELEMENT);
    public static final LittleItemRenderer INGREDIENT = new LittleItemRenderer(Mode.INGREDIENT);

    public record Unbaked() implements SpecialModelRenderer.Unbaked<Mesh> {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

        @Override
        public SpecialModelRenderer<Mesh> bake(SpecialModelRenderer.BakingContext context) {
            clearCache(); // models may have changed
            return INSTANCE;
        }

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked<Mesh>> type() {
            return MAP_CODEC;
        }
    }

    public record UnbakedElement() implements SpecialModelRenderer.Unbaked<Mesh> {
        public static final MapCodec<UnbakedElement> MAP_CODEC = MapCodec.unit(new UnbakedElement());

        @Override
        public SpecialModelRenderer<Mesh> bake(SpecialModelRenderer.BakingContext context) {
            clearCache();
            return ELEMENT;
        }

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked<Mesh>> type() {
            return MAP_CODEC;
        }
    }

    public record UnbakedIngredient() implements SpecialModelRenderer.Unbaked<Mesh> {
        public static final MapCodec<UnbakedIngredient> MAP_CODEC = MapCodec.unit(new UnbakedIngredient());

        @Override
        public SpecialModelRenderer<Mesh> bake(SpecialModelRenderer.BakingContext context) {
            clearCache();
            return INGREDIENT;
        }

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked<Mesh>> type() {
            return MAP_CODEC;
        }
    }

    private static void clearCache() {
        synchronized (CACHE) {
            CACHE.clear();
        }
    }

    private final Mode mode;

    /** Lazily built vertex data for one tile group (x, y, z, u, v, argb, nx, ny, nz per vertex; quads). */
    public static final class Mesh {
        final LittleGroup group;
        /** Boxes given directly (element / ingredient modes), used as they are (no fitting). */
        final List<RenderBox> boxes;
        String name = "?";
        float[] solid;
        float[] translucent;
        boolean built;

        Mesh(LittleGroup group) {
            this.group = group;
            this.boxes = null;
        }

        Mesh(List<RenderBox> boxes) {
            this.group = null;
            this.boxes = boxes;
        }

        boolean empty() {
            return group == null && (boxes == null || boxes.isEmpty());
        }
    }

    private static final int STRIDE = 9;
    private static final int MAX_CACHE = 256;
    private static final Map<Object, Mesh> CACHE = new LinkedHashMap<>(64, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Object, Mesh> eldest) {
            return size() > MAX_CACHE;
        }
    };

    private LittleItemRenderer(Mode mode) {
        this.mode = mode;
    }

    @Override
    public Mesh extractArgument(ItemStack stack) {
        if (mode == Mode.ELEMENT) {
            var element = team.creative.littletiles.common.block.little.element.LittleElement.getOrDefault(stack);
            BlockState state = element.getState();
            Object key = new Key(state, element.color);
            synchronized (CACHE) {
                Mesh mesh = CACHE.get(key);
                if (mesh != null)
                    return mesh;
            }
            RenderBox box = new RenderBox(0, 0, 0, 1, 1, 1, state);
            box.color = element.color;
            Mesh mesh = new Mesh(List.of(box));
            synchronized (CACHE) {
                CACHE.put(key, mesh);
            }
            return mesh;
        }
        if (mode == Mode.INGREDIENT) {
            var ingredient = team.creative.littletiles.common.item.ItemBlockIngredient.loadIngredient(stack);
            if (ingredient == null || ingredient.block == null)
                return null;
            Object key = new Key(ingredient.block, Math.min(1, ingredient.value));
            synchronized (CACHE) {
                Mesh mesh = CACHE.get(key);
                if (mesh != null)
                    return mesh;
            }
            Mesh mesh = new Mesh(ingredientBoxes(ingredient.block.getState(), ingredient.value));
            synchronized (CACHE) {
                CACHE.put(key, mesh);
            }
            return mesh;
        }
        if (!(stack.getItem() instanceof ILittlePlacer placer))
            return null;
        Object data = stack.get(LittleTilesRegistry.DATA.value());
        Object key = data == null ? stack.getItem() : new Key(stack.getItem(), data);
        synchronized (CACHE) {
            Mesh mesh = CACHE.get(key);
            if (mesh != null)
                return mesh;
        }
        Mesh mesh;
        try {
            mesh = placer.hasTiles(stack) ? new Mesh(placer.getTiles(stack)) : new Mesh((LittleGroup) null);
            mesh.name = stack.getHoverName().getString();
        } catch (Throwable e) {
            LittleTiles.LOGGER.error("Failed to read tiles of item {}", stack, e);
            mesh = new Mesh((LittleGroup) null);
        }
        synchronized (CACHE) {
            CACHE.put(key, mesh);
        }
        return mesh;
    }

    private record Key(Object item, Object data) {}

    /** The original LittleTiles block-ingredient icon: the block filled up to the ingredient's volume (planes, then a row,
     * then single pixels), or a small centred cube for less than one plane. */
    private static List<RenderBox> ingredientBoxes(BlockState state, double value) {
        List<RenderBox> cubes = new ArrayList<>();
        double volume = Math.min(1, value);
        var grid = team.creative.littletiles.common.grid.LittleGrid.overallDefault();
        long pixels = (long) (volume * grid.count3d);
        if (pixels < grid.count * grid.count)
            cubes.add(new RenderBox(0.4F, 0.4F, 0.4F, 0.6F, 0.6F, 0.6F, state));
        else {
            long remaining = pixels;
            long planes = pixels / grid.count2d;
            remaining -= planes * grid.count2d;
            long rows = remaining / grid.count;
            remaining -= rows * grid.count;
            float height = (float) (planes * grid.pixelLength);
            if (planes > 0)
                cubes.add(new RenderBox(0, 0, 0, 1, height, 1, state));
            float width = (float) (rows * grid.pixelLength);
            if (rows > 0)
                cubes.add(new RenderBox(0, height, 0, 1, height + (float) grid.pixelLength, width, state));
            if (remaining > 0)
                cubes.add(new RenderBox(0, height, width, 1, height + (float) grid.pixelLength, width + (float) grid.pixelLength, state));
        }
        return cubes;
    }

    @Override
    public void submit(Mesh mesh, PoseStack pose, SubmitNodeCollector collector, int light, int overlay, boolean glint, int outline) {
        if (mesh == null || mesh.empty())
            return;
        if (mode == Mode.ELEMENT) {
            // Small isometric cube in the lower right corner of the flat tool icon, a little in front of it.
            PoseStack local = new PoseStack(); // own stack: the submitted pose must not change after we return
            local.last().pose().set(pose.last().pose());
            local.last().normal().set(pose.last().normal());
            local.translate(0.72F, 0.26F, 0.6F);
            local.rotate(com.mojang.math.Axis.XP.rotationDegrees(30));
            local.rotate(com.mojang.math.Axis.YP.rotationDegrees(225));
            local.scale(0.42F, 0.42F, 0.42F);
            local.translate(-0.5F, -0.5F, -0.5F);
            draw(mesh, local, collector, light, overlay);
            return;
        }
        draw(mesh, pose, collector, light, overlay);
    }

    private void draw(Mesh mesh, PoseStack pose, SubmitNodeCollector collector, int light, int overlay) {
        if (!mesh.built) {
            mesh.built = true;
            try {
                build(mesh);
            } catch (Throwable e) {
                LittleTiles.LOGGER.error("Failed to build item model for tiles", e);
            }
        }
        if (mesh.solid != null && mesh.solid.length > 0) {
            float[] data = mesh.solid;
            collector.submitCustomGeometry(pose, Sheets.cutoutBlockItemSheet(), (p, consumer) -> replay(data, p, consumer, light, overlay));
        }
        if (mesh.translucent != null && mesh.translucent.length > 0) {
            float[] data = mesh.translucent;
            collector.submitCustomGeometry(pose, Sheets.translucentBlockItemSheet(), (p, consumer) -> replay(data, p, consumer, light, overlay));
        }
    }

    private static void replay(float[] data, PoseStack.Pose pose, VertexConsumer consumer, int light, int overlay) {
        for (int i = 0; i + STRIDE <= data.length; i += STRIDE)
            consumer.addVertex(pose, data[i], data[i + 1], data[i + 2]).setColor(Float.floatToRawIntBits(data[i + 5])).setUv(data[i + 3], data[i + 4]).setOverlay(overlay)
                    .setLight(light).setNormal(pose, data[i + 6], data[i + 7], data[i + 8]);
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        output.accept(new Vector3f(0, 0, 0));
        output.accept(new Vector3f(1, 1, 1));
    }

    // ================ building ================

    private static void build(Mesh mesh) {
        if (mesh.boxes != null) {
            List<RenderBox> solid = new ArrayList<>(), translucent = new ArrayList<>();
            for (RenderBox box : mesh.boxes)
                (box.isTranslucent() ? translucent : solid).add(box);
            Builder builder = new Builder(new float[] { 1, 0, 0, 0 });
            mesh.solid = builder.boxes(solid);
            mesh.translucent = builder.boxes(translucent);
            return;
        }
        List<RenderBox> solid = mesh.group.getRenderingBoxes(false);
        List<RenderBox> translucent = mesh.group.getRenderingBoxes(true);

        // Fit into the unit cube: centre the group, shrink anything bigger than a block.
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, minZ = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE, maxZ = -Float.MAX_VALUE;
        for (List<RenderBox> list : List.of(solid, translucent))
            for (RenderBox box : list) {
                minX = Math.min(minX, box.minX);
                minY = Math.min(minY, box.minY);
                minZ = Math.min(minZ, box.minZ);
                maxX = Math.max(maxX, box.maxX);
                maxY = Math.max(maxY, box.maxY);
                maxZ = Math.max(maxZ, box.maxZ);
            }
        if (minX > maxX)
            return;
        float size = Math.max(maxX - minX, Math.max(maxY - minY, maxZ - minZ));
        float scale = size > 1 ? 1 / size : 1;
        float[] transform = { scale, 0.5F - (minX + maxX) / 2 * scale, 0.5F - (minY + maxY) / 2 * scale, 0.5F - (minZ + maxZ) / 2 * scale };

        Builder builder = new Builder(transform);
        mesh.solid = builder.boxes(solid);
        mesh.translucent = builder.boxes(translucent);
    }

    private static final class Builder {

        private final Minecraft mc = Minecraft.getInstance();
        private final float[] transform;
        private final List<BlockStateModelPart> parts = new ArrayList<>();
        private final List<Vec3f[]> polygons = new ArrayList<>();
        private final RandomSource random = RandomSource.create();
        private float[] out = new float[STRIDE * 256];
        private int size;

        Builder(float[] transform) {
            this.transform = transform;
        }

        float[] boxes(List<RenderBox> boxes) {
            size = 0;
            for (RenderBox box : boxes)
                box(box);
            float[] result = new float[size];
            System.arraycopy(out, 0, result, 0, size);
            return result;
        }

        private void box(RenderBox box) {
            BlockState state = box.state;
            if (state == null)
                return;
            BlockStateModel model = mc.getModelManager().getBlockStateModelSet().get(state);
            parts.clear();
            random.setSeed(42L);
            model.collectParts(random, parts);
            if (parts.isEmpty())
                return;
            int boxColor = box.color == -1 ? ColorUtils.WHITE : box.color;
            for (Facing facing : Facing.VALUES) {
                polygons.clear();
                if (box instanceof LittleRenderBox little)
                    little.quasar$collectFacePolygons(facing, polygons);
                else
                    polygons.add(rectangle(box, facing));
                if (polygons.isEmpty())
                    continue;
                Direction dir = facing.toVanilla();
                for (BlockStateModelPart part : parts) {
                    quads(part.getQuads(dir), dir, facing, state, boxColor);
                    quads(part.getQuads(null), dir, facing, state, boxColor);
                }
            }
        }

        private void quads(List<BakedQuad> quads, Direction dir, Facing facing, BlockState state, int boxColor) {
            for (BakedQuad quad : quads) {
                if (quad.direction() != dir)
                    continue;
                for (Vec3f[] polygon : polygons)
                    cells(quad, polygon, facing, state, boxColor);
            }
        }

        /** Groups can span several blocks; textures repeat per block, so faces are cut at block borders first and each piece
         * is mapped as if it were inside block (0, 0). */
        private void cells(BakedQuad quad, Vec3f[] polygon, Facing facing, BlockState state, int boxColor) {
            Axis one = facing.one();
            Axis two = facing.two();
            float minA = Float.MAX_VALUE, minB = Float.MAX_VALUE, maxA = -Float.MAX_VALUE, maxB = -Float.MAX_VALUE;
            for (Vec3f v : polygon) {
                minA = Math.min(minA, v.get(one));
                maxA = Math.max(maxA, v.get(one));
                minB = Math.min(minB, v.get(two));
                maxB = Math.max(maxB, v.get(two));
            }
            int a0 = (int) Math.floor(minA + 1.0E-4F), a1 = (int) Math.ceil(maxA - 1.0E-4F);
            int b0 = (int) Math.floor(minB + 1.0E-4F), b1 = (int) Math.ceil(maxB - 1.0E-4F);
            if (a1 - a0 <= 1 && b1 - b0 <= 1) {
                shiftA = a0;
                shiftB = b0;
                emit(quad, shift(polygon, one, -a0, two, -b0), facing, state, boxColor);
                return;
            }
            for (int ca = a0; ca < a1; ca++)
                for (int cb = b0; cb < b1; cb++) {
                    Vec3f[] piece = LittleChunkGeometry.Mesher.clip(polygon, one, two, ca, cb, ca + 1, cb + 1);
                    if (piece == null)
                        continue;
                    shiftA = ca;
                    shiftB = cb;
                    emit(quad, shift(piece, one, -ca, two, -cb), facing, state, boxColor);
                }
        }

        private int shiftA, shiftB;
        private Axis shiftOne, shiftTwo;

        private Vec3f[] shift(Vec3f[] polygon, Axis one, float da, Axis two, float db) {
            shiftOne = one;
            shiftTwo = two;
            if (da == 0 && db == 0)
                return polygon;
            Vec3f[] result = new Vec3f[polygon.length];
            for (int i = 0; i < polygon.length; i++) {
                Vec3f v = new Vec3f(polygon[i]);
                add(v, one, da);
                add(v, two, db);
                result[i] = v;
            }
            return result;
        }

        private static void add(Vec3f v, Axis axis, float value) {
            switch (axis) {
                case X -> v.x += value;
                case Y -> v.y += value;
                case Z -> v.z += value;
            }
        }

        private void emit(BakedQuad quad, Vec3f[] polygon, Facing facing, BlockState state, int boxColor) {
            Axis one = facing.one();
            Axis two = facing.two();
            float minA = Float.MAX_VALUE, minB = Float.MAX_VALUE, maxA = -Float.MAX_VALUE, maxB = -Float.MAX_VALUE;
            for (int k = 0; k < 4; k++) {
                Vector3fc p = quad.position(k);
                float a = LittleChunkGeometry.Mesher.get(p, one), b = LittleChunkGeometry.Mesher.get(p, two);
                minA = Math.min(minA, a);
                maxA = Math.max(maxA, a);
                minB = Math.min(minB, b);
                maxB = Math.max(maxB, b);
            }
            if (maxA - minA < 1.0E-6F || maxB - minB < 1.0E-6F)
                return;
            float[] uvMap = LittleChunkGeometry.Mesher.uvMap(quad, one, two);
            if (uvMap == null)
                return;
            // Texture position-locked per block cell like in the world: tile faces use the part of the texture they cover.
            Vec3f[] shape = polygon;
            if (minA > 0.0001F || minB > 0.0001F || maxA < 0.9999F || maxB < 0.9999F) {
                shape = LittleChunkGeometry.Mesher.clip(polygon, one, two, minA, minB, maxA, maxB);
                if (shape == null)
                    return;
            }
            int color = LittleChunkGeometry.Mesher.multiply(boxColor, tint(quad, state));
            Direction n = facing.toVanilla();
            int index = 1;
            while (index < shape.length - 2) {
                vertex(shape[0], one, two, uvMap, color, n);
                vertex(shape[index], one, two, uvMap, color, n);
                vertex(shape[index + 1], one, two, uvMap, color, n);
                vertex(shape[index + 2], one, two, uvMap, color, n);
                index += 2;
            }
            if (index < shape.length - 1) {
                vertex(shape[0], one, two, uvMap, color, n);
                vertex(shape[index], one, two, uvMap, color, n);
                vertex(shape[index + 1], one, two, uvMap, color, n);
                vertex(shape[index + 1], one, two, uvMap, color, n);
            }
        }

        private void vertex(Vec3f vec, Axis one, Axis two, float[] uvMap, int color, Direction n) {
            // Texture coordinates repeat per block: fold the face coordinates back into [0, 1] of their block cell.
            float a = vec.get(one), b = vec.get(two);
            float u = uvMap[0] * a + uvMap[1] * b + uvMap[2];
            float v = uvMap[3] * a + uvMap[4] * b + uvMap[5];
            if (size + STRIDE > out.length) {
                float[] grown = new float[out.length * 2];
                System.arraycopy(out, 0, grown, 0, size);
                out = grown;
            }
            float x = vec.x, y = vec.y, z = vec.z;
            if (shiftA != 0 || shiftB != 0) { // back to the piece's real block cell
                Vec3f real = new Vec3f(x, y, z);
                add(real, shiftOne, shiftA);
                add(real, shiftTwo, shiftB);
                x = real.x;
                y = real.y;
                z = real.z;
            }
            float s = transform[0];
            out[size++] = x * s + transform[1];
            out[size++] = y * s + transform[2];
            out[size++] = z * s + transform[3];
            out[size++] = u;
            out[size++] = v;
            out[size++] = Float.intBitsToFloat(color);
            out[size++] = n.getStepX();
            out[size++] = n.getStepY();
            out[size++] = n.getStepZ();
        }

        private int tint(BakedQuad quad, BlockState state) {
            if (!quad.materialInfo().isTinted())
                return -1;
            try {
                BlockTintSource source = mc.getBlockColors().getTintSource(state, quad.materialInfo().tintIndex());
                if (source == null || mc.level == null || mc.player == null || !(mc.level instanceof BlockAndTintGetter getter))
                    return -1;
                return 0xFF000000 | source.colorInWorld(state, getter, mc.player.blockPosition());
            } catch (Throwable e) {
                return -1;
            }
        }

        private static Vec3f[] rectangle(RenderBox box, Facing facing) {
            float x0 = box.minX, y0 = box.minY, z0 = box.minZ, x1 = box.maxX, y1 = box.maxY, z1 = box.maxZ;
            return switch (facing) {
                case DOWN -> new Vec3f[] { new Vec3f(x0, y0, z1), new Vec3f(x0, y0, z0), new Vec3f(x1, y0, z0), new Vec3f(x1, y0, z1) };
                case UP -> new Vec3f[] { new Vec3f(x0, y1, z0), new Vec3f(x0, y1, z1), new Vec3f(x1, y1, z1), new Vec3f(x1, y1, z0) };
                case NORTH -> new Vec3f[] { new Vec3f(x1, y1, z0), new Vec3f(x1, y0, z0), new Vec3f(x0, y0, z0), new Vec3f(x0, y1, z0) };
                case SOUTH -> new Vec3f[] { new Vec3f(x0, y1, z1), new Vec3f(x0, y0, z1), new Vec3f(x1, y0, z1), new Vec3f(x1, y1, z1) };
                case WEST -> new Vec3f[] { new Vec3f(x0, y1, z0), new Vec3f(x0, y0, z0), new Vec3f(x0, y0, z1), new Vec3f(x0, y1, z1) };
                case EAST -> new Vec3f[] { new Vec3f(x1, y1, z1), new Vec3f(x1, y0, z1), new Vec3f(x1, y0, z0), new Vec3f(x1, y1, z0) };
            };
        }
    }
}

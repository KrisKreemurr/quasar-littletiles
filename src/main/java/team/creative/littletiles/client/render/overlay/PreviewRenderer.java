package team.creative.littletiles.client.render.overlay;

import net.minecraft.gizmos.Gizmos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.GizmoProperties;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import org.joml.Vector4f;
import org.joml.Matrix4fStack;
import org.joml.Matrix4f;
import java.nio.ByteOrder;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.ints.Int2BooleanFunction;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.common.util.math.box.ABB;
import team.creative.creativecore.common.util.mc.PlayerUtils;
import team.creative.creativecore.common.util.mc.TickUtils;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.action.LittleActionHandlerClient;
import team.creative.littletiles.client.render.mc.MeshDataExtender;
import team.creative.littletiles.client.render.tile.LittleRenderBox;
import team.creative.littletiles.client.tool.shaper.ShapePosition;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
import team.creative.littletiles.common.level.context.ILittleLevelContext;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.LittleBoxAbsolute;
import team.creative.littletiles.common.math.box.LittleBoxLevel;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

public class PreviewRenderer {

    public static void renderShape(PoseStack pose, VertexConsumer consumer, VoxelShape shape, double x, double y, double z, float red, float green, float blue, float alpha) {}

    public static final PoseStack EMPTY = new PoseStack();
    public PreviewManager manager;
    public PreviewRenderer(PreviewManager manager) {
        this.manager = manager;
    }

    public Player player() {
        return manager.player();
    }

    public Level level() {
        return manager.level();
    }

    public BlockHitResult blockHit() {
        return manager.blockHit();
    }

    public Level blockHitLevel() {
        return manager.blockHitLevel();
    }

    public ILittleLevelContext blockHitContext() {
        return manager.blockHitContext();
    }

    public boolean isUsingSecondMode() {
        return team.creative.littletiles.client.action.LittleActionHandlerClient.isUsingSecondMode();
    }

    public LittleTileContext selectFocused(BlockHitResult result) {
        return LittleTileContext.selectFocused(blockHitLevel(), result.getBlockPos(), player());
    }

    public float partialTickTime() {
        return team.creative.creativecore.common.util.mc.TickUtils.getFrameTime(level());
    }

    public boolean isVisible(AABB bb) {
        return true; // TODO(quasar): frustum check once previews render again
    }


    private Vec3[] quasar$ray() {
        var player = player();
        float partialTickTime = partialTickTime();
        Vec3 pos = player.getEyePosition(partialTickTime);
        double reach = team.creative.creativecore.common.util.mc.PlayerUtils.getReach(player);
        Vec3 view = player.getViewVector(partialTickTime);
        return new Vec3[] { pos, pos.add(view.x * reach, view.y * reach, view.z * reach) };
    }
    
    public int select(ILittleLevelContext context, List<ShapePosition> positions) {
        int index = -1;
        double distance = Double.MAX_VALUE;
        Vec3[] ray = quasar$ray();
        Vec3 pos = context.toFakeWorld(ray[0]);
        Vec3 look = context.toFakeWorld(ray[1]);
        for (int i = 0; i < positions.size(); i++) {
            java.util.Optional<Vec3> result = positions.get(i).getBox().clip(pos, look);
            if (result.isPresent()) {
                double tempDistance = pos.distanceToSqr(result.get());
                if (tempDistance < distance) {
                    index = i;
                    distance = tempDistance;
                }
            }
        }
        return index;
    }
    
    public int selectBox(List<LittleBoxLevel> positions) {
        int index = -1;
        double distance = Double.MAX_VALUE;
        Vec3[] ray = quasar$ray();
        for (int i = 0; i < positions.size(); i++) {
            var context = positions.get(i).context;
            var contextPos = context.toFakeWorld(ray[0]);
            var contextLook = context.toFakeWorld(ray[1]);
            java.util.Optional<Vec3> result = positions.get(i).toAABB().clip(contextPos, contextLook);
            if (result.isPresent()) {
                double tempDistance = contextPos.distanceToSqr(result.get());
                if (tempDistance < distance) {
                    index = i;
                    distance = tempDistance;
                }
            }
        }
        return index;
    }
    
    public int selectBox(ILittleLevelContext context, List<LittleBoxAbsolute> positions) {
        int index = -1;
        double distance = Double.MAX_VALUE;
        Vec3[] ray = quasar$ray();
        Vec3 pos = context.toFakeWorld(ray[0]);
        Vec3 look = context.toFakeWorld(ray[1]);
        for (int i = 0; i < positions.size(); i++) {
            java.util.Optional<Vec3> result = positions.get(i).toAABB().clip(pos, look);
            if (result.isPresent()) {
                double tempDistance = pos.distanceToSqr(result.get());
                if (tempDistance < distance) {
                    index = i;
                    distance = tempDistance;
                }
            }
        }
        return index;
    }



    protected void startPreviewRender(boolean lines) {}

    protected void endPreviewRender() {}

    // ================ Quasar MC (26.3): previews drawn as world gizmos ================
    // 26.x has no immediate-mode drawing (RenderSystem / BufferUploader / Tesselator). Tools still build their preview
    // meshes exactly as before; drawing them reads the vertices back and emits vanilla gizmos (lines and filled quads) in
    // world space. PreviewManager calls the tools from RenderFrameEvent.Pre, which runs inside vanilla's per-frame gizmo
    // collector, so the previews are drawn in that same frame.
    
    /** Camera position the tools rendered against this frame (vertices are camera-relative, gizmos are world-space). */
    public Vec3 frameCam = Vec3.ZERO;
    /** Extra model transform set by wrapPreviewRender(context, …) (sub-level / animation contexts may rotate). */
    private final Matrix4f transform = new Matrix4f();
    private final Vector4f scratch = new Vector4f();
    private final ByteBufferBuilder tesselatorLines = new ByteBufferBuilder(16384);
    private final ByteBufferBuilder tesselatorQuads = new ByteBufferBuilder(16384);
    
    public void wrapPreviewRender(ILittleLevelContext context, BlockPos pos, Vec3 cam, boolean lines, Runnable run) {
        Matrix4fStack matrix = new Matrix4fStack(4);
        context.transformMatrix(matrix, pos.getX(), pos.getY(), pos.getZ(), cam, partialTickTime());
        transform.set(matrix);
        try {
            run.run();
        } finally {
            transform.identity();
        }
    }
    
    public void wrapPreviewRender(boolean lines, Runnable run) {
        run.run();
    }
    
    public ByteBufferBuilder createBuffer() {
        return new ByteBufferBuilder(86432);
    }
    
    public BufferBuilder createBuilder(ByteBufferBuilder buffer, boolean lines) {
        if (lines)
            return new BufferBuilder(buffer, PrimitiveTopology.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
        return new BufferBuilder(buffer, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_COLOR);
    }
    
    /** Short-lived builder for meshes drawn straight away (like vanilla's old Tesselator: one shared buffer, cleared per use). */
    public BufferBuilder createTesselatorBuilder(boolean lines) {
        ByteBufferBuilder buffer = lines ? tesselatorLines : tesselatorQuads;
        buffer.clear();
        return createBuilder(buffer, lines);
    }
    
    public void buildBox(PoseStack pose, RenderBox box, BufferBuilder builder, int colorAlpha, boolean lines) {
        buildBox(pose, box, builder, colorAlpha, lines, false);
    }
    
    public void buildBox(PoseStack pose, RenderBox box, BufferBuilder builder, int colorAlpha, boolean lines, boolean forceWhite) {
        if (lines) {
            box.renderLines(pose, builder, colorAlpha, box.getCenter(), 0.001);
            return;
        }
        int color = forceWhite || box.color == -1 ? 0xFFFFFF : box.color & 0xFFFFFF;
        int r = (color >> 16) & 0xFF, g = (color >> 8) & 0xFF, b = color & 0xFF;
        var last = pose.last();
        float x0 = box.minX, y0 = box.minY, z0 = box.minZ, x1 = box.maxX, y1 = box.maxY, z1 = box.maxZ;
        float[][] faces = {
            { x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1 }, // down
            { x0, y1, z1, x1, y1, z1, x1, y1, z0, x0, y1, z0 }, // up
            { x1, y1, z0, x1, y0, z0, x0, y0, z0, x0, y1, z0 }, // north
            { x0, y1, z1, x0, y0, z1, x1, y0, z1, x1, y1, z1 }, // south
            { x0, y1, z0, x0, y0, z0, x0, y0, z1, x0, y1, z1 }, // west
            { x1, y1, z1, x1, y0, z1, x1, y0, z0, x1, y1, z0 } }; // east
        for (float[] f : faces)
            for (int i = 0; i < 12; i += 3)
                builder.addVertex(last, f[i], f[i + 1], f[i + 2]).setColor(r, g, b, colorAlpha);
    }
    
    public BoxRenderResult buildBoxes(PoseStack pose, LittleBoxes boxes, boolean lines) {
        return buildBoxes(pose, boxes, lines, false);
    }
    
    public BoxRenderResult buildBoxes(PoseStack pose, LittleBoxes boxes, boolean lines, boolean forceWhite) {
        ByteBufferBuilder buffer = createBuffer();
        var builder = createBuilder(buffer, lines);
        for (LittleBox box : boxes.all()) {
            LittleRenderBox cube = box.getRenderingBox(boxes.getGrid());
            if (cube != null)
                buildBox(pose, cube, builder, 255, lines, forceWhite);
        }
        var mesh = builder.build();
        if (mesh instanceof MeshDataExtender m)
            m.keepAlive(true);
        return new BoxRenderResult(boxes, boxes.pos, buffer, mesh);
    }
    
    /** Draws a preview mesh (lines or filled boxes) with the configured preview colours. */
    public void draw(MeshData data, boolean lines) {
        var config = LittleTiles.CONFIG.rendering.preview;
        if (lines) {
            drawLines(data, config.lineColor.getAlphaDecimal(), false);
            return;
        }
        if (data == null)
            return;
        float pulse = (float) (Math.sin(System.nanoTime() / 200000000D) * config.boxAlphaFade);
        float alpha = Mth.clamp(config.boxColor.getAlphaDecimal() + pulse, 0, 1);
        emit(data, config.boxColor.toInt(), alpha, false, 0);
    }
    
    /** {@link #draw(MeshData, boolean)} with an extra translation applied first (the old pushMatrix/translate inside a wrap). */
    public void drawTranslated(MeshData data, boolean lines, double x, double y, double z) {
        Matrix4f saved = new Matrix4f(transform);
        transform.translate((float) x, (float) y, (float) z);
        try {
            draw(data, lines);
        } finally {
            transform.set(saved);
        }
    }
    
    public void drawLines(MeshData data, boolean highlight) {
        drawLines(data, LittleTiles.CONFIG.rendering.preview.lineColor.getAlphaDecimal(), highlight);
    }
    
    public void drawLines(MeshData data, float alpha, boolean highlight) {
        if (data == null)
            return;
        var config = LittleTiles.CONFIG.rendering.preview;
        emit(data, config.lineColor.toInt(), alpha, false, (float) config.lineThickness);
        if (highlight)
            emit(data, config.highlightLineColor.toInt(), alpha, true, (float) config.hightlightLineThickness);
    }
    
    /** Reads a mesh back (position + colour of every vertex) and turns it into gizmos: LINES -> lines, QUADS -> filled rects. */
    /** Diagnostics: gizmos emitted / meshes drawn since the counters were last reset (PreviewManager logs them once). */
    public int debugGizmos, debugMeshes, debugEmptyMeshes;
    private boolean loggedSample;
    
    private void emit(MeshData data, int baseColor, float alpha, boolean onTop, float lineWidth) {
        var state = data.drawState();
        int count = state.vertexCount();
        debugMeshes++;
        if (count <= 0)
            debugEmptyMeshes++;
        if (count <= 0)
            return;
        int stride = state.format().getVertexSize();
        ByteBuffer buffer = data.vertexBuffer().duplicate().order(ByteOrder.nativeOrder());
        boolean lines = state.primitiveTopology() == PrimitiveTopology.LINES;
        int per = lines ? 2 : 4;
        Vec3[] points = new Vec3[per];
        for (int i = 0; i + per <= count; i += per) {
            int color = 0;
            for (int k = 0; k < per; k++) {
                int offset = (i + k) * stride;
                points[k] = toWorld(buffer.getFloat(offset), buffer.getFloat(offset + 4), buffer.getFloat(offset + 8));
                if (k == 0)
                    color = buffer.getInt(offset + 12); // colour follows the position in both preview formats (RGBA bytes)
            }
            int argb = combine(baseColor, color, alpha);
            GizmoProperties gizmo;
            if (lines)
                gizmo = Gizmos.line(points[0], points[1], argb, lineWidth);
            else
                gizmo = Gizmos.rect(points[0], points[1], points[2], points[3], GizmoStyle.fill(argb));
            if (onTop)
                gizmo.setAlwaysOnTop();
            if (debugGizmos == 0 && !loggedSample) {
                loggedSample = true;
                LittleTiles.LOGGER.info("LittleTiles preview sample: first {} from {} to {} argb={} camera={} stride={} vertices={}", lines ? "line" : "rect", points[0], points[1],
                    Integer.toHexString(argb), frameCam, stride, count);
            }
            debugGizmos++;
        }
    }
    
    /** Shader colour (config) x vertex colour, like the old setShaderColor * vertex colour. */
    private static int combine(int baseArgb, int vertexRgbaLittleEndian, float alpha) {
        int vr = vertexRgbaLittleEndian & 0xFF;
        int vg = (vertexRgbaLittleEndian >> 8) & 0xFF;
        int vb = (vertexRgbaLittleEndian >> 16) & 0xFF;
        int va = (vertexRgbaLittleEndian >>> 24) & 0xFF;
        int r = ((baseArgb >> 16) & 0xFF) * vr / 255;
        int g = ((baseArgb >> 8) & 0xFF) * vg / 255;
        int b = (baseArgb & 0xFF) * vb / 255;
        int a = Mth.clamp((int) (alpha * 255), 0, 255); // config alpha only: a misread vertex alpha must never hide the preview
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
    
    private Vec3 toWorld(float x, float y, float z) {
        scratch.set(x, y, z, 1);
        transform.transform(scratch);
        return new Vec3(scratch.x + frameCam.x, scratch.y + frameCam.y, scratch.z + frameCam.z);
    }
    
    private Vec3 toWorld(PoseStack pose, double x, double y, double z) {
        scratch.set((float) x, (float) y, (float) z, 1);
        pose.last().pose().transform(scratch);
        transform.transform(scratch);
        return new Vec3(scratch.x + frameCam.x, scratch.y + frameCam.y, scratch.z + frameCam.z);
    }
    
    public void drawABB(PoseStack pose, ABB box, boolean highlight) {
        box.inflate(0.002);
        double[] xs = { box.minX, box.maxX }, ys = { box.minY, box.maxY }, zs = { box.minZ, box.maxZ };
        for (int a = 0; a < 2; a++)
            for (int b = 0; b < 2; b++) {
                drawLine(pose, new Vec3(xs[0], ys[a], zs[b]), new Vec3(xs[1], ys[a], zs[b]), highlight);
                drawLine(pose, new Vec3(xs[a], ys[0], zs[b]), new Vec3(xs[a], ys[1], zs[b]), highlight);
                drawLine(pose, new Vec3(xs[a], ys[b], zs[0]), new Vec3(xs[a], ys[b], zs[1]), highlight);
            }
    }
    
    public void drawLine(PoseStack pose, Vec3 start, Vec3 end, boolean selected) {
        var config = LittleTiles.CONFIG.rendering.preview;
        Vec3 a = toWorld(pose, start.x, start.y, start.z);
        Vec3 b = toWorld(pose, end.x, end.y, end.z);
        Gizmos.line(a, b, config.lineColor.toInt(), (float) config.lineThickness);
        if (selected)
            Gizmos.line(a, b, config.highlightLineColor.toInt(), (float) config.hightlightLineThickness).setAlwaysOnTop();
    }
    
    public void renderLineBox(PoseStack pose, Vec3 cam, LittleBoxLevel box) {
        pose.pushPose();
        var pos = box.pos;
        box.context.transformPose(pose, pos.getX(), pos.getY(), pos.getZ(), cam, partialTickTime());
        pose.translate(-pos.getX(), -pos.getY(), -pos.getZ());
        var buffer = createTesselatorBuilder(true);
        buildBox(pose, box.getRenderingBoxWithoutOffset(), buffer, 255, true);
        drawLines(buffer.build(), false);
        pose.popPose();
    }
    
    public void renderLineBox(ILittleLevelContext context, PoseStack pose, Vec3 cam, ABB bb, BlockPos pos, boolean highlight) {
        pose.pushPose();
        context.transformPose(pose, pos.getX(), pos.getY(), pos.getZ(), cam, partialTickTime());
        bb.move(-pos.getX(), -pos.getY(), -pos.getZ());
        drawABB(pose, bb, highlight);
        pose.popPose();
    }
    
    public void renderLineBoxes(PoseStack pose, Vec3 cam, List<LittleBoxLevel> boxes, @Nullable Int2BooleanFunction marked) {
        for (int i = 0; i < boxes.size(); i++) {
            pose.pushPose();
            var box = boxes.get(i);
            var pos = box.pos;
            box.context.transformPose(pose, pos.getX(), pos.getY(), pos.getZ(), cam, partialTickTime());
            var buffer = createTesselatorBuilder(true);
            buildBox(pose, box.getRenderingBoxWithoutOffset(), buffer, 255, true);
            drawLines(buffer.build(), marked != null && marked.get(i));
            pose.popPose();
        }
    }
    
    public void renderLineBoxes(PoseStack pose, ILittleLevelContext context, BlockPos pos, Vec3 cam, List<LittleBoxAbsolute> boxes, @Nullable Int2BooleanFunction marked) {
        wrapPreviewRender(context, pos, cam, true, () -> {
            pose.pushPose();
            pose.translate(-pos.getX(), -pos.getY(), -pos.getZ());
            for (int i = 0; i < boxes.size(); i++) {
                var bb = boxes.get(i).toABB();
                bb.move(-pos.getX(), -pos.getY(), -pos.getZ());
                drawABB(pose, bb, marked != null && marked.get(i));
            }
            pose.popPose();
        });
    }
    
    public void renderLinesHighlight(ILittleLevelContext context, Vec3 cam, BlockPos pos, MeshData data, int color) {
        wrapPreviewRender(context, pos, cam, true, () -> drawLines(data, true));
    }
    
    public void renderLinesHighlight(ILittleLevelContext context, Vec3 cam, BlockPos pos, MeshData data, int color, float alpha) {
        wrapPreviewRender(context, pos, cam, true, () -> drawLines(data, alpha, true));
    }
    
    public void renderPositions(PoseStack pose, ILittleLevelContext context, BlockPos pos, Vec3 cam, List<ShapePosition> positions, @Nullable Int2BooleanFunction marked) {
        wrapPreviewRender(context, pos, cam, true, () -> {
            for (int i = 0; i < positions.size(); i++) {
                var bb = positions.get(i).getBB();
                bb.move(-pos.getX(), -pos.getY(), -pos.getZ());
                drawABB(pose, bb, marked != null && marked.get(i));
            }
        });
    }
    
    public static record BoxRenderResult(LittleBoxes boxes, BlockPos pos, ByteBufferBuilder buffer, MeshData data) {
        
        public void close() {
            if (data instanceof MeshDataExtender m) {
                m.keepAlive(false);
                data.close();
            }
            if (buffer != null)
                buffer.close();
        }
    }
    
}


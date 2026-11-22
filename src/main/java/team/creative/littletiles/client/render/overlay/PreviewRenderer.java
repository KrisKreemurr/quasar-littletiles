package team.creative.littletiles.client.render.overlay;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import org.lwjgl.opengl.GL14;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.ints.Int2BooleanFunction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
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

    public static PoseStack EMPTY;
    public PreviewManager manager;
    public PreviewRenderer(PreviewManager manager) {}

    public Player player() { return null; }

    public Level level() { return null; }

    public BlockHitResult blockHit() { return null; }

    public Level blockHitLevel() { return null; }

    public ILittleLevelContext blockHitContext() { return null; }

    public boolean isUsingSecondMode() { return false; }

    public LittleTileContext selectFocused(BlockHitResult result) { return null; }

    public float partialTickTime() { return 0F; }

    public boolean isVisible(AABB bb) { return false; }

    public int select(ILittleLevelContext context, List<ShapePosition> positions) { return 0; }

    public int selectBox(List<LittleBoxLevel> positions) { return 0; }

    public int selectBox(ILittleLevelContext context, List<LittleBoxAbsolute> positions) { return 0; }

    protected void startPreviewRender(boolean lines) {}

    protected void endPreviewRender() {}

    public void wrapPreviewRender(ILittleLevelContext context, BlockPos pos, Vec3 cam, boolean lines, Runnable run) {}

    public void wrapPreviewRender(boolean lines, Runnable run) {}

    public ByteBufferBuilder createBuffer() { return null; }

    public BufferBuilder createBuilder(ByteBufferBuilder buffer, boolean lines) { return null; }

    public BufferBuilder createTesselatorBuilder(boolean lines) { return null; }

    public void buildBox(PoseStack pose, RenderBox box, BufferBuilder builder, int colorAlpha, boolean lines) {}

    public void buildBox(PoseStack pose, RenderBox box, BufferBuilder builder, int colorAlpha, boolean lines, boolean forceWhite) {}

    public BoxRenderResult buildBoxes(PoseStack pose, LittleBoxes boxes, boolean lines) { return null; }

    public BoxRenderResult buildBoxes(PoseStack pose, LittleBoxes boxes, boolean lines, boolean forceWhite) { return null; }

    public void drawLines(MeshData data, boolean highlight) {}

    public void drawLines(MeshData data, float alpha, boolean highlight) {}

    public void drawABB(PoseStack pose, ABB box, boolean highlight) {}

    public void drawLine(PoseStack pose, Vec3 start, Vec3 end, boolean selected) {}

    public void renderLineBox(PoseStack pose, Vec3 cam, LittleBoxLevel box) {}

    public void renderLineBox(ILittleLevelContext context, PoseStack pose, Vec3 cam, ABB bb, BlockPos pos, boolean highlight) {}

    public void renderLineBoxes(PoseStack pose, Vec3 cam, List<LittleBoxLevel> boxes, @Nullable Int2BooleanFunction marked) {}

    public void renderLineBoxes(PoseStack pose, ILittleLevelContext context, BlockPos pos, Vec3 cam, List<LittleBoxAbsolute> boxes, @Nullable Int2BooleanFunction marked) {}

    public void renderLinesHighlight(ILittleLevelContext context, Vec3 cam, BlockPos pos, MeshData data, int color) {}

    public void renderLinesHighlight(ILittleLevelContext context, Vec3 cam, BlockPos pos, MeshData data, int color, float alpha) {}

    public void renderPositions(PoseStack pose, ILittleLevelContext context, BlockPos pos, Vec3 cam, List<ShapePosition> positions, @Nullable Int2BooleanFunction marked) {}

    public static record BoxRenderResult(LittleBoxes boxes, BlockPos pos, ByteBufferBuilder buffer, MeshData data) {

        public void close() {}

    }

    protected PreviewRenderer() {}

}


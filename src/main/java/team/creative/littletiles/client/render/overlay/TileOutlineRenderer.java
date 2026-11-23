package team.creative.littletiles.client.render.overlay;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;
import net.neoforged.neoforge.common.NeoForge;
import team.creative.littletiles.common.block.mc.BlockTile;

/** Quasar MC (26.3): the selection outline around the tile you look at (or the whole block / structure box in second
 * mode), instead of vanilla's outline of the whole tile block. The shape is picked on the main thread while the outline
 * state is extracted; drawing replaces vanilla's outline for that block. */
public final class TileOutlineRenderer {

    private TileOutlineRenderer() {}

    public static void init() {
        NeoForge.EVENT_BUS.addListener(TileOutlineRenderer::extract);
    }

    private static void extract(ExtractBlockOutlineRenderStateEvent event) {
        if (!(event.getBlockState().getBlock() instanceof BlockTile block))
            return;
        VoxelShape shape;
        try {
            shape = block.getSelectionShape(event.getLevel(), event.getBlockPos());
        } catch (RuntimeException e) {
            return;
        }
        if (shape == null || shape.isEmpty())
            return;
        BlockPos pos = event.getBlockPos();
        boolean highContrast = event.isHighContrast();
        event.addCustomRenderer((state, collector, pose, level) -> {
            var cam = level.cameraRenderState.pos;
            PoseStack at = new PoseStack();
            at.last().pose().set(pose.last().pose());
            at.last().normal().set(pose.last().normal());
            at.translate(pos.getX() - cam.x, pos.getY() - cam.y, pos.getZ() - cam.z);
            collector.submitShapeOutline(at, shape, highContrast ? RenderTypes.secondaryBlockOutline() : RenderTypes.lines(), highContrast ? 0xFF000000 : 0x66000000, 2.0F,
                false);
            return true; // replaces vanilla's outline of the whole block
        });
    }
}

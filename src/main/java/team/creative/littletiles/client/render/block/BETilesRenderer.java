package team.creative.littletiles.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.attribute.LittleStructureAttribute;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

public class BETilesRenderer implements BlockEntityRenderer<BETiles> {

    public boolean shouldRender(BETiles be, Vec3 vec) { return false; }

    public AABB getRenderBoundingBox(BETiles be) { return null; }

    public boolean shouldRenderOffScreen(BETiles be) { return false; }

    public void render(BETiles be, float partialTickTime, PoseStack pose, MultiBufferSource buffer, int destroyStage, int p_112312_) {}

}


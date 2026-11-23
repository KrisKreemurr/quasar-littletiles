package team.creative.littletiles.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.TntRenderer;
import net.minecraft.client.renderer.entity.state.TntRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.item.PrimedTnt;
import team.creative.littletiles.common.entity.PrimedSizedTnt;

/** Quasar MC (26.3): little TNT, drawn as vanilla primed TNT scaled to its tile size. */
public class RenderSizedTNTPrimed extends TntRenderer {

    public static class State extends TntRenderState {
        public float sizeX = 1, sizeY = 1, sizeZ = 1;
    }

    public RenderSizedTNTPrimed(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public TntRenderState createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(PrimedTnt entity, TntRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        if (state instanceof State s && entity instanceof PrimedSizedTnt tnt && tnt.size != null && tnt.grid != null) {
            s.sizeX = (float) tnt.size.getPosX(tnt.grid);
            s.sizeY = (float) tnt.size.getPosY(tnt.grid);
            s.sizeZ = (float) tnt.size.getPosZ(tnt.grid);
        }
    }

    @Override
    public void submit(TntRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        pose.pushPose();
        if (state instanceof State s)
            pose.scale(s.sizeX, s.sizeY, s.sizeZ);
        super.submit(state, pose, collector, camera);
        pose.popPose();
    }
}

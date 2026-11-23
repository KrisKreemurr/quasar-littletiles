package team.creative.littletiles.client.gui.animation;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.Projection;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import team.creative.littletiles.LittleTiles;

/** Quasar MC (26.3): draws LittleTiles structure previews (blueprint editor, axis view, add dialog) into an offscreen texture.
 * <p>
 * 1.21 pointed the GL viewport at the control and rendered the preview's chunk sections directly. 26.x GUIs are render states, so this uses vanilla's
 * picture-in-picture path (the same one that shows your player in the inventory): the scene is submitted into its own texture, which is then drawn where
 * the control is. Vanilla gives every picture an orthographic projection; perspective scenes swap in their own before the geometry is drawn. */
public class LittlePreviewRenderer extends PictureInPictureRenderer<LittlePreviewRenderState> {
    
    public interface Scene {
        
        void submit(Target target);
        
    }
    
    public final class Target {
        
        /** texture size in pixels */
        public final int width;
        public final int height;
        /** pixels per GUI unit */
        public final float pixelScale;
        public final SubmitNodeCollector collector;
        
        Target(int width, int height, float pixelScale, SubmitNodeCollector collector) {
            this.width = width;
            this.height = height;
            this.pixelScale = pixelScale;
            this.collector = collector;
        }
        
        /** Camera at the origin looking down -Z, Y up (like the world). */
        public PoseStack perspective(float fov) {
            perspective.setupPerspective(0.05F, 1000F, fov, width, height);
            RenderSystem.setProjectionMatrix(perspectiveBuffer.getBuffer(perspective), ProjectionType.PERSPECTIVE);
            return new PoseStack();
        }
        
        /** Origin in the middle of the picture, units are pixels, X right, Y up, Z towards the viewer (depth range +-1000). */
        public PoseStack ortho() {
            PoseStack pose = new PoseStack();
            pose.translate(width / 2F, height / 2F, 0);
            pose.scale(1, -1, 1);
            return pose;
        }
    }
    
    private final Projection perspective = new Projection();
    private final ProjectionMatrixBuffer perspectiveBuffer = new ProjectionMatrixBuffer("littletiles_preview");
    private int guiScale = 1;
    private static boolean logged;
    private static boolean reported;
    
    @Override
    public Class<LittlePreviewRenderState> getRenderStateClass() {
        return LittlePreviewRenderState.class;
    }
    
    @Override
    public void prepare(LittlePreviewRenderState state, GuiRenderState renderState, FeatureRenderDispatcher dispatcher, int guiScale) {
        this.guiScale = guiScale;
        super.prepare(state, renderState, dispatcher, guiScale);
    }
    
    @Override
    protected void renderToTexture(LittlePreviewRenderState state, PoseStack pose, SubmitNodeCollector collector) {
        int width = (state.x1() - state.x0()) * guiScale;
        int height = (state.y1() - state.y0()) * guiScale;
        try {
            Minecraft.getInstance().gameRenderer.lighting().setupFor(Lighting.Entry.ITEMS_3D);
            int before = PreviewScenes.submitted;
            state.scene().submit(new Target(width, height, guiScale, collector));
            if (!reported) {
                reported = true;
                LittleTiles.LOGGER.info("Blueprint preview: first frame drawn ({}x{} px, {} block meshes)", width, height, PreviewScenes.submitted - before);
            }
        } catch (Throwable e) {
            if (!logged) {
                logged = true;
                LittleTiles.LOGGER.error("Failed to draw LittleTiles structure preview (logged once)", e);
            }
        }
    }
    
    @Override
    protected String getTextureLabel() {
        return "littletiles_preview";
    }
    
    @Override
    public void close() {
        super.close();
        perspectiveBuffer.close();
    }
    
}

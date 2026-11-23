package team.creative.littletiles.client.gui.animation;

import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;

/** One structure preview inside a GUI, drawn offscreen by {@link LittlePreviewRenderer} and blitted into the screen. */
public record LittlePreviewRenderState(LittlePreviewRenderer.Scene scene, int x0, int y0, int x1, int y1, float scale, Matrix3x2fc pose, ScreenRectangle scissorArea,
        ScreenRectangle bounds) implements PictureInPictureRenderState {
    
    /** Queues a preview covering {@code (0, 0)-(width, height)} in the graphics' current local space. */
    public static void submit(GuiGraphicsExtractor graphics, int width, int height, LittlePreviewRenderer.Scene scene) {
        if (width <= 0 || height <= 0)
            return;
        Matrix3x2f pose = new Matrix3x2f(graphics.pose());
        ScreenRectangle scissor = graphics.peekScissorStack();
        graphics.submitPictureInPictureRenderState(new LittlePreviewRenderState(scene, 0, 0, width, height, 1F, pose, scissor, PictureInPictureRenderState.getBounds(0, 0,
            width, height, pose, scissor)));
    }
    
}

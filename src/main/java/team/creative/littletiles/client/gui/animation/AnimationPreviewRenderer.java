package team.creative.littletiles.client.gui.animation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import team.creative.creativecore.client.render.gui.CreativeGuiGraphics;
import team.creative.littletiles.common.gui.control.animation.GuiAnimationViewerStorage;

/**
 * Draws structure previews inside GUIs.
 * <p>
 * TODO(quasar): 1.21 rendered the preview's chunk sections straight into a GL viewport (RenderSystem viewport/projection + VertexBuffer layers). The 26.x
 * GUI is render-state based, so this needs a picture-in-picture renderer that draws the sub-level's meshes into a texture; that comes with the LittleTiles
 * render rewrite. Until then the viewers show a placeholder but keep all their controls (rotate, pan, zoom, axis picking).
 */
public class AnimationPreviewRenderer {
    
    private static final Component PLACEHOLDER = Component.literal("3D preview not ported yet");
    
    public static void renderPlaceholder(GuiGraphicsExtractor graphics, GuiAnimationViewerStorage storage, int width, int height) {
        CreativeGuiGraphics g = (CreativeGuiGraphics) graphics;
        g.colorRect(0, 0, width, height, 0x80000000);
        var font = Minecraft.getInstance().font;
        int textWidth = font.width(PLACEHOLDER);
        g.drawString(PLACEHOLDER.getVisualOrderText(), width / 2 - textWidth / 2, height / 2 - font.lineHeight / 2, 0xFFAAAAAA, 0, false);
    }
    
}

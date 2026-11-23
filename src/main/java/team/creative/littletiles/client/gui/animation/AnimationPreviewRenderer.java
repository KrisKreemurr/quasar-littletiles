package team.creative.littletiles.client.gui.animation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import team.creative.creativecore.client.render.gui.CreativeGuiGraphics;
import team.creative.littletiles.common.gui.control.animation.GuiAnimationViewerStorage;

public class AnimationPreviewRenderer {
    
    /** Shown while the preview is still being built (or why it failed). */
    public static void renderStatus(GuiGraphicsExtractor graphics, GuiAnimationViewerStorage storage, int width, int height) {
        CreativeGuiGraphics g = (CreativeGuiGraphics) graphics;
        String status = storage.status();
        Component text = Component.literal(status != null ? status : "Building preview...");
        var font = Minecraft.getInstance().font;
        int y = height / 2 - font.lineHeight / 2;
        for (var line : font.split(text, Math.max(20, width - 8))) {
            g.drawString(line, width / 2 - font.width(line) / 2, y, status != null ? 0xFFFF7070 : 0xFFDDDDDD, 0, false);
            y += font.lineHeight + 1;
        }
    }
    
}

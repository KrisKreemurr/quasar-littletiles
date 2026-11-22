package team.creative.littletiles.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import team.creative.creativecore.client.gui.GuiClientLayer;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.util.math.geo.Rect;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.common.gui.LittleGuiHooks;

/** Client dist for LittleTiles layers that use {@link LittleGuiHooks}. */
public class LittleClientLayer extends GuiClientLayer {
    
    public LittleClientLayer(GuiLayer layer) {
        super(layer);
    }
    
    @Override
    public void render(GuiGraphicsExtractor graphics, Rect controlRect, Rect realRect, double scale, int mouseX, int mouseY) {
        if (control instanceof LittleGuiHooks.PreRender hook)
            hook.preRender();
        super.render(graphics, controlRect, realRect, scale, mouseX, mouseY);
    }
    
    @Override
    public boolean keyPressed(KeyEvent key) {
        if (super.keyPressed(key))
            return true;
        if (control instanceof LittleGuiHooks.CloseOnConfigureKey && LittleTilesClient.KEY_CONFIGURE.matches(key)) {
            control.closeTopLayer();
            return true;
        }
        return false;
    }
    
    public static boolean handles(Object control) {
        return control instanceof GuiLayer && (control instanceof LittleGuiHooks.PreRender || control instanceof LittleGuiHooks.CloseOnConfigureKey);
    }
    
}

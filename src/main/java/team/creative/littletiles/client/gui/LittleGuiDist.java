package team.creative.littletiles.client.gui;

import team.creative.creativecore.client.gui.GuiClientControl;
import team.creative.creativecore.client.gui.GuiControlRect;
import team.creative.creativecore.common.gui.GuiControl;

/** Client-only access to a control's layout rect (since CreativeCore 26.x the rect lives on the client dist handler). Only call from client code paths. */
public class LittleGuiDist {
    
    public static GuiControlRect rect(GuiControl control) {
        return ((GuiClientControl) control.dist()).rect;
    }
    
    public static GuiClientControl client(GuiControl control) {
        return (GuiClientControl) control.dist();
    }
    
}

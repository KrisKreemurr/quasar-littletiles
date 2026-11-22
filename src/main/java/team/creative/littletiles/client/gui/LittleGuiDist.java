package team.creative.littletiles.client.gui;

import team.creative.creativecore.client.gui.GuiClientControl;
import team.creative.creativecore.client.gui.GuiControlRect;
import team.creative.creativecore.common.gui.GuiControl;

/** Client-only access to a control's layout rect (since CreativeCore 26.x the rect lives on the client dist handler). Only call from client code paths. */
public class LittleGuiDist {
    
    public static GuiControlRect rect(GuiControl control) {
        return ((GuiClientControl) control.dist()).rect;
    }
    
    /** 26.3 renumbered mouse buttons; maps them back to the classic 0 = left, 1 = right, 2 = middle that LittleTiles' logic uses. */
    public static int legacyButton(int button) {
        if (button == com.mojang.blaze3d.platform.InputConstants.MOUSE_BUTTON_LEFT)
            return 0;
        if (button == com.mojang.blaze3d.platform.InputConstants.MOUSE_BUTTON_RIGHT)
            return 1;
        if (button == com.mojang.blaze3d.platform.InputConstants.MOUSE_BUTTON_MIDDLE)
            return 2;
        return button;
    }
    
    public static GuiClientControl client(GuiControl control) {
        return (GuiClientControl) control.dist();
    }
    
}

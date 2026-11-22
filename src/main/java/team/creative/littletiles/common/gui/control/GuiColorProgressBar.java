package team.creative.littletiles.common.gui.control;

import team.creative.creativecore.common.gui.IGuiParent;
import team.creative.creativecore.common.gui.control.simple.GuiProgressbar;
import team.creative.creativecore.common.util.type.Color;

/** State only. Drawing and input live in {@code GuiClientColorProgressBar} (CreativeCore 26.x dist split). */
public class GuiColorProgressBar extends GuiProgressbar {
    
    public Color color;
    
    public GuiColorProgressBar(IGuiParent parent, String name, double pos, double max, Color color) {
        super(parent, name, pos, max);
        this.color = color;
    }
    
}

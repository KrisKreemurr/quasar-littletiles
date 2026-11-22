package team.creative.littletiles.client.gui.signal;

import team.creative.littletiles.client.gui.LittleGuiDist;
import net.minecraft.client.input.MouseButtonInfo;
import team.creative.creativecore.client.gui.GuiClientParent;
import team.creative.littletiles.common.gui.signal.node.GuiSignalNode;

public class GuiClientSignalNode extends GuiClientParent<GuiSignalNode> {
    
    public GuiClientSignalNode(GuiSignalNode control) {
        super(control);
    }
    
    @Override
    public boolean testForDoubleClick(double x, double y, MouseButtonInfo info) {
        return LittleGuiDist.legacyButton(info.button()) != 1;
    }
    
    @Override
    public boolean mouseDoubleClicked(double x, double y, MouseButtonInfo info) {
        if (control.doubleClicked())
            return true;
        return super.mouseDoubleClicked(x, y, info);
    }
    
}

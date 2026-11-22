package team.creative.littletiles.client.gui.control;

import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.sounds.SoundEvents;
import team.creative.creativecore.client.gui.control.parent.GuiClientPanel;
import team.creative.littletiles.common.gui.tool.blueprint.GuiBlueprintAdd.GuiBlueprintAddEntry;

public class GuiClientBlueprintAddEntry extends GuiClientPanel<GuiBlueprintAddEntry> {
    
    public GuiClientBlueprintAddEntry(GuiBlueprintAddEntry control) {
        super(control);
    }
    
    @Override
    public boolean mouseClicked(double x, double y, MouseButtonInfo info) {
        control.clicked();
        playSound(SoundEvents.UI_BUTTON_CLICK);
        return true;
    }
    
}

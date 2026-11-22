package team.creative.littletiles.client.gui.control;

import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.sounds.SoundEvents;
import team.creative.creativecore.client.gui.control.inventory.GuiClientSlotViewer;
import team.creative.creativecore.common.gui.style.GuiStyle;
import team.creative.creativecore.common.gui.style.display.DisplayColor;
import team.creative.creativecore.common.gui.style.display.StyleDisplay;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.common.gui.structure.GuiBlankOMatic.GuiSlotControlSelect;

public class GuiClientSlotControlSelect extends GuiClientSlotViewer<GuiSlotControlSelect> {
    
    private static DisplayColor SELECTED_DISPLAY;
    
    public GuiClientSlotControlSelect(GuiSlotControlSelect control) {
        super(control);
    }
    
    @Override
    public StyleDisplay getBorder(GuiStyle style, StyleDisplay display) {
        if (control.selected) {
            if (SELECTED_DISPLAY == null)
                SELECTED_DISPLAY = new DisplayColor(ColorUtils.YELLOW);
            return SELECTED_DISPLAY;
        }
        return super.getBorder(style, display);
    }
    
    @Override
    public boolean mouseClicked(double x, double y, MouseButtonInfo info) {
        control.selector.select(control.index);
        playSound(SoundEvents.UI_BUTTON_CLICK);
        return true;
    }
    
}

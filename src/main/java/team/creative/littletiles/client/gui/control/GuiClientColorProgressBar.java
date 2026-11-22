package team.creative.littletiles.client.gui.control;

import team.creative.littletiles.client.gui.LittleGuiDist;
import java.util.List;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import team.creative.creativecore.client.gui.control.simple.GuiClientProgressbar;
import team.creative.creativecore.client.render.gui.CreativeGuiGraphics;
import team.creative.creativecore.common.gui.style.ControlFormatting;
import team.creative.littletiles.common.gui.control.GuiColorProgressBar;

public class GuiClientColorProgressBar extends GuiClientProgressbar<GuiColorProgressBar> {
    
    public GuiClientColorProgressBar(GuiColorProgressBar control) {
        super(control);
    }
    
    @Override
    protected ControlFormatting defaultFormatting() {
        return ControlFormatting.NESTED_NO_PADDING;
    }
    
    @Override
    public List<Component> getTooltip() {
        List<Component> tooltip = super.getTooltip();
        if (tooltip != null)
            tooltip.add(Component.translatable("gui.color.rightclick"));
        return tooltip;
    }
    
    @Override
    public boolean mouseClicked(double x, double y, MouseButtonInfo info) {
        if (LittleGuiDist.legacyButton(info.button()) == 1) {
            playSound(SoundEvents.UI_BUTTON_CLICK);
            return true;
        }
        return false;
    }
    
    @Override
    protected void renderProgress(GuiGraphicsExtractor graphics, double percent) {
        ((CreativeGuiGraphics) graphics).colorRect(0, 0, (int) (rect.getContentWidth() * percent), rect.getContentHeight(), control.color.toInt());
    }
    
}

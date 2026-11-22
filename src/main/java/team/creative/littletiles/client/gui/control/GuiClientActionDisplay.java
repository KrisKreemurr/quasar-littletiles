package team.creative.littletiles.client.gui.control;

import org.joml.Matrix3x2fStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import team.creative.creativecore.client.gui.GuiClientControl;
import team.creative.creativecore.client.render.text.CompiledText;
import team.creative.creativecore.common.gui.style.ControlFormatting;
import team.creative.littletiles.common.gui.control.GuiActionDisplay;
import team.creative.littletiles.common.gui.control.GuiActionDisplay.ActionMessage;

public class GuiClientActionDisplay extends GuiClientControl<GuiActionDisplay> {
    
    public GuiClientActionDisplay(GuiActionDisplay control) {
        super(control);
    }
    
    private static CompiledText text(ActionMessage message) {
        if (message.clientCache instanceof CompiledText text)
            return text;
        CompiledText text = new CompiledText(Integer.MAX_VALUE, Integer.MAX_VALUE);
        text.setText(message.text);
        message.clientCache = text;
        return text;
    }
    
    @Override
    protected ControlFormatting defaultFormatting() {
        return ControlFormatting.TRANSPARENT;
    }
    
    @Override
    public void flowX(int width, int preferred) {
        for (int i = 0; i < control.messages.size(); i++)
            text(control.messages.get(i)).setDimension(width, Integer.MAX_VALUE);
    }
    
    @Override
    public void flowY(int width, int height, int preferred) {}
    
    @Override
    protected int minWidth(int availableWidth) {
        return 10;
    }
    
    @Override
    protected int preferredWidth(int availableWidth) {
        int max = 0;
        for (int i = 0; i < control.messages.size(); i++)
            max = Math.max(max, text(control.messages.get(i)).getTotalWidth());
        return max;
    }
    
    @Override
    protected int minHeight(int width, int availableHeight) {
        return Minecraft.getInstance().font.lineHeight;
    }
    
    @Override
    protected int preferredHeight(int width, int availableHeight) {
        int height = 0;
        for (int i = 0; i < control.messages.size(); i++)
            height += text(control.messages.get(i)).getTotalHeight();
        return height;
    }
    
    @Override
    protected void renderContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        int i = 0;
        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        while (i < control.messages.size()) {
            ActionMessage message = control.messages.get(i);
            long timer = System.currentTimeMillis() - message.timestamp;
            if (timer >= control.totalTime)
                control.removeMessage(i);
            else {
                float alpha = timer > control.displayTime ? (1 - Math.max(0F, timer - control.displayTime) / control.fadeTime) : 1;
                int a = (int) (Math.max(0.05F, alpha) * 255) << 24;
                CompiledText text = text(message);
                text.setDefaultColor((text.getDefaultColor() & 0xFFFFFF) | a);
                text.render(graphics);
                i++;
                pose.translate(0, text.getTotalHeight());
            }
        }
        pose.popMatrix();
    }
    
}

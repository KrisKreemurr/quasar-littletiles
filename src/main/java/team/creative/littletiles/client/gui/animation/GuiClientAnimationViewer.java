package team.creative.littletiles.client.gui.animation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonInfo;
import team.creative.creativecore.client.gui.GuiClientControl;
import team.creative.creativecore.common.gui.style.ControlFormatting;
import team.creative.creativecore.common.util.math.geo.Rect;
import team.creative.littletiles.common.gui.control.animation.GuiAnimationViewer;
import team.creative.littletiles.common.gui.control.animation.GuiAnimationViewer.ViewerDragMode;

public class GuiClientAnimationViewer extends GuiClientControl<GuiAnimationViewer> {
    
    private boolean forward;
    private boolean backward;
    private boolean left;
    private boolean right;
    private boolean up;
    private boolean down;
    
    public GuiClientAnimationViewer(GuiAnimationViewer control) {
        super(control);
    }
    
    @Override
    protected ControlFormatting defaultFormatting() {
        return ControlFormatting.NESTED_NO_PADDING;
    }
    
    @Override
    public void mouseMoved(double x, double y) {
        super.mouseMoved(x, y);
        if (control.grabMode == ViewerDragMode.NONE)
            return;
        
        switch (control.grabMode) {
            case LEFT -> {
                int scale = Math.min(rect.getWidth(), rect.getHeight());
                control.rotY.setStart(control.rotY.aimed() + (x - control.grabX) / scale * 180);
                control.rotX.setStart(control.rotX.aimed() + (y - control.grabY) / scale * 180);
            }
            default -> control.projection().dragMouse(control, x - control.grabX, y - control.grabY);
        }
        control.grabX = x;
        control.grabY = y;
    }
    
    @Override
    public boolean mouseClicked(double x, double y, MouseButtonInfo info) {
        if (control.grabMode == ViewerDragMode.NONE) {
            control.grabMode = ViewerDragMode.of(info.button());
            control.grabX = x;
            control.grabY = y;
            return true;
        }
        return false;
    }
    
    @Override
    public void mouseReleased(double x, double y, MouseButtonInfo info) {
        if (info.button() == control.grabMode.ordinal() - 1)
            control.grabMode = ViewerDragMode.NONE;
    }
    
    @Override
    public boolean mouseScrolled(double x, double y, double delta) {
        control.distance.set(Math.max(control.distance.aimed() + delta * -(Minecraft.getInstance().hasControlDown() ? 5 : 1), 0));
        return true;
    }
    
    private void setMovement(KeyEvent key, boolean pressed) {
        Options options = Minecraft.getInstance().options;
        if (options.keyUp.matches(key))
            forward = pressed;
        if (options.keyDown.matches(key))
            backward = pressed;
        if (options.keyRight.matches(key))
            left = pressed;
        if (options.keyLeft.matches(key))
            right = pressed;
        if (options.keyJump.matches(key))
            up = pressed;
        if (options.keyShift.matches(key))
            down = pressed;
    }
    
    @Override
    public boolean keyPressed(KeyEvent key) {
        setMovement(key, true);
        return super.keyPressed(key);
    }
    
    @Override
    public boolean keyReleased(KeyEvent key) {
        setMovement(key, false);
        return super.keyReleased(key);
    }
    
    @Override
    protected void renderContent(GuiGraphicsExtractor graphics, Rect controlRect, Rect realRect, double controlScale, int mouseX, int mouseY) {
        if (!control.storage.isReady())
            return;
        
        if (!control.initialized) {
            control.resetView();
            control.projection().init(control);
            control.initialized = true;
        }
        
        control.rotX.tick();
        control.rotY.tick();
        control.rotZ.tick();
        control.distance.tick();
        control.offX.tick();
        control.offY.tick();
        control.offZ.tick();
        
        float amount = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaTicks() * 2;
        if (Minecraft.getInstance().hasControlDown())
            amount *= 4;
        if (forward)
            control.projection().forward(amount, control);
        if (backward)
            control.projection().forward(-amount, control);
        if (left)
            control.projection().lateral(amount, control);
        if (right)
            control.projection().lateral(-amount, control);
        if (up)
            control.projection().up(amount, control);
        if (down)
            control.projection().up(-amount, control);
        
        AnimationPreviewRenderer.renderPlaceholder(graphics, control.storage, rect.getContentWidth(), rect.getContentHeight());
    }
    
    @Override
    protected void renderContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {}
    
    @Override
    public void flowX(int width, int preferred) {}
    
    @Override
    public void flowY(int width, int height, int preferred) {}
    
    @Override
    protected int preferredWidth(int availableWidth) {
        return 10;
    }
    
    @Override
    protected int preferredHeight(int width, int availableHeight) {
        return 10;
    }
    
}

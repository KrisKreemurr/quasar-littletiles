package team.creative.littletiles.client.gui.animation;

import com.mojang.blaze3d.vertex.PoseStack;

import team.creative.littletiles.client.gui.LittleGuiDist;
import org.joml.Matrix3x2fStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.Component;
import team.creative.creativecore.client.gui.GuiClientControl;
import team.creative.creativecore.client.render.gui.CreativeGuiGraphics;
import team.creative.creativecore.common.gui.style.Icon;
import team.creative.creativecore.common.gui.style.ControlFormatting;
import team.creative.creativecore.common.util.math.geo.Rect;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.common.gui.control.animation.GuiIsoAnimationViewer;
import team.creative.littletiles.common.gui.control.animation.GuiIsoAnimationViewer.GuiIsoView;
import team.creative.littletiles.common.gui.tool.blueprint.GuiBlueprintAnimationStorage;

public class GuiClientIsoAnimationViewer extends GuiClientControl<GuiIsoAnimationViewer> {
    
    private boolean grabbed = false;
    private double grabX;
    private double grabY;
    
    public GuiClientIsoAnimationViewer(GuiIsoAnimationViewer control) {
        super(control);
    }
    
    @Override
    protected ControlFormatting defaultFormatting() {
        return ControlFormatting.NESTED_NO_PADDING;
    }
    
    protected float calculateScale() {
        float dimensionScale = Math.min(rect.getContentWidth(), (float) rect.getContentHeight());
        return (float) (dimensionScale * Math.pow(control.scale.current(), 2));
    }
    
    @Override
    public void mouseMoved(double x, double y) {
        super.mouseMoved(x, y);
        if (!grabbed)
            return;
        double scale = calculateScale();
        control.offX.add((x - grabX) / scale);
        control.offY.add((y - grabY) / scale);
        grabX = x;
        grabY = y;
    }
    
    @Override
    public boolean mouseClicked(double x, double y, MouseButtonInfo info) {
        switch (LittleGuiDist.legacyButton(info.button())) {
            case 0 -> {
                grabX = x;
                grabY = y;
                grabbed = true;
            }
            case 1 -> control.clickToSetAxis(x, y, rect.getWidth(), rect.getHeight(), calculateScale());
            case 2 -> control.resetView();
        }
        return true;
    }
    
    @Override
    public void mouseReleased(double x, double y, MouseButtonInfo info) {
        grabbed = false;
    }
    
    @Override
    public boolean mouseScrolled(double x, double y, double delta) {
        control.scale.add(0.05 * delta * (Minecraft.getInstance().hasControlDown() ? 5 : 1));
        if (Math.pow(control.scale.aimed(), 2) > GuiIsoAnimationViewer.MAXIMUM_ZOOM)
            control.scale.set(Math.sqrt(GuiIsoAnimationViewer.MAXIMUM_ZOOM));
        if (control.scale.aimed() < 0)
            control.scale.set(0);
        return true;
    }
    
    @Override
    protected void renderContent(GuiGraphicsExtractor graphics, Rect controlRect, Rect realRect, double controlScale, int mouseX, int mouseY) {
        GuiBlueprintAnimationStorage storage = control.item.blueprint.storage;
        if (!storage.isReady() || !storage.isReady(control.item))
            return;
        
        if (!control.initialized) {
            control.resetView();
            control.initialized = true;
        }
        
        control.rotX.tick();
        control.rotY.tick();
        control.rotZ.tick();
        control.scale.tick();
        control.offX.tick();
        control.offY.tick();
        
        GuiIsoAnimationViewer viewer = control;
        float guiPixels = calculateScale();
        LittlePreviewRenderState.submit(graphics, rect.getWidth(), rect.getHeight(), target -> {
            // orthographic: depth is squashed so the axis can sit in front of everything without leaving the depth range
            PoseStack pose = target.ortho();
            transform(pose, viewer, storage, guiPixels * target.pixelScale);
            PreviewScenes.submitStorage(storage, pose, target.collector);
            if (viewer.visibleAxis && viewer.getBox() != null) {
                PoseStack axis = target.ortho();
                axis.translate(0, 0, 900);
                transform(axis, viewer, storage, guiPixels * target.pixelScale);
                net.minecraft.world.phys.AABB bb = viewer.getBox().getBB(viewer.getGrid());
                target.collector.submitShapeOutline(axis, net.minecraft.world.phys.shapes.Shapes.create(bb), net.minecraft.client.renderer.rendertype.RenderTypes.lines(),
                    0xFF000000, 5.0F, false);
                target.collector.submitShapeOutline(PreviewScenes.copy(axis), net.minecraft.world.phys.shapes.Shapes.create(bb),
                    net.minecraft.client.renderer.rendertype.RenderTypes.lines(), 0xFFFFFFFF, 2.5F, false);
            }
        });
        
        CreativeGuiGraphics g = (CreativeGuiGraphics) graphics;
        Matrix3x2fStack pose = graphics.pose();
        GuiIsoView view = control.view();
        int axisWidth = 20;
        int axisHeight = 16;
        Font font = Minecraft.getInstance().font;
        
        pose.pushMatrix();
        g.drawString(Component.literal(view.xAxis.axis.name()).getVisualOrderText(), rect.getWidth() / 2 - axisWidth / 2, rect.getHeight() - axisHeight + font.lineHeight / 2,
            ColorUtils.WHITE, 0, false);
        Icon icon = view.xAxis.positive ? Icon.ARROW_RIGHT : Icon.ARROW_LEFT;
        pose.translate(rect.getWidth() / 2 - axisWidth / 2, rect.getHeight() - axisHeight);
        drawIcon(g, icon, 9, 0, ColorUtils.BLACK);
        drawIcon(g, icon, 8, 0, ColorUtils.WHITE);
        pose.popMatrix();
        
        pose.pushMatrix();
        g.drawString(Component.literal(view.yAxis.axis.name()).getVisualOrderText(), 5, rect.getHeight() / 2 - axisHeight / 2, ColorUtils.WHITE, 0, false);
        icon = view.yAxis.positive ? Icon.ARROW_UP : Icon.ARROW_DOWN;
        pose.translate(0, rect.getHeight() / 2 - axisHeight - 2);
        drawIcon(g, icon, 0, -9, ColorUtils.BLACK);
        drawIcon(g, icon, 0, -8, ColorUtils.WHITE);
        pose.popMatrix();
    }
    
    private static void transform(PoseStack pose, GuiIsoAnimationViewer viewer, GuiBlueprintAnimationStorage storage, float scale) {
        pose.scale(1, 1, 0.2F);
        pose.scale(scale, scale, scale);
        pose.translate(viewer.offX.current(), -viewer.offY.current(), 0);
        pose.rotate(com.mojang.math.Axis.XP.rotationDegrees((float) viewer.rotX.current()));
        pose.rotate(com.mojang.math.Axis.YP.rotationDegrees((float) viewer.rotY.current()));
        pose.rotate(com.mojang.math.Axis.ZP.rotationDegrees((float) viewer.rotZ.current()));
        var center = storage.center();
        pose.translate(-center.x, -center.y, -center.z);
    }
    
    private static void drawIcon(CreativeGuiGraphics g, Icon icon, int x, int y, int color) {
        g.textureRectColor(icon.identifier(), x, y, icon.width(), icon.height(), icon.minX(), icon.minY(), icon.minX() + icon.width(), icon.minY() + icon.height(), color);
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

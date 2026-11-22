package team.creative.littletiles.client.gui.signal;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix3x2fStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import team.creative.creativecore.client.gui.GuiClientControl;
import team.creative.creativecore.client.gui.GuiClientParent;
import team.creative.creativecore.client.gui.GuiControlRect;
import team.creative.creativecore.client.render.gui.CreativeGuiGraphics;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.style.ControlFormatting;
import team.creative.creativecore.common.util.math.geo.Rect;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.type.itr.ConsecutiveIterator;
import team.creative.littletiles.client.gui.LittleGuiDist;
import team.creative.littletiles.common.gui.signal.GuiSignalConnection;
import team.creative.littletiles.common.gui.signal.GuiSignalController;
import team.creative.littletiles.common.gui.signal.node.GuiSignalNode;

/** Client half of the signal editor: grid layout, panning/zooming, node drawing and connection lines. */
public class GuiClientSignalController extends GuiClientParent<GuiSignalController> implements GuiSignalController.GridLayout {
    
    private double startScrollX;
    private double startScrollY;
    private int dragX;
    private int dragY;
    private boolean scrolling;
    private Rect controllerRect;
    
    public GuiClientSignalController(GuiSignalController control) {
        super(control);
    }
    
    private List<GuiClientControl> gridControls() {
        List<GuiClientControl> list = new ArrayList<>();
        for (List<GuiControl> column : control.grid)
            for (GuiControl child : column)
                if (child != null)
                    list.add(LittleGuiDist.client(child));
        return list;
    }
    
    @Override
    public Iterable<GuiClientControl> all() {
        Iterable<GuiClientControl> normal = super.all();
        List<GuiClientControl> grid = gridControls();
        return () -> new ConsecutiveIterator<>(normal.iterator(), grid.iterator());
    }
    
    @Override
    public double getOffsetX() {
        return -control.scrolledX.current();
    }
    
    @Override
    public double getOffsetY() {
        return -control.scrolledY.current();
    }
    
    @Override
    protected ControlFormatting defaultFormatting() {
        return ControlFormatting.NESTED_NO_PADDING;
    }
    
    @Override
    protected void renderContent(GuiGraphicsExtractor graphics, Rect contentRect, Rect realContentRect, double scale, int mouseX, int mouseY) {
        if (realContentRect == null)
            return;
        
        control.scrolledX.tick();
        control.scrolledY.tick();
        control.zoom.tick();
        
        setScale(control.zoom.current());
        
        float controlScale = (float) scaleFactor();
        double gridScale = scale * scaleFactor();
        double xOffset = getOffsetX();
        double yOffset = getOffsetY();
        
        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.scale(controlScale, controlScale);
        controllerRect = realContentRect;
        List<GuiClientControl> grid = gridControls();
        renderControls(graphics, contentRect, realContentRect, mouseX, mouseY, grid.listIterator(grid.size()), gridScale, xOffset, yOffset, false);
        controllerRect = null;
        pose.popMatrix();
        
        super.renderContent(graphics, contentRect, realContentRect, scale, mouseX, mouseY);
    }
    
    @Override
    protected void renderControl(GuiGraphicsExtractor graphics, GuiClientControl child, Rect controlRect, Rect realRect, double scale, int mouseX, int mouseY, boolean hover) {
        if (controllerRect != null && child.control instanceof GuiSignalNode com) {
            scissor(graphics, controllerRect);
            if (com.hasUnderline()) {
                Font font = Minecraft.getInstance().font;
                String underline = com.getUnderline();
                ((CreativeGuiGraphics) graphics).drawString(Component.literal(underline).getVisualOrderText(), child.rect.getWidth() / 2 - font.width(underline) / 2, child.rect
                        .getHeight() + 4, ColorUtils.WHITE, 0, false);
            }
            renderConnections(graphics, com, scale, realRect != null && realRect.inside(mouseX, mouseY), mouseX, mouseY);
            scissor(graphics, realRect);
        }
        super.renderControl(graphics, child, controlRect, realRect, scale, mouseX, mouseY, hover);
    }
    
    private void renderConnections(GuiGraphicsExtractor graphics, GuiSignalNode node, double scale, boolean hover, double mouseX, double mouseY) {
        GuiControlRect rect = LittleGuiDist.rect(node);
        float originX = rect.getX() - getContentOffset();
        float originY = rect.getY() - getContentOffset();
        float width = (float) Math.max(1, 2 * scale) / (float) scale;
        for (GuiSignalConnection connection : node.toConnections()) {
            GuiControl other = connection.from();
            if (!hover) {
                GuiControlRect otherRect = LittleGuiDist.rect(other);
                hover = other.toScreenRect(new Rect(0, 0, otherRect.getWidth(), otherRect.getHeight())).inside(mouseX, mouseY);
            }
            drawLine(graphics, connection.fromX() - originX, connection.fromY() - originY, connection.toX() - originX, connection.toY() - originY, width,
                hover ? ColorUtils.WHITE : ColorUtils.BLACK);
        }
    }
    
    /** The 26.x GUI has no line primitive; a line is a thin rectangle rotated onto the segment. */
    private static void drawLine(GuiGraphicsExtractor graphics, float x1, float y1, float x2, float y2, float width, int color) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float length = Mth.sqrt(dx * dx + dy * dy);
        if (length < 0.01F)
            return;
        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(x1, y1);
        pose.rotate((float) Math.atan2(dy, dx));
        ((CreativeGuiGraphics) graphics).colorRect(0F, -width / 2, length, width, color);
        pose.popMatrix();
    }
    
    @Override
    public void flowCell(GuiControl child, int col, int row) {
        if (control.getParent() == null)
            return;
        GuiControlRect rect = LittleGuiDist.rect(child);
        int cellWidth = control.cellWidth;
        int cellHeight = control.cellHeight;
        int widthNode = Math.min(cellWidth, rect.getPreferredWidth(cellWidth));
        rect.setWidth(widthNode, cellWidth);
        rect.setX(cellWidth * col + cellWidth / 2 - rect.getWidth() / 2);
        rect.flowX();
        int heightNode = Math.min(cellHeight, rect.getPreferredHeight(cellHeight));
        rect.setHeight(heightNode, cellHeight);
        rect.setY(cellHeight * row + cellHeight / 2 - rect.getHeight() / 2);
        rect.flowY();
    }
    
    @Override
    public void flowX(int width, int preferred) {
        super.flowX(width, preferred);
        int cellWidth = control.cellWidth;
        for (int x = 0; x < control.grid.size(); x++) {
            List<GuiControl> rows = control.grid.get(x);
            for (int y = 0; y < rows.size(); y++) {
                GuiControl child = rows.get(y);
                if (child == null)
                    continue;
                GuiControlRect rect = LittleGuiDist.rect(child);
                int widthNode = Math.min(cellWidth, rect.getPreferredWidth(cellWidth));
                rect.setWidth(widthNode, cellWidth);
                rect.setX(cellWidth * x + cellWidth / 2 - rect.getWidth() / 2);
                rect.flowX();
            }
        }
    }
    
    @Override
    public void flowY(int width, int height, int preferred) {
        super.flowY(width, height, preferred);
        int cellHeight = control.cellHeight;
        for (int x = 0; x < control.grid.size(); x++) {
            List<GuiControl> rows = control.grid.get(x);
            for (int y = 0; y < rows.size(); y++) {
                GuiControl child = rows.get(y);
                if (child == null)
                    continue;
                GuiControlRect rect = LittleGuiDist.rect(child);
                int heightNode = Math.min(cellHeight, rect.getPreferredHeight(cellHeight));
                rect.setHeight(heightNode, cellHeight);
                rect.setY(cellHeight * y + cellHeight / 2 - rect.getHeight() / 2);
                rect.flowY();
            }
        }
    }
    
    @Override
    public boolean mouseClicked(double x, double y, MouseButtonInfo info) {
        control.startedDragging = false;
        if (info.button() == 2) {
            control.zoom.set(1);
            control.scrolledX.set(0);
            control.scrolledY.set(0);
            return true;
        }
        if (!super.mouseClicked(x, y, info)) {
            control.select(null, null);
            scrolling = true;
            dragX = (int) x;
            dragY = (int) y;
            startScrollX = control.scrolledX.current();
            startScrollY = control.scrolledY.current();
        }
        return true;
    }
    
    @Override
    public boolean mouseScrolled(double x, double y, double delta) {
        if (!super.mouseScrolled(x, y, delta))
            control.zoom.set(Mth.clamp(control.zoom.aimed() + delta * 0.2, 0.1, 2));
        return true;
    }
    
    @Override
    public void mouseDragged(double x, double y, MouseButtonInfo info, double dragX, double dragY, double time) {
        super.mouseDragged(x, y, info, dragX, dragY, time);
        if (time > 0.2 && control.dragged != null) {
            control.set(control.dragged, (int) Math.max(0, (x * scaleFactorInv() + control.scrolledX.current()) / control.cellWidth), (int) Math.max(0, (y * scaleFactorInv() + control.scrolledY
                    .current()) / control.cellHeight));
            control.startedDragging = true;
        }
    }
    
    @Override
    public void mouseMoved(double x, double y) {
        if (scrolling) {
            control.scrolledX.set(Mth.clamp(dragX - x + startScrollX, -40, control.sizeX() * control.cellWidth));
            control.scrolledY.set(Mth.clamp(dragY - y + startScrollY, -40, control.sizeY() * control.cellHeight));
        }
        super.mouseMoved(x, y);
    }
    
    @Override
    public void mouseReleased(double x, double y, MouseButtonInfo info) {
        super.mouseReleased(x, y, info);
        scrolling = false;
        if (control.dragged != null && !control.startedDragging)
            control.select(control.dragged, control.selectedAnchor);
        control.startedDragging = false;
        control.dragged = null;
    }
    
}

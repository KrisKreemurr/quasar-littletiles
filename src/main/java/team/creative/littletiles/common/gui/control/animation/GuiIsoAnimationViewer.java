package team.creative.littletiles.common.gui.control.animation;

import team.creative.littletiles.common.gui.LittleGuiUtils;
import team.creative.creativecore.common.gui.IGuiParent;


import com.mojang.math.Axis;

import net.minecraft.sounds.SoundEvents;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.event.GuiControlChangedEvent;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.vec.SmoothValue;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.tool.blueprint.GuiBlueprintAnimationStorage;
import team.creative.littletiles.common.gui.tool.blueprint.GuiTreeItemStructure;
import team.creative.littletiles.common.math.box.LittleBox;

public class GuiIsoAnimationViewer extends GuiControl {
    
    public static final float MAXIMUM_ZOOM = 1F / (float) LittleGrid.OVERALL_DEFAULT_PIXEL_LENGTH;
    
    public SmoothValue rotX = new SmoothValue(200);
    public SmoothValue rotY = new SmoothValue(200);
    public SmoothValue rotZ = new SmoothValue(200);
    public SmoothValue scale = new SmoothValue(200, 1);
    
    public SmoothValue offX = new SmoothValue(200);
    public SmoothValue offY = new SmoothValue(200);
    
    private GuiIsoView view;
    
    public boolean initialized = false;
    public final GuiTreeItemStructure item;
    
    private LittleBox box;
    private LittleGrid grid;
    private boolean even;
    
    public boolean visibleAxis = true;
    
    public GuiIsoAnimationViewer(IGuiParent parent, String name, GuiTreeItemStructure item, LittleBox box, LittleGrid grid, boolean even) {
        super(parent, name);
        this.item = item;
        this.setView(GuiIsoView.UP);
        this.box = box.copy();
        this.grid = grid;
        this.even = even;
    }
    
    public LittleBox getBox() {
        return box;
    }
    
    public LittleGrid getGrid() {
        return grid;
    }
    
    public void setAxis(LittleBox box, LittleGrid grid) {
        this.box = box.copy();
        this.grid = grid;
        raiseEvent(new GuiAnimationAxisChangedEvent(this));
    }
    
    public boolean isEven() {
        return even;
    }
    
    public void setEven(boolean even) {
        boolean changed = this.even != even;
        this.even = even;
        
        if (!changed || box == null)
            return;
        
        box = box.copy();
        if (even) {
            box.minX -= 1;
            box.minY -= 1;
            box.minZ -= 1;
        } else {
            box.minX += 1;
            box.minY += 1;
            box.minZ += 1;
        }
        raiseEvent(new GuiAnimationAxisChangedEvent(this));
    }
    
    public void setView(GuiIsoView view) {
        this.view = view;
        this.rotX.set(view.rotX);
        this.rotY.set(view.rotY);
        this.rotZ.set(view.rotZ);
        if (getParent() != null)
            raiseEvent(new GuiAnimationViewChangedEvent(this));
    }
    
    /** @param width,height control size, scale the client view scale (see the client dist). */
    public void clickToSetAxis(double x, double y, int width, int height, float scale) {
        GuiBlueprintAnimationStorage storage = item.blueprint.storage;
        if (!storage.isReady() || !storage.isReady(item))
            return;
        
        Vec3d center = storage.center();
        
        team.creative.creativecore.common.util.math.base.Axis one = view.xAxis.axis;
        int posOne = grid.toGrid(-offX.current() * view.xAxis.offset() + ((x - width / 2) / scale) * view.xAxis.offset() + center.get(one));
        int sizeOne = box.getSize(one);
        box.setMax(one, posOne + sizeOne);
        box.setMin(one, posOne);
        
        team.creative.creativecore.common.util.math.base.Axis two = view.yAxis.axis;
        int posTwo = grid.toGrid((-offY.current() + (y - height / 2) / scale) * -view.yAxis.offset() + center.get(two));
        int sizeTwo = box.getSize(two);
        box.setMax(two, posTwo + sizeTwo);
        box.setMin(two, posTwo);
        
        raiseEvent(new GuiAnimationAxisChangedEvent(this));
        
        LittleGuiUtils.playSound(this, SoundEvents.WOODEN_BUTTON_CLICK_ON);
    }
    
    public void resetView() {
        offX.set(0);
        offY.set(0);
        
        scale.set(Math.sqrt(0.9 / item.blueprint.storage.longestSide()));
    }
    
    public void nextAxis() {
        setView(team.creative.creativecore.common.util.math.base.Axis.values()[(view.zAxis.axis.ordinal() + 1) % team.creative.creativecore.common.util.math.base.Axis
                .values().length].facing(true));
    }
    
    public void mirrorView() {
        setView(view.zAxis.opposite());
    }
    
    public void setView(Facing facing) {
        setView(switch (facing) {
            case EAST -> GuiIsoView.EAST;
            case WEST -> GuiIsoView.WEST;
            case UP -> GuiIsoView.UP;
            case DOWN -> GuiIsoView.DOWN;
            case SOUTH -> GuiIsoView.SOUTH;
            case NORTH -> GuiIsoView.NORTH;
        });
    }
    
    @Override
    public void closed() {}
    
    @Override
    public void init() {}
    
    @Override
    public void tick() {}
    
    public GuiIsoView view() {
        return view;
    }
    
    public team.creative.creativecore.common.util.math.base.Axis axis() {
        return view.zAxis.axis;
    }
    
    public Facing getXFacing() {
        return view.xAxis;
    }
    
    public Facing getYFacing() {
        return view.yAxis;
    }
    
    public Facing getZFacing() {
        return view.zAxis;
    }
    
    public static class GuiAnimationAxisChangedEvent extends GuiControlChangedEvent {
        
        public GuiAnimationAxisChangedEvent(GuiIsoAnimationViewer viewer) {
            super(viewer);
        }
        
    }
    
    public static class GuiAnimationViewChangedEvent extends GuiControlChangedEvent {
        
        public GuiAnimationViewChangedEvent(GuiIsoAnimationViewer viewer) {
            super(viewer);
        }
        
    }
    
    public static enum GuiIsoView {
        
        UP(90, 90, 0, Facing.SOUTH, Facing.EAST, Facing.UP),
        DOWN(-90, 90, 0, Facing.SOUTH, Facing.WEST, Facing.DOWN),
        EAST(0, -90, 0, Facing.NORTH, Facing.UP, Facing.EAST),
        WEST(0, 90, 0, Facing.SOUTH, Facing.UP, Facing.WEST),
        SOUTH(0, 0, 0, Facing.EAST, Facing.UP, Facing.SOUTH),
        NORTH(0, 180, 0, Facing.WEST, Facing.UP, Facing.NORTH);
        
        public final float rotX;
        public final float rotY;
        public final float rotZ;
        
        public final Facing xAxis;
        public final Facing yAxis;
        public final Facing zAxis;
        
        private GuiIsoView(float rotX, float rotY, float rotZ, Facing xAxis, Facing yAxis, Facing zAxis) {
            this.rotX = rotX;
            this.rotY = rotY;
            this.rotZ = rotZ;
            this.xAxis = xAxis;
            this.yAxis = yAxis;
            this.zAxis = zAxis;
        }
    }
    
}

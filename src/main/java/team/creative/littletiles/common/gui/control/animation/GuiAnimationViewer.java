package team.creative.littletiles.common.gui.control.animation;

import org.joml.Matrix4f;
import team.creative.creativecore.common.gui.IGuiParent;

import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.util.math.vec.SmoothValue;
import team.creative.creativecore.common.util.math.vec.Vec3d;

public class GuiAnimationViewer extends GuiControl {
    
    public SmoothValue offX = new SmoothValue(200);
    public SmoothValue offY = new SmoothValue(200);
    public SmoothValue offZ = new SmoothValue(200);
    
    public SmoothValue rotX = new SmoothValue(200);
    public SmoothValue rotY = new SmoothValue(200);
    public SmoothValue rotZ = new SmoothValue(200);
    public SmoothValue distance = new SmoothValue(200);
    
    public ViewerDragMode grabMode = ViewerDragMode.NONE;
    private ProjectionMode projection = ProjectionMode.SHOWCASE;
    public boolean initialized = false;
    public double grabX;
    public double grabY;
    public GuiAnimationViewerStorage storage;
    
    
    public GuiAnimationViewer(IGuiParent parent, String name, GuiAnimationViewerStorage storage) {
        super(parent, name);
        this.storage = storage;
    }
    
    public ProjectionMode projection() {
        return projection;
    }
    
    public void nextProjection() {
        setProjection(projection.next());
    }
    
    public void setProjection(ProjectionMode mode) {
        this.projection = mode;
        initialized = false;
    }
    
    public void resetView() {
        offX.set(0);
        offY.set(0);
        offZ.set(0);
        rotX.set(0);
        rotY.set(0);
        rotZ.set(0);
        initialized = false;
    }
    
    @Override
    public void closed() {}
    
    @Override
    public void init() {}
    
    @Override
    public void tick() {}
    
    public static enum ViewerDragMode {
        
        NONE,
        LEFT,
        RIGHT,
        MIDDLE;
        
        public static ViewerDragMode of(int button) {
            return switch (button) {
                case 0 -> LEFT;
                case 1 -> RIGHT;
                case 2 -> MIDDLE;
                default -> NONE;
            };
        }
        
    }
    
    public static enum ProjectionMode {
        
        SHOWCASE {
            
            @Override
            public void init(GuiAnimationViewer viewer) {
                viewer.distance.set(viewer.storage.longestSide() / 2D + 2);
            }
            
            @Override
            public void dragMouse(GuiAnimationViewer viewer, double x, double y) {
                double grabOffset = 0.01;
                switch (viewer.grabMode) {
                    case RIGHT -> {
                        Vector3f offset = new Vector3f((float) (x * grabOffset), 0, (float) (y * grabOffset));
                        apply(offset, viewer);
                    }
                    case MIDDLE -> {
                        Vector3f offset = new Vector3f((float) (x * grabOffset), (float) (y * -grabOffset), 0);
                        apply(offset, viewer);
                    }
                }
            }
            
            @Override
            public void forward(float amount, GuiAnimationViewer viewer) {
                viewer.rotX.add(amount);
            }
            
            @Override
            public void lateral(float amount, GuiAnimationViewer viewer) {
                viewer.rotY.add(-amount);
            }
            
            public void apply(Vector3f vec, GuiAnimationViewer viewer) {
                vec.rotate(Axis.XP.rotationDegrees((float) viewer.rotX.current()));
                vec.rotate(Axis.YP.rotationDegrees((float) viewer.rotY.current()));
                vec.rotate(Axis.ZP.rotationDegrees((float) viewer.rotZ.current()));
                viewer.offX.set(viewer.offX.aimed() + vec.x);
                viewer.offY.set(viewer.offY.aimed() + vec.y);
                viewer.offZ.set(viewer.offZ.aimed() + vec.z);
            }
            
            @Override
            public void prepareRendering(PoseStack pose, Vec3d center, GuiAnimationViewer viewer) {
                
                pose.translate(viewer.offX.current(), viewer.offY.current(), viewer.offZ.current() - viewer.distance.current());
                
                pose.mulPose(new Matrix4f().rotation(Axis.XP.rotationDegrees((float) viewer.rotX.current())));
                pose.mulPose(new Matrix4f().rotation(Axis.YP.rotationDegrees((float) viewer.rotY.current())));
                pose.mulPose(new Matrix4f().rotation(Axis.ZP.rotationDegrees((float) viewer.rotZ.current())));
                
                pose.translate(-center.x, -center.y, -center.z);
            }
            
            @Override
            public ProjectionMode next() {
                return PLAYER;
            }
            
            @Override
            public void up(float amount, GuiAnimationViewer viewer) {
                viewer.offZ.add(amount * 0.05F);
            }
        },
        PLAYER {
            
            @Override
            public void init(GuiAnimationViewer viewer) {
                viewer.offZ.set(viewer.storage.longestSide() / 2D);
                viewer.offY.set(-viewer.storage.overall().getSize());
                viewer.rotX.set(45);
                viewer.rotY.set(180);
            }
            
            @Override
            public void dragMouse(GuiAnimationViewer viewer, double x, double y) {}
            
            @Override
            public void forward(float amount, GuiAnimationViewer viewer) {
                amount *= 0.1;
                viewer.offX.add(amount * (Math.sin(-viewer.rotY.aimed() * Math.PI / 180)));
                viewer.offZ.add(amount * (Math.cos(viewer.rotY.aimed() * Math.PI / 180)));
            }
            
            @Override
            public void lateral(float amount, GuiAnimationViewer viewer) {
                amount *= -0.1;
                viewer.offX.add(amount * (Math.cos(-viewer.rotY.aimed() * Math.PI / 180)));
                viewer.offZ.add(amount * (Math.sin(viewer.rotY.aimed() * Math.PI / 180)));
            }
            
            @Override
            public void prepareRendering(PoseStack pose, Vec3d center, GuiAnimationViewer viewer) {
                pose.mulPose(new Matrix4f().rotation(Axis.XP.rotationDegrees((float) viewer.rotX.current())));
                pose.mulPose(new Matrix4f().rotation(Axis.YP.rotationDegrees((float) viewer.rotY.current())));
                pose.mulPose(new Matrix4f().rotation(Axis.ZP.rotationDegrees((float) viewer.rotZ.current())));
                
                pose.translate(viewer.offX.current(), viewer.offY.current(), viewer.offZ.current());
                pose.translate(-center.x, -center.y, -center.z);
                
            }
            
            @Override
            public ProjectionMode next() {
                return SHOWCASE;
            }
            
            @Override
            public void up(float amount, GuiAnimationViewer viewer) {
                viewer.offY.add(amount * -0.1);
            }
        };
        
        public abstract ProjectionMode next();
        
        public abstract void init(GuiAnimationViewer viewer);
        
        public abstract void prepareRendering(PoseStack pose, Vec3d center, GuiAnimationViewer viewer);
        
        public abstract void dragMouse(GuiAnimationViewer viewer, double x, double y);
        
        public abstract void forward(float amount, GuiAnimationViewer viewer);
        
        public abstract void lateral(float amount, GuiAnimationViewer viewer);
        
        public abstract void up(float amount, GuiAnimationViewer viewer);
        
    }
}
package team.creative.littletiles.common.gui.control.animation;



import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.littletiles.common.gui.AnimationPreview;

/** Data side of the structure previews. Drawing lives in {@code team.creative.littletiles.client.gui.animation.AnimationPreviewRenderer}. */
public interface GuiAnimationViewerStorage {
    
    public boolean isReady();
    
    public double longestSide();
    
    public AABB overall();
    
    public Vec3d center();
    
    public boolean highlightSelected();
    
    public void highlightSelected(boolean value);
    
    @OnlyIn(Dist.CLIENT)
    public Iterable<AnimationPreview> previewsToRender();
    
}
package team.creative.littletiles.client.gui.animation;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.Vec3;
import team.creative.creativecore.common.gui.control.tree.GuiTreeItem;
import team.creative.creativecore.common.util.math.box.ABB;
import team.creative.creativecore.common.util.math.box.BoxesVoxelShape;
import team.creative.littletiles.client.render.entity.LittleEntityRenderer;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.AnimationPreview;
import team.creative.littletiles.common.gui.control.animation.GuiAnimationViewerStorage;
import team.creative.littletiles.common.gui.tool.blueprint.GuiBlueprintAnimationStorage;
import team.creative.littletiles.common.gui.tool.blueprint.GuiTreeItemStructure;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.vec.LittleVecGrid;

/** What the structure previews draw (port of the 1.21 storage renderAll/renderItem). */
public class PreviewScenes {
    
    /** blocks submitted so far (diagnostics) */
    public static int submitted;
    
    public static float partialTick() {
        return Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
    }
    
    public static void submitPreview(AnimationPreview preview, PoseStack pose, SubmitNodeCollector collector) {
        if (preview == null || preview.animation == null || preview.animation.getSubLevel() == null)
            return;
        submitted += LittleEntityRenderer.submitStructure(preview.animation, pose, Vec3.ZERO, partialTick(), LightCoordsUtil.FULL_BRIGHT, collector);
    }
    
    public static void submitStorage(GuiAnimationViewerStorage storage, PoseStack pose, SubmitNodeCollector collector) {
        if (storage instanceof GuiBlueprintAnimationStorage blueprint) {
            submitItem(blueprint, blueprint.tree().root(), pose, collector);
            if (blueprint.highlightSelected() && blueprint.tree().selected() instanceof GuiTreeItemStructure selected)
                submitHighlight(selected, pose, collector);
            return;
        }
        for (AnimationPreview preview : storage.previewsToRender())
            submitPreview(preview, pose, collector);
    }
    
    private static void submitItem(GuiBlueprintAnimationStorage storage, GuiTreeItem item, PoseStack pose, SubmitNodeCollector collector) {
        pose.pushPose();
        if ((!item.tree.hasCheckboxes() || item.isChecked()) && item instanceof GuiTreeItemStructure s) {
            AnimationPreview preview = storage.get(s);
            if (preview == null) {
                pose.popPose();
                return;
            }
            s.prepareRendering(preview);
            LittleVecGrid offset = s.getOffset();
            if (offset != null)
                pose.translate(offset.getPosX(), offset.getPosY(), offset.getPosZ());
            submitPreview(preview, pose, collector);
        }
        for (GuiTreeItem child : item.items())
            submitItem(storage, child, pose, collector);
        pose.popPose();
    }
    
    private static void submitHighlight(GuiTreeItemStructure selected, PoseStack pose, SubmitNodeCollector collector) {
        LittleVecGrid offset = selected.getOverallOffest();
        PoseStack at = copy(pose);
        if (offset != null)
            at.translate(offset.getPosX(), offset.getPosY(), offset.getPosZ());
        LittleGrid grid = selected.group.getGrid();
        List<ABB> boxes = new ArrayList<>();
        for (LittleBox box : selected.group.allBoxes())
            boxes.add(box.getABB(grid));
        if (!boxes.isEmpty())
            collector.submitShapeOutline(at, BoxesVoxelShape.create(boxes), RenderTypes.lines(), 0xFFFFFFFF, 2.0F, false);
    }
    
    public static PoseStack copy(PoseStack pose) {
        PoseStack copy = new PoseStack();
        copy.last().pose().set(pose.last().pose());
        copy.last().normal().set(pose.last().normal());
        return copy;
    }
    
}

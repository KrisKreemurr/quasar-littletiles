package team.creative.littletiles.common.gui.tool.blueprint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.control.tree.GuiTree;
import team.creative.creativecore.common.gui.control.tree.GuiTreeItem;
import team.creative.creativecore.common.util.math.vec.SmoothValue;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.AnimationPreview;
import team.creative.littletiles.common.gui.control.animation.GuiAnimationViewerStorage;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.math.box.collection.LittleBoxesNoOverlap;

public class GuiBlueprintAnimationStorage implements Iterable<Entry<GuiTreeItemStructure, AnimationPreview>>, GuiAnimationViewerStorage {
    
    private boolean highlightSelected = false;
    
    private final GuiTree tree;
    private LinkedHashMap<GuiTreeItemStructure, AnimationPreview> availablePreviews = new LinkedHashMap<>();
    private LittleBoxesNoOverlap overlappingBoxes = null;
    private ConcurrentLinkedQueue<AnimationPair> change = new ConcurrentLinkedQueue<>();
    
    private AABB overall = null;
    private SmoothValue offX = new SmoothValue(200);
    private SmoothValue offY = new SmoothValue(200);
    private SmoothValue offZ = new SmoothValue(200);
    
    private boolean unloaded = false;
    
    public GuiBlueprintAnimationStorage(GuiTree tree) {
        this.tree = tree;
    }
    
    @Override
    public boolean highlightSelected() {
        return highlightSelected;
    }
    
    @Override
    public void highlightSelected(boolean value) {
        this.highlightSelected = value;
    }
    
    @Override
    public boolean isReady() {
        return overall != null && !availablePreviews.isEmpty();
    }
    
    public void resetOverlap() {
        overlappingBoxes = null;
    }
    
    public boolean hasOverlap() {
        return overlappingBoxes != null && !overlappingBoxes.isEmpty();
    }
    
    public LittleBoxesNoOverlap getOverlap() {
        return overlappingBoxes;
    }
    
    public void addOverlap(LittleBoxes boxes) {
        if (overlappingBoxes == null)
            overlappingBoxes = new LittleBoxesNoOverlap(BlockPos.ZERO, LittleGrid.MIN);
        if (boxes instanceof LittleBoxesNoOverlap no)
            for (Entry<BlockPos, ArrayList<LittleBox>> entry : no.generateBlockWise().entrySet())
                overlappingBoxes.addBoxes(boxes.getGrid(), entry.getKey(), entry.getValue());
        else
            for (LittleBox box : boxes.all())
                overlappingBoxes.addBox(boxes.getGrid(), boxes.pos, box.copy());
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public Iterable<AnimationPreview> previewsToRender() {
        return Collections.EMPTY_LIST;
    }
    
    // TODO(quasar): renderItem/renderAll (tree of previews + selection outline + overlap boxes, GL immediate lines) move to the client preview renderer
    // with the render rewrite; see upstream 1.21 GuiBlueprintAnimationStorage.renderAll.
    
    private void updateBox() {
        if (availablePreviews.isEmpty()) {
            overall = null;
            return;
        }
        
        boolean init = overall == null;
        
        overall = null;
        for (AnimationPreview preview : availablePreviews.values())
            if (overall == null)
                overall = preview.box;
            else
                overall = overall.minmax(preview.box);
            
        Vec3 center = overall.getCenter();
        if (init) {
            offX.setStart(center.x);
            offY.setStart(center.y);
            offZ.setStart(center.z);
        } else {
            offX.set(center.x);
            offY.set(center.y);
            offZ.set(center.z);
        }
    }
    
    @Override
    public Vec3d center() {
        return new Vec3d(offX.current(), offY.current(), offZ.current());
    }
    
    @Override
    public double longestSide() {
        return Math.max(overall.maxX - overall.minX, Math.max(overall.maxY - overall.minY, overall.maxZ - overall.minZ));
    }
    
    @Override
    public AABB overall() {
        return overall;
    }
    
    public boolean isReady(GuiTreeItemStructure structure) {
        return availablePreviews.containsKey(structure);
    }
    
    @OnlyIn(Dist.CLIENT)
    public void renderItemAndChildren(PoseStack pose, Matrix4f projection, Minecraft mc, GuiTreeItemStructure structure) {
        int[][] pixels = GuiAnimationViewerStorage.makeLightBright();
        renderItemAndChildrenInternal(pose, projection, mc, structure);
        GuiAnimationViewerStorage.resetLight(pixels);
    }
    
    @OnlyIn(Dist.CLIENT)
    protected void renderItemAndChildrenInternal(PoseStack pose, Matrix4f projection, Minecraft mc, GuiTreeItemStructure structure) {
        AnimationPreview preview = availablePreviews.get(structure);
        if (preview != null)
            renderPreview(pose, projection, preview, mc);
        for (GuiTreeItem item : structure.items())
            renderItemAndChildrenInternal(pose, projection, mc, (GuiTreeItemStructure) item);
    }
    
    public AnimationPreview get(GuiTreeItemStructure structure) {
        return availablePreviews.get(structure);
    }
    
    protected void remove(GuiTreeItemStructure structure) {
        AnimationPreview removed = availablePreviews.remove(structure);
        if (removed != null)
            removed.unload();
        updateBox();
    }
    
    protected void put(GuiTreeItemStructure structure, AnimationPreview preview) {
        AnimationPreview previous = availablePreviews.put(structure, preview);
        if (previous != null)
            previous.unload();
        if (unloaded)
            preview.unload();
        updateBox();
    }
    
    public void removed(GuiTreeItemStructure structure) {
        if (RenderSystem.isOnRenderThread())
            remove(structure);
        else
            change.add(new AnimationPair(structure, null));
    }
    
    public void completed(GuiTreeItemStructure structure, AnimationPreview preview) {
        if (RenderSystem.isOnRenderThread())
            put(structure, preview);
        else
            change.add(new AnimationPair(structure, preview));
    }
    
    public void renderTick() {
        offX.tick();
        offY.tick();
        offZ.tick();
        
        if (!change.isEmpty()) {
            AnimationPair pair;
            while ((pair = change.poll()) != null) {
                if (pair.preview == null)
                    remove(pair.item);
                else
                    put(pair.item, pair.preview);
            }
            updateBox();
        }
    }
    
    public void tick() {
        for (AnimationPreview preview : availablePreviews.values())
            preview.tick();
    }
    
    @Override
    public Iterator<Entry<GuiTreeItemStructure, AnimationPreview>> iterator() {
        return availablePreviews.entrySet().iterator();
    }
    
    public void unload() {
        unloaded = true;
        
        for (AnimationPair pair : change)
            if (pair.preview != null)
                pair.preview.unload();
            
        for (AnimationPreview preview : availablePreviews.values())
            preview.unload();
    }
    
    private static record AnimationPair(GuiTreeItemStructure item, AnimationPreview preview) {}
}

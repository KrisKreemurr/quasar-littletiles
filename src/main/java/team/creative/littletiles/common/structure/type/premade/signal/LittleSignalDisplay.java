package team.creative.littletiles.common.structure.type.premade.signal;




import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.vec.Vec3f;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.type.map.ChunkLayerMapList;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.client.render.tile.LittleRenderBox;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.SurroundingBox;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.structure.directional.StructureDirectional;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.structure.relative.StructureRelative;
import team.creative.littletiles.common.structure.signal.output.InternalSignalOutput;
import team.creative.littletiles.common.structure.type.premade.LittleStructurePremade;

public class LittleSignalDisplay extends LittleStructurePremade {
    
    public static final int RENDER_DISTANCE = 64;
    
    @StructureDirectional(color = ColorUtils.CYAN)
    public StructureRelative frame;
    
    @StructureDirectional
    public Facing facing;
    
    @StructureDirectional
    public Vec3f topRight;
    
    public int color = ColorUtils.WHITE;
    
    private int textureId = -1;
    
    public LittleSignalDisplay(LittlePremadeType type, IStructureParentCollection mainBlock) {
        super(type, mainBlock);
    }
    
    @Override
    protected void loadExtra(CompoundTag nbt, HolderLookup.Provider provider) {
        if (nbt.contains("color"))
            color = nbt.getIntOr("color", 0);
        else
            color = ColorUtils.WHITE;
    }
    
    @Override
    protected void saveExtra(CompoundTag nbt, HolderLookup.Provider provider) {
        if (color != -1)
            nbt.putInt("color", color);
        else
            nbt.remove("color");
    }
    
    @Override
    public void receiveInternalOutputChange(InternalSignalOutput output) {
        if (output.component.is("pixels") && isClient()) {
            // TODO(quasar): updateTexture() parked for the render rewrite
        }
    }
    
    // TODO(quasar): signal display texture upload + renderTick (GL texture quad) parked for the render rewrite.
    
    @Override
    public boolean hasStructureColor() {
        return true;
    }
    
    @Override
    public int getStructureColor() {
        return color;
    }
    
    @Override
    public int getDefaultColor() {
        return ColorUtils.WHITE;
    }
    
    @Override
    public void paint(int color) {
        this.color = color;
        if (isClient()) {
            // TODO(quasar): updateTexture() parked for the render rewrite
        }
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public double getMaxRenderDistance() {
        return RENDER_DISTANCE;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public AABB getRenderBoundingBox() {
        return frame.getBox().getBB(frame.getGrid());
    }
    
    @Override
    public void unload() {
        super.unload();
        // TODO(quasar): free the display texture once the render rewrite uploads one again
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void getRenderingBoxes(BlockPos pos, ChunkLayerMapList<LittleRenderBox> cubes) {
        if (ColorUtils.isInvisible(color))
            return;
        
        int color = this.color == ColorUtils.WHITE ? LittleSignalCableBase.DEFAULT_CABLE_COLOR : this.color;
        ChunkSectionLayer layer = ColorUtils.isTransparent(color) ? ChunkSectionLayer.TRANSLUCENT : ChunkSectionLayer.SOLID;
        
        try {
            SurroundingBox box = getSurroundingBox();
            LittleVec min = box.getMinPosOffset();
            LittleVec max = box.getSize();
            max.add(min);
            LittleBox overallBox = new LittleBox(min, max);
            BlockPos difference = pos.subtract(box.getMinPos());
            overallBox.sub(box.getGrid().toGrid(difference.getX()), box.getGrid().toGrid(difference.getY()), box.getGrid().toGrid(difference.getZ()));
            
            LittleRenderBox block = new LittleRenderBox(box.getGrid(), overallBox, LittleTilesRegistry.CLEAN.value().defaultBlockState()).setColor(color);
            block.allowOverlap = true;
            cubes.add(layer, block);
        } catch (CorruptedConnectionException | NotYetConnectedException e) {}
    }
    
}

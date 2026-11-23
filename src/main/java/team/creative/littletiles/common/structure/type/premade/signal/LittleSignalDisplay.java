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
    
    /** Quasar MC (26.3): the display's pixels, drawn every frame by {@link team.creative.littletiles.client.render.level.SignalDisplayRenderer}
     * (one coloured square per signal bit instead of the old GL texture). */
    @OnlyIn(Dist.CLIENT)
    public void quasar$renderPixels(java.util.function.BiConsumer<net.minecraft.world.phys.Vec3[], Integer> out) {
        if (!hasLevel() || frame == null || facing == null || topRight == null)
            return;
        var output = getOutput(0);
        if (output == null)
            return;
        int bandwidth = output.getBandwidth();
        int size = (int) Math.sqrt(bandwidth);
        if (size <= 0)
            return;
        var state = output.getState();
        team.creative.creativecore.common.util.math.box.AlignedBox box = frame.getBox().getBox(frame.getGrid());
        var face = team.creative.creativecore.common.util.math.box.BoxFace.get(facing);
        var uAxis = face.getTexUAxis();
        var vAxis = face.getTexVAxis();
        var n = facing.axis;
        float plane = facing.positive ? box.getMin(n) + 0.005F : box.getMax(n) - 0.005F;
        // Same orientation as the old texture: u/v = 1 on the side away from topRight.
        boolean uFlip = topRight.get(uAxis) > 0, vFlip = topRight.get(vAxis) > 0;
        float u0 = box.getMin(uAxis), u1 = box.getMax(uAxis), v0 = box.getMin(vAxis), v1 = box.getMax(vAxis);
        BlockPos pos = getStructurePos();
        int alpha = ColorUtils.alpha(color);
        int on = color == -1 ? 0xFFFFFFFF : color;
        int off = (alpha << 24);
        for (int i = 0; i < size * size; i++) {
            int col = i % size, row = i / size;
            float ua = col / (float) size, ub = (col + 1) / (float) size;
            float va = row / (float) size, vb = (row + 1) / (float) size;
            float pu0 = uFlip ? u1 - ub * (u1 - u0) : u0 + ua * (u1 - u0);
            float pu1 = uFlip ? u1 - ua * (u1 - u0) : u0 + ub * (u1 - u0);
            float pv0 = vFlip ? v1 - vb * (v1 - v0) : v0 + va * (v1 - v0);
            float pv1 = vFlip ? v1 - va * (v1 - v0) : v0 + vb * (v1 - v0);
            net.minecraft.world.phys.Vec3[] quad = { point(pos, n, plane, uAxis, pu0, vAxis, pv0), point(pos, n, plane, uAxis, pu1, vAxis, pv0), point(pos, n, plane, uAxis,
                pu1, vAxis, pv1), point(pos, n, plane, uAxis, pu0, vAxis, pv1) };
            out.accept(quad, state != null && state.is(i) ? on : off);
        }
    }

    private static net.minecraft.world.phys.Vec3 point(BlockPos pos, team.creative.creativecore.common.util.math.base.Axis n, float plane,
            team.creative.creativecore.common.util.math.base.Axis u, float uv, team.creative.creativecore.common.util.math.base.Axis v, float vv) {
        double[] c = new double[3];
        c[n.ordinal()] = plane;
        c[u.ordinal()] = uv;
        c[v.ordinal()] = vv;
        return new net.minecraft.world.phys.Vec3(pos.getX() + c[0], pos.getY() + c[1], pos.getZ() + c[2]);
    }
    
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
        team.creative.littletiles.client.render.level.SignalDisplayRenderer.track(this);
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

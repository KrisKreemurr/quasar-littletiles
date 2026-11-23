package team.creative.littletiles.client.render.tile;

import team.creative.creativecore.common.util.math.vec.Vec3f;
import team.creative.creativecore.common.util.math.geo.VectorFan;
import team.creative.creativecore.common.util.math.base.Facing;
import java.util.List;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.common.util.math.box.AlignedBox;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;

public class LittleRenderBox extends RenderBox {
    
    public LittleBox box;
    
    public LittleRenderBox(AlignedBox box) {
        super(box);
    }
    
    public LittleRenderBox(AlignedBox box, BlockState state) {
        super(box, state);
    }
    
    public LittleRenderBox(LittleGrid grid, LittleBox box) {
        super(grid.toVanillaGridF(box.minX), grid.toVanillaGridF(box.minY), grid.toVanillaGridF(box.minZ), grid.toVanillaGridF(box.maxX), grid.toVanillaGridF(box.maxY), grid
                .toVanillaGridF(box.maxZ), (BlockState) null);
        this.color = ColorUtils.WHITE;
        this.box = box;
    }
    
    public LittleRenderBox(LittleGrid grid, LittleBox box, BlockState state) {
        super(grid.toVanillaGridF(box.minX), grid.toVanillaGridF(box.minY), grid.toVanillaGridF(box.minZ), grid.toVanillaGridF(box.maxX), grid.toVanillaGridF(box.maxY), grid
                .toVanillaGridF(box.maxZ), state);
        this.color = ColorUtils.WHITE;
        this.box = box;
    }
    
    public LittleRenderBox(LittleGrid grid, LittleBox box, LittleElement element) {
        super(grid.toVanillaGridF(box.minX), grid.toVanillaGridF(box.minY), grid.toVanillaGridF(box.minZ), grid.toVanillaGridF(box.maxX), grid.toVanillaGridF(box.maxY), grid
                .toVanillaGridF(box.maxZ), element.getState());
        this.color = element.color;
        this.box = box;
    }
    
    @Override
    public LittleRenderBox setColor(int color) {
        return (LittleRenderBox) super.setColor(color);
    }
    

    /** Quasar MC: the polygons of one face in block-local coordinates (0..1), as the old quad cutter used them.
     * Plain boxes give one rectangle; partial faces and sloped (transformable) boxes give one or more fans. */
    public void quasar$collectFacePolygons(Facing facing, List<Vec3f[]> out) {
        if (!shouldRenderFace(facing))
            return;
        Object quads = getRenderQuads(facing);
        if (quads == null)
            return;
        boolean noOffset = onlyScaleOnceNoOffset(facing);
        float overall = getOverallScale(facing);
        if (quads instanceof List<?> list) {
            for (Object o : list)
                if (o instanceof VectorFan fan)
                    out.add(quasar$toBlockSpace(fan, noOffset, overall));
        } else if (quads instanceof VectorFan fan)
            out.add(quasar$toBlockSpace(fan, noOffset, overall));
    }
    
    private Vec3f[] quasar$toBlockSpace(VectorFan fan, boolean noOffset, float overall) {
        Vec3f[] coords = fan.getCoords();
        Vec3f[] result = new Vec3f[coords.length];
        for (int i = 0; i < coords.length; i++) {
            Vec3f c = coords[i];
            if (noOffset)
                result[i] = new Vec3f(c.x * overall, c.y * overall, c.z * overall);
            else
                result[i] = new Vec3f(c.x * getScaleX() + getOffsetX(), c.y * getScaleY() + getOffsetY(), c.z * getScaleZ() + getOffsetZ());
        }
        return result;
    }
    
}

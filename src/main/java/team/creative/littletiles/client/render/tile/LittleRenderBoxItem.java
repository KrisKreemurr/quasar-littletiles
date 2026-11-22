package team.creative.littletiles.client.render.tile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import team.creative.creativecore.client.render.VertexFormatUtils;
import team.creative.creativecore.client.render.box.QuadGeneratorContext;
import team.creative.creativecore.client.render.model.CreativeBakedQuad;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.creativecore.common.util.math.vec.Vec3f;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.structure.type.LittleItemHolder;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

public class LittleRenderBoxItem extends LittleRenderBox {

    private static Vec3f defaultDirection;
    private static Vec3f center;
    public LittleItemHolder structure;
    public LittleRenderBoxItem(LittleItemHolder structure, AlignedBox cube, LittleBox box) {}

    public List<BakedQuad> getBakedQuad(@Nullable QuadGeneratorContext holder, LevelAccessor level, BlockPos pos, BlockPos offset, BlockState state, BakedModel blockModel,
            ModelData modelData, Facing facing, ChunkSectionLayer layer, RandomSource rand, boolean overrideTint, int defaultColor) { return null; }

    public boolean intersectsWithFace(Facing facing, QuadGeneratorContext holder, BlockPos offset) { return false; }

}


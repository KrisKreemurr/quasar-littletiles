package team.creative.littletiles.client.render.block;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.littletiles.api.common.block.LittleBlock;

/** Which chunk layers (solid / cutout / translucent) a LittleTiles block draws into.
 * <p>
 * Quasar MC (26.3): 26.x has no per-block render type any more; each baked quad carries its own layer. So a block's layers are
 * the layers of its model's quads (e.g. grass = solid + cutout overlay, glass = cutout, stained glass = translucent). */
public class LittleBlockClientRegistry {
    
    private static final Map<LittleBlock, EnumSet<ChunkSectionLayer>> CACHED_LAYERS = new HashMap<>();
    private static final Map<LittleBlock, Boolean> GUI_TRANSLUCENT = new HashMap<>();
    private static final Direction[] SIDES_AND_NULL = { Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, null };
    
    public static synchronized boolean canRenderInLayer(LittleBlock block, ChunkSectionLayer layer) {
        return CACHED_LAYERS.computeIfAbsent(block, LittleBlockClientRegistry::computeLayers).contains(layer);
    }
    
    private static EnumSet<ChunkSectionLayer> computeLayers(LittleBlock block) {
        if (!block.shouldUseStateForRenderType())
            return EnumSet.of(ChunkSectionLayer.SOLID);
        EnumSet<ChunkSectionLayer> layers = EnumSet.noneOf(ChunkSectionLayer.class);
        BlockState state = block.getState();
        BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state);
        List<BlockStateModelPart> parts = new ArrayList<>();
        model.collectParts(RandomSource.create(42L), parts);
        for (BlockStateModelPart part : parts)
            for (Direction dir : SIDES_AND_NULL)
                for (BakedQuad quad : part.getQuads(dir))
                    layers.add(quad.materialInfo().layer());
        if (layers.isEmpty())
            layers.add(ChunkSectionLayer.SOLID);
        return layers;
    }
    
    public static synchronized boolean isTranslucentInGui(LittleBlock block) {
        return GUI_TRANSLUCENT.computeIfAbsent(block, b -> !b.shouldUseStateForRenderType() || canRenderInLayer(b, ChunkSectionLayer.TRANSLUCENT));
    }
    
    /** Call after a resource reload (models may have changed). */
    public static synchronized void clearCache() {
        CACHED_LAYERS.clear();
        GUI_TRANSLUCENT.clear();
    }
    
}

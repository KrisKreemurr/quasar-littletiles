package team.creative.littletiles.client.render.block;

import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.RenderTypeHelper;
import net.neoforged.neoforge.client.model.data.ModelData;
import team.creative.littletiles.api.common.block.ILittleMCBlock;
import team.creative.littletiles.api.common.block.LittleBlock;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

public class LittleBlockClientRegistry {

    private static ChunkRenderTypeSet SOLID;
    private static Map<LittleBlock, ChunkRenderTypeSet> CACHED_LAYERS;
    private static Map<LittleBlock, Boolean> GUI_TRANSLUCENT;
    public static synchronized boolean canRenderInLayer(LittleBlock block, RenderType layer) { return false; }

    public static synchronized boolean isTranslucentInGui(LittleBlock block) { return false; }

    public static synchronized void clearCache() {}

}


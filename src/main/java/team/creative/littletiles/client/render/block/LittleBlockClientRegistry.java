package team.creative.littletiles.client.render.block;

import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import team.creative.littletiles.api.common.block.ILittleMCBlock;
import team.creative.littletiles.api.common.block.LittleBlock;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

public class LittleBlockClientRegistry {

    private static Map<LittleBlock, Boolean> GUI_TRANSLUCENT;

    public static synchronized boolean isTranslucentInGui(LittleBlock block) { return false; }

    /** Quasar placeholder: everything opaque goes to SOLID until the render rewrite asks the block model for its layer. */
    public static synchronized boolean canRenderInLayer(LittleBlock block, ChunkSectionLayer layer) { return layer == ChunkSectionLayer.SOLID; }

    public static synchronized void clearCache() {}

}


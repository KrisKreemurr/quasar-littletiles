package team.creative.littletiles.client.render.cache.pipeline;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import team.creative.creativecore.common.util.type.list.SingletonList;
import team.creative.creativecore.common.util.type.map.ChunkLayerMap;
import team.creative.littletiles.client.render.cache.buffer.BufferCache;
import team.creative.littletiles.client.render.cache.build.RenderingBlockContext;
import team.creative.littletiles.client.render.cache.build.RenderingThread.RenderingException;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

public abstract class LittleRenderPipeline {

    public static Minecraft MC;

    public abstract void reload();

    public abstract void release();

}


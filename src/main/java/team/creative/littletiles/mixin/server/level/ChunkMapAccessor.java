package team.creative.littletiles.mixin.server.level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;

@Mixin(ChunkMap.class)
public interface ChunkMapAccessor {
    
    /** 26.3: getChunks() is gone; the visible holder map is the same set. */
    @Accessor("visibleChunkMap")
    public it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap<ChunkHolder> getVisibleChunkMap();
    
}

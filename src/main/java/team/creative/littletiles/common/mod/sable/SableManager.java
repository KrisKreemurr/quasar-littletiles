package team.creative.littletiles.common.mod.sable;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.ModList;
import team.creative.creativecore.common.util.math.origin.IVecOrigin;
import team.creative.littletiles.common.level.context.ILittleLevelContext;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;

public class SableManager {
    
    public static final String MODID = "sable";
    // Quasar MC: Sable isn't on 26.3, so its compat is left out of the build; every call takes the vanilla path.
    public static final boolean INSTALLED = false;
    
    public static @Nullable ILittleLevelContext context(LevelAccessor level, BlockPos pos) {
        if (INSTALLED)
            throw new IllegalStateException("Sable compat is not built");
        return ILittleLevelContext.STANDARD;
    }
    
    public static @Nullable ILittleLevelContext context(LevelAccessor level, ChunkPos pos) {
        if (INSTALLED)
            throw new IllegalStateException("Sable compat is not built");
        return ILittleLevelContext.STANDARD;
    }
    
    public static @Nullable ILittleLevelContext context(LevelAccessor level, Vec3 pos) {
        if (INSTALLED)
            throw new IllegalStateException("Sable compat is not built");
        return ILittleLevelContext.STANDARD;
    }
    
    public static boolean isSubLevel(LevelAccessor level, BlockPos pos) {
        if (INSTALLED)
            throw new IllegalStateException("Sable compat is not built");
        return false;
    }
    
    @OnlyIn(Dist.CLIENT)
    public static @Nullable RenderChunkExtender getRenderChunk(Level level, long pos) {
        return null;
    }
    
    @OnlyIn(Dist.CLIENT)
    public static void markDirty(LevelAccessor level, BlockPos pos) {
    }
    
    public static @Nullable IVecOrigin originWrapper(LevelAccessor level, Vec3 pos, IVecOrigin origin) {
        if (INSTALLED)
            throw new IllegalStateException("Sable compat is not built");
        return origin;
    }
    
}

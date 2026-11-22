package team.creative.littletiles.mixin.common.block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.littletiles.api.common.block.LittleBlock;
import team.creative.littletiles.common.block.little.registry.LittleBlockProvider;
import team.creative.littletiles.common.block.little.registry.LittleBlockRegistry;
import team.creative.littletiles.common.block.mc.BlockTile;

@Mixin(Block.class)
public class BlockMixin implements LittleBlockProvider {
    
    @Unique
    private boolean specialBlock;
    
    @Unique
    private LittleBlock block;
    
    private void checkCache() {
        if (block == null)
            LittleBlockRegistry.calculateCache((Block) (Object) this);
    }
    
    @Override
    public LittleBlock getLittleBlock() {
        checkCache();
        return block;
    }
    
    @Override
    public boolean isSpecialBlock() {
        checkCache();
        return specialBlock;
    }
    
    @Override
    public void setCache(LittleBlock block, boolean special) {
        this.block = block;
        this.specialBlock = special;
    }
    
    /** 26.x: shouldRenderFace(level, pos, state, neighborState, direction). A LittleTiles neighbour decides itself whether it hides the face. */
    @Inject(method = "shouldRenderFace(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)Z",
            at = @At("HEAD"), require = 1, cancellable = true)
    private static void shouldRenderFace(BlockGetter level, BlockPos pos, BlockState state, BlockState neighborState, Direction direction, CallbackInfoReturnable<Boolean> info) {
        if (neighborState.getBlock() instanceof BlockTile b)
            info.setReturnValue(!b.hidesNeighborFace(level, pos.relative(direction), neighborState, state, direction.getOpposite()));
    }
}

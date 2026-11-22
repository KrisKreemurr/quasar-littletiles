package team.creative.littletiles.mixin.common.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import team.creative.littletiles.common.structure.type.bed.ILittleBedPlayerExtension;

@Mixin(LivingEntity.class)
public abstract class LivingEntityBedMixin extends Entity {
    
    public LivingEntityBedMixin(EntityType<?> type, Level level) {
        super(type, level);
    }
    
    @Inject(at = @At("HEAD"), method = "setPosToBed(Lnet/minecraft/core/BlockPos;)Z", cancellable = true, require = 1)
    private void setPosToBed(BlockPos pos, CallbackInfoReturnable<Boolean> info) {
        if (this instanceof ILittleBedPlayerExtension b && b.setPositionToBed())
            info.setReturnValue(true);
    }
    
}

package team.creative.littletiles.mixin.common.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;

@Mixin(PrimedTnt.class)
public interface PrimedTntAccessor {
    
    // 26.3: PrimedTnt stores its owner as an EntityReference (PrimedTnt.getOwner() is public for reading).
    @Accessor("owner")
    public void setOwnerReference(EntityReference<LivingEntity> owner);
    
}

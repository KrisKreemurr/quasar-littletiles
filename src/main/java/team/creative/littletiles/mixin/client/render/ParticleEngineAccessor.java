package team.creative.littletiles.mixin.client.render;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.resources.Identifier;

@Mixin(ParticleEngine.class)
public interface ParticleEngineAccessor {
    
    /** 26.3: sprite sets moved into ParticleResources (see ParticleResourcesAccessor). */
    @Accessor("resourceManager")
    public net.minecraft.client.particle.ParticleResources getResources();
    
}

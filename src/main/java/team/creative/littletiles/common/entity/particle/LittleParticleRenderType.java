package team.creative.littletiles.common.entity.particle;

import net.minecraft.client.particle.SingleQuadParticle;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public enum LittleParticleRenderType {
    
    TERRAIN_SHEET,
    PARTICLE_SHEET_OPAQUE,
    PARTICLE_SHEET_TRANSLUCENT,
    PARTICLE_SHEET_LIT,
    CUSTOM,
    NO_RENDER;
    
    /** 26.x: single-quad particles pick an atlas/pipeline layer; there is no separate "lit" or custom sheet any more. */
    @OnlyIn(Dist.CLIENT)
    public SingleQuadParticle.Layer toLayer() {
        return switch (this) {
            case TERRAIN_SHEET -> SingleQuadParticle.Layer.OPAQUE_TERRAIN;
            case PARTICLE_SHEET_TRANSLUCENT -> SingleQuadParticle.Layer.TRANSLUCENT;
            default -> SingleQuadParticle.Layer.OPAQUE;
        };
    }
    
    public boolean rendered() {
        return this != NO_RENDER;
    }
    
}

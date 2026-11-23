package team.creative.littletiles.common.packet.structure;

import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.littletiles.client.render.level.transition.LittleLevelTransitionManagerClient;
import team.creative.littletiles.common.entity.animation.LittleAnimationEntity;
import team.creative.littletiles.common.packet.entity.LittleEntityPacket;

public class StructureEntityToBlockPacket extends LittleEntityPacket<LittleAnimationEntity> {
    
    public StructureEntityToBlockPacket() {}
    
    public StructureEntityToBlockPacket(LittleAnimationEntity entity) {
        super(entity);
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void execute(Player player, LittleAnimationEntity entity) {
        requiresClient(player);
        // Quasar MC (26.3): 1.21 handed the animation's compiled chunk buffers back to the world here. Both are now meshed straight from their
        // blocks (LittleChunkGeometry / LittleEntityRenderer), so there is nothing to hand over.
    }
    
}

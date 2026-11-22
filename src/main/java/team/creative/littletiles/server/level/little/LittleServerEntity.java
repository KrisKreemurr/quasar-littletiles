package team.creative.littletiles.server.level.little;

import java.util.function.Predicate;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.UpdateInterval;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class LittleServerEntity extends ServerEntity {
    
    public final BiConsumer<ServerPlayer, Packet<?>> singleBroadcast;
    
    public LittleServerEntity(ServerLevel level, Entity entity, int updateInterval, boolean trackDelta, Consumer<Packet<?>> broadcast, BiConsumer<ServerPlayer, Packet<?>> singleBroadcast) {
        super(level, entity, UpdateInterval.periodic(updateInterval), trackDelta, new Synchronizer() {
            // 26.3: ServerEntity talks to a Synchronizer; LittleTiles routes all of it through its broadcast callback
            @Override
            public void sendToTrackingPlayers(Packet<? super ClientGamePacketListener> packet) {
                broadcast.accept(packet);
            }
            
            @Override
            public void sendToTrackingPlayersAndSelf(Packet<? super ClientGamePacketListener> packet) {
                broadcast.accept(packet);
            }
            
            @Override
            public void sendToTrackingPlayersFiltered(Packet<? super ClientGamePacketListener> packet, Predicate<ServerPlayer> filter) {
                broadcast.accept(packet);
            }
        });
        this.singleBroadcast = singleBroadcast;
    }
    
    public void sendPacket(ServerPlayer player, Packet packet) {
        singleBroadcast.accept(player, packet);
    }
}

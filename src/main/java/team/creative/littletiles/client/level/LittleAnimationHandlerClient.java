package team.creative.littletiles.client.level;

import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import team.creative.creativecore.common.util.type.itr.FilterIterator;
import team.creative.littletiles.client.render.level.transition.LittleLevelTransitionManagerClient;
import team.creative.littletiles.common.entity.LittleEntity;
import team.creative.littletiles.common.level.handler.LittleAnimationHandler;
import team.creative.littletiles.api.client.entity.LevelTransitionListener;
import team.creative.littletiles.mixin.common.entity.EntityAccessor;

/**
 * Client side of animated structures: level transitions of entities and ticking.
 * <p>
 * QUASAR: the 1.21 version also ran a section-compile thread and drew the sub-levels from render events; that part is parked
 * (src/parked/.../LittleAnimationHandlerClient.java) until the render rewrite. Animated structures tick and move, but aren't drawn yet.
 */
public class LittleAnimationHandlerClient extends LittleAnimationHandler implements Iterable<LittleEntity> {
    
    private static final int LONG_TICK_INTERVAL = 40;
    public static final int MAX_INTERVALS_WAITING = 2;
    
    private final HashMap<UUID, EntityTransitionHolder> transitions = new HashMap<>();
    private int longTickCounter = LONG_TICK_INTERVAL;
    public int longTickIndex = Integer.MIN_VALUE;
    
    public LittleAnimationHandlerClient(Level level) {
        super(level);
    }
    
    public boolean checkInTransition(Entity entity) {
        if (transitions.containsKey(entity.getUUID())) {
            ((EntityAccessor) entity).callUnsetRemoved();
            return true;
        }
        return false;
    }
    
    public Entity pollEntityInTransition(ClientboundAddEntityPacket packet) {
        EntityTransitionHolder holder = transitions.get(packet.getUUID());
        if (holder == null)
            return null;
        
        Entity entity = holder.entity;
        Level oldLevel = entity.level();
        if (entity instanceof LevelTransitionListener listener)
            listener.prepareChangeLevel(oldLevel, holder.newLevel);
        
        ((EntityAccessor) entity).callSetLevel(holder.newLevel);
        transitions.remove(packet.getUUID());
        if (entity instanceof LevelTransitionListener listener)
            listener.changedLevel(oldLevel, holder.newLevel);
        return entity;
    }
    
    public void queueEntityForTransition(Entity entity, Level newLevel) {
        transitions.put(entity.getUUID(), new EntityTransitionHolder(entity, newLevel, longTickIndex + MAX_INTERVALS_WAITING));
    }
    
    @Override
    public synchronized Iterator<LittleEntity> iterator() {
        return new FilterIterator<>(entities, x -> x.hasLoaded());
    }
    
    protected void longTick() {
        if (!transitions.isEmpty())
            for (Iterator<EntityTransitionHolder> iterator = transitions.values().iterator(); iterator.hasNext();) {
                EntityTransitionHolder holder = iterator.next();
                if (longTickIndex >= holder.index)
                    iterator.remove();
            }
        LittleLevelTransitionManagerClient.longTick(longTickIndex);
    }
    
    @Override
    public void tick() {
        super.tick();
        
        longTickCounter--;
        if (longTickCounter <= 0) {
            longTickCounter = LONG_TICK_INTERVAL;
            longTick();
            longTickIndex++;
        }
    }
    
    @Override
    public void unload() {
        super.unload();
        transitions.clear();
        LittleLevelTransitionManagerClient.unload();
    }
    
    @Override
    protected void tickEntity(LittleEntity entity) {
        if (!entity.hasLoaded())
            return;
        super.tickEntity(entity);
    }
    
    public static record EntityTransitionHolder(Entity entity, Level newLevel, int index) {}
    
}

package team.creative.littletiles.client.player;

import java.util.BitSet;
import java.util.Iterator;

import javax.annotation.Nullable;

import org.apache.logging.log4j.Logger;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.inventory.CommandBlockEditScreen;
import net.minecraft.client.gui.screens.inventory.HorseInventoryScreen;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.particle.ItemPickupParticle;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.resources.sounds.GuardianAttackSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ClientboundCustomReportDetailsPacket;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.common.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import net.minecraft.network.protocol.common.ClientboundResourcePackPopPacket;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.network.protocol.common.ClientboundServerLinksPacket;
import net.minecraft.network.protocol.common.ClientboundStoreCookiePacket;
import net.minecraft.network.protocol.common.ClientboundTransferPacket;
import net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket;
import net.minecraft.network.protocol.cookie.ClientboundCookieRequestPacket;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.protocol.ping.ClientboundPongResponsePacket;
import net.minecraft.server.RunningOnDifferentThreadException;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.inventory.HorseInventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.connection.ConnectionType;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.level.ClientLevelExtender;
import team.creative.littletiles.client.level.little.LittleClientLevel;
import team.creative.littletiles.common.entity.animation.LittleAnimationLevel;
import team.creative.littletiles.common.level.little.LittleLevel;
import team.creative.littletiles.common.packet.entity.LittleVanillaPacket;
import team.creative.littletiles.mixin.client.network.ClientPacketListenerAccessor;

public class LittleClientPlayerHandler implements TickablePacketListener, ClientGamePacketListener {
    
    private static final Logger LOGGER = LittleTiles.LOGGER;
    private static final Minecraft mc = Minecraft.getInstance();
    public Level level;
    
    public LittleClientLevel requiresClientLevel() {
        if (level instanceof LittleClientLevel s)
            return s;
        throw new RuntimeException("Cannot run this packet on this level " + level);
    }
    
    public ClientLevelExtender requiresClientLevelExtender() {
        if (level instanceof ClientLevelExtender s)
            return s;
        throw new RuntimeException("Cannot run this packet on this level " + level);
    }
    
    public void ensureRunningOnSameThread(Packet packet) throws RunningOnDifferentThreadException {
        if (!mc.isSameThread()) {
            mc.executeIfPossible(() -> LittleTilesClient.PLAYER_CONNECTION.runInContext((LittleLevel) level, x -> packet.handle(x)));
            throw RunningOnDifferentThreadException.RUNNING_ON_DIFFERENT_THREAD;
        }
    }
    
    public ClientPacketListener vanilla() {
        return Minecraft.getInstance().getConnection();
    }
    
    /** Quasar MC: run vanilla's own 26.3 packet handler with this sub-level as its level, then restore the real one.
     * Vanilla's handlers act on their "level" field, so this applies the packet to the sub-level without copying
     * vanilla's code (which 26.3 rewrote). Only used for packets that don't answer the server. */
    public void inSubLevel(Runnable action) {
        ClientPacketListenerAccessor accessor = vanillaAccessor();
        ClientLevel real = accessor.littletiles$getLevel();
        accessor.littletiles$setLevel(requiresClientLevel());
        try {
            action.run();
        } finally {
            accessor.littletiles$setLevel(real);
        }
    }
    
    public ClientPacketListenerAccessor vanillaAccessor() {
        return (ClientPacketListenerAccessor) Minecraft.getInstance().getConnection();
    }
    
    @Override
    public void handleLogin(ClientboundLoginPacket packet) {
        vanilla().handleLogin(packet);
    }
    
    @Override
    public void handleDisconnect(ClientboundDisconnectPacket packet) {
        vanilla().handleDisconnect(packet);
    }
    
    @Override
    public void onDisconnect(DisconnectionDetails details) {
        vanilla().onDisconnect(details);
    }
    
    @Override
    public void handleRespawn(ClientboundRespawnPacket packet) {
        vanilla().handleRespawn(packet);
    }
    
    @Override
    public void handleResourcePackPush(ClientboundResourcePackPushPacket packet) {
        vanilla().handleResourcePackPush(packet);
    }
    
    @Override
    public void handleResourcePackPop(ClientboundResourcePackPopPacket packet) {
        vanilla().handleResourcePackPop(packet);
    }
    
    @Override
    public void handlePlayerInfoUpdate(ClientboundPlayerInfoUpdatePacket packet) {
        vanilla().handlePlayerInfoUpdate(packet);
    }
    
    @Override
    public void handlePlayerInfoRemove(ClientboundPlayerInfoRemovePacket packet) {
        vanilla().handlePlayerInfoRemove(packet);
    }
    
    @Override
    public void handleBlockDestruction(ClientboundBlockDestructionPacket packet) {
        ensureRunningOnSameThread(packet);
        level.destroyBlockProgress(packet.getId(), packet.getPos(), packet.getProgress());
    }
    
    @Override
    public void handleExplosion(ClientboundExplodePacket packet) {
        ensureRunningOnSameThread(packet);
        inSubLevel(() -> vanilla().handleExplosion(packet));
    }
    
    @Override
    public void handleBlockEntityData(ClientboundBlockEntityDataPacket packet) {
        ensureRunningOnSameThread(packet);
        inSubLevel(() -> vanilla().handleBlockEntityData(packet));
    }
    
    @Override
    public void handleBlockEvent(ClientboundBlockEventPacket packet) {
        ensureRunningOnSameThread(packet);
        level.blockEvent(packet.getPos(), packet.getBlock(), packet.getB0(), packet.getB1());
    }
    
    @Override
    public void handleLevelEvent(ClientboundLevelEventPacket packet) {
        ensureRunningOnSameThread(packet);
        if (packet.isGlobalEvent())
            level.globalLevelEvent(packet.getType(), packet.getPos(), packet.getData());
        else
            level.levelEvent(packet.getType(), packet.getPos(), packet.getData());
        
    }
    
    @Override
    public void handleSoundEvent(ClientboundSoundPacket packet) {
        ensureRunningOnSameThread(packet);
        level.playSeededSound(mc.player, packet.getX(), packet.getY(), packet.getZ(), packet.getSound(), packet.getSource(), packet.getVolume(), packet.getPitch(), packet
                .getSeed());
    }
    
    @Override
    public void handleSoundEntityEvent(ClientboundSoundEntityPacket packet) {
        ensureRunningOnSameThread(packet);
        Entity entity = level.getEntity(packet.getId());
        if (entity != null)
            level.playSeededSound(mc.player, entity, packet.getSound(), packet.getSource(), packet.getVolume(), packet.getPitch(), packet.getSeed());
    }
    
    @Override
    public void send(Packet<?> packet) {
        LittleTiles.NETWORK.sendToServer(new LittleVanillaPacket((LittleLevel) level, packet));
    }
    
    public void send(Packet<?> packet, @Nullable PacketSendListener listener) {
        send(packet);
        if (listener != null)
            listener.onSuccess();
    }
    
    @Nullable
    private Entity createEntityFromPacket(ClientboundAddEntityPacket packet) {
        EntityType<?> entitytype = packet.getType();
        if (entitytype == EntityType.PLAYER) {
            PlayerInfo playerinfo = vanilla().getPlayerInfo(packet.getUUID());
            if (playerinfo == null) {
                LOGGER.warn("Server attempted to add player prior to sending player info (Player id {})", packet.getUUID());
                return null;
            }
            return new RemotePlayer(requiresClientLevel(), playerinfo.getProfile());
        }
        return entitytype.create(this.level);
    }
    
    @Override
    public void handleAddEntity(ClientboundAddEntityPacket packet) {
        ensureRunningOnSameThread(packet);
        
        Entity entity = LittleTilesClient.ANIMATION_HANDLER.pollEntityInTransition(packet);
        if (entity == null)
            entity = createEntityFromPacket(packet);
        
        if (entity != null) {
            entity.recreateFromPacket(packet);
            if (level instanceof LittleAnimationLevel a)
                a.addFreshEntityFromPacket(entity);
            else
                requiresClientLevel().addEntity(entity);
            vanillaAccessor().callPostAddEntitySoundInstance(entity);
        } else
            LOGGER.warn("Skipping Entity with id {}", packet.getType());
    }
    
    
    @Override
    public void handleSetEntityMotion(ClientboundSetEntityMotionPacket packet) {
        ensureRunningOnSameThread(packet);
        inSubLevel(() -> vanilla().handleSetEntityMotion(packet));
    }
    
    @Override
    public void handleSetEntityData(ClientboundSetEntityDataPacket packet) {
        ensureRunningOnSameThread(packet);
        Entity entity = this.level.getEntity(packet.id());
        if (entity != null)
            entity.getEntityData().assignValues(packet.packedItems());
    }
    
    @Override
    public void handleTeleportEntity(ClientboundTeleportEntityPacket packet) {
        ensureRunningOnSameThread(packet);
        inSubLevel(() -> vanilla().handleTeleportEntity(packet));
    }
    
    
    @Override
    public void handleMoveEntity(ClientboundMoveEntityPacket packet) {
        ensureRunningOnSameThread(packet);
        inSubLevel(() -> vanilla().handleMoveEntity(packet));
    }
    
    @Override
    public void handleRotateMob(ClientboundRotateHeadPacket packet) {
        ensureRunningOnSameThread(packet);
        Entity entity = packet.getEntity(this.level);
        if (entity != null) {
            float f = packet.getYHeadRot() * 360 / 256.0F;
            entity.lerpHeadTo(f, 3);
        }
    }
    
    @Override
    public void handleRemoveEntities(ClientboundRemoveEntitiesPacket packet) {
        ensureRunningOnSameThread(packet);
        inSubLevel(() -> vanilla().handleRemoveEntities(packet));
    }
    
    @Override
    public void handleMovePlayer(ClientboundPlayerPositionPacket packet) {
        ensureRunningOnSameThread(packet);
        Player player = mc.player;
        PositionMoveRotation current = PositionMoveRotation.of(player);
        PositionMoveRotation target = PositionMoveRotation.calculateAbsolute(current, packet.change(), packet.relatives());
        Vec3 delta = target.position().subtract(current.position());
        player.xOld += delta.x;
        player.yOld += delta.y;
        player.zOld += delta.z;
        player.xo += delta.x;
        player.yo += delta.y;
        player.zo += delta.z;
        player.setPos(target.position());
        player.setDeltaMovement(target.deltaMovement());
        player.xRotO += target.xRot() - player.getXRot();
        player.yRotO += target.yRot() - player.getYRot();
        player.setXRot(target.xRot());
        player.setYRot(target.yRot());
        // 26.3 merged the position echo into the teleport ack
        send(new ServerboundAcceptTeleportationPacket(packet.id(), player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot()));
    }
    
    @Override
    public void handleChunkBlocksUpdate(ClientboundSectionBlocksUpdatePacket packet) {
        ensureRunningOnSameThread(packet);
        ClientLevel level = requiresClientLevel();
        packet.runUpdates((p_205524_, p_205525_) -> level.setServerVerifiedBlockState(p_205524_, p_205525_, 19));
    }
    
    @Override
    public void handleLevelChunkWithLight(ClientboundLevelChunkWithLightPacket packet) {
        ensureRunningOnSameThread(packet);
        inSubLevel(() -> vanilla().handleLevelChunkWithLight(packet));
    }
    
    @Override
    public void handleChunksBiomes(ClientboundChunksBiomesPacket packet) {
        ensureRunningOnSameThread(packet);
        inSubLevel(() -> vanilla().handleChunksBiomes(packet));
    }
    
    private void updateLevelChunk(ClientLevel level, int x, int z, ClientboundLevelChunkPacketData data) {
        level.getChunkSource().replaceWithPacketData(x, z, data.getReadBuffer(), data.getHeightmaps(), data.getBlockEntitiesTagsConsumer(x, z));
    }
    
    private void queueLightUpdate(ClientLevel level, int x, int z, ClientboundLightUpdatePacketData data) {
        level.queueLightUpdate(() -> {
            this.applyLightData(level, x, z, data);
            LevelChunk levelchunk = this.level.getChunkSource().getChunk(x, z, false);
            if (levelchunk != null) {
                this.enableChunkLight(level, levelchunk, x, z);
            }
            
        });
    }
    
    private void applyLightData(ClientLevel level, int x, int z, ClientboundLightUpdatePacketData data) {
        LevelLightEngine levellightengine = this.level.getChunkSource().getLightEngine();
        BitSet bitset = data.getSkyYMask();
        BitSet bitset1 = data.getEmptySkyYMask();
        Iterator<byte[]> iterator = data.getSkyUpdates().iterator();
        this.readSectionList(level, x, z, levellightengine, LightLayer.SKY, bitset, bitset1, iterator);
        BitSet bitset2 = data.getBlockYMask();
        BitSet bitset3 = data.getEmptyBlockYMask();
        Iterator<byte[]> iterator1 = data.getBlockUpdates().iterator();
        this.readSectionList(level, x, z, levellightengine, LightLayer.BLOCK, bitset2, bitset3, iterator1);
        levellightengine.setLightEnabled(new ChunkPos(x, z), true);
    }
    
    private void readSectionList(ClientLevel level, int x, int z, LevelLightEngine light, LightLayer layer, BitSet minSet, BitSet maxSet, Iterator<byte[]> data) {
        for (int i = 0; i < light.getLightSectionCount(); ++i) {
            int j = light.getMinLightSection() + i;
            boolean flag = minSet.get(i);
            boolean flag1 = maxSet.get(i);
            if (flag || flag1) {
                light.queueSectionData(layer, SectionPos.of(x, j, z), flag ? new DataLayer(data.next().clone()) : new DataLayer());
                level.setSectionDirtyWithNeighbors(x, j, z);
            }
        }
        
    }
    
    private void enableChunkLight(ClientLevel level, LevelChunk chunk, int x, int z) {
        LevelLightEngine levellightengine = this.level.getChunkSource().getLightEngine();
        LevelChunkSection[] alevelchunksection = chunk.getSections();
        ChunkPos chunkpos = chunk.getPos();
        
        for (int i = 0; i < alevelchunksection.length; ++i) {
            LevelChunkSection levelchunksection = alevelchunksection[i];
            int j = this.level.getSectionYFromSectionIndex(i);
            levellightengine.updateSectionStatus(SectionPos.of(chunkpos, j), levelchunksection.hasOnlyAir());
            level.setSectionDirtyWithNeighbors(x, j, z);
        }
    }
    
    @Override
    public void handleForgetLevelChunk(ClientboundForgetLevelChunkPacket packet) {
        ensureRunningOnSameThread(packet);
        ClientLevel level = requiresClientLevel();
        ChunkSource chunkSource = this.level.getChunkSource();
        if (chunkSource instanceof ClientChunkCache client)
            client.drop(packet.pos());
        this.queueLightRemoval(level, packet);
    }
    
    private void queueLightRemoval(ClientLevel level, ClientboundForgetLevelChunkPacket packet) {
        ChunkPos chunkpos = packet.pos();
        level.queueLightUpdate(() -> {
            LevelLightEngine levellightengine = this.level.getLightEngine();
            levellightengine.setLightEnabled(chunkpos, false);
            
            for (int i = levellightengine.getMinLightSection(); i < levellightengine.getMaxLightSection(); ++i) {
                SectionPos sectionpos = SectionPos.of(chunkpos, i);
                levellightengine.queueSectionData(LightLayer.BLOCK, sectionpos, (DataLayer) null);
                levellightengine.queueSectionData(LightLayer.SKY, sectionpos, (DataLayer) null);
            }
            
            for (int j = this.level.getMinSectionY(); j < this.level.getMaxSection(); ++j)
                levellightengine.updateSectionStatus(SectionPos.of(chunkpos, j), true);
        });
    }
    
    @Override
    public void handleBlockUpdate(ClientboundBlockUpdatePacket packet) {
        ensureRunningOnSameThread(packet);
        ClientLevelExtender level = requiresClientLevelExtender();
        level.setServerVerifiedBlockStateExtender(packet.getPos(), packet.getBlockState(), 19);
    }
    
    @Override
    public void handleTakeItemEntity(ClientboundTakeItemEntityPacket packet) {
        ensureRunningOnSameThread(packet);
        inSubLevel(() -> vanilla().handleTakeItemEntity(packet));
    }
    
    @Override
    public void handleSystemChat(ClientboundSystemChatPacket packet) {
        vanilla().handleSystemChat(packet);
    }
    
    @Override
    public void handlePlayerChat(ClientboundPlayerChatPacket packet) {
        vanilla().handlePlayerChat(packet);
    }
    
    @Override
    public void handleDisguisedChat(ClientboundDisguisedChatPacket packet) {
        vanilla().handleDisguisedChat(packet);
    }
    
    @Override
    public void handleDeleteChat(ClientboundDeleteChatPacket packet) {
        handleDeleteChat(packet);
    }
    
    @Override
    public void handleAnimate(ClientboundAnimatePacket packet) {
        ensureRunningOnSameThread(packet);
        inSubLevel(() -> vanilla().handleAnimate(packet));
    }
    
    @Override
    public void handleHurtAnimation(ClientboundHurtAnimationPacket packet) {
        ensureRunningOnSameThread(packet);
        Entity entity = this.level.getEntity(packet.id());
        if (entity != null)
            entity.animateHurt(packet.yaw());
    }
    
    @Override
    public void handleSetTime(ClientboundSetTimePacket packet) {
        vanilla().handleSetTime(packet);
    }
    
    @Override
    public void handleSetSpawn(ClientboundSetDefaultSpawnPositionPacket packet) {
        vanilla().handleSetSpawn(packet);
    }
    
    @Override
    public void handleSetEntityPassengersPacket(ClientboundSetPassengersPacket packet) {
        ensureRunningOnSameThread(packet);
        inSubLevel(() -> vanilla().handleSetEntityPassengersPacket(packet));
    }
    
    @Override
    public void handleEntityLinkPacket(ClientboundSetEntityLinkPacket packet) {
        ensureRunningOnSameThread(packet);
        Entity entity = this.level.getEntity(packet.getSourceId());
        if (entity instanceof Mob)
            ((Mob) entity).setDelayedLeashHolderId(packet.getDestId());
    }
    
    private static ItemStack findTotem(Player p_104928_) {
        for (InteractionHand interactionhand : InteractionHand.values()) {
            ItemStack itemstack = p_104928_.getItemInHand(interactionhand);
            if (itemstack.is(Items.TOTEM_OF_UNDYING)) {
                return itemstack;
            }
        }
        
        return new ItemStack(Items.TOTEM_OF_UNDYING);
    }
    
    @Override
    public void handleEntityEvent(ClientboundEntityEventPacket packet) {
        ensureRunningOnSameThread(packet);
        inSubLevel(() -> vanilla().handleEntityEvent(packet));
    }
    
    @Override
    public void handleDamageEvent(ClientboundDamageEventPacket packet) {
        ensureRunningOnSameThread(packet);
        Entity entity = this.level.getEntity(packet.entityId());
        if (entity != null)
            entity.handleDamageEvent(packet.getSource(this.level));
    }
    
    @Override
    public void handleSetHealth(ClientboundSetHealthPacket packet) {
        vanilla().handleSetHealth(packet);
    }
    
    @Override
    public void handleSetExperience(ClientboundSetExperiencePacket packet) {
        vanilla().handleSetExperience(packet);
    }
    
    
    @Override
    public void handleOpenScreen(ClientboundOpenScreenPacket packet) {
        vanilla().handleOpenScreen(packet);
    }
    
    @Override
    public void handleContainerSetSlot(ClientboundContainerSetSlotPacket packet) {
        vanilla().handleContainerSetSlot(packet);
    }
    
    @Override
    public void handleContainerContent(ClientboundContainerSetContentPacket packet) {
        vanilla().handleContainerContent(packet);
    }
    
    @Override
    public void handleOpenSignEditor(ClientboundOpenSignEditorPacket packet) {
        ensureRunningOnSameThread(packet);
        inSubLevel(() -> vanilla().handleOpenSignEditor(packet));
    }
    
    @Override
    public void handleContainerSetData(ClientboundContainerSetDataPacket packet) {
        vanilla().handleContainerSetData(packet);
    }
    
    @Override
    public void handleSetEquipment(ClientboundSetEquipmentPacket packet) {
        ensureRunningOnSameThread(packet);
        if (this.level.getEntity(packet.getEntity()) instanceof LivingEntity living)
            packet.getSlots().forEach((slot) -> living.setItemSlot(slot.getFirst(), slot.getSecond()));
    }
    
    @Override
    public void handleContainerClose(ClientboundContainerClosePacket packet) {
        vanilla().handleContainerClose(packet);
    }
    
    @Override
    public void handleGameEvent(ClientboundGameEventPacket packet) {
        vanilla().handleGameEvent(packet);
    }
    
    @Override
    public void handleMapItemData(ClientboundMapItemDataPacket packet) {
        vanilla().handleMapItemData(packet);
    }
    
    @Override
    public void handleUpdateAdvancementsPacket(ClientboundUpdateAdvancementsPacket packet) {
        vanilla().handleUpdateAdvancementsPacket(packet);
    }
    
    @Override
    public void handleSelectAdvancementsTab(ClientboundSelectAdvancementsTabPacket packet) {
        vanilla().handleSelectAdvancementsTab(packet);
    }
    
    @Override
    public void handleCommands(ClientboundCommandsPacket packet) {
        vanilla().handleCommands(packet);
    }
    
    @Override
    public void handleStopSoundEvent(ClientboundStopSoundPacket packet) {
        vanilla().handleStopSoundEvent(packet);
    }
    
    @Override
    public void handleCommandSuggestions(ClientboundCommandSuggestionsPacket packet) {
        vanilla().handleCommandSuggestions(packet);;
    }
    
    @Override
    public void handleUpdateRecipes(ClientboundUpdateRecipesPacket packet) {
        vanilla().handleUpdateRecipes(packet);
    }
    
    @Override
    public void handleLookAt(ClientboundPlayerLookAtPacket packet) {
        ensureRunningOnSameThread(packet);
        Vec3 vec3 = packet.getPosition(this.level);
        if (vec3 != null)
            mc.player.lookAt(packet.getFromAnchor(), vec3);
    }
    
    @Override
    public void handleTagQueryPacket(ClientboundTagQueryPacket packet) {
        vanilla().handleTagQueryPacket(packet);
    }
    
    @Override
    public void handleAwardStats(ClientboundAwardStatsPacket packet) {
        vanilla().handleAwardStats(packet);
    }
    
    
    @Override
    public void handleUpdateMobEffect(ClientboundUpdateMobEffectPacket packet) {
        ensureRunningOnSameThread(packet);
        Entity entity = this.level.getEntity(packet.getEntityId());
        if (entity instanceof LivingEntity living) {
            Holder<MobEffect> mobeffect = packet.getEffect();
            MobEffectInstance instance = new MobEffectInstance(mobeffect, packet.getEffectDurationTicks(), packet.getEffectAmplifier(), packet.isEffectAmbient(), packet
                    .isEffectVisible(), packet.effectShowsIcon(), (MobEffectInstance) null);
            if (!packet.shouldBlend())
                instance.skipBlending();
            living.forceAddEffect(instance, (Entity) null);
        }
    }
    
    @Override
    public void handleUpdateTags(ClientboundUpdateTagsPacket packet) {
        vanilla().handleUpdateTags(packet);
    }
    
    @Override
    public void handlePlayerCombatEnd(ClientboundPlayerCombatEndPacket packet) {}
    
    @Override
    public void handlePlayerCombatEnter(ClientboundPlayerCombatEnterPacket packet) {}
    
    @Override
    public void handlePlayerCombatKill(ClientboundPlayerCombatKillPacket packet) {
        ensureRunningOnSameThread(packet);
        Entity entity = this.level.getEntity(packet.playerId());
        if (entity == mc.player)
            if (mc.player.shouldShowDeathScreen())
                mc.setScreen(new DeathScreen(packet.message(), this.level.getLevelData().isHardcore()));
            else
                mc.player.respawn();
    }
    
    @Override
    public void handleChangeDifficulty(ClientboundChangeDifficultyPacket packet) {
        vanilla().handleChangeDifficulty(packet);
    }
    
    @Override
    public void handleSetCamera(ClientboundSetCameraPacket packet) {
        ensureRunningOnSameThread(packet);
        Entity entity = packet.getEntity(this.level);
        if (entity != null)
            mc.setCameraEntity(entity);
    }
    
    @Override
    public void handleInitializeBorder(ClientboundInitializeBorderPacket packet) {
        vanilla().handleInitializeBorder(packet);
    }
    
    @Override
    public void handleSetBorderCenter(ClientboundSetBorderCenterPacket packet) {
        vanilla().handleSetBorderCenter(packet);
    }
    
    @Override
    public void handleSetBorderLerpSize(ClientboundSetBorderLerpSizePacket packet) {
        vanilla().handleSetBorderLerpSize(packet);
    }
    
    @Override
    public void handleSetBorderSize(ClientboundSetBorderSizePacket packet) {
        vanilla().handleSetBorderSize(packet);
    }
    
    @Override
    public void handleSetBorderWarningDistance(ClientboundSetBorderWarningDistancePacket packet) {
        vanilla().handleSetBorderWarningDistance(packet);
    }
    
    @Override
    public void handleSetBorderWarningDelay(ClientboundSetBorderWarningDelayPacket packet) {
        vanilla().handleSetBorderWarningDelay(packet);
    }
    
    @Override
    public void handleTitlesClear(ClientboundClearTitlesPacket packet) {
        vanilla().handleTitlesClear(packet);
    }
    
    @Override
    public void handleServerData(ClientboundServerDataPacket packet) {
        vanilla().handleServerData(packet);
    }
    
    @Override
    public void handleCustomChatCompletions(ClientboundCustomChatCompletionsPacket packet) {
        vanilla().handleCustomChatCompletions(packet);
    }
    
    @Override
    public void setActionBarText(ClientboundSetActionBarTextPacket packet) {
        vanilla().setActionBarText(packet);
    }
    
    @Override
    public void setTitleText(ClientboundSetTitleTextPacket packet) {
        vanilla().setTitleText(packet);
    }
    
    @Override
    public void setSubtitleText(ClientboundSetSubtitleTextPacket packet) {
        vanilla().setSubtitleText(packet);
    }
    
    @Override
    public void setTitlesAnimation(ClientboundSetTitlesAnimationPacket packet) {
        vanilla().setTitlesAnimation(packet);
    }
    
    @Override
    public void handleTabListCustomisation(ClientboundTabListPacket packet) {
        vanilla().handleTabListCustomisation(packet);
    }
    
    @Override
    public void handleRemoveMobEffect(ClientboundRemoveMobEffectPacket packet) {
        ensureRunningOnSameThread(packet);
        Entity entity = packet.getEntity(this.level);
        if (entity instanceof LivingEntity living)
            living.removeEffectNoUpdate(packet.effect());
    }
    
    @Override
    public void handleKeepAlive(ClientboundKeepAlivePacket packet) {
        vanilla().handleKeepAlive(packet);
    }
    
    @Override
    public void handlePlayerAbilities(ClientboundPlayerAbilitiesPacket packet) {
        vanilla().handlePlayerAbilities(packet);
    }
    
    @Override
    public void handleBossUpdate(ClientboundBossEventPacket packet) {
        vanilla().handleBossUpdate(packet);
    }
    
    @Override
    public void handleItemCooldown(ClientboundCooldownPacket packet) {
        vanilla().handleItemCooldown(packet);
    }
    
    @Override
    public void handleMoveVehicle(ClientboundMoveVehiclePacket packet) {
        ensureRunningOnSameThread(packet);
        Entity entity = mc.player.getRootVehicle();
        if (entity != mc.player && entity.isLocalInstanceAuthoritative()) {
            Vec3 pos = packet.movingTo().position();
            if (entity.isInterpolating())
                entity.getInterpolation().cancel();
            entity.absSnapTo(pos.x, pos.y, pos.z, packet.movingTo().yRot(), packet.movingTo().xRot());
            send(ServerboundMoveVehiclePacket.fromEntity(entity));
        }
    }
    
    @Override
    public void handleOpenBook(ClientboundOpenBookPacket packet) {
        vanilla().handleOpenBook(packet);
    }
    
    @Override
    public void handleCustomPayload(ClientboundCustomPayloadPacket packet) {
        vanilla().handleCustomPayload(packet);
    }
    
    @Override
    public void handleAddObjective(ClientboundSetObjectivePacket packet) {
        vanilla().handleAddObjective(packet);
    }
    
    @Override
    public void handleSetScore(ClientboundSetScorePacket packet) {
        vanilla().handleSetScore(packet);
    }
    
    @Override
    public void handleSetDisplayObjective(ClientboundSetDisplayObjectivePacket packet) {
        vanilla().handleSetDisplayObjective(packet);
    }
    
    @Override
    public void handleSetPlayerTeamPacket(ClientboundSetPlayerTeamPacket packet) {
        vanilla().handleSetPlayerTeamPacket(packet);
    }
    
    @Override
    public void handleParticleEvent(ClientboundLevelParticlesPacket packet) {
        ensureRunningOnSameThread(packet);
        inSubLevel(() -> vanilla().handleParticleEvent(packet));
    }
    
    @Override
    public void handlePing(ClientboundPingPacket packet) {
        vanilla().handlePing(packet);
    }
    
    @Override
    public void handleUpdateAttributes(ClientboundUpdateAttributesPacket packet) {
        ensureRunningOnSameThread(packet);
        Entity entity = this.level.getEntity(packet.getEntityId());
        if (entity != null) {
            if (!(entity instanceof LivingEntity))
                throw new IllegalStateException("Server tried to update attributes of a non-living entity (actually: " + entity + ")");
            
            AttributeMap attributemap = ((LivingEntity) entity).getAttributes();
            
            for (ClientboundUpdateAttributesPacket.AttributeSnapshot attribute : packet.getValues()) {
                AttributeInstance attributeinstance = attributemap.getInstance(attribute.attribute());
                if (attributeinstance == null)
                    LOGGER.warn("Entity {} does not have attribute {}", entity, attribute.attribute().getRegisteredName());
                else {
                    attributeinstance.setBaseValue(attribute.base());
                    attributeinstance.removeModifiers();
                    
                    for (AttributeModifier attributemodifier : attribute.modifiers())
                        attributeinstance.addTransientModifier(attributemodifier);
                }
            }
        }
    }
    
    @Override
    public void handlePlaceRecipe(ClientboundPlaceGhostRecipePacket packet) {
        vanilla().handlePlaceRecipe(packet);
    }
    
    @Override
    public void handleLightUpdatePacket(ClientboundLightUpdatePacket packet) {
        ensureRunningOnSameThread(packet);
        inSubLevel(() -> vanilla().handleLightUpdatePacket(packet));
    }
    
    @Override
    public void handleMerchantOffers(ClientboundMerchantOffersPacket packet) {
        vanilla().handleMerchantOffers(packet);
    }
    
    @Override
    public void handleSetChunkCacheRadius(ClientboundSetChunkCacheRadiusPacket packet) {
        vanilla().handleSetChunkCacheRadius(packet);
    }
    
    @Override
    public void handleSetSimulationDistance(ClientboundSetSimulationDistancePacket packet) {
        vanilla().handleSetSimulationDistance(packet);
    }
    
    @Override
    public void handleSetChunkCacheCenter(ClientboundSetChunkCacheCenterPacket packet) {
        vanilla().handleSetChunkCacheCenter(packet);
    }
    
    @Override
    public void handleBundlePacket(ClientboundBundlePacket packet) {
        ensureRunningOnSameThread(packet);
        for (Packet<? super ClientGamePacketListener> sub : packet.subPackets())
            sub.handle(this);
    }
    
    @Override
    public void handleBlockChangedAck(ClientboundBlockChangedAckPacket packet) {
        ensureRunningOnSameThread(packet);
        ClientLevelExtender level = requiresClientLevelExtender();
        level.handleBlockChangedAckExtender(packet.sequence());
    }
    
    @Override
    public void tick() {}
    
    @Override
    public boolean isAcceptingMessages() {
        return vanilla().isAcceptingMessages();
    }
    
    @Override
    public void handlePongResponse(ClientboundPongResponsePacket packet) {
        vanilla().handlePongResponse(packet);
    }
    
    @Override
    public void handleStoreCookie(ClientboundStoreCookiePacket packet) {
        vanilla().handleStoreCookie(packet);
    }
    
    @Override
    public void handleTransfer(ClientboundTransferPacket packet) {
        vanilla().handleTransfer(packet);
    }
    
    @Override
    public void handleCustomReportDetails(ClientboundCustomReportDetailsPacket packet) {
        vanilla().handleCustomReportDetails(packet);
    }
    
    @Override
    public void handleServerLinks(ClientboundServerLinksPacket packet) {
        vanilla().handleServerLinks(packet);
    }
    
    @Override
    public void handleRequestCookie(ClientboundCookieRequestPacket packet) {
        vanilla().handleRequestCookie(packet);
    }
    
    @Override
    public Connection getConnection() {
        return vanilla().getConnection();
    }
    
    @Override
    public ConnectionType getConnectionType() {
        return vanilla().getConnectionType();
    }
    
    @Override
    public void handleResetScore(ClientboundResetScorePacket packet) {
        vanilla().handleResetScore(packet);
    }
    
    @Override
    public void handleTickingState(ClientboundTickingStatePacket packet) {
        ensureRunningOnSameThread(packet);
        TickRateManager tickratemanager = level.tickRateManager();
        tickratemanager.setTickRate(packet.tickRate());
        tickratemanager.setFrozen(packet.isFrozen());
    }
    
    @Override
    public void handleTickingStep(ClientboundTickingStepPacket packet) {
        ensureRunningOnSameThread(packet);
        if (level != null) {
            TickRateManager tickratemanager = level.tickRateManager();
            tickratemanager.setFrozenTicksToRun(packet.tickSteps());
        }
    }
    
    @Override
    public void handleConfigurationStart(ClientboundStartConfigurationPacket packet) {
        vanilla().handleConfigurationStart(packet);
    }
    
    @Override
    public void handleChunkBatchStart(ClientboundChunkBatchStartPacket packet) {
        vanilla().handleChunkBatchStart(packet);
    }
    
    @Override
    public void handleChunkBatchFinished(ClientboundChunkBatchFinishedPacket packet) {
        vanilla().handleChunkBatchFinished(packet);
    }
    
    @Override
    public void handleDebugSample(ClientboundDebugSamplePacket packet) {
        vanilla().handleDebugSample(packet);
    }
    
    @Override
    public void handleProjectilePowerPacket(ClientboundProjectilePowerPacket packet) {
        ensureRunningOnSameThread(packet);
        inSubLevel(() -> vanilla().handleProjectilePowerPacket(packet));
    }
    
    // ===== Quasar MC: listener methods added in 26.3 (delegating to vanilla like the rest of this handler) =====

    @Override
    public void handleSwingAnimation(net.minecraft.network.protocol.game.ClientboundSwingAnimationPacket packet) {
        // TODO(quasar): 26.3 packet for entities; LittleTiles applies entity packets to the sub-level itself — not ported yet
    }

    @Override
    public void handleRecipeBookAdd(net.minecraft.network.protocol.game.ClientboundRecipeBookAddPacket packet) {
        vanilla().handleRecipeBookAdd(packet);
    }

    @Override
    public void handleRecipeBookRemove(net.minecraft.network.protocol.game.ClientboundRecipeBookRemovePacket packet) {
        vanilla().handleRecipeBookRemove(packet);
    }

    @Override
    public void handleRecipeBookSettings(net.minecraft.network.protocol.game.ClientboundRecipeBookSettingsPacket packet) {
        vanilla().handleRecipeBookSettings(packet);
    }

    @Override
    public void handleMountScreenOpen(net.minecraft.network.protocol.game.ClientboundMountScreenOpenPacket packet) {
        vanilla().handleMountScreenOpen(packet);
    }

    @Override
    public void handleAddTransientBlockPacket(net.minecraft.network.protocol.game.ClientboundAddTransientBlockPacket packet) {
        vanilla().handleAddTransientBlockPacket(packet);
    }

    @Override
    public void handleMinecartAlongTrack(net.minecraft.network.protocol.game.ClientboundMoveMinecartPacket packet) {
        // TODO(quasar): 26.3 packet for entities; LittleTiles applies entity packets to the sub-level itself — not ported yet
    }

    @Override
    public void handleRotatePlayer(net.minecraft.network.protocol.game.ClientboundPlayerRotationPacket packet) {
        vanilla().handleRotatePlayer(packet);
    }

    @Override
    public void handleGameRuleValues(net.minecraft.network.protocol.game.ClientboundGameRuleValuesPacket packet) {
        vanilla().handleGameRuleValues(packet);
    }

    @Override
    public void handleSetHeldSlot(net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket packet) {
        vanilla().handleSetHeldSlot(packet);
    }

    @Override
    public void handleEntityPositionSync(net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket packet) {
        // TODO(quasar): 26.3 packet for entities; LittleTiles applies entity packets to the sub-level itself — not ported yet
    }

    @Override
    public void handleSetCursorItem(net.minecraft.network.protocol.game.ClientboundSetCursorItemPacket packet) {
        vanilla().handleSetCursorItem(packet);
    }

    @Override
    public void handleSetPlayerInventory(net.minecraft.network.protocol.game.ClientboundSetPlayerInventoryPacket packet) {
        vanilla().handleSetPlayerInventory(packet);
    }

    @Override
    public void handleTestInstanceBlockStatus(net.minecraft.network.protocol.game.ClientboundTestInstanceBlockStatus packet) {
        vanilla().handleTestInstanceBlockStatus(packet);
    }

    @Override
    public void handleWaypoint(net.minecraft.network.protocol.game.ClientboundTrackedWaypointPacket packet) {
        vanilla().handleWaypoint(packet);
    }

    @Override
    public void handleDebugChunkValue(net.minecraft.network.protocol.game.ClientboundDebugChunkValuePacket packet) {
        vanilla().handleDebugChunkValue(packet);
    }

    @Override
    public void handleDebugBlockValue(net.minecraft.network.protocol.game.ClientboundDebugBlockValuePacket packet) {
        vanilla().handleDebugBlockValue(packet);
    }

    @Override
    public void handleDebugEntityValue(net.minecraft.network.protocol.game.ClientboundDebugEntityValuePacket packet) {
        vanilla().handleDebugEntityValue(packet);
    }

    @Override
    public void handleDebugEvent(net.minecraft.network.protocol.game.ClientboundDebugEventPacket packet) {
        vanilla().handleDebugEvent(packet);
    }

    @Override
    public void handleGameTestHighlightPos(net.minecraft.network.protocol.game.ClientboundGameTestHighlightPosPacket packet) {
        vanilla().handleGameTestHighlightPos(packet);
    }

    @Override
    public void handleLowDiskSpaceWarning(net.minecraft.network.protocol.game.ClientboundLowDiskSpaceWarningPacket packet) {
        vanilla().handleLowDiskSpaceWarning(packet);
    }

    @Override
    public net.minecraft.network.protocol.PacketFlow flow() {
        return vanilla().flow();
    }

    @Override
    public net.minecraft.network.ConnectionProtocol protocol() {
        return vanilla().protocol();
    }

    @Override
    public void handlePostEffects(net.minecraft.network.protocol.common.ClientboundPostEffectsPacket packet) {
        vanilla().handlePostEffects(packet);
    }

    @Override
    public void handleClearDialog(net.minecraft.network.protocol.common.ClientboundClearDialogPacket packet) {
        vanilla().handleClearDialog(packet);
    }

    @Override
    public void handleShowDialog(net.minecraft.network.protocol.common.ClientboundShowDialogPacket packet) {
        vanilla().handleShowDialog(packet);
    }

    @Override
    public void send(net.minecraft.network.protocol.common.custom.CustomPacketPayload packet) {
        vanilla().send(packet);
    }

    @Override
    public void disconnect(net.minecraft.network.chat.Component packet) {
        // a sub-level handler must never drop the real connection
    }

    @Override
    public net.minecraft.network.PacketProcessor getPacketProcessor() {
        return vanilla().getPacketProcessor();
    }
}

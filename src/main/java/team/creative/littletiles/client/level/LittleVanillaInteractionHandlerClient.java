package team.creative.littletiles.client.level;

import org.apache.commons.lang3.mutable.MutableObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.client.multiplayer.prediction.PredictiveAction;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.common.level.handler.LevelHandler;
import team.creative.littletiles.mixin.client.MultiPlayerGameModeAccessor;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

public class LittleVanillaInteractionHandlerClient extends LevelHandler {

    private static final Minecraft mc = Minecraft.getInstance();
    private Level destroyLevel;
    private BlockPos destroyBlockPos;
    private ItemStack destroyingItem;
    private float destroyProgress;
    private float destroyTicks;
    private boolean isDestroying;
    public LittleVanillaInteractionHandlerClient(Level level) { super(level); }

    public Player getPlayer() { return mc.player; }

    public GameType getGameMode() { return mc.gameMode == null ? GameType.SURVIVAL : mc.gameMode.getPlayerMode(); }

    public ClientPacketListener getVanillaConnection() { return mc.getConnection(); }

    private void ensureHasSentCarriedItem() {}

    public boolean destroyBlock(Level level, BlockPos pos) { return false; }

    public boolean startDestroyBlock(Level level, BlockPos pos, Direction direction) { return false; }

    public void stopDestroyBlock() {}

    public boolean continueDestroyBlock(Level level, BlockPos pos, Direction direction) { return false; }

    private void startPrediction(Level level, PredictiveAction action) {}

    private boolean sameDestroyTarget(Level level, BlockPos pos) { return false; }

    /** Quasar MC (26.3): right click on a block inside an animated structure (e.g. closing an open door). Predicts the
     * result locally like vanilla and sends the use packet into the structure's sub-level. */
    public InteractionResult useItemOn(Level level, LocalPlayer player, InteractionHand hand, BlockHitResult result) {
        if (!level.getWorldBorder().isWithinBounds(result.getBlockPos()))
            return InteractionResult.FAIL;
        InteractionResult interaction = performUseItemOn(level, player, hand, result);
        LittleTilesClient.PLAYER_CONNECTION.send(level, new ServerboundUseItemOnPacket(hand, result, 0));
        return interaction;
    }

    private InteractionResult performUseItemOn(Level level, LocalPlayer player, InteractionHand hand, BlockHitResult hit) {
        BlockPos blockpos = hit.getBlockPos();
        ItemStack itemstack = player.getItemInHand(hand);
        var event = CommonHooks.onRightClickBlock(player, hand, blockpos, hit);
        if (event.isCanceled())
            return event.getCancellationResult();
        if (getGameMode() == GameType.SPECTATOR)
            return InteractionResult.SUCCESS;
        UseOnContext context = new UseOnContext(level, player, hand, itemstack, hit);
        if (event.getUseItem() != net.minecraft.util.TriState.FALSE) {
            InteractionResult first = itemstack.onItemUseFirst(context);
            if (first != InteractionResult.PASS)
                return first;
        }
        boolean holding = !player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty();
        boolean bypass = player.isSecondaryUseActive() && holding && !(player.getMainHandItem().doesSneakBypassUse(level, blockpos, player) && player.getOffhandItem()
                .doesSneakBypassUse(level, blockpos, player));
        if (event.getUseBlock().isTrue() || (event.getUseBlock().isDefault() && !bypass)) {
            BlockState state = level.getBlockState(blockpos);
            InteractionResult withItem = state.useItemOn(itemstack, level, player, hand, hit);
            if (withItem.consumesAction())
                return withItem;
            if (withItem instanceof InteractionResult.TryEmptyHandInteraction && hand == InteractionHand.MAIN_HAND) {
                InteractionResult without = state.useWithoutItem(level, player, hit);
                if (without.consumesAction())
                    return without;
            }
        }
        if (event.getUseBlock().isFalse())
            return InteractionResult.PASS;
        if (event.getUseItem().isTrue() || (!itemstack.isEmpty() && !player.getCooldowns().isOnCooldown(itemstack))) {
            if (getGameMode().isCreative()) {
                int count = itemstack.getCount();
                InteractionResult used = itemstack.useOn(context);
                itemstack.setCount(count);
                return used;
            }
            return itemstack.useOn(context);
        }
        return InteractionResult.PASS;
    }

    public InteractionResult useItem(Level level, Player player, InteractionHand hand) { return null; }

    public void attack(Level level, Player player, Entity entity) {}

    public InteractionResult interact(Level level, Player player, Entity entity, InteractionHand hand) { return null; }

    public InteractionResult interactAt(Level level, Player player, Entity entity, EntityHitResult hit, InteractionHand hand) { return null; }

    public void releaseUsingItem(Level level, Player player) {}

    public boolean isDestroying() { return false; }

    public void handlePickItem(int slot) {}


}


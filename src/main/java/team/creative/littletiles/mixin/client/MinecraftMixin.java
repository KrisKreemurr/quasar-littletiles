package team.creative.littletiles.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.common.math.vec.LittleHitResult;

/** Quasar MC (26.3): clicks on blocks inside animated structures. The crosshair hit is a {@link LittleHitResult} (an
 * entity hit on the structure's holder entity); without this vanilla would just "interact with the entity". Right click
 * runs the block use inside the sub-level (open/close doors etc.); left click is swallowed (breaking tiles inside
 * moving structures is not ported). Head injections only, so nothing depends on vanilla's local variables. */
@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Shadow
    public HitResult hitResult;

    @Shadow
    public LocalPlayer player;

    @Shadow
    public MultiPlayerGameMode gameMode;

    @Shadow
    private int rightClickDelay;

    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true, require = 1)
    private void littletiles$startUseItem(CallbackInfo info) {
        if (!(hitResult instanceof LittleHitResult hit) || !hit.isBlock() || player == null || gameMode == null || gameMode.isDestroying())
            return;
        rightClickDelay = 4;
        if (player.isHandsBusy())
            return;
        BlockHitResult block = hit.asBlockHit();
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            InteractionResult result = LittleTilesClient.INTERACTION_HANDLER.useItemOn(hit.level.asLevel(), player, hand, block);
            if (result == null)
                continue;
            if (result.consumesAction()) {
                if (result instanceof InteractionResult.Success success && success.swingSource() != InteractionResult.SwingSource.NONE)
                    player.swing(hand, false);
                info.cancel();
                return;
            }
            if (result instanceof InteractionResult.Fail) {
                info.cancel();
                return;
            }
        }
        info.cancel(); // never fall through to "interact with the structure entity"
    }

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true, require = 1)
    private void littletiles$startAttack(CallbackInfoReturnable<Boolean> info) {
        if (hitResult instanceof LittleHitResult)
            info.setReturnValue(false);
    }
}

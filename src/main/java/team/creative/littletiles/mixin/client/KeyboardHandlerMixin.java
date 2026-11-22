package team.creative.littletiles.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;
import team.creative.littletiles.client.LittleTilesClient;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {
    
    @Inject(method = "keyPress(JILnet/minecraft/client/input/KeyEvent;)V", at = @At(value = "INVOKE",
            target = "Lcom/mojang/blaze3d/platform/InputConstants;getKey(Lnet/minecraft/client/input/KeyEvent;)Lcom/mojang/blaze3d/platform/InputConstants$Key;"), cancellable = true,
            require = 0)
    private void onKeyPressed(long window, int action, KeyEvent event, CallbackInfo info) {
        if (LittleTilesClient.PREVIEW_RENDERER != null && LittleTilesClient.PREVIEW_RENDERER.keyPressed(event.key(), event.scancode(), action, event.modifiers()))
            info.cancel();
    }
    
}

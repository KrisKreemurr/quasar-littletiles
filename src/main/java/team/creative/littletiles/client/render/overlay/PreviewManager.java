package team.creative.littletiles.client.render.overlay;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.TriState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.InputEvent.InteractionKeyMappingTriggered;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock.Action;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickEmpty;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickEmpty;
import team.creative.creativecore.common.gui.IGuiParent;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.LittleTilesGuiRegistry;
import team.creative.littletiles.api.common.tool.ILittleTool;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.action.LittleActionHandlerClient;
import team.creative.littletiles.client.level.LevelAwareHandler;
import team.creative.littletiles.client.tool.LittleTool;
import team.creative.littletiles.client.tool.mode.BuildingModeFeature;
import team.creative.littletiles.client.tool.mode.BuildingModeFeatures;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.action.exception.LittleActionException;
import team.creative.littletiles.common.gui.tool.GuiConfigure;
import team.creative.littletiles.common.level.context.ILittleLevelContext;
import team.creative.littletiles.common.math.vec.LittleHitResult;
import team.creative.littletiles.common.mod.sable.SableManager;

/** Turns clicks and keys into LittleTiles tool actions (place, chisel, hammer, configure, undo/redo, tool keys).
 * <p>
 * Quasar MC (26.3): restored from the parked original. The per-frame tool update runs on the client tick instead of a
 * render-level stage. TODO(quasar): the preview drawing (ghost tiles, tool lines and the tile-shaped block outline) comes
 * back with the render rewrite; until then tools work without a visible preview. */
public class PreviewManager implements LevelAwareHandler {

    public static final Identifier WHITE_TEXTURE = Identifier.tryBuild(LittleTiles.MODID, "textures/preview.png");
    public static final Minecraft MC = Minecraft.getInstance();

    public final PreviewRenderer renderer = new PreviewRenderer(this);
    private ItemStack lastHeld = ItemStack.EMPTY;
    private LittleTool tool;

    public PreviewManager() {
        NeoForge.EVENT_BUS.register(this);
    }

    public LittleTool tool() {
        return tool;
    }

    public void clearToolPreviews() {
        lastHeld = ItemStack.EMPTY;
        if (tool != null)
            tool.remove();
        tool = null;
    }

    @Override
    public void unload() {
        clearToolPreviews();
        for (BuildingModeFeature feature : BuildingModeFeatures.REGISTRY.values())
            feature.unloadLevel();
    }

    @SubscribeEvent
    protected void tick(ClientTickEvent.Post event) {
        if (MC.player == null || MC.level == null || MC.gui.screen() instanceof IGuiParent)
            return;

        Player player = MC.player;
        ItemStack stack = player.getMainHandItem();

        if (!ItemStack.isSameItem(stack, lastHeld) || (stack.getItem() instanceof ILittleTool tool && !tool.isCorrectTool(stack, this.tool))) {
            boolean buildingMode = tool != null && tool.buildingMode();
            clearToolPreviews();
            if (stack.getItem() instanceof ILittleTool tool)
                this.tool = tool.tool(stack);
            else
                this.tool = null;
            if (tool != null && buildingMode)
                tool.startBuildingMode();
            lastHeld = stack.copy();
        }

        if (!LittleAction.canPlace(player)) {
            processKeys(stack, false); // Make sure the actions are not queued and performed once things can be placed
            return;
        }

        if (tool != null) {
            tool.stack = stack;
            tool.tick(renderer);
        }

        processKeys(stack, true);
    }

    private void processKeys(ItemStack stack, boolean execute) {
        while (LittleTilesClient.KEY_UNDO.consumeClick()) {
            try {
                if (execute && LittleActionHandlerClient.canUseUndoOrRedo())
                    LittleTilesClient.ACTION_HANDLER.undo();
            } catch (LittleActionException e) {
                LittleActionHandlerClient.handleException(e);
            }
        }

        while (LittleTilesClient.KEY_REDO.consumeClick()) {
            try {
                if (execute && LittleActionHandlerClient.canUseUndoOrRedo())
                    LittleTilesClient.ACTION_HANDLER.redo();
            } catch (LittleActionException e) {
                LittleActionHandlerClient.handleException(e);
            }
        }

        while (LittleTilesClient.KEY_CONFIGURE.consumeClick())
            if (stack.getItem() instanceof ILittleTool t) {
                GuiConfigure gui = t.getConfigure(MC.player, ContainerSlotView.mainHand(MC.player), false);
                if (gui != null)
                    LittleTilesGuiRegistry.OPEN_CONFIG.open(MC.player);
            }

        while (LittleTilesClient.KEY_CONFIGURE_SECONDARY.consumeClick())
            if (stack.getItem() instanceof ILittleTool t) {
                GuiConfigure gui = t.getConfigure(MC.player, ContainerSlotView.mainHand(MC.player), true);
                if (gui != null) {
                    CompoundTag nbt = new CompoundTag();
                    nbt.putBoolean("second", true);
                    LittleTilesGuiRegistry.OPEN_CONFIG.open(nbt, MC.player);
                }
            }

        for (KeyMapping key : LittleTilesClient.TOOL_KEYS)
            while (key.consumeClick())
                if (execute && tool != null)
                    if (tool.toolKeyPressed(renderer, key))
                        break;
    }

    public boolean keyPressed(int keyCode, int scanCode, int action, int modifiers) {
        if (tool != null)
            return tool.keyPressed(renderer, keyCode, scanCode, action, modifiers);
        return false;
    }

    public Player player() {
        return MC.player;
    }

    public Level level() {
        return MC.level;
    }

    public BlockHitResult blockHit() {
        if (MC.hitResult instanceof BlockHitResult b)
            return b;
        if (MC.hitResult instanceof LittleHitResult result && result.isBlock())
            return result.asBlockHit();
        return null;
    }

    public Level blockHitLevel() {
        if (MC.hitResult instanceof LittleHitResult result && result.isBlock())
            return (Level) result.level;
        return MC.level;
    }

    public ILittleLevelContext blockHitContext() {
        if (MC.hitResult instanceof LittleHitResult result && result.isBlock())
            return result.level;
        if (MC.hitResult instanceof BlockHitResult b)
            return SableManager.context(MC.level, b.getBlockPos());
        return ILittleLevelContext.STANDARD;
    }

    @SubscribeEvent
    protected void onMouseWheelClick(InteractionKeyMappingTriggered event) {
        if (!event.isPickBlock() || tool == null)
            return;
        BlockHitResult hit = blockHit();
        if (tool.onMouseWheelClickBlock(renderer, blockHitLevel(), hit))
            event.setCanceled(true);
    }

    @SubscribeEvent
    protected void onLeftClickAir(LeftClickEmpty event) {
        if (!event.getLevel().isClientSide() || tool == null)
            return;
        tool.onLeftClick(renderer, blockHitLevel(), blockHit());
    }

    @SubscribeEvent
    protected void onLeftClickBlock(LeftClickBlock event) {
        if (!event.getLevel().isClientSide() || tool == null || event.getAction() != Action.START)
            return;
        if (tool.onLeftClick(renderer, blockHitLevel(), blockHit()))
            event.setUseItem(TriState.TRUE);
    }

    @SubscribeEvent
    protected void onRightClickAir(RightClickEmpty event) {
        if (!event.getLevel().isClientSide() || tool == null)
            return;
        tool.onRightClick(renderer, blockHitLevel(), blockHit());
    }

    @SubscribeEvent
    protected void onRightClickBlock(RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND || !event.getLevel().isClientSide() || tool == null)
            return;
        if (tool.onRightClick(renderer, blockHitLevel(), blockHit())) {
            event.setCancellationResult(InteractionResult.CONSUME);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    protected void mouseInput(InputEvent.MouseButton.Pre event) {
        if (tool != null)
            tool.mouseInput(event);
    }
}

package team.creative.littletiles.client;

import net.minecraft.client.input.KeyEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.minecraft.client.renderer.entity.NoopRenderer;
import team.creative.littletiles.client.gui.LittleGuiClientRegistry;
import java.util.List;
import java.util.Map.Entry;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Key;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.common.NeoForge;
import team.creative.creativecore.client.CreativeCoreClient;
import team.creative.creativecore.common.config.holder.ConfigHolderDynamic;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.matrix.IntMatrix3c;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.client.action.LittleActionHandlerClient;
import team.creative.littletiles.client.action.interact.LittleInteractionHandlerClient;
import team.creative.littletiles.client.level.LevelHandlersClient;
import team.creative.littletiles.client.level.LittleAnimationHandlerClient;
import team.creative.littletiles.client.level.LittleVanillaInteractionHandlerClient;
import team.creative.littletiles.client.player.LittleClientPlayerConnection;
import team.creative.littletiles.client.render.item.ItemRenderCache;
import team.creative.littletiles.client.render.level.LittleClientEventHandler;
import team.creative.littletiles.client.render.overlay.LittleTilesProfilerOverlay;
import team.creative.littletiles.client.render.overlay.OverlayRenderer;
import team.creative.littletiles.client.render.overlay.PreviewManager;
import team.creative.littletiles.client.tool.mode.BuildingModeFeature;
import team.creative.littletiles.client.tool.mode.BuildingModeFeatures;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.placement.mode.PlacementMode;

@OnlyIn(Dist.CLIENT)
public class LittleTilesClient {
    
    public static final Minecraft MC = Minecraft.getInstance();
    
    public static final IKeyConflictContext LITTLE_KEY_CONTEXT = new IKeyConflictContext() {
        
        @Override
        public boolean isActive() {
            return true;
        }
        
        @Override
        public boolean conflicts(IKeyConflictContext other) {
            return this == other;
        }
    };
    
    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(LittleTiles.MODID, "littletiles"));
    
    public static final LevelHandlersClient LEVEL_HANDLERS = new LevelHandlersClient();
    public static LittleActionHandlerClient ACTION_HANDLER;
    public static LittleAnimationHandlerClient ANIMATION_HANDLER;
    public static LittleVanillaInteractionHandlerClient INTERACTION_HANDLER;
    public static LittleInteractionHandlerClient INTERACTION;
    public static PreviewManager PREVIEW_RENDERER;
    public static ItemRenderCache ITEM_RENDER_CACHE;
    public static LittleClientPlayerConnection PLAYER_CONNECTION;
    public static OverlayRenderer OVERLAY_RENDERER;
    
    public static KeyMapping KEY_MIRROR;
    public static KeyMapping KEY_MARK;
    public static KeyMapping KEY_CONFIGURE;
    public static KeyMapping KEY_CONFIGURE_SECONDARY;
    public static KeyMapping KEY_UP;
    public static KeyMapping KEY_DOWN;
    public static KeyMapping KEY_RIGHT;
    public static KeyMapping KEY_LEFT;
    public static KeyMapping KEY_BUILDING_MODE;
    
    public static KeyMapping[] TOOL_KEYS;
    
    public static KeyMapping KEY_UNDO;
    public static KeyMapping KEY_REDO;
    
    public static void grid(LittleGrid grid) {
        ACTION_HANDLER.setting.grid(grid);
    }
    
    public static void placementMode(PlacementMode mode) {
        ACTION_HANDLER.setting.placementMode(mode);
    }
    
    public static void setPlace(LittleGrid grid, PlacementMode mode) {
        ACTION_HANDLER.setting.set(grid, mode);
    }
    
    public static Component arrowKeysTooltip() {
        if (KEY_UP.isDefault() && KEY_DOWN.isDefault() && KEY_RIGHT.isDefault() && KEY_LEFT.isDefault())
            return Component.translatable("gui.tooltip.arrow_keys");
        return Component.empty().append(KEY_UP.getTranslatedKeyMessage()).append(", ").append(KEY_DOWN.getTranslatedKeyMessage()).append(", ").append(KEY_RIGHT
                .getTranslatedKeyMessage()).append(", ").append(KEY_LEFT.getTranslatedKeyMessage());
    }
    
    public static void displayActionMessage(List<Component> message) {
        OVERLAY_RENDERER.displayActionMessage(message);
    }
    
    public static Facing facingFromKeybind(Player player, int keyCode, int scanCode) {
        return facingFromKeybind(player, new KeyEvent(keyCode, scanCode, 0));
    }
    
    public static Facing facingFromKeybind(Player player, KeyEvent key) {
        for (int i = 0; i < TOOL_KEYS.length; i++)
            if (TOOL_KEYS[i].matches(key))
                return facingFromKeybind(player, TOOL_KEYS[i]);
        return null;
    }
    
    public static Facing facingFromKeybind(Player player, KeyMapping key) {
        if (key == LittleTilesClient.KEY_UP)
            return LittleActionHandlerClient.isUsingSecondMode() ? Facing.UP : Facing.EAST;
        if (key == LittleTilesClient.KEY_DOWN)
            return LittleActionHandlerClient.isUsingSecondMode() ? Facing.DOWN : Facing.WEST;
        if (key == LittleTilesClient.KEY_RIGHT)
            return Facing.SOUTH;
        if (key == LittleTilesClient.KEY_LEFT)
            return Facing.NORTH;
        return null;
    }
    
    public static IntMatrix3c fromKeybind(Player player, KeyMapping key) {
        if (key == LittleTilesClient.KEY_UP)
            return Rotation.Z_CLOCKWISE.getMatrix();
        if (key == LittleTilesClient.KEY_DOWN)
            return Rotation.Z_COUNTER_CLOCKWISE.getMatrix();
        if (key == LittleTilesClient.KEY_RIGHT)
            return Rotation.Y_COUNTER_CLOCKWISE.getMatrix();
        if (key == LittleTilesClient.KEY_LEFT)
            return Rotation.Y_CLOCKWISE.getMatrix();
        if (key == LittleTilesClient.KEY_MIRROR)
            return Facing.of(player).axis.getMatrix();
        return null;
    }
    
    public static void load(IEventBus bus) {
        bus.addListener(LittleTilesClient::setup);
        NeoForge.EVENT_BUS.addListener(LittleTilesClient::commands);
        // TODO(quasar): colored water tint (was RegisterColorHandlersEvent.Block) - 26.x block tints are data-driven.
        bus.addListener(LittleTilesClient::registerKeys);
        bus.addListener(LittleTilesClient::registerRenderers);
        team.creative.littletiles.client.render.level.LittleChunkGeometry.init();
        team.creative.littletiles.client.render.level.SignalDisplayRenderer.init();
        bus.addListener((net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent event) -> event.register(
            net.minecraft.resources.Identifier.fromNamespaceAndPath(LittleTiles.MODID, "tiles"), team.creative.littletiles.client.render.level.LittleItemRenderer.Unbaked.MAP_CODEC));
        bus.addListener((net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent event) -> {
            event.register(net.minecraft.resources.Identifier.fromNamespaceAndPath(LittleTiles.MODID, "element"),
                team.creative.littletiles.client.render.level.LittleItemRenderer.UnbakedElement.MAP_CODEC);
            event.register(net.minecraft.resources.Identifier.fromNamespaceAndPath(LittleTiles.MODID, "ingredient"),
                team.creative.littletiles.client.render.level.LittleItemRenderer.UnbakedIngredient.MAP_CODEC);
        });
        bus.addListener((net.neoforged.neoforge.client.event.RegisterColorHandlersEvent.ItemTintSources event) -> event.register(
            net.minecraft.resources.Identifier.fromNamespaceAndPath(LittleTiles.MODID, "color"), team.creative.littletiles.client.item.ColorTint.MAP_CODEC));
        bus.addListener((net.neoforged.neoforge.client.event.RegisterSelectItemModelPropertyEvent event) -> event.register(
            net.minecraft.resources.Identifier.fromNamespaceAndPath(LittleTiles.MODID, "premade"), team.creative.littletiles.client.item.PremadeIdProperty.TYPE));
        bus.addListener((net.neoforged.neoforge.client.event.RegisterRangeSelectItemModelPropertyEvent event) -> event.register(
            net.minecraft.resources.Identifier.fromNamespaceAndPath(LittleTiles.MODID, "filled"), team.creative.littletiles.client.item.BottleFilledProperty.MAP_CODEC));
        // TODO(quasar): item models (multi tiles, premade, glove/chisel previews, block ingredient), item tints and the "filled" bottle property
        // are data-driven client item definitions in 26.x; they come back with the render rewrite (special item models).
    }
    
    private static void registerKeys(RegisterKeyMappingsEvent event) {
        KEY_UP = new LittleKeyMapping("key.rotateup", LITTLE_KEY_CONTEXT, InputConstants.KEY_UP, CATEGORY).ignoreModifier();
        KEY_DOWN = new LittleKeyMapping("key.rotatedown", LITTLE_KEY_CONTEXT, InputConstants.KEY_DOWN, CATEGORY).ignoreModifier();
        KEY_RIGHT = new LittleKeyMapping("key.rotateright", LITTLE_KEY_CONTEXT, InputConstants.KEY_RIGHT, CATEGORY).ignoreModifier();
        KEY_LEFT = new LittleKeyMapping("key.rotateleft", LITTLE_KEY_CONTEXT, InputConstants.KEY_LEFT, CATEGORY).ignoreModifier();
        
        KEY_MIRROR = new LittleKeyMapping("key.little.mirror", LITTLE_KEY_CONTEXT, InputConstants.KEY_G, CATEGORY);
        KEY_MARK = new LittleKeyMapping("key.little.mark", LITTLE_KEY_CONTEXT, InputConstants.KEY_M, CATEGORY).ignoreModifier();
        KEY_CONFIGURE = new LittleKeyMapping("key.little.config.item", LITTLE_KEY_CONTEXT, InputConstants.KEY_C, CATEGORY);
        KEY_CONFIGURE_SECONDARY = new LittleKeyMapping("key.little.config_secondary.item", LITTLE_KEY_CONTEXT, KeyModifier.SHIFT, InputConstants.KEY_C, CATEGORY);
        KEY_BUILDING_MODE = new LittleKeyMapping("key.little.building_mode", LITTLE_KEY_CONTEXT, KeyModifier.NONE, InputConstants.KEY_B, CATEGORY);
        
        KEY_UNDO = new LittleKeyMapping("key.little.undo", LITTLE_KEY_CONTEXT, KeyModifier.CONTROL, InputConstants.KEY_Z, CATEGORY);
        KEY_REDO = new LittleKeyMapping("key.little.redo", LITTLE_KEY_CONTEXT, KeyModifier.CONTROL, InputConstants.KEY_Y, CATEGORY);
        
        event.register(KEY_UP);
        event.register(KEY_DOWN);
        event.register(KEY_RIGHT);
        event.register(KEY_LEFT);
        
        event.register(KEY_MIRROR);
        event.register(KEY_MARK);
        event.register(KEY_CONFIGURE);
        event.register(KEY_CONFIGURE_SECONDARY);
        event.register(KEY_BUILDING_MODE);
        
        TOOL_KEYS = new KeyMapping[] { KEY_UP, KEY_DOWN, KEY_RIGHT, KEY_LEFT, KEY_MIRROR, KEY_MARK, KEY_CONFIGURE, KEY_CONFIGURE_SECONDARY, KEY_BUILDING_MODE };
        
        event.register(KEY_UNDO);
        event.register(KEY_REDO);
    }
    
    private static void setup(final FMLClientSetupEvent event) {
        LittleGuiClientRegistry.init();
        
        // overlay.add(new OverlayControl(new GuiAxisIndicatorControl("axis"), OverlayPositionType.CENTER).setShouldRender(() -> PreviewRenderer.marked != null));
        NeoForge.EVENT_BUS.register(new LittleClientEventHandler());
        
        LEVEL_HANDLERS.register(LittleActionHandlerClient::new, x -> ACTION_HANDLER = x);
        LEVEL_HANDLERS.register(LittleVanillaInteractionHandlerClient::new, x -> INTERACTION_HANDLER = x);
        LEVEL_HANDLERS.register(LittleTiles.ANIMATION_HANDLERS::get, x -> ANIMATION_HANDLER = (LittleAnimationHandlerClient) x);
        LEVEL_HANDLERS.register(PREVIEW_RENDERER = new PreviewManager());
        LEVEL_HANDLERS.register(ITEM_RENDER_CACHE = new ItemRenderCache());
        LEVEL_HANDLERS.register(PLAYER_CONNECTION = new LittleClientPlayerConnection());
        LEVEL_HANDLERS.register(INTERACTION = new LittleInteractionHandlerClient());
        
        // Init overlays
        NeoForge.EVENT_BUS.register(LittleTilesProfilerOverlay.class);
        LEVEL_HANDLERS.register(OVERLAY_RENDERER = new OverlayRenderer());
        
        CreativeCoreClient.registerClientConfig(LittleTiles.MODID);
        
        // Register feature configuration
        ConfigHolderDynamic holder = LittleTiles.CONFIG.building.buildingMode;
        
        holder.clear();
        for (Entry<String, BuildingModeFeature> entry : BuildingModeFeatures.REGISTRY.entrySet())
            holder.registerValue(entry.getKey(), entry.getValue());
    }
    
    /** TODO(quasar): real renderers for animated structures / sized TNT / seats come with the render rewrite; until then they are invisible. */
    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(LittleTilesRegistry.SIZED_TNT_TYPE.get(), NoopRenderer::new);
        event.registerEntityRenderer(LittleTilesRegistry.ENTITY_LEVEL.get(), team.creative.littletiles.client.render.entity.LittleEntityRenderer::new);
        event.registerEntityRenderer(LittleTilesRegistry.ENTITY_ANIMATION.get(), team.creative.littletiles.client.render.entity.LittleEntityRenderer::new);
        event.registerEntityRenderer(LittleTilesRegistry.SIT_TYPE.get(), NoopRenderer::new);
    }
    
    private static void commands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(LiteralArgumentBuilder.<CommandSourceStack>literal("lt-debug").executes(x -> {
            if (LittleTilesProfilerOverlay.isActive())
                LittleTilesProfilerOverlay.stop();
            else
                LittleTilesProfilerOverlay.start();
            return Command.SINGLE_SUCCESS;
        }));
    }
    
    public static class LittleKeyMapping extends KeyMapping {
        
        private boolean ignoreModifier = false;
        
        public LittleKeyMapping(String description, IKeyConflictContext keyConflictContext, int keyCode, KeyMapping.Category category) {
            super(description, keyConflictContext, KeyModifier.NONE, InputConstants.Type.KEYBOARD, keyCode, category);
        }
        
        public LittleKeyMapping(String description, IKeyConflictContext keyConflictContext, KeyModifier keyModifier, int keyCode, KeyMapping.Category category) {
            super(description, keyConflictContext, keyModifier, InputConstants.Type.KEYBOARD, keyCode, category);
        }
        
        public LittleKeyMapping(String description, IKeyConflictContext keyConflictContext, KeyModifier keyModifier, InputConstants.Type type, int keyCode, KeyMapping.Category category) {
            super(description, keyConflictContext, keyModifier, type, keyCode, category);
        }
        
        public LittleKeyMapping ignoreModifier() {
            ignoreModifier = true;
            return this;
        }
        
        @Override
        public boolean isActiveAndMatches(Key keyCode) {
            if (ignoreModifier)
                return keyCode != InputConstants.UNKNOWN && keyCode.equals(getKey());
            return super.isActiveAndMatches(keyCode);
        }
        
        @Override
        public boolean same(KeyMapping other) {
            if (other instanceof LittleKeyMapping && super.same(other))
                return true;
            return false;
        }
    }
    
}

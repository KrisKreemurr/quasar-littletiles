package team.creative.littletiles.client.render.overlay;

import net.minecraft.gizmos.TextGizmo;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.joml.Vector3f;
import com.google.common.base.Strings;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.integration.IGuiIntegratedParent;
import team.creative.creativecore.common.gui.style.ControlFormatting;
import team.creative.creativecore.common.gui.style.GuiStyle;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.creativecore.common.util.math.vec.Vec2d;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.mc.LanguageUtils;
import team.creative.creativecore.common.util.type.list.SingletonList;
import team.creative.creativecore.common.util.type.list.Tuple;
import team.creative.creativecore.common.util.type.list.TupleList;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.level.LevelAwareHandler;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.gui.control.GuiActionDisplay;
import team.creative.littletiles.common.item.tooltip.IItemTooltip;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

/** LittleTiles HUD overlay: tool / item hints (bottom left), action messages and measurement labels.
 * <p>
 * Quasar MC (26.3): drawn on NeoForge's RenderGuiEvent.Post with the 26.x GuiGraphicsExtractor; measurement labels are world
 * text gizmos (the old screen projection used RenderSystem matrices that no longer exist). The CreativeCore overlay layer is
 * kept as a container for building-mode controls but isn't drawn yet. TODO(quasar): draw OverlayGuiLayer controls. */
public class OverlayRenderer implements LevelAwareHandler {
    
    private static final Minecraft MC = Minecraft.getInstance();
    private static final long MESSAGE_TIME = 5000;
    
    private OverlayGuiLayer transparentLayer;
    private final List<Message> messages = new ArrayList<>();
    
    private record Message(List<Component> lines, long time) {}
    
    public OverlayRenderer() {
        NeoForge.EVENT_BUS.addListener(this::renderPost);
    }
    
    public OverlayGuiLayer gui() {
        if (transparentLayer == null)
            transparentLayer = new OverlayGuiLayer(true, "overlay");
        return transparentLayer;
    }
    
    public void displayActionMessage(List<Component> message) {
        synchronized (messages) {
            messages.add(new Message(message, net.minecraft.util.Util.getMillis()));
            while (messages.size() > 4)
                messages.remove(0);
        }
    }
    
    /** World-space label (measurements). Must run inside a gizmo collector (PreviewManager calls tools from RenderFrameEvent.Pre). */
    public void renderLabel(Vec3 cam, Vec3d vec, Component component, int color) {
        try {
            Gizmos.billboardText(component.getString(), new Vec3(vec.x, vec.y, vec.z), TextGizmo.Style.forColorAndCentered(color));
        } catch (IllegalStateException e) {} // no collector active
    }
    
    public void renderPost(RenderGuiEvent.Post event) {
        Player player = MC.player;
        if (player == null)
            return;
        Font font = MC.font;
        GuiGraphicsExtractor graphics = event.getGuiGraphics();
        int width = MC.getWindow().getGuiScaledWidth();
        int height = MC.getWindow().getGuiScaledHeight();
        
        // Action messages (e.g. missing ingredients), centred above the hotbar, fading out.
        long now = net.minecraft.util.Util.getMillis();
        synchronized (messages) {
            messages.removeIf(m -> now - m.time > MESSAGE_TIME);
            int y = height - 72;
            for (int i = messages.size() - 1; i >= 0; i--) {
                Message m = messages.get(i);
                float left = 1F - (now - m.time) / (float) MESSAGE_TIME;
                int alpha = Math.max(16, Math.min(255, (int) (left * 4 * 255)));
                for (int l = m.lines.size() - 1; l >= 0; l--) {
                    Component line = m.lines.get(l);
                    y -= font.lineHeight + 1;
                    graphics.text(font, line, (width - font.width(line)) / 2, y, (alpha << 24) | 0xFFFFFF, true);
                }
                y -= 3;
            }
        }
        
        // Building Mode top bar (grid, measures, transform mode, toggles), one line across the top of the screen.
        if (LittleTilesClient.PREVIEW_RENDERER.tool() != null) {
            List<Component> infos = new ArrayList<>();
            for (var feature : LittleTilesClient.PREVIEW_RENDERER.tool().activeFeatures())
                if (feature instanceof team.creative.littletiles.client.tool.mode.BuildingModeTopBar.BuildingModeInfo info)
                    try {
                        Component text = info.quasar$info();
                        if (text != null)
                            infos.add(text);
                    } catch (RuntimeException e) {}
            if (!infos.isEmpty()) {
                int total = -10;
                for (Component c : infos)
                    total += font.width(c) + 10;
                graphics.fill(0, 0, width, font.lineHeight + 6, 0x90000000);
                int x = Math.max(4, (width - total) / 2);
                for (Component c : infos) {
                    graphics.text(font, c, x, 3, 0xFFFFFFFF, true);
                    x += font.width(c) + 10;
                }
            }
        }
        
        // Tool hint, or the held item's help text.
        Component tooltip = null;
        if (LittleTilesClient.PREVIEW_RENDERER.tool() != null)
            tooltip = LittleTilesClient.PREVIEW_RENDERER.tool().tooltip();
        if (tooltip == null && LittleTiles.CONFIG.rendering.showTooltip && player.getMainHandItem().getItem() instanceof IItemTooltip item) {
            ItemStack stack = player.getMainHandItem();
            var id = stack.getItem().builtInRegistryHolder().key().identifier();
            String tooltipKey = item.tooltipTranslateKey(stack, id.getNamespace() + "." + id.getPath() + ".tooltip");
            if (LanguageUtils.can(tooltipKey))
                tooltip = Component.translatable(tooltipKey, item.tooltipData(stack));
        }
        if (tooltip != null) {
            String[] lines = tooltip.getString().split("\\n");
            int y = height - 2;
            for (int i = lines.length - 1; i >= 0; i--) {
                String s = lines[i];
                if (Strings.isNullOrEmpty(s))
                    continue;
                y -= font.lineHeight;
                int w = font.width(s);
                int top = 2 + y;
                graphics.fill(1, top - 1, 2 + w + 1, top + font.lineHeight - 1, -1873784752);
                graphics.text(font, s, 2, top, 0xFFE0E0E0, false);
            }
        }
    }
    
    @Override
    public void unload() {
        synchronized (messages) {
            messages.clear();
        }
    }
    
    public static class OverlayGuiLayer extends GuiLayer {

        private GuiActionDisplay actionDisplay;
        public OverlayGuiLayer(boolean client, String name) { super(client, name); }

        private TupleList<GuiControl, OverlayPosition> positions;
        public void create() {}

        public void addOverlayControl(GuiControl control, OverlayPosition position) {}

        public boolean hasMinimumOuterSpacing() { return false; }

        @Deprecated
        public GuiParent add(boolean conditional, Supplier<GuiControl> controlSupplier) { return null; }

        @Deprecated
        public GuiParent add(GuiControl control) { return null; }

        @Deprecated
        public GuiParent add(GuiControl... controls) { return null; }

        public ControlFormatting getControlFormatting() { return null; }

        public boolean hasGrayBackground() { return false; }

        public void flowY(int width, int height, int preferred) {}

        public boolean isExpandableX() { return false; }

        public boolean isExpandableY() { return false; }

        public void clearMessages() {}

        public void addMessage(List<Component> message) {}


    }

    public static enum OverlayPosition {

        TOP_STRETCH, CENTER, ACTION_BAR;

    }

    public static record OverlayText(Vec2d pos, Component component, int color) {

    }

}


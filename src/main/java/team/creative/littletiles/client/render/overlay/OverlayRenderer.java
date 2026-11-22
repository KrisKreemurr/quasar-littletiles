package team.creative.littletiles.client.render.overlay;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.joml.Vector3f;
import com.google.common.base.Strings;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderBlockScreenEffectEvent;
import net.neoforged.neoforge.client.event.RenderBlockScreenEffectEvent.OverlayType;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.GuiControlRect;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.integration.IGuiIntegratedParent;
import team.creative.creativecore.common.gui.integration.ScreenEventListener;
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

public class OverlayRenderer implements LevelAwareHandler {

    private static Minecraft MC;
    private boolean doneInit;
    private OverlayGuiLayer transparentLayer;
    private SingletonList<GuiLayer> layers;
    private Screen screen;
    private ScreenEventListener listener;
    private int lastWidth;
    private int lastHeight;
    private List<OverlayText> scheduled;
    public OverlayRenderer() {}

    public OverlayGuiLayer gui() { return null; }

    public void displayActionMessage(List<Component> message) {}

    public void renderBlockOverlay(RenderBlockScreenEffectEvent event) {}

    private Vec2d projectToScreen(Vec3 cam, Vec3d pos) { return null; }

    public void renderLabel(Vec3 cam, Vec3d vec, Component component, int color) {}

    public void renderPost(RenderGuiEvent.Post event) {}

    public boolean isContainer() { return false; }

    public boolean isClient() { return false; }

    public Player getPlayer() { return null; }

    public void closeTopLayer() {}

    public void closeLayer(GuiLayer layer) {}

    public List<GuiLayer> getLayers() { return null; }

    public GuiLayer getTopLayer() { return null; }

    public void openLayer(GuiLayer layer) {}

    public void closeLayer(int layer) {}

    public void send(CreativePacket message) {}

    public void unload() {}

    public Provider provider() { return null; }

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

        protected OverlayGuiLayer() {}

    }

    public static enum OverlayPosition {

        TOP_STRETCH, CENTER, ACTION_BAR;
        protected abstract void positionControl(GuiControlRect control, int width, int height);

    }

    public static record OverlayText(Vec2d pos, Component component, int color) {

    }

}


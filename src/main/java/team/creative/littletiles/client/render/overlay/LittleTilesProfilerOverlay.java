package team.creative.littletiles.client.render.overlay;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import com.google.common.base.Strings;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import team.creative.creativecore.common.util.type.list.Pair;
import team.creative.creativecore.common.util.type.list.PairList;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.render.cache.build.RenderingThread;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

public class LittleTilesProfilerOverlay {

    private static boolean showDebugInfo;
    public static int chunkUpdates;
    public static int uploaded;
    private static int updateTime;
    private static int updateTicker;
    private static List<Long> durations;
    private static long averageDuration;
    private static DecimalFormat df;
    private static Minecraft mc;
    public static boolean isActive() { return false; }

    private static String format(Object value) { return null; }

    private static String format(PairList<String, Object> details) { return null; }

    public static void start() {}

    public static void stop() {}

    public static void finishBuildingCache(long duration) {}

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Post event) {}

    @SubscribeEvent
    public static void onRender(RenderGuiEvent.Post event) {}

}


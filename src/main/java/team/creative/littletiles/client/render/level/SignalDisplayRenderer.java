package team.creative.littletiles.client.render.level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import net.minecraft.client.Minecraft;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.common.NeoForge;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.structure.type.premade.signal.LittleSignalDisplay;

/** Quasar MC (26.3): draws signal display pixels (was a per-display GL texture + BlockEntityRenderer). Displays register
 * themselves when their block is meshed; each frame every lit pixel is a filled quad gizmo, so a state change shows the
 * next frame without rebuilding anything. */
public final class SignalDisplayRenderer {

    private static final Set<LittleSignalDisplay> DISPLAYS = Collections.newSetFromMap(new WeakHashMap<>());
    private static boolean logged;

    private SignalDisplayRenderer() {}

    public static void init() {
        NeoForge.EVENT_BUS.addListener(SignalDisplayRenderer::render);
    }

    public static void track(LittleSignalDisplay display) {
        synchronized (DISPLAYS) {
            DISPLAYS.add(display);
        }
    }

    private static void render(RenderFrameEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null)
            return;
        ArrayList<LittleSignalDisplay> list;
        synchronized (DISPLAYS) {
            if (DISPLAYS.isEmpty())
                return;
            list = new ArrayList<>(DISPLAYS);
        }
        var cam = mc.gameRenderer.mainCamera().position();
        for (LittleSignalDisplay display : list) {
            try {
                if (!display.hasLevel()) {
                    synchronized (DISPLAYS) {
                        DISPLAYS.remove(display);
                    }
                    continue;
                }
                if (display.getStructureLevel() != mc.level) // inside animated structures: not drawn yet
                    continue;
                var pos = display.getStructurePos();
                if (pos.distToCenterSqr(cam) > LittleSignalDisplay.RENDER_DISTANCE * LittleSignalDisplay.RENDER_DISTANCE)
                    continue;
                display.quasar$renderPixels((quad, argb) -> Gizmos.rect(quad[0], quad[1], quad[2], quad[3], GizmoStyle.fill(argb)));
            } catch (Throwable e) {
                if (!logged) {
                    logged = true;
                    LittleTiles.LOGGER.error("Failed to draw a signal display (logged once)", e);
                }
            }
        }
    }
}

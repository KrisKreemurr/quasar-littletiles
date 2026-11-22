package team.creative.littletiles.client.gui;

import team.creative.creativecore.client.gui.registry.GuiClientRegistry;
import team.creative.littletiles.client.gui.animation.GuiClientAnimationViewer;
import team.creative.littletiles.client.gui.animation.GuiClientIsoAnimationViewer;
import team.creative.littletiles.client.gui.control.GuiClientActionDisplay;
import team.creative.littletiles.client.gui.control.GuiClientBlueprintAddEntry;
import team.creative.littletiles.client.gui.control.GuiClientColorProgressBar;
import team.creative.littletiles.client.gui.control.GuiClientSlotControlSelect;
import team.creative.littletiles.client.gui.signal.GuiClientSignalController;
import team.creative.littletiles.client.gui.signal.GuiClientSignalNode;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.littletiles.common.gui.control.GuiActionDisplay;
import team.creative.littletiles.common.gui.control.animation.GuiAnimationViewer;
import team.creative.littletiles.common.gui.control.animation.GuiIsoAnimationViewer;
import team.creative.littletiles.common.gui.tool.blueprint.GuiBlueprintAdd.GuiBlueprintAddEntry;
import team.creative.littletiles.common.gui.control.GuiColorProgressBar;
import team.creative.littletiles.common.gui.signal.GuiSignalController;
import team.creative.littletiles.common.gui.signal.node.GuiSignalNode;
import team.creative.littletiles.common.gui.structure.GuiBlankOMatic.GuiSlotControlSelect;

/** Client halves of LittleTiles' custom GUI controls (CreativeCore 26.x splits every control into common state + a client dist handler). */
public class LittleGuiClientRegistry {
    
    public static void init() {
        GuiClientRegistry.register(GuiActionDisplay.class, GuiClientActionDisplay::new);
        GuiClientRegistry.register(GuiColorProgressBar.class, GuiClientColorProgressBar::new);
        GuiClientRegistry.register(GuiSlotControlSelect.class, GuiClientSlotControlSelect::new);
        GuiClientRegistry.register(GuiSignalController.class, GuiClientSignalController::new);
        GuiClientRegistry.register(GuiSignalNode.class, GuiClientSignalNode::new);
        GuiClientRegistry.register(GuiBlueprintAddEntry.class, GuiClientBlueprintAddEntry::new);
        GuiClientRegistry.register(GuiAnimationViewer.class, GuiClientAnimationViewer::new);
        GuiClientRegistry.register(GuiIsoAnimationViewer.class, GuiClientIsoAnimationViewer::new);
        GuiClientRegistry.registerSpecial(control -> LittleClientLayer.handles(control) ? new LittleClientLayer((GuiLayer) control) : null);
    }
    
}

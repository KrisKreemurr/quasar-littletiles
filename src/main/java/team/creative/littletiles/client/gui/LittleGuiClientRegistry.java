package team.creative.littletiles.client.gui;

import team.creative.creativecore.client.gui.registry.GuiClientRegistry;
import team.creative.littletiles.client.gui.control.GuiClientActionDisplay;
import team.creative.littletiles.client.gui.control.GuiClientColorProgressBar;
import team.creative.littletiles.client.gui.control.GuiClientSlotControlSelect;
import team.creative.littletiles.client.gui.signal.GuiClientSignalController;
import team.creative.littletiles.client.gui.signal.GuiClientSignalNode;
import team.creative.littletiles.common.gui.control.GuiActionDisplay;
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
    }
    
}

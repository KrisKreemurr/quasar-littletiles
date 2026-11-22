package team.creative.littletiles.common.structure.registry.gui;

import team.creative.creativecore.common.gui.IGuiParent;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.littletiles.common.gui.tool.blueprint.GuiTreeItemStructure;
import team.creative.littletiles.common.structure.LittleStructure;

@OnlyIn(Dist.CLIENT)
public class LittleStructurePremadeGui extends LittleStructureGuiControl {
    
    public LittleStructurePremadeGui(IGuiParent parent, LittleStructureGui gui, GuiTreeItemStructure item) {
        super(parent, gui, item);
    }
    
    @Override
    public void create(LittleStructure structure) {}
    
    @Override
    public LittleStructure save(LittleStructure structure) {
        if (item.group.getStructureTag() != null)
            structure.load(item.group.getStructureTag(), item.provider());
        return structure;
    }
    
    @Override
    public boolean canChangeType() {
        return false;
    }
    
}
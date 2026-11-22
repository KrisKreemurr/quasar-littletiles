package team.creative.littletiles.common.structure.registry.gui;

import team.creative.creativecore.common.gui.IGuiParent;

import javax.annotation.Nullable;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.control.simple.GuiCheckBox;
import team.creative.littletiles.common.gui.tool.blueprint.GuiTreeItemStructure;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.type.LittleNoClipStructure;

@OnlyIn(Dist.CLIENT)
public class LittleNoClipGui extends LittleStructureGuiControl {
    
    public LittleNoClipGui(IGuiParent parent, LittleStructureGui gui, GuiTreeItemStructure item) {
        super(parent, gui, item);
    }
    
    @Override
    public void create(@Nullable LittleStructure structure) {
        boolean slowness = true;
        if (structure instanceof LittleNoClipStructure)
            slowness = ((LittleNoClipStructure) structure).web;
        add(new GuiCheckBox(this, "web", slowness).setTranslate("gui.noclip.slowness"));
    }
    
    @Override
    public LittleStructure save(LittleStructure structure) {
        ((LittleNoClipStructure) structure).web = get("web", GuiCheckBox.class).get();
        return structure;
    }
    
}
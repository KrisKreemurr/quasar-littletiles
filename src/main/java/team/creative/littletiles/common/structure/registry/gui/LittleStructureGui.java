package team.creative.littletiles.common.structure.registry.gui;

import team.creative.creativecore.common.gui.IGuiParent;
import com.mojang.datafixers.util.Function3;

import net.minecraft.network.chat.Component;
import team.creative.littletiles.common.gui.tool.blueprint.GuiTreeItemStructure;
import team.creative.littletiles.common.structure.LittleStructureType;

public record LittleStructureGui(String id, LittleStructureType type, Function3<IGuiParent, LittleStructureGui, GuiTreeItemStructure, LittleStructureGuiControl> factory, boolean supportsName) {
    
    public LittleStructureGui(String id, LittleStructureType type, Function3<IGuiParent, LittleStructureGui, GuiTreeItemStructure, LittleStructureGuiControl> factory, boolean supportsName) {
        this.id = id;
        this.type = type;
        this.factory = factory;
        this.supportsName = supportsName;
    }
    
    public LittleStructureGui(String id, LittleStructureType type, Function3<IGuiParent, LittleStructureGui, GuiTreeItemStructure, LittleStructureGuiControl> factory) {
        this(id, type, factory, true);
    }
    
    public Component translatable() {
        return Component.translatable("structure." + id);
    }
    
    public LittleStructureGuiControl create(IGuiParent parent, GuiTreeItemStructure item) {
        return factory.apply(parent, this, item);
    }
    
}

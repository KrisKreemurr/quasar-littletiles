package team.creative.littletiles.common.gui.control;

import team.creative.creativecore.common.gui.IGuiParent;

import team.creative.creativecore.common.gui.control.parent.GuiColumn;
import team.creative.creativecore.common.gui.control.parent.GuiLabeledControl;
import team.creative.creativecore.common.gui.control.parent.GuiRow;
import team.creative.creativecore.common.gui.control.parent.GuiTable;
import team.creative.creativecore.common.gui.control.simple.GuiTextfield;
import team.creative.creativecore.common.gui.event.GuiControlChangedEvent;
import team.creative.littletiles.common.structure.animation.PhysicalState;

public class GuiPhysicalStateControl extends GuiTable {
    
    public GuiPhysicalStateControl(IGuiParent parent, String name, PhysicalState state) {
        super(parent, name);
        GuiRow offset = new GuiRow(this);
        addRow(offset);
        GuiColumn offX = new GuiColumn(this);
        offset.addColumn(offX);
        offX.add(new GuiLabeledControl(this, "gui.door.offx", new GuiTextfield(this, "offX", "" + state.offX()).setFloatOnly()));
        GuiColumn offY = new GuiColumn(this);
        offset.addColumn(offY);
        offY.add(new GuiLabeledControl(this, "gui.door.offy", new GuiTextfield(this, "offY", "" + state.offY()).setFloatOnly()));
        GuiColumn offZ = new GuiColumn(this);
        offset.addColumn(offZ);
        offZ.add(new GuiLabeledControl(this, "gui.door.offz", new GuiTextfield(this, "offZ", "" + state.offZ()).setFloatOnly()));
        
        GuiRow rotation = new GuiRow(this);
        addRow(rotation);
        GuiColumn rotX = new GuiColumn(this);
        rotation.addColumn(rotX);
        rotX.add(new GuiLabeledControl(this, "gui.door.rotx", new GuiTextfield(this, "rotX", "" + state.rotX()).setFloatOnly()));
        GuiColumn rotY = new GuiColumn(this);
        rotation.addColumn(rotY);
        rotY.add(new GuiLabeledControl(this, "gui.door.roty", new GuiTextfield(this, "rotY", "" + state.rotY()).setFloatOnly()));
        GuiColumn rotZ = new GuiColumn(this);
        rotation.addColumn(rotZ);
        rotZ.add(new GuiLabeledControl(this, "gui.door.rotz", new GuiTextfield(this, "rotZ", "" + state.rotZ()).setFloatOnly()));
        
        registerEventChanged(x -> {
            if (x.control instanceof GuiTextfield)
                raiseEvent(new GuiControlChangedEvent(this));
        });
    }
    
    public PhysicalState create() {
        PhysicalState state = new PhysicalState();
        state.offX(get("offX", GuiTextfield.class).parseDouble());
        state.offY(get("offY", GuiTextfield.class).parseDouble());
        state.offZ(get("offZ", GuiTextfield.class).parseDouble());
        state.rotX(get("rotX", GuiTextfield.class).parseDouble());
        state.rotY(get("rotY", GuiTextfield.class).parseDouble());
        state.rotZ(get("rotZ", GuiTextfield.class).parseDouble());
        return state;
    }
    
}

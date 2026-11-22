package team.creative.littletiles.common.gui.structure;

import team.creative.creativecore.common.gui.IGuiParent;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.SimpleContainer;
import net.minecraft.util.Prediction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.control.inventory.GuiInventoryGrid;
import team.creative.creativecore.common.gui.control.inventory.GuiPlayerInventoryGrid;
import team.creative.creativecore.common.gui.control.inventory.GuiSlotViewer;
import team.creative.creativecore.common.gui.control.simple.GuiButton;
import team.creative.creativecore.common.gui.control.simple.GuiLabel;
import team.creative.creativecore.common.gui.control.simple.GuiProgressbar;
import team.creative.creativecore.common.gui.control.simple.GuiShowItem;
import team.creative.creativecore.common.gui.event.GuiControlChangedEvent;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.style.ControlFormatting;
import team.creative.creativecore.common.gui.sync.GuiSyncLocal;
import team.creative.creativecore.common.util.mc.PlayerUtils;
import team.creative.creativecore.common.util.text.TextBuilder;
import team.creative.creativecore.common.util.type.list.Pair;
import team.creative.littletiles.common.recipe.BlankOMaticRecipeRegistry;
import team.creative.littletiles.common.recipe.BlankOMaticRecipeRegistry.BleachRecipe;
import team.creative.littletiles.common.structure.type.premade.LittleBlankOMatic;

public class GuiBlankOMatic extends GuiLayer {
    
    public LittleBlankOMatic whitener;
    public SimpleContainer whiteInput;
    
    public final GuiSyncLocal<IntTag> VOLUME = getSyncHolder().register("volume", x -> get("volume", GuiProgressbar.class).pos = x.value());
    public final GuiSyncLocal<CompoundTag> CRAFT = getSyncHolder().register("craft", x -> {
        int amount = x.getIntOr("amount", 0);
        ItemStack stack = whitener.inventory.getItem(0);
        int stackSize = 1;
        if (amount > 1)
            stackSize = stack.getCount();
        List<BleachRecipe> recipes = BlankOMaticRecipeRegistry.getRecipe(stack);
        if (!recipes.isEmpty()) {
            int index = 0;
            int variant = x.getIntOr("variant", 0);
            BleachRecipe selected = null;
            Block block = null;
            for (BleachRecipe recipe : recipes) {
                if (variant >= index + recipe.results.length)
                    index += recipe.results.length;
                else {
                    selected = recipe;
                    block = recipe.results[variant - index];
                    break;
                }
            }
            if (selected == null)
                return;
            boolean result = !selected.isResult(stack);
            if (result && selected.needed > 0)
                stackSize = Math.min(stackSize, whitener.whiteColor / selected.needed);
            ItemStack newStack = new ItemStack(block, stackSize);
            stack.shrink(stackSize);
            whitener.inventory.setItem(0, stack);
            Player player = getPlayer();
            if (!player.addItem(newStack))
                player.drop(newStack, false, Prediction.PREDICTED);
            if (result && selected.needed > 0)
                whitener.whiteColor -= stackSize * selected.needed;
            VOLUME.send(IntTag.valueOf(whitener.whiteColor));
        }
    });
    
    public GuiBlankOMatic(boolean client, LittleBlankOMatic whitener) {
        super(client, "whitener", 170, 200);
        this.whitener = whitener;
        this.whiteInput = new SimpleContainer(1) {
            @Override
            public void setChanged() {
                super.setChanged();
                whitener.markDirty();
            }
        };
        
        registerEventChanged(x -> {
            if (x.control.is("variant"))
                updateLabel();
        });
    }
    
    @Override
    public void init() {
        super.init();
        if (!isClient())
            VOLUME.send(IntTag.valueOf(whitener.whiteColor));
    }
    
    @Override
    public void closed() {
        super.closed();
        PlayerUtils.addOrDrop(getPlayer(), whiteInput);
    }
    
    @Override
    public void create() {
        setFlow(GuiFlow.STACK_Y);
        GuiParent machine = new GuiParent(this);
        add(machine);
        GuiParent left = new GuiParent(this, GuiFlow.STACK_Y);
        machine.add(left);
        
        GuiParent leftUpper = new GuiParent(this);
        left.add(leftUpper.setExpandable());
        
        leftUpper.add(new GuiInventoryGrid(this, "inv", whitener.inventory, 1, 1, (c, i) -> new Slot(c, i, 0, 0) {
            
            @Override
            public boolean mayPlace(ItemStack stack) {
                return !BlankOMaticRecipeRegistry.getRecipe(stack).isEmpty();
            }
            
        }).addListener(x -> updateVariants()));
        
        leftUpper.add(new GuiVariantSelector(this, "variant"));
        
        GuiParent leftLower = new GuiParent(this);
        left.add(leftLower);
        
        leftLower.add(new GuiInventoryGrid(this, "input", whiteInput, 1, 1, (c, i) -> new Slot(c, i, 0, 0) {
            
            @Override
            public boolean mayPlace(ItemStack stack) {
                return BlankOMaticRecipeRegistry.getVolume(stack) > 0;
            }
            
        }).addListener(x -> {
            ItemStack stack = whiteInput.getItem(0);
            int volume = BlankOMaticRecipeRegistry.getVolume(stack);
            if (volume > 0) {
                boolean added = false;
                while (!stack.isEmpty() && volume + whitener.whiteColor <= BlankOMaticRecipeRegistry.bleachTotalVolume) {
                    stack.shrink(1);
                    whitener.whiteColor += volume;
                    added = true;
                }
                if (added)
                    getPlayer().playSound(SoundEvents.BREWING_STAND_BREW, 1.0F, 1.0F);
                VOLUME.send(IntTag.valueOf(whitener.whiteColor));
            }
        }));
        
        leftLower.add(new GuiProgressbar(this, "volume", whitener.whiteColor, BlankOMaticRecipeRegistry.bleachTotalVolume).setExpandableX());
        
        GuiParent right = new GuiParent(this, GuiFlow.STACK_Y);
        machine.add(right);
        
        right.add(new GuiButton(this, "craft", x -> {
            CompoundTag nbt = new CompoundTag();
            nbt.putInt("amount", net.minecraft.client.Minecraft.getInstance().hasShiftDown() ? 2 : net.minecraft.client.Minecraft.getInstance().hasControlDown() ? 1 : 0);
            nbt.putInt("variant", get("variant", GuiVariantSelector.class).selected);
            CRAFT.send(nbt);
        }).setTranslate("gui.blancomatic.craft"));
        
        right.add(new GuiLabel(this, "cost"));
        right.add(new GuiShowItem(this, "item").setDim(50, 50));
        
        add(new GuiPlayerInventoryGrid(this));
        
        updateVariants();
    }
    
    public void updateLabel() {
        GuiVariantSelector selector = get("variant", GuiVariantSelector.class);
        Block block = selector.getSeleted();
        ItemStack stack;
        if (block != null)
            stack = new ItemStack(block, 1);
        else
            stack = ItemStack.EMPTY;
        get("item", GuiShowItem.class).stack = stack;
        BleachRecipe recipe = selector.getSelectedRecipe();
        get("cost", GuiLabel.class).setTitle(recipe == null ? Component.empty() : Component.translatable("gui.blancomatic.cost").append(": " + recipe.needed));
    }
    
    public void updateVariants() {
        GuiVariantSelector selector = (GuiVariantSelector) get("variant");
        selector.setRecipes(BlankOMaticRecipeRegistry.getRecipe(whitener.inventory.getItem(0)));
        updateLabel();
    }
    
    public static class GuiVariantSelector extends GuiParent {
        
        public List<Pair<BleachRecipe, Block>> states;
        public int selected = 0;
        
        public GuiVariantSelector(IGuiParent parent, String name) {
            super(parent, name, GuiFlow.FIT_X);
            setFormatting(ControlFormatting.TRANSPARENT);
        }
        
        public BleachRecipe getSelectedRecipe() {
            if (selected >= 0 && selected < states.size())
                return states.get(selected).key;
            return null;
        }
        
        public Block getSeleted() {
            if (selected >= 0 && selected < states.size())
                return states.get(selected).value;
            return null;
        }
        
        public void select(int index) {
            this.selected = index;
            for (int i = 0; i < size(); i++) {
                GuiSlotControlSelect slot = (GuiSlotControlSelect) get(i);
                slot.selected = index == i;
            }
            raiseEvent(new GuiControlChangedEvent(this));
        }
        
        public void setRecipes(List<BleachRecipe> recipes) {
            clear();
            
            List<Pair<BleachRecipe, Block>> states = new ArrayList<>();
            for (BleachRecipe recipe : recipes)
                for (int j = 0; j < recipe.results.length; j++)
                    states.add(new Pair<>(recipe, recipe.results[j]));
                
            this.states = states;
            
            for (int i = 0; i < states.size(); i++)
                add(new GuiSlotControlSelect(this, this, i, new ItemStack(states.get(i).value)).setTooltip(new TextBuilder().translate("gui.blancomatic.cost").text(": " + states.get(
                    i).key.needed).build()));
            
            if (selected >= states.size())
                select(0);
            else
                select(selected);
        }
        
    }
    
    public static class GuiSlotControlSelect extends GuiSlotViewer {
        
        public final GuiVariantSelector selector;
        public final int index;
        public boolean selected = false;
        
        public GuiSlotControlSelect(IGuiParent parent, GuiVariantSelector selector, int index, ItemStack stack) {
            super(parent, stack);
            this.selector = selector;
            this.index = index;
        }
        
    }
    
}

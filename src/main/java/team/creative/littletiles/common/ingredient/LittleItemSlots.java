package team.creative.littletiles.common.ingredient;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

/**
 * Slot view LittleTiles' ingredient code works on. Replaces NeoForge's removed {@code IItemHandler}.
 * <p>
 * Container-backed slots hand out the live stacks (LittleInventory edits counts in place, as it did with the player inventory wrapper). Slots backed by
 * NeoForge's 26.x {@link ResourceHandler} are read-only snapshots for now.
 */
public interface LittleItemSlots {
    
    public int size();
    
    public ItemStack get(int index);
    
    /** Puts the stack into an empty slot. Returns false if this view can't be written. */
    public boolean insert(int index, ItemStack stack);
    
    public static LittleItemSlots of(Container container) {
        return new LittleItemSlots() {
            
            @Override
            public int size() {
                return container.getContainerSize();
            }
            
            @Override
            public ItemStack get(int index) {
                return container.getItem(index);
            }
            
            @Override
            public boolean insert(int index, ItemStack stack) {
                container.setItem(index, stack);
                container.setChanged();
                return true;
            }
        };
    }
    
    // TODO(quasar): writable through Transactions so tools can also take from/give to backpacks and other item-capability inventories.
    public static LittleItemSlots readOnly(ResourceHandler<ItemResource> handler) {
        return new LittleItemSlots() {
            
            @Override
            public int size() {
                return handler.size();
            }
            
            @Override
            public ItemStack get(int index) {
                ItemResource resource = handler.getResource(index);
                if (resource.isEmpty())
                    return ItemStack.EMPTY;
                return resource.toStack(handler.getAmountAsInt(index));
            }
            
            @Override
            public boolean insert(int index, ItemStack stack) {
                return false;
            }
        };
    }
    
}

package team.creative.littletiles.client.render.item;

import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.item.ItemMultiTiles;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

@OnlyIn(Dist.CLIENT)
public interface LittleModelItemPreview {

    public LittleElement getElement(ItemStack stack);

    public default ItemStack getPreview(ItemStack stack) { return null; }

}


package team.creative.littletiles.client.item;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;
import team.creative.littletiles.common.ingredient.ColorIngredient;
import team.creative.littletiles.common.item.ItemColorIngredient;

/** 26.x replacement for the old "littletiles:filled" item property: how full a color bottle is (0..1). */
public record BottleFilledProperty() implements RangeSelectItemModelProperty {
    
    public static final MapCodec<BottleFilledProperty> MAP_CODEC = MapCodec.unit(new BottleFilledProperty());
    
    @Override
    public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
        if (stack.getItem() instanceof ItemColorIngredient bottle)
            return bottle.getColor(stack) / (float) ColorIngredient.BOTTLE_SIZE;
        return 0;
    }
    
    @Override
    public MapCodec<BottleFilledProperty> type() {
        return MAP_CODEC;
    }
}

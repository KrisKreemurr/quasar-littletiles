package team.creative.littletiles.client.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import team.creative.littletiles.LittleTilesRegistry;

/** Quasar MC (26.3): item tint {@code littletiles:color} - the stack's LittleTiles colour component (paint brush colour,
 * blueprint colours). Replaces the old ItemColor handlers; 26.x item tints are declared in the item JSON. */
public record ColorTint(boolean secondary, int defaultColor) implements ItemTintSource {

    public static final MapCodec<ColorTint> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(Codec.BOOL.optionalFieldOf("secondary", false).forGetter(ColorTint::secondary),
        Codec.INT.optionalFieldOf("default", -1).forGetter(ColorTint::defaultColor)).apply(i, ColorTint::new));

    @Override
    public int calculate(ItemStack stack, ClientLevel level, LivingEntity entity) {
        Integer color = stack.get(secondary ? LittleTilesRegistry.COLOR_SECONDARY.value() : LittleTilesRegistry.COLOR.value());
        return 0xFF000000 | (color == null ? defaultColor : color);
    }

    @Override
    public MapCodec<ColorTint> type() {
        return MAP_CODEC;
    }
}

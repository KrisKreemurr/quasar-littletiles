package team.creative.littletiles.client.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import team.creative.littletiles.common.item.ItemPremadeStructure;

/** Quasar MC (26.3): select property {@code littletiles:premade} = the premade structure id of the stack, so premade
 * types with their own item texture (signal cables, inputs/outputs, displays: the numbered icons) can use it. */
public record PremadeIdProperty() implements SelectItemModelProperty<String> {

    public static final PremadeIdProperty INSTANCE = new PremadeIdProperty();
    public static final SelectItemModelProperty.Type<PremadeIdProperty, String> TYPE = SelectItemModelProperty.Type.create(MapCodec.unit(INSTANCE), Codec.STRING);

    @Override
    public String get(ItemStack stack, ClientLevel level, LivingEntity entity, int seed, ItemDisplayContext context) {
        return ItemPremadeStructure.getPremadeId(stack);
    }

    @Override
    public Codec<String> valueCodec() {
        return Codec.STRING;
    }

    @Override
    public SelectItemModelProperty.Type<PremadeIdProperty, String> type() {
        return TYPE;
    }
}

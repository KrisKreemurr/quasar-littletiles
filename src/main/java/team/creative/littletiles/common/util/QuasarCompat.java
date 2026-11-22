package team.creative.littletiles.common.util;

import com.google.gson.JsonParser;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.JsonOps;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.item.ItemStack;

/** Small 26.x replacements for helpers that Minecraft removed. Formats on disk stay the same. */
public class QuasarCompat {
    
    /** Replaces {@code TagParser.parseTag}. */
    public static CompoundTag parseTag(String text) throws CommandSyntaxException {
        return TagParser.parseCompoundFully(text);
    }
    
    /** Replaces {@code ItemStack.parseOptional} (same NBT layout, via the optional item codec). */
    public static ItemStack loadItem(HolderLookup.Provider provider, CompoundTag nbt) {
        if (nbt == null || nbt.isEmpty())
            return ItemStack.EMPTY;
        return ItemStack.OPTIONAL_CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), nbt).result().orElse(ItemStack.EMPTY);
    }
    
    /** Replaces {@code ItemStack.saveOptional}. */
    public static Tag saveItem(HolderLookup.Provider provider, ItemStack stack) {
        if (stack.isEmpty())
            return new CompoundTag();
        return ItemStack.OPTIONAL_CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), stack).getOrThrow();
    }
    
    /** Replaces {@code ParserUtils.parseJson(registries, reader, ComponentSerialization.CODEC)}. */
    public static Component parseComponent(HolderLookup.Provider provider, String json) {
        return ComponentSerialization.CODEC.parse(provider.createSerializationContext(JsonOps.INSTANCE), JsonParser.parseString(json)).getOrThrow();
    }
    
}

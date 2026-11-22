package team.creative.littletiles.common.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Quasar MC (26.3 port): entities save through ValueInput/ValueOutput now. LittleTiles keeps its
 * CompoundTag save code; these helpers pass the exact same tags through, so saves stay byte-compatible
 * with LittleTiles 1.21.1 worlds.
 */
public final class QuasarNbtBridge {
    
    private QuasarNbtBridge() {}
    
    /** Everything stored in this input, as the CompoundTag the old code expects. */
    public static CompoundTag read(ValueInput input) {
        CompoundTag nbt = new CompoundTag();
        for (String key : input.keySet())
            input.read(key, ExtraCodecs.NBT).ifPresent(tag -> nbt.put(key, tag));
        return nbt;
    }
    
    /** Merges the tag's entries into the output, unchanged. */
    public static void write(ValueOutput output, CompoundTag nbt) {
        output.store(nbt);
    }
}

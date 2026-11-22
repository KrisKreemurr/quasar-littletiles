package team.creative.littletiles.common.gui.handler;

import com.mojang.datafixers.util.Function3;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.creator.GuiCreator;
import team.creative.littletiles.common.action.exception.LittleActionException;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
import team.creative.littletiles.common.math.location.TileLocation;

public class LittleTileGuiCreator extends GuiCreator {
    
    public LittleTileGuiCreator(Function3<CompoundTag, Player, LittleTileContext, GuiLayer> function) {
        super((nbt, player) -> {
            try {
                return function.apply(nbt, player, new TileLocation(nbt.getCompoundOrEmpty("location")).find(player.level()));
            } catch (LittleActionException e) {
                e.printStackTrace();
                return null;
            }
        });
    }
    
    public void open(Player player, LittleTileContext context) {
        open(new CompoundTag(), player, context);
    }
    
    public void open(CompoundTag nbt, Player player, LittleTileContext context) {
        nbt.put("location", new TileLocation(context).write(new CompoundTag()));
        openGui(nbt, player);
    }
    
}

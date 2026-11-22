package team.creative.littletiles.client.render.item;

import java.util.Collections;
import java.util.List;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.world.item.ItemStack;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.client.render.model.box.ItemModelBox;
import team.creative.littletiles.api.common.tool.ILittlePlacer;
import team.creative.littletiles.client.LittleTilesClient;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

public class LittleModelItemTiles extends ItemModelBox {

    public LittleModelItemTiles() {}

    public List<? extends RenderBox> getBoxes(ItemStack stack, boolean translucent) { return null; }

    public boolean checkTranslucentLayer(ItemStack stack) { return false; }

    public boolean hasTranslucentLayer(ItemStack stack) { return false; }

    public void saveCachedModel(boolean translucent, List<BakedQuad> cachedQuads, ItemStack stack, boolean threaded) {}

    public List<BakedQuad> getCachedModel(boolean translucent, ItemStack stack, boolean threaded) { return null; }

}


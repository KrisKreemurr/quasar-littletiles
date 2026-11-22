package team.creative.littletiles.client.render.item;

import java.util.Collections;
import java.util.List;
import net.minecraft.client.renderer.block.model.BakedQuad;
import team.creative.littletiles.LittleTiles;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

public class ItemModelCache {

    private boolean building;
    private List<BakedQuad> cache;
    private long lastUsed;
    public ItemModelCache() {}

    public boolean expired() { return false; }

    public void setQuads(boolean translucent, List<BakedQuad> baked) {}

    public boolean isBuilding() { return false; }

    public void complete() {}

    public List<BakedQuad> getQuads(boolean translucent) { return null; }

    public boolean hasTranslucent() { return false; }

}


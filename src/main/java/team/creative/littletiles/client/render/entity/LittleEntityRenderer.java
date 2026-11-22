package team.creative.littletiles.client.render.entity;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.InventoryMenu;
import team.creative.littletiles.common.entity.LittleEntity;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

public class LittleEntityRenderer {

    public LittleEntityRenderer(EntityRendererProvider.Context context) { super(context); }

    public boolean shouldRender(LittleEntity animation, Frustum frustum, double camX, double camY, double camZ) { return false; }

    public Identifier getTextureLocation(LittleEntity animation) { return null; }

    public static boolean isVisible(LittleEntity animation, Frustum frustum, double camX, double camY, double camZ) { return false; }

    protected LittleEntityRenderer() {}

}


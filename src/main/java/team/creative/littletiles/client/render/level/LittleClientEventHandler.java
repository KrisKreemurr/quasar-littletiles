package team.creative.littletiles.client.render.level;

import org.joml.Matrix4f;
import org.joml.Vector3d;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.mc.TickUtils;
import team.creative.creativecore.common.util.type.list.Pair;
import team.creative.littletiles.client.render.cache.build.RenderingThread;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

public class LittleClientEventHandler {

    private static Identifier RES_UNDERWATER_OVERLAY;
    @SubscribeEvent
    public synchronized void levelUnload(LevelEvent.Unload event) {}


}


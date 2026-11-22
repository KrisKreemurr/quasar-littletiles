package team.creative.littletiles.client.render.cache.build;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import team.creative.creativecore.common.level.ISubLevel;
import team.creative.littletiles.client.level.little.FakeClientLevel;
import team.creative.littletiles.client.render.cache.pipeline.LittleRenderPipelineType;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;
import team.creative.littletiles.client.render.mc.ViewAreaExtender;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.entity.animation.LittleAnimationEntity;
import team.creative.littletiles.common.level.little.LittleLevel;
import team.creative.littletiles.common.mod.sable.SableManager;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

public abstract class RenderingLevelHandler {

    public static Vec3 offsetCorrection(Vec3i to, Vec3i from) { return null; }

    public static Vec3 offsetCorrection(Vec3i to, int x, int y, int z) { return null; }

    public static Vec3 offsetCorrection(RenderingLevelHandler target, Level targetLevel, RenderingLevelHandler origin, Level originLevel, SectionPos pos) { return null; }

    public static RenderingLevelHandler VANILLA;
    public static RenderingLevelHandler ENTITY;
    public static RenderingLevelHandler ANIMATION;
    public static RenderingLevelHandler of(Level level, BlockPos pos) { return null; }

    public abstract LittleRenderPipelineType getPipeline();

    public void prepareModelOffset(Level level, MutableBlockPos modelOffset, BlockPos pos) {}

    public long sectionPos(BETiles be) { return 0L; }

    public long prepareQueue(long pos) { return 0L; }

    public abstract RenderChunkExtender getRenderChunk(Level level, long pos);

    public int sectionIndex(Level level, long pos) { return 0; }

    public abstract BlockPos standardOffset(Level level, SectionPos pos);

}


package team.creative.littletiles.client.render.block;

import java.util.Random;
import org.joml.Vector3d;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;
import team.creative.creativecore.common.util.type.list.Pair;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.math.box.LittleBox;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

public class BlockTileRenderProperties implements IClientBlockExtensions {

    private Random random;
    public static BlockTileRenderProperties INSTANCE;
    public boolean addDestroyEffects(BlockState state, Level level, BlockPos pos, ParticleEngine manager) { return false; }

    public boolean addHitEffects(BlockState state, Level level, HitResult target, ParticleEngine manager) { return false; }

    public Vector3d getFogColor(BlockState state, LevelReader level, BlockPos pos, Entity entity, Vector3d originalColor, float partialTicks) { return null; }

}


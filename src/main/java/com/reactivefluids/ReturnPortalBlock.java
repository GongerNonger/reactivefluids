package com.reactivefluids;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Glowing portal block placed at the entrance of pocket dimension structures.
 * Right-click to return to where you cast the spell.
 */
public class ReturnPortalBlock extends HalfTransparentBlock {

    public ReturnPortalBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_PURPLE)
                .strength(-1.0F, 3600000.0F) // unbreakable
                .sound(SoundType.AMETHYST)
                .noOcclusion()
                .isSuffocating((s, l, p) -> false)
                .isViewBlocking((s, l, p) -> false)
                .lightLevel(s -> 15)
                .noLootTable());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level,
                                                BlockPos pos, Player player,
                                                BlockHitResult hitResult) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        if (player instanceof ServerPlayer serverPlayer) {
            MinecraftServer server = serverPlayer.server;
            PocketDimensionData data = PocketDimensionData.get(server);
            data.teleportBack(serverPlayer, server);
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        // Ambient portal particles
        double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.5;
        double y = pos.getY() + 0.5 + (random.nextDouble() - 0.5) * 0.5;
        double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.5;
        level.addParticle(ParticleTypes.REVERSE_PORTAL, x, y, z, 0, 0.05, 0);
        if (random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.PORTAL, x, y, z, 0, 0.1, 0);
        }
    }
}

package com.reactivefluids.pinata.garden;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Garden Plot block — placed to define a garden center.
 * Right-click to view garden stats. Breaking removes the garden.
 *
 * The block itself is a decorated plot marker (like a sign/flag).
 * When placed, it registers a new garden in the GardenManager.
 * When broken, it removes the garden.
 */
public class GardenPlotBlock extends Block {

    public GardenPlotBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (level instanceof ServerLevel serverLevel && !oldState.is(this)) {
            // Auto-create garden — use null UUID for now (will be set by player interaction)
            GardenManager manager = GardenManager.get(serverLevel);
            if (manager.getGarden(pos) == null) {
                manager.createGarden(pos, new java.util.UUID(0, 0));
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (level instanceof ServerLevel serverLevel && !newState.is(this)) {
            GardenManager.get(serverLevel).removeGarden(pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                Player player, BlockHitResult hitResult) {
        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            GardenManager manager = GardenManager.get(serverLevel);
            GardenManager.GardenData garden = manager.getGarden(pos);

            if (garden != null) {
                // Set owner if not set
                if (garden.ownerUUID.getMostSignificantBits() == 0
                        && garden.ownerUUID.getLeastSignificantBits() == 0) {
                    garden.ownerUUID = player.getUUID();
                    manager.setDirty();
                }

                // Scan blocks for fresh data
                manager.scanGardenBlocks(serverLevel, garden);

                // Count residents
                var residents = manager.getResidentPinatas(serverLevel, garden);
                int residentCount = residents.size();
                long speciesCount = residents.stream()
                        .map(p -> p.getType().getDescriptionId())
                        .distinct().count();

                // Display stats to player
                serverPlayer.sendSystemMessage(Component.literal(
                        "=== Garden (Level " + garden.level + ") ==="));
                serverPlayer.sendSystemMessage(Component.literal(
                        "Radius: " + garden.radius + " blocks | XP: " + garden.xp + "/" + (garden.level * 100)));
                serverPlayer.sendSystemMessage(Component.literal(
                        "Grass: " + garden.grassCount + " | Water: " + garden.waterCount
                                + " | Flowers: " + garden.flowerCount));
                serverPlayer.sendSystemMessage(Component.literal(
                        "Long Grass: " + garden.longGrassCount + " | Sand: " + garden.sandCount
                                + " | Snow: " + garden.snowCount));
                serverPlayer.sendSystemMessage(Component.literal(
                        "Residents: " + residentCount + " (" + speciesCount + " species)"));

                // Particle burst to show garden boundary
                showGardenBoundary(serverLevel, garden);

                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private void showGardenBoundary(ServerLevel level, GardenManager.GardenData garden) {
        int r = garden.radius;
        BlockPos c = garden.center;

        // Particle outline at the garden edges
        for (int i = -r; i <= r; i++) {
            spawnBoundaryParticle(level, c.getX() + i, c.getY() + 1, c.getZ() - r);
            spawnBoundaryParticle(level, c.getX() + i, c.getY() + 1, c.getZ() + r);
            spawnBoundaryParticle(level, c.getX() - r, c.getY() + 1, c.getZ() + i);
            spawnBoundaryParticle(level, c.getX() + r, c.getY() + 1, c.getZ() + i);
        }
    }

    private void spawnBoundaryParticle(ServerLevel level, int x, int y, int z) {
        // Only spawn every few blocks for performance
        if ((x + z) % 3 == 0) {
            int surfaceY = level.getHeightmapPos(
                    net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    new BlockPos(x, y, z)).getY();
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    x + 0.5, surfaceY + 1.0, z + 0.5, 1, 0, 0.1, 0, 0);
        }
    }
}

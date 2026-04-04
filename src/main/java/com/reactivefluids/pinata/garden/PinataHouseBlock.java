package com.reactivefluids.pinata.garden;

import com.reactivefluids.pinata.BasePinataEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;
import java.util.function.Supplier;

/**
 * Piñata House block — provides passive happiness boost to nearby piñatas
 * of the matching species. Required for romance in many species.
 *
 * VP reference: Each species has a unique house design purchasable from
 * Willy Builder. Having the house is a romance requirement.
 *
 * MC implementation: Generic block parameterized by species name.
 * - Passive +2 happiness per minute to matching species within 10 blocks
 * - Romance requirement check reads this block's presence
 * - Right-click shows which piñatas are benefiting
 */
public class PinataHouseBlock extends Block {

    private final String speciesName;

    public PinataHouseBlock(Properties properties, String speciesName) {
        super(properties);
        this.speciesName = speciesName;
    }

    public String getSpeciesName() {
        return speciesName;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                Player player, BlockHitResult hitResult) {
        if (level instanceof ServerLevel sl && player instanceof ServerPlayer sp) {
            // Count matching piñatas nearby
            AABB area = new AABB(pos).inflate(10);
            List<BasePinataEntity> matching = sl.getEntitiesOfClass(BasePinataEntity.class, area,
                    p -> p.getPinataSpeciesName().equals(speciesName) && p.isResident());

            sp.sendSystemMessage(Component.literal(
                    speciesName + " House — " + matching.size() + " resident(s) nearby"));

            if (!matching.isEmpty()) {
                sl.sendParticles(ParticleTypes.HEART,
                        pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5,
                        3, 0.3, 0.2, 0.3, 0.02);
            }

            return InteractionResult.SUCCESS;
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos,
                            net.minecraft.util.RandomSource random) {
        // Cozy smoke particles from chimney
        if (random.nextInt(8) == 0) {
            level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.3,
                    pos.getY() + 1.2,
                    pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.3,
                    0, 0.03, 0);
        }
    }
}

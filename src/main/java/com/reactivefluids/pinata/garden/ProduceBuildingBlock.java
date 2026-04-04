package com.reactivefluids.pinata.garden;

import com.reactivefluids.pinata.BasePinataEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;
import java.util.function.Supplier;

/**
 * Produce Building block — when a qualifying piñata is nearby,
 * it periodically generates produce items.
 *
 * Three types:
 * - Honey Hive: Buzzlegum → Honeycomb
 * - Milking Shed: (future Moozipan) → Milk Bucket
 * - Shearing Shed: (future Goobaa) → String/Wool
 *
 * Right-click to collect produce. Particles indicate active production.
 */
public class ProduceBuildingBlock extends Block {

    public enum ProduceType {
        HONEY_HIVE("Honey Hive", () -> Items.HONEYCOMB, 3),
        MILKING_SHED("Milking Shed", () -> Items.MILK_BUCKET, 1),
        SHEARING_SHED("Shearing Shed", () -> Items.STRING, 4);

        public final String displayName;
        public final Supplier<net.minecraft.world.item.Item> produceItem;
        public final int produceCount;

        ProduceType(String name, Supplier<net.minecraft.world.item.Item> item, int count) {
            this.displayName = name;
            this.produceItem = item;
            this.produceCount = count;
        }
    }

    private final ProduceType produceType;

    public ProduceBuildingBlock(Properties properties, ProduceType produceType) {
        super(properties);
        this.produceType = produceType;
    }

    public ProduceType getProduceType() {
        return produceType;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                Player player, BlockHitResult hitResult) {
        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            // Check for qualifying piñata nearby
            boolean hasProducer = hasQualifyingPinata(serverLevel, pos);

            if (hasProducer) {
                // Dispense produce
                ItemStack produce = new ItemStack(produceType.produceItem.get(), produceType.produceCount);
                if (!player.getInventory().add(produce)) {
                    player.drop(produce, false);
                }

                // Effects
                serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                        8, 0.3, 0.3, 0.3, 0.02);
                level.playSound(null, pos, SoundEvents.ITEM_PICKUP,
                        SoundSource.BLOCKS, 0.8F, 1.0F);

                serverPlayer.sendSystemMessage(Component.literal(
                        "Collected " + produceType.produceCount + "x " + produceType.displayName + " produce!"));
            } else {
                serverPlayer.sendSystemMessage(Component.literal(
                        "No qualifying piñata nearby for the " + produceType.displayName + "!"));
            }

            return InteractionResult.SUCCESS;
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, net.minecraft.util.RandomSource random) {
        // Active indicator particles when a producer piñata is nearby
        if (random.nextInt(5) == 0) {
            double px = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.5;
            double py = pos.getY() + 1.2;
            double pz = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.5;

            switch (produceType) {
                case HONEY_HIVE -> level.addParticle(ParticleTypes.FALLING_HONEY, px, py, pz, 0, 0, 0);
                case MILKING_SHED -> level.addParticle(ParticleTypes.FALLING_WATER, px, py, pz, 0, 0, 0);
                case SHEARING_SHED -> level.addParticle(ParticleTypes.END_ROD, px, py, pz, 0, 0.01, 0);
            }
        }
    }

    private boolean hasQualifyingPinata(ServerLevel level, BlockPos pos) {
        AABB area = new AABB(pos).inflate(8);
        List<BasePinataEntity> pinatas = level.getEntitiesOfClass(BasePinataEntity.class, area,
                p -> p.isResident() && p.getHappiness() > 50);

        for (BasePinataEntity pinata : pinatas) {
            switch (produceType) {
                case HONEY_HIVE:
                    if (pinata instanceof com.reactivefluids.pinata.BuzzlegumEntity) return true;
                    break;
                case MILKING_SHED:
                    // Future: Moozipan, Flapyak
                    if (pinata.getPinataSpeciesName().equals("Moozipan")) return true;
                    break;
                case SHEARING_SHED:
                    // Future: Goobaa
                    if (pinata.getPinataSpeciesName().equals("Goobaa")) return true;
                    break;
            }
        }
        return false;
    }
}

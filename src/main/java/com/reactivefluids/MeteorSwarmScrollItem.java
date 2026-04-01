package com.reactivefluids;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * 9th-level — right-click a block to target that area. Four meteor entities
 * spawn high in the sky and streak down to impact near the clicked position.
 * Based on D&D 5e Meteor Swarm.
 */
public class MeteorSwarmScrollItem extends Item {

    private static final int METEOR_COUNT = 4;
    private static final double SCATTER_RADIUS = 8.0;

    public MeteorSwarmScrollItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        ServerLevel serverLevel = (ServerLevel) level;
        BlockPos clickedPos = context.getClickedPos();
        double centerX = clickedPos.getX() + 0.5;
        double centerZ = clickedPos.getZ() + 0.5;

        // Dramatic thunder sound
        level.playSound(null, clickedPos, SoundEvents.LIGHTNING_BOLT_THUNDER,
                SoundSource.PLAYERS, 2.0F, 0.6F);

        // Spawn Y: high in the sky
        int spawnY = Math.min(level.getMaxBuildHeight() + 50, 350);

        // Spawn 4 meteor entities
        for (int i = 0; i < METEOR_COUNT; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 2.0 * SCATTER_RADIUS;
            double offsetZ = (level.random.nextDouble() - 0.5) * 2.0 * SCATTER_RADIUS;

            double meteorX = centerX + offsetX;
            double meteorZ = centerZ + offsetZ;

            // Spawn the meteor entity
            MeteorEntity meteor = new MeteorEntity(ModEntities.METEOR.get(), level);
            meteor.setPos(meteorX, spawnY, meteorZ);
            meteor.setDeltaMovement(0, -3.0, 0);
            serverLevel.addFreshEntity(meteor);

            // Portal visual at spawn point: REVERSE_PORTAL particles
            serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL,
                    meteorX, spawnY, meteorZ,
                    30, 1.0, 0.5, 1.0, 0.05);
        }

        // Consume scroll
        if (!player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                 List<Component> tooltipComponents, TooltipFlag flag) {
        tooltipComponents.add(Component.translatable("item.reactivefluids.meteor_swarm_scroll.tooltip")
                .withStyle(ChatFormatting.GRAY));
    }
}

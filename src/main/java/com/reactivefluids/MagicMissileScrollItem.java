package com.reactivefluids;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.Comparator;
import java.util.List;

/**
 * Magic Missile scroll — fires 4 arcane bolts that home in on the nearest
 * hostile mob. Each bolt automatically targets and never misses.
 * If multiple hostiles are in range, bolts spread across up to 4 targets.
 */
public class MagicMissileScrollItem extends Item {

    private static final int MISSILE_COUNT = 4;
    private static final double SEARCH_RANGE = 32.0;

    public MagicMissileScrollItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) return InteractionResult.SUCCESS;

        ServerLevel serverLevel = (ServerLevel) level;

        // Find nearby hostile mobs, sorted by distance
        AABB searchBox = player.getBoundingBox().inflate(SEARCH_RANGE);
        List<Monster> hostiles = serverLevel.getEntitiesOfClass(Monster.class, searchBox,
                e -> e.isAlive() && player.hasLineOfSight(e));

        if (hostiles.isEmpty()) {
            player.displayClientMessage(
                    Component.literal("No hostile targets in range!").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        hostiles.sort(Comparator.comparingDouble(player::distanceToSqr));

        // Cast sound
        serverLevel.playSound(null, player.blockPosition(),
                SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 1.0F, 1.8F);

        // Particles at cast point
        Vec3 castPos = player.getEyePosition(1.0F).add(player.getLookAngle().scale(0.5));
        serverLevel.sendParticles(
                new DustParticleOptions(new Vector3f(0.47f, 0.31f, 1.0f), 1.5f),
                castPos.x, castPos.y, castPos.z,
                12, 0.2, 0.2, 0.2, 0.05);

        // Spawn 4 missiles, distributing across available targets
        for (int i = 0; i < MISSILE_COUNT; i++) {
            Monster target = hostiles.get(i % hostiles.size());

            MagicMissileEntity missile = new MagicMissileEntity(
                    ModEntities.MAGIC_MISSILE.get(), serverLevel);
            missile.setTarget(target);

            // Stagger starting positions slightly for visual spread
            double offsetX = (Math.random() - 0.5) * 0.5;
            double offsetY = (Math.random() - 0.5) * 0.3;
            double offsetZ = (Math.random() - 0.5) * 0.5;
            missile.setPos(castPos.x + offsetX, castPos.y + offsetY, castPos.z + offsetZ);

            // Initial velocity toward target
            Vec3 toTarget = target.position().add(0, target.getBbHeight() * 0.5, 0)
                    .subtract(missile.position()).normalize().scale(0.8);
            missile.setDeltaMovement(toTarget);

            // Stagger spawn timing with slight delay
            serverLevel.addFreshEntity(missile);
        }

        // Consume scroll
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        // Cooldown
        player.getCooldowns().addCooldown(this, 20); // 1 second

        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Fires 4 arcane bolts at nearby hostiles")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.literal("The missiles always hit their target")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }
}

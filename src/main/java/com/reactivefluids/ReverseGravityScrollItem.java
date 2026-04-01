package com.reactivefluids;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * 7th-level — reverse gravity for all entities in a 10-block radius.
 * Launches them skyward and applies Slow Falling.
 * Based on D&D 5e Reverse Gravity.
 */
public class ReverseGravityScrollItem extends Item {

    private static final double RADIUS = 10.0;
    private static final double LAUNCH_POWER = 2.5; // strong upward velocity
    private static final int SLOW_FALL_DURATION = 200; // 10 seconds

    public ReverseGravityScrollItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) return InteractionResultHolder.success(stack);

        ServerLevel serverLevel = (ServerLevel) level;
        Vec3 center = player.position();
        AABB area = new AABB(
                center.x - RADIUS, center.y - 3, center.z - RADIUS,
                center.x + RADIUS, center.y + RADIUS, center.z + RADIUS);

        int affected = 0;
        for (Entity entity : level.getEntities(player, area)) {
            if (entity == player) continue;

            // Launch upward
            Vec3 motion = entity.getDeltaMovement();
            entity.setDeltaMovement(motion.x, LAUNCH_POWER, motion.z);
            entity.hurtMarked = true; // force motion sync to client

            // Apply Slow Falling to living entities
            if (entity instanceof LivingEntity living) {
                living.addEffect(new MobEffectInstance(
                        MobEffects.SLOW_FALLING, SLOW_FALL_DURATION, 0, false, true));
            }

            // Per-entity particles
            serverLevel.sendParticles(ParticleTypes.ENCHANT,
                    entity.getX(), entity.getY(), entity.getZ(),
                    15, 0.5, 0.5, 0.5, 0.5);
            affected++;
        }

        // Central effect — big particle column
        for (double y = 0; y < 20; y += 0.5) {
            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    center.x, center.y + y, center.z,
                    2, RADIUS * 0.3, 0.1, RADIUS * 0.3, 0.01);
        }

        // Ground-level ring burst
        serverLevel.sendParticles(ParticleTypes.CLOUD,
                center.x, center.y + 0.5, center.z,
                50, RADIUS * 0.5, 0.2, RADIUS * 0.5, 0.1);

        level.playSound(null, player.blockPosition(), SoundEvents.EVOKER_CAST_SPELL,
                SoundSource.PLAYERS, 2.0F, 0.4F);
        level.playSound(null, player.blockPosition(), SoundEvents.ELYTRA_FLYING,
                SoundSource.PLAYERS, 1.5F, 0.5F);

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                 List<Component> tooltipComponents, TooltipFlag flag) {
        tooltipComponents.add(Component.translatable("item.reactivefluids.reverse_gravity_scroll.tooltip")
                .withStyle(ChatFormatting.GRAY));
    }
}

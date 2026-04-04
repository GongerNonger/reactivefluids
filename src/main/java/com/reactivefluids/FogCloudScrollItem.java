package com.reactivefluids;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

import java.util.List;

/**
 * 1st-level -- creates a persistent blinding fog cloud around the caster.
 * Spawns a FogCloudEntity that continuously emits fog particles and
 * applies Blindness to hostile mobs in range.
 * Based on D&D 5e Fog Cloud.
 */
public class FogCloudScrollItem extends Item {

    public FogCloudScrollItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) return InteractionResultHolder.success(stack);

        ServerLevel serverLevel = (ServerLevel) level;

        // Spawn persistent fog cloud entity at player position
        FogCloudEntity fogCloud = new FogCloudEntity(ModEntities.FOG_CLOUD.get(), level);
        fogCloud.setPos(player.getX(), player.getY() + 1.0, player.getZ());
        level.addFreshEntity(fogCloud);

        // Sound effect
        level.playSound(null, player.blockPosition(), SoundEvents.FIRE_EXTINGUISH,
                SoundSource.PLAYERS, 2.0F, 0.8F);

        // Small initial burst of vanilla CLOUD particles
        for (int i = 0; i < 30 + serverLevel.getRandom().nextInt(21); i++) {
            double theta = serverLevel.getRandom().nextDouble() * Math.PI * 2;
            double phi = Math.acos(2.0 * serverLevel.getRandom().nextDouble() - 1.0);
            double r = serverLevel.getRandom().nextDouble() * 5.0;
            double px = player.getX() + r * Math.sin(phi) * Math.cos(theta);
            double py = player.getY() + 1.0 + r * Math.sin(phi) * Math.sin(theta);
            double pz = player.getZ() + r * Math.cos(phi);
            serverLevel.sendParticles(ParticleTypes.CLOUD,
                    px, py, pz,
                    1, 0.1, 0.1, 0.1, 0.01);
        }

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                 List<Component> tooltipComponents, TooltipFlag flag) {
        tooltipComponents.add(Component.translatable("item.reactivefluids.fog_cloud_scroll.tooltip")
                .withStyle(ChatFormatting.GRAY));
    }
}

package com.reactivefluids;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Consumable spell scroll that conjures four dancing lights —
 * glowing orbs that orbit the caster and illuminate the area for 1 minute.
 */
public class DancingLightsScrollItem extends Item {

    public DancingLightsScrollItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) level;

            // Spawn 4 dancing lights at cardinal offsets
            for (int i = 0; i < 4; i++) {
                DancingLightEntity light = new DancingLightEntity(
                        ModEntities.DANCING_LIGHT.get(), serverLevel);
                float phase = (float) (i * Math.PI / 2.0); // 0, 90, 180, 270 degrees
                double spawnX = player.getX() + Math.cos(phase) * 2.0;
                double spawnY = player.getY() + 1.5;
                double spawnZ = player.getZ() + Math.sin(phase) * 2.0;
                light.moveTo(spawnX, spawnY, spawnZ, 0, 0);
                light.setOwner(player.getUUID());
                light.setOrbitPhase(phase);
                light.setColorIndex(i);
                serverLevel.addFreshEntity(light);
            }

            // Casting effects
            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    player.getX(), player.getY() + 1.5, player.getZ(),
                    20, 1.5, 1.0, 1.5, 0.05);
            level.playSound(null, player, SoundEvents.AMETHYST_BLOCK_CHIME,
                    SoundSource.PLAYERS, 1.0F, 1.5F);

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                 List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("item.reactivefluids.dancing_lights_scroll.tooltip")
                .withStyle(ChatFormatting.GRAY));
    }
}

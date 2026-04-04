package com.reactivefluids;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
 * Consumable spell scroll that conjures a Phantom Steed —
 * a fast, ghostly horse that vanishes on damage or after 10 minutes.
 */
public class SteedScrollItem extends Item {

    public SteedScrollItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        ServerLevel serverLevel = (ServerLevel) level;
        BlockPos spawnPos = context.getClickedPos().relative(context.getClickedFace());

        // Spawn the phantom steed facing the same direction as the player
        PhantomSteedEntity steed = new PhantomSteedEntity(
                ModEntities.PHANTOM_STEED.get(), serverLevel);
        steed.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(),
                spawnPos.getZ() + 0.5, player.getYRot(), 0);
        steed.setOwnerUUID(player.getUUID());
        steed.setTamed(true);
        serverLevel.addFreshEntity(steed);

        // Summoning effects
        serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                spawnPos.getX() + 0.5, spawnPos.getY() + 1.0, spawnPos.getZ() + 0.5,
                40, 0.8, 0.8, 0.8, 0.02);
        level.playSound(null, spawnPos, SoundEvents.EVOKER_CAST_SPELL,
                SoundSource.PLAYERS, 1.0F, 1.2F);

        // Consume scroll
        if (!player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                 List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("item.reactivefluids.steed_scroll.tooltip")
                .withStyle(ChatFormatting.GRAY));
    }
}

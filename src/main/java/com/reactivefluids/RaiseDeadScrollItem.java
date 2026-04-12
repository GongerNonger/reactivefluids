package com.reactivefluids;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * 3rd-level — Raise Dead. Cast on two stacked rotten flesh blocks to raise a zombie,
 * or two stacked bone blocks to raise a skeleton. The undead fights for the caster.
 * Based on D&D 5e Animate Dead / Raise Dead.
 */
public class RaiseDeadScrollItem extends Item {

    private static final double RANGE = 8.0;

    public RaiseDeadScrollItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        ServerLevel serverLevel = (ServerLevel) level;

        // Raycast to find targeted block
        Vec3 eyePos = player.getEyePosition(1.0F);
        Vec3 lookVec = player.getLookAngle();
        Vec3 endPos = eyePos.add(lookVec.x * RANGE, lookVec.y * RANGE, lookVec.z * RANGE);
        BlockHitResult hit = level.clip(new ClipContext(
                eyePos, endPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));

        if (hit.getType() == HitResult.Type.MISS) {
            player.displayClientMessage(
                    Component.translatable("message.reactivefluids.raise_dead_fail"), true);
            return InteractionResult.FAIL;
        }

        BlockPos hitPos = hit.getBlockPos();
        BlockState hitState = level.getBlockState(hitPos);
        BlockState aboveState = level.getBlockState(hitPos.above());

        // Check for two stacked rotten flesh blocks (bottom = hitPos or hitPos.below)
        boolean isZombie = false;
        boolean isSkeleton = false;
        BlockPos bottomPos;

        if (isRottenFleshBlock(hitState) && isRottenFleshBlock(aboveState)) {
            isZombie = true;
            bottomPos = hitPos;
        } else if (isRottenFleshBlock(hitState) && isRottenFleshBlock(level.getBlockState(hitPos.below()))) {
            isZombie = true;
            bottomPos = hitPos.below();
        } else if (isBoneBlock(hitState) && isBoneBlock(aboveState)) {
            isSkeleton = true;
            bottomPos = hitPos;
        } else if (isBoneBlock(hitState) && isBoneBlock(level.getBlockState(hitPos.below()))) {
            isSkeleton = true;
            bottomPos = hitPos.below();
        } else {
            player.displayClientMessage(
                    Component.translatable("message.reactivefluids.raise_dead_fail"), true);
            return InteractionResult.FAIL;
        }

        // Remove the two blocks
        level.setBlock(bottomPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(bottomPos.above(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);

        // Spawn the undead minion
        double spawnX = bottomPos.getX() + 0.5;
        double spawnY = bottomPos.getY();
        double spawnZ = bottomPos.getZ() + 0.5;

        if (isZombie) {
            RaisedZombieEntity zombie = new RaisedZombieEntity(
                    ModEntities.RAISED_ZOMBIE.get(), serverLevel);
            zombie.setOwnerUUID(player.getUUID());
            zombie.setBaby(false);
            zombie.moveTo(spawnX, spawnY, spawnZ, player.getYRot(), 0);
            serverLevel.addFreshEntity(zombie);
        } else {
            RaisedSkeletonEntity skeleton = new RaisedSkeletonEntity(
                    ModEntities.RAISED_SKELETON.get(), serverLevel);
            skeleton.setOwnerUUID(player.getUUID());
            skeleton.moveTo(spawnX, spawnY, spawnZ, player.getYRot(), 0);
            serverLevel.addFreshEntity(skeleton);
        }

        // Effects
        serverLevel.sendParticles(ParticleTypes.SOUL,
                spawnX, spawnY + 1.0, spawnZ,
                20, 0.5, 0.8, 0.5, 0.02);
        serverLevel.sendParticles(ParticleTypes.SMOKE,
                spawnX, spawnY + 0.5, spawnZ,
                15, 0.3, 0.5, 0.3, 0.03);
        level.playSound(null, bottomPos, SoundEvents.WITHER_SPAWN,
                SoundSource.PLAYERS, 0.5F, 1.5F);
        level.playSound(null, bottomPos, SoundEvents.EVOKER_CAST_SPELL,
                SoundSource.PLAYERS, 1.0F, 0.8F);

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResult.CONSUME;
    }

    private boolean isRottenFleshBlock(BlockState state) {
        return state.is(ModBlocks.ROTTEN_FLESH_BLOCK.get());
    }

    private boolean isBoneBlock(BlockState state) {
        return state.is(Blocks.BONE_BLOCK);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                 List<Component> tooltipComponents, TooltipFlag flag) {
        tooltipComponents.add(Component.translatable("item.reactivefluids.raise_dead_scroll.tooltip")
                .withStyle(ChatFormatting.GRAY));
    }
}

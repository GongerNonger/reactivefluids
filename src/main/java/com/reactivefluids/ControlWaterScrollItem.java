package com.reactivefluids;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * 4th-level — parts a body of water, creating a 3-wide, 40-long walkable
 * corridor by replacing water with barrier blocks for 60 seconds.
 * Based on D&D 5e Control Water.
 *
 * Uses a fluid-aware raycast so it can target water blocks directly.
 */
public class ControlWaterScrollItem extends Item {

    private static final int CORRIDOR_LENGTH = 40;
    private static final int CORRIDOR_HALF_WIDTH = 2; // 5 wide: -2..+2
    private static final int CORRIDOR_HEIGHT = 4;     // 4 blocks tall interior
    public static final int RESTORE_TICKS = 1200; // 60 seconds

    public ControlWaterScrollItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) return InteractionResultHolder.success(stack);

        ServerLevel serverLevel = (ServerLevel) level;

        // Fluid-aware raycast — finds water blocks the player is looking at
        Vec3 eyePos = player.getEyePosition(1.0F);
        Vec3 lookVec = player.getViewVector(1.0F);
        Vec3 endPos = eyePos.add(lookVec.scale(64.0));
        BlockHitResult hitResult = level.clip(new ClipContext(
                eyePos, endPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, player));

        if (hitResult.getType() == HitResult.Type.MISS) {
            player.displayClientMessage(
                    Component.translatable("message.reactivefluids.control_water_fail"), true);
            return InteractionResultHolder.fail(stack);
        }

        BlockPos hitPos = hitResult.getBlockPos();
        BlockState hitState = level.getBlockState(hitPos);

        // Must target water
        if (!hitState.getFluidState().is(Fluids.WATER)
                && !hitState.getFluidState().is(Fluids.FLOWING_WATER)) {
            player.displayClientMessage(
                    Component.translatable("message.reactivefluids.control_water_fail"), true);
            return InteractionResultHolder.fail(stack);
        }

        // Determine corridor direction from player facing (horizontal only)
        Direction facing = player.getDirection();
        Direction right = facing.getClockWise();

        // Build a hollow corridor: barrier walls/floor/ceiling, air interior
        List<BlockPos> replacedPositions = new ArrayList<>();
        List<BlockState> originalStates = new ArrayList<>();

        for (int forward = 0; forward < CORRIDOR_LENGTH; forward++) {
            for (int side = -(CORRIDOR_HALF_WIDTH + 1); side <= (CORRIDOR_HALF_WIDTH + 1); side++) {
                for (int dy = -1; dy <= CORRIDOR_HEIGHT; dy++) {
                    BlockPos pos = hitPos
                            .relative(facing, forward)
                            .relative(right, side)
                            .above(dy);
                    BlockState state = level.getBlockState(pos);

                    // Only act on water blocks
                    if (!state.getFluidState().is(Fluids.WATER)
                            && !state.getFluidState().is(Fluids.FLOWING_WATER)) continue;

                    boolean isWall = side == -(CORRIDOR_HALF_WIDTH + 1) || side == (CORRIDOR_HALF_WIDTH + 1);
                    boolean isFloor = dy == -1;
                    boolean isCeiling = dy == CORRIDOR_HEIGHT;
                    boolean isShell = isWall || isFloor || isCeiling;

                    replacedPositions.add(pos.immutable());
                    originalStates.add(state);

                    if (isShell) {
                        level.setBlock(pos, Blocks.BARRIER.defaultBlockState(), Block.UPDATE_ALL);
                    } else {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    }
                }
            }
        }

        if (replacedPositions.isEmpty()) {
            player.displayClientMessage(
                    Component.translatable("message.reactivefluids.control_water_fail"), true);
            return InteractionResultHolder.fail(stack);
        }

        // Register for restoration
        ControlWaterData.get(serverLevel).addParting(
                replacedPositions, originalStates, level.getGameTime() + RESTORE_TICKS);

        // Splash particles along the corridor
        for (int forward = 0; forward < CORRIDOR_LENGTH; forward += 2) {
            BlockPos particlePos = hitPos.relative(facing, forward);
            serverLevel.sendParticles(ParticleTypes.SPLASH,
                    particlePos.getX() + 0.5, particlePos.getY() + 1.0, particlePos.getZ() + 0.5,
                    15, 1.0, 0.5, 1.0, 0.3);
        }

        // Sound effects
        level.playSound(null, hitPos, SoundEvents.AMBIENT_UNDERWATER_LOOP,
                SoundSource.PLAYERS, 2.0F, 0.5F);
        level.playSound(null, hitPos, SoundEvents.TRIDENT_RETURN,
                SoundSource.PLAYERS, 1.5F, 0.8F);

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                 List<Component> tooltipComponents, TooltipFlag flag) {
        tooltipComponents.add(Component.translatable("item.reactivefluids.control_water_scroll.tooltip")
                .withStyle(ChatFormatting.GRAY));
    }
}

package com.reactivefluids;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import java.util.List;
import java.util.Optional;

/**
 * Consumable spell scroll — fires a thin green ray that deals massive damage.
 * Kills reduce the target to dust. Also destroys a 3x3x3 cube of blocks at impact.
 */
public class DisintegrateScrollItem extends Item {

    private static final double RANGE = 60.0;
    private static final float DAMAGE = 75.0F; // ~10d6+40 average

    public DisintegrateScrollItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) return InteractionResultHolder.success(stack);

        ServerLevel serverLevel = (ServerLevel) level;

        // Raycast for blocks
        Vec3 eyePos = player.getEyePosition(1.0F);
        Vec3 lookVec = player.getLookAngle();
        Vec3 endPos = eyePos.add(lookVec.x * RANGE, lookVec.y * RANGE, lookVec.z * RANGE);

        BlockHitResult blockHit = level.clip(new ClipContext(
                eyePos, endPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));

        // Raycast for entities
        AABB searchBox = player.getBoundingBox().expandTowards(lookVec.scale(RANGE)).inflate(2.0);
        EntityHitResult entityHit = raycastEntities(level, player, eyePos, endPos, searchBox);

        // Determine which hit is closer
        double blockDist = blockHit.getType() != HitResult.Type.MISS
                ? eyePos.distanceToSqr(blockHit.getLocation()) : Double.MAX_VALUE;
        double entityDist = entityHit != null
                ? eyePos.distanceToSqr(entityHit.getLocation()) : Double.MAX_VALUE;

        Vec3 impactPos;
        boolean hitEntity = false;

        if (entityDist < blockDist && entityHit != null) {
            // Hit entity first
            impactPos = entityHit.getLocation();
            hitEntity = true;
            Entity target = entityHit.getEntity();

            if (target instanceof LivingEntity living) {
                float healthBefore = living.getHealth();
                living.hurt(level.damageSources().indirectMagic(player, player), DAMAGE);

                // If killed, disintegrate — remove drops, leave dust
                if (living.isDeadOrDying() || living.getHealth() <= 0) {
                    disintegrateEntity(serverLevel, living);
                }
            }
        } else if (blockHit.getType() != HitResult.Type.MISS) {
            impactPos = blockHit.getLocation();
        } else {
            impactPos = endPos;
        }

        // Destroy 3x3x3 blocks at impact (if we hit blocks or killed an entity at that spot)
        if (!hitEntity || (entityHit != null && entityDist < blockDist)) {
            disintegrateBlocks(serverLevel, BlockPos.containing(impactPos));
        }

        // Spawn continuous beam entity
        DisintegrateBeamEntity beam = new DisintegrateBeamEntity(
                ModEntities.DISINTEGRATE_BEAM.get(), serverLevel);
        beam.moveTo(eyePos.x, eyePos.y, eyePos.z, 0, 0);
        beam.setBeamEnd(impactPos);
        serverLevel.addFreshEntity(beam);

        // Sound at caster and impact
        level.playSound(null, player.blockPosition(), SoundEvents.BREEZE_SHOOT,
                SoundSource.PLAYERS, 1.5F, 0.5F);
        level.playSound(null, BlockPos.containing(impactPos), SoundEvents.GENERIC_EXPLODE.value(),
                SoundSource.PLAYERS, 0.8F, 1.5F);

        // Consume scroll
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        return InteractionResultHolder.consume(stack);
    }

    private static EntityHitResult raycastEntities(Level level, Player player,
                                                    Vec3 start, Vec3 end, AABB box) {
        Entity closest = null;
        Vec3 closestHit = null;
        double closestDist = Double.MAX_VALUE;

        for (Entity entity : level.getEntities(player, box,
                e -> e instanceof LivingEntity && e.isAlive() && e.isPickable())) {
            AABB entityBox = entity.getBoundingBox().inflate(entity.getPickRadius());
            Optional<Vec3> hit = entityBox.clip(start, end);
            if (hit.isPresent()) {
                double dist = start.distanceToSqr(hit.get());
                if (dist < closestDist) {
                    closest = entity;
                    closestHit = hit.get();
                    closestDist = dist;
                }
            }
        }

        return closest != null ? new EntityHitResult(closest, closestHit) : null;
    }

    private static void disintegrateEntity(ServerLevel level, LivingEntity entity) {
        BlockPos pos = entity.blockPosition();

        // Remove all item drops near the entity (items dropped on death)
        AABB dropBox = entity.getBoundingBox().inflate(2.0);
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, dropBox)) {
            if (item.hasPickUpDelay()) { // Recently spawned drops
                item.discard();
            }
        }

        // Leave a pile of gray dust
        BlockState dust = Blocks.LIGHT_GRAY_CONCRETE_POWDER.defaultBlockState();
        if (level.getBlockState(pos).isAir()) {
            level.setBlock(pos, dust, Block.UPDATE_ALL);
        }

        // Disintegration particle burst
        level.sendParticles(ModParticles.DISINTEGRATE.get(),
                entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(),
                60, entity.getBbWidth() / 2, entity.getBbHeight() / 2, entity.getBbWidth() / 2, 0.1);

        // Force remove the entity instantly (prevents death animation)
        entity.remove(Entity.RemovalReason.KILLED);
    }

    private static void disintegrateBlocks(ServerLevel level, BlockPos center) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(pos);

                    // "Magic items" = indestructible blocks survive
                    if (state.isAir()) continue;
                    if (state.getDestroySpeed(level, pos) < 0) continue; // bedrock, barriers, etc.
                    float hardness = state.getDestroySpeed(level, pos);
                    if (hardness > 50.0F) continue; // obsidian, reinforced deepslate, etc.

                    // Disintegrate — no drops, just gone
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    level.sendParticles(ModParticles.DISINTEGRATE.get(),
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            5, 0.3, 0.3, 0.3, 0.05);
                }
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                 List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("item.reactivefluids.disintegrate_scroll.tooltip")
                .withStyle(ChatFormatting.GRAY));
    }
}

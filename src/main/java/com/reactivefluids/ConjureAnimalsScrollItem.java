package com.reactivefluids;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * 3rd-level — summon a pack of spectral creatures that fight for or support the caster.
 * Spawns a random mix of wolves (melee), foxes (pounce attack), and axolotls (healer).
 * Based on D&D 5e Conjure Animals.
 */
public class ConjureAnimalsScrollItem extends Item {

    private static final int CREATURE_COUNT = 5;
    private static final DyeColor[] COLLAR_COLORS = {
            DyeColor.LIGHT_BLUE, DyeColor.LIME, DyeColor.MAGENTA,
            DyeColor.ORANGE, DyeColor.CYAN
    };

    public ConjureAnimalsScrollItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        ServerLevel serverLevel = (ServerLevel) level;

        for (int i = 0; i < CREATURE_COUNT; i++) {
            double angle = (2 * Math.PI * i) / CREATURE_COUNT;
            double spawnX = player.getX() + Math.cos(angle) * 3.0;
            double spawnZ = player.getZ() + Math.sin(angle) * 3.0;

            Mob creature = spawnRandomCreature(serverLevel, player, i);
            creature.moveTo(spawnX, player.getY(), spawnZ, player.getYRot(), 0);
            serverLevel.addFreshEntity(creature);

            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    spawnX, player.getY() + 0.5, spawnZ,
                    10, 0.3, 0.3, 0.3, 0.02);
        }

        serverLevel.sendParticles(ParticleTypes.ENCHANT,
                player.getX(), player.getY() + 1.0, player.getZ(),
                30, 1.5, 1.0, 1.5, 0.3);
        level.playSound(null, player.blockPosition(), SoundEvents.WOLF_HOWL,
                SoundSource.PLAYERS, 1.5F, 1.2F);
        level.playSound(null, player.blockPosition(), SoundEvents.EVOKER_CAST_SPELL,
                SoundSource.PLAYERS, 1.0F, 1.0F);

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResult.CONSUME;
    }

    private Mob spawnRandomCreature(ServerLevel level, Player player, int index) {
        // Guaranteed: at least 2 wolves, 1 fox, 1 axolotl. 5th is random.
        int type;
        if (index < 2) {
            type = 0; // wolf
        } else if (index == 2) {
            type = 1; // fox
        } else if (index == 3) {
            type = 2; // axolotl
        } else {
            type = level.random.nextInt(3); // random
        }

        return switch (type) {
            case 1 -> {
                SpectralFoxEntity fox = new SpectralFoxEntity(
                        ModEntities.SPECTRAL_FOX.get(), level);
                fox.setOwnerUUID(player.getUUID());
                fox.setVariantIndex(level.random.nextInt(SpectralFoxEntity.VARIANT_COUNT));
                fox.equipRandomWeapon();
                yield fox;
            }
            case 2 -> {
                SpectralAxolotlEntity axolotl = new SpectralAxolotlEntity(
                        ModEntities.SPECTRAL_AXOLOTL.get(), level);
                axolotl.setOwnerUUID(player.getUUID());
                axolotl.setVariantIndex(level.random.nextInt(SpectralAxolotlEntity.VARIANT_COUNT));
                yield axolotl;
            }
            default -> {
                SpectralWolfEntity wolf = new SpectralWolfEntity(
                        ModEntities.SPECTRAL_WOLF.get(), level);
                wolf.setTame(true, true);
                wolf.setOwnerUUID(player.getUUID());
                wolf.setVariantIndex(level.random.nextInt(SpectralWolfEntity.VARIANT_COUNT));
                wolf.setSpectralCollarColor(COLLAR_COLORS[index % COLLAR_COLORS.length]);
                yield wolf;
            }
        };
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                 List<Component> tooltipComponents, TooltipFlag flag) {
        tooltipComponents.add(Component.translatable("item.reactivefluids.conjure_animals_scroll.tooltip")
                .withStyle(ChatFormatting.GRAY));
    }
}

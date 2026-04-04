package com.reactivefluids.pinata;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Manages piñata evolution/transformation rules.
 *
 * In VP, feeding certain items to specific piñatas transforms them into
 * a different species. The transformation is instant with a visual effect.
 *
 * This system is checked when a piñata is fed (in BasePinataEntity.mobInteract).
 */
public class PinataEvolution {

    private static final List<EvolutionRule> RULES = new ArrayList<>();

    static {
        // Sparrowmint → Candary (feed dandelion)
        // Note: Candary entity not yet implemented, placeholder for future
        // RULES.add(new EvolutionRule(
        //     () -> ModPinataEntities.SPARROWMINT.get(),
        //     Items.DANDELION,
        //     () -> ModPinataEntities.CANDARY.get()));

        // These rules will be activated as the evolved species are implemented:
        // Fudgehog → Parmadillo (feed cocoa beans / coconut equivalent)
        // Horstachio → Zumbug (feed daisy + blackberry / sweet berries)
        // Pretztail → Pieena (feed bone)
        // Quackberry → Juicygoose (feed gooseberry / sweet berries)
        // Newtgat → Salamango (feed blaze powder / chili equivalent)
        // Cluckles → Chocstrich (feed cactus / prickly pear)
        // Doenut → Moojoo (feed spruce sapling / fir seed)
        // Fizzlybear → Polollybear (feed lapis / blue gem)
        // Lickatoad → Lackatoad (complex: feed nightshade + shovel hit)
    }

    /**
     * Check if feeding this item to this piñata triggers an evolution.
     * Returns the new entity type if so, null otherwise.
     */
    public static EntityType<?> checkEvolution(BasePinataEntity pinata, ItemStack food) {
        for (EvolutionRule rule : RULES) {
            if (pinata.getType() == rule.sourceType.get() && food.is(rule.triggerItem)) {
                return rule.resultType.get();
            }
        }
        return null;
    }

    /**
     * Perform the evolution transformation.
     * Replaces the source entity with the evolved entity, preserving data.
     */
    public static boolean evolve(BasePinataEntity source, EntityType<?> evolvedType) {
        if (!(source.level() instanceof ServerLevel serverLevel)) return false;

        var evolved = evolvedType.create(serverLevel);
        if (evolved == null) return false;

        // Position and rotation
        evolved.moveTo(source.getX(), source.getY(), source.getZ(),
                source.getYRot(), source.getXRot());

        // Preserve piñata data if the evolved entity is also a piñata
        if (evolved instanceof BasePinataEntity evolvedPinata) {
            evolvedPinata.setLifecycle(source.getLifecycle());
            evolvedPinata.setHappiness(source.getHappiness());
            evolvedPinata.setPinataVariant(source.getPinataVariant());
            if (source.hasCustomName()) {
                evolvedPinata.setCustomName(source.getCustomName());
            }
        }

        // Visual effects — lightning-like transformation
        serverLevel.sendParticles(ParticleTypes.FLASH,
                source.getX(), source.getY() + source.getBbHeight() / 2, source.getZ(),
                1, 0, 0, 0, 0);
        serverLevel.sendParticles(ParticleTypes.FIREWORK,
                source.getX(), source.getY() + source.getBbHeight() / 2, source.getZ(),
                20, 0.5, 0.5, 0.5, 0.15);
        serverLevel.sendParticles(ParticleTypes.ENCHANT,
                source.getX(), source.getY(), source.getZ(),
                30, 0.5, 1.0, 0.5, 0.5);

        // Sound
        serverLevel.playSound(null, source.blockPosition(),
                SoundEvents.PLAYER_LEVELUP, SoundSource.NEUTRAL, 1.0F, 0.8F);

        // Swap entities
        serverLevel.addFreshEntity(evolved);
        source.discard();

        return true;
    }

    // --- Rule definition ---

    static class EvolutionRule {
        final Supplier<EntityType<?>> sourceType;
        final Item triggerItem;
        final Supplier<EntityType<?>> resultType;

        EvolutionRule(Supplier<EntityType<?>> sourceType, Item triggerItem,
                      Supplier<EntityType<?>> resultType) {
            this.sourceType = sourceType;
            this.triggerItem = triggerItem;
            this.resultType = resultType;
        }
    }

    /**
     * Register an evolution rule. Call from mod init.
     */
    public static void registerRule(Supplier<EntityType<?>> source, Item trigger,
                                    Supplier<EntityType<?>> result) {
        RULES.add(new EvolutionRule(source, trigger, result));
    }
}

package com.reactivefluids;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Tulip Mania — a feverish effect brewed from the legendary Semper Augustus.
 * Boosts attack damage and dig speed, echoing the frenzied speculation
 * of the Dutch Golden Age tulip market.
 */
public class TulipManiaMobEffect extends MobEffect {

    public TulipManiaMobEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xCC2244); // crimson-red color
        addAttributeModifier(
                Attributes.ATTACK_SPEED,
                ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "tulip_mania_attack_speed"),
                0.3,  // +30% attack speed
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
        addAttributeModifier(
                Attributes.BLOCK_BREAK_SPEED,
                ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "tulip_mania_dig_speed"),
                0.4,  // +40% dig speed (stacks with Haste)
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
    }
}

package com.reactivefluids.pinata;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * An accessory item that can be equipped onto piñata entities.
 * Right-click a piñata with this item to equip it.
 * The accessory ID links to the PinataAccessory registry.
 */
public class AccessoryItem extends Item {

    private final String accessoryId;

    public AccessoryItem(Properties properties, String accessoryId) {
        super(properties);
        this.accessoryId = accessoryId;
    }

    public String getAccessoryId() {
        return accessoryId;
    }

    public PinataAccessory getAccessory() {
        return PinataAccessory.get(accessoryId);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        PinataAccessory acc = getAccessory();
        if (acc != null) {
            tooltip.add(Component.literal("Slot: " + acc.getSlot().name())
                    .withStyle(net.minecraft.ChatFormatting.GRAY));
            tooltip.add(Component.literal("Happiness: +" + acc.getHappinessBonus())
                    .withStyle(net.minecraft.ChatFormatting.GREEN));
            if (acc.getEffect() != PinataAccessory.SpecialEffect.NONE) {
                tooltip.add(Component.literal("Effect: " + formatEffect(acc.getEffect()))
                        .withStyle(net.minecraft.ChatFormatting.GOLD));
            }
        }
    }

    private String formatEffect(PinataAccessory.SpecialEffect effect) {
        return switch (effect) {
            case AUTO_PRODUCE -> "Auto-Produce";
            case AUTO_HEAL -> "Auto-Heal Sickness";
            case SPEED_BOOST -> "Speed Boost";
            case ROMANCE_REQ -> "Romance Requirement";
            case VALUE_BOOST -> "Value Boost";
            default -> "";
        };
    }
}

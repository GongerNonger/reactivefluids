package com.reactivefluids;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Fletcher's Fireball — launches a single fireball projectile in the player's
 * look direction. Smaller and faster than a Meteor Swarm meteor.
 *
 * Easter egg: if the player holds a dye in their offhand when casting, the
 * fireball takes on that dye's color (and consumes the dye in survival).
 */
public class FletchersFireballScrollItem extends Item {

    /** Speed in blocks/tick — about 1.5× faster than a meteor */
    private static final double SPEED = 1.5;

    public FletchersFireballScrollItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) return InteractionResultHolder.success(stack);

        // --- Determine fireball color from offhand dye (easter egg) ---
        int color = FireballEntity.DEFAULT_COLOR;
        ItemStack offhand = player.getOffhandItem();
        if (offhand.getItem() instanceof DyeItem dyeItem) {
            color = dyeColorToRGB(dyeItem.getDyeColor());
            // Consume one dye in survival
            if (!player.getAbilities().instabuild) {
                offhand.shrink(1);
            }
        }

        // --- Spawn the fireball ---
        FireballEntity fireball = new FireballEntity(ModEntities.FIREBALL.get(), level);
        fireball.setOwnerUUID(player.getUUID());
        fireball.setColor(color);

        // Spawn just ahead of the player's eye to avoid self-collision
        var eyePos = player.getEyePosition(1.0F);
        var look = player.getLookAngle();
        fireball.moveTo(eyePos.x + look.x * 0.5,
                        eyePos.y + look.y * 0.5,
                        eyePos.z + look.z * 0.5,
                        player.getYRot(), player.getXRot());
        fireball.setDeltaMovement(look.x * SPEED, look.y * SPEED, look.z * SPEED);
        level.addFreshEntity(fireball);

        // Sound
        level.playSound(null, player.blockPosition(),
                SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0F, 0.9F);

        // Consume scroll
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        return InteractionResultHolder.consume(stack);
    }

    /**
     * Maps a Minecraft DyeColor to an RGB int for the fireball tint.
     * Colors are the standard dye RGB values from the vanilla color palette.
     */
    private static int dyeColorToRGB(DyeColor dye) {
        return switch (dye) {
            case WHITE      -> 0xF9FFFE;
            case ORANGE     -> 0xF9801D;
            case MAGENTA    -> 0xC74EBD;
            case LIGHT_BLUE -> 0x3AB3DA;
            case YELLOW     -> 0xFED83D;
            case LIME       -> 0x80C71F;
            case PINK       -> 0xF38BAA;
            case GRAY       -> 0x474F52;
            case LIGHT_GRAY -> 0x9D9D97;
            case CYAN       -> 0x169C9C;
            case PURPLE     -> 0x8932B8;
            case BLUE       -> 0x3C44AA;
            case BROWN      -> 0x835432;
            case GREEN      -> 0x5E7C16;
            case RED        -> 0xB02E26;
            case BLACK      -> 0x1D1D21;
        };
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltipComponents, TooltipFlag flag) {
        tooltipComponents.add(Component.translatable("item.reactivefluids.fletchers_fireball_scroll.tooltip")
                .withStyle(ChatFormatting.GRAY));
    }
}

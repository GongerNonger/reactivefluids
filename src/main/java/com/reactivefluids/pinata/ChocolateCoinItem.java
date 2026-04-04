package com.reactivefluids.pinata;

import net.minecraft.world.item.Item;

/**
 * Chocolate Coin — the universal currency in Viva Piñata.
 *
 * Used for: buying items from NPC shops, bribing ruffians,
 * hiring helpers. Earned by selling piñatas and produce.
 *
 * Stacks to 64. Not edible (despite being chocolate — it's foil-wrapped).
 */
public class ChocolateCoinItem extends Item {

    public ChocolateCoinItem(Properties properties) {
        super(properties);
    }
}

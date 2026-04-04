package com.reactivefluids;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Decorative crystal block that grows around crystal solution source blocks.
 * Semi-transparent, glass-like, emits a faint light.
 */
public class CrystalBlock extends Block {

    public CrystalBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_LIGHT_BLUE)
                .strength(1.0f, 3.0f)
                .sound(SoundType.AMETHYST)
                .lightLevel(state -> 5)
                .noOcclusion()
                .isViewBlocking((s, l, p) -> false));
    }
}

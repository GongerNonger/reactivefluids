package com.reactivefluids;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

/**
 * Arcane Barrier -- the translucent shimmering block used by Tiny Hut.
 * Breakable from inside the dome (normal speed) but completely unbreakable
 * from outside. Explosion-proof (blast resistance 3,600,000).
 */
public class ArcaneBarrierBlock extends HalfTransparentBlock {

    public ArcaneBarrierBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_LIGHT_BLUE)
                .strength(0.5F, 3600000.0F)
                .sound(SoundType.AMETHYST)
                .noOcclusion()
                .isSuffocating((s, l, p) -> false)
                .isViewBlocking((s, l, p) -> false)
                .lightLevel(s -> 3));
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        // Server-side: allow breaking only if the player is inside an active hut dome
        if (player.level() instanceof ServerLevel serverLevel) {
            TinyHutData data = TinyHutData.get(serverLevel);
            if (data.isPlayerInsideHut(player.blockPosition())) {
                return super.getDestroyProgress(state, player, level, pos);
            }
        }
        // Client-side or outside the dome: show no mining progress (unbreakable)
        return 0.0F;
    }
}

package net.tobiasfiller.miltenmagic.common.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.tobiasfiller.miltenmagic.core.registry.BlockEntityRegistry;

public class MagicalLecternBlockEntity extends BlockEntity {
    public MagicalLecternBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(BlockEntityRegistry.MAGICAL_LECTERN_BLOCK_ENTITY.get(), pWorldPosition, pBlockState);
    }
}

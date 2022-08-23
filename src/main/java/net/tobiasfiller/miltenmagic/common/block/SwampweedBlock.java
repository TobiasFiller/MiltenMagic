package net.tobiasfiller.miltenmagic.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Random;

public class SwampweedBlock extends BushBlock {

    private final VoxelShape SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 20.0D, 12.0D);

    public SwampweedBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, Random pRandom) {
        if (pRandom.nextInt(25) == 0) {
            BlockPos blockpos1 = pPos.offset(pRandom.nextInt(3) - 1, pRandom.nextInt(2) - pRandom.nextInt(2), pRandom.nextInt(3) - 1);

            for(int k = 0; k < 4; ++k) {
                if (pLevel.isEmptyBlock(blockpos1) && pState.canSurvive(pLevel, blockpos1)) {
                    pPos = blockpos1;
                }

                blockpos1 = pPos.offset(pRandom.nextInt(3) - 1, pRandom.nextInt(2) - pRandom.nextInt(2), pRandom.nextInt(3) - 1);
            }

            if (pLevel.isEmptyBlock(blockpos1) && pState.canSurvive(pLevel, blockpos1) && Biome.getBiomeCategory(pLevel.getBiome(blockpos1)).equals(Biome.BiomeCategory.SWAMP)) {
                pLevel.setBlock(blockpos1, pState, 2);
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        Vec3 offset = state.getOffset(worldIn, pos);
        return SHAPE.move(offset.x,offset.y,offset.z);
    }

    @Override
    public OffsetType getOffsetType() {
        return super.getOffsetType().XZ;
    }
}


package net.tobiasfiller.miltenmagic.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.tobiasfiller.miltenmagic.MiltenMagic;

public class SwampweedBlock extends BushBlock implements BonemealableBlock {

    /** Custom biome tag so swampweed only spreads inside swamps. */
    public static final TagKey<Biome> IS_SWAMP = TagKey.create(Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(MiltenMagic.MOD_ID, "is_swamp"));

    private final VoxelShape SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 20.0D, 12.0D);

    public SwampweedBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
        if (pRandom.nextInt(25) == 0) {
            attemptSpread(pState, pLevel, pPos, pRandom);
        }
    }

    private void attemptSpread(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
        BlockPos blockpos1 = pPos.offset(pRandom.nextInt(3) - 1, pRandom.nextInt(2) - pRandom.nextInt(2), pRandom.nextInt(3) - 1);

        for(int k = 0; k < 4; ++k) {
            if (pLevel.isEmptyBlock(blockpos1) && pState.canSurvive(pLevel, blockpos1)) {
                pPos = blockpos1;
            }

            blockpos1 = pPos.offset(pRandom.nextInt(3) - 1, pRandom.nextInt(2) - pRandom.nextInt(2), pRandom.nextInt(3) - 1);
        }

        if (pLevel.isEmptyBlock(blockpos1) && pState.canSurvive(pLevel, blockpos1) && pLevel.getBiome(blockpos1).is(IS_SWAMP)) {
            pLevel.setBlock(blockpos1, pState, 2);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        Vec3 offset = state.getOffset(worldIn, pos);
        return SHAPE.move(offset.x,offset.y,offset.z);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader pLevel, BlockPos pPos, BlockState pState) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level pLevel, RandomSource pRandom, BlockPos pPos, BlockState pState) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel pLevel, RandomSource pRandom, BlockPos pPos, BlockState pState) {
        attemptSpread(pState,pLevel,pPos,pRandom);
    }

    @Override
    public com.mojang.serialization.MapCodec<? extends BushBlock> codec() {
        return simpleCodec(SwampweedBlock::new);
    }
}
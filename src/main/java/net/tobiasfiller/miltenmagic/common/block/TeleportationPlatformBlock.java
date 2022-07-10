package net.tobiasfiller.miltenmagic.common.block;

import net.tobiasfiller.miltenmagic.core.registry.ParticleRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

public class TeleportationPlatformBlock extends Block {

    public static final IntegerProperty TILENUMBER = IntegerProperty.create("tile_number", 1, 26);
    public static final IntegerProperty TILEMAT = IntegerProperty.create("tile_mat", 1, 2);

    private static final List<Integer> isLapis = Arrays.asList(5, 16, 17, 20, 21);

    public TeleportationPlatformBlock() {
        super(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.STONE)
                .strength(1.5F, 6.0F)
                .sound(SoundType.STONE)
        );
        this.registerDefaultState(this.stateDefinition.any().setValue(TILENUMBER, 10));
        this.registerDefaultState(this.stateDefinition.any().setValue(TILEMAT, 1));
    }

    @Override
    public void animateTick(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, Random random) {

        if (random.nextInt() % 3 == 0) {

            level.addParticle(ParticleRegistry.TELEPORTATION_PARTICLE.get(),
                    ((double) pos.getX()) + (random.nextDouble()),
                    ((double) pos.getY()) + 1 + (random.nextDouble() * 0.3),
                    ((double) pos.getZ()) + (random.nextDouble()),
                    0, random.nextDouble() * 0.02, 0);
        }
    }

    public static BlockState SetTileNumber(BlockState blockState, int number) {
        if (isLapis.contains(number)){
            blockState = blockState.setValue(TILEMAT,2);
        }
        return blockState.setValue(TILENUMBER, number);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TILENUMBER);
        builder.add(TILEMAT);
    }

    @Override
    public void wasExploded(@NotNull Level worldIn, @NotNull BlockPos pos, Explosion explosionIn) {
        List<BlockPos> blockPoses = explosionIn.getToBlow();

        for (BlockPos current : blockPoses) {
            if (worldIn.getBlockState(current).is(this)) {
                destroy(worldIn, current, worldIn.getBlockState(current));
            }
        }
    }

    @Override
    public void playerDestroy(@NotNull Level worldIn, @NotNull Player player, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable BlockEntity blockEntity, @NotNull ItemStack stack) {
        destroy(worldIn,pos,state);
    }

    public void destroy(@NotNull LevelAccessor worldIn, @NotNull BlockPos pos, BlockState state){
        if (state.is(this)) {
            switch (state.getValue(TILENUMBER)) {
                case 1:
                    destroyPlatform3x3((Level) worldIn, new BlockPos(pos.getX() + 1, pos.getY(), pos.getZ() + 1), pos);
                case 2:
                    destroyPlatform3x3((Level) worldIn, new BlockPos(pos.getX(), pos.getY(), pos.getZ() + 1), pos);
                case 3:
                    destroyPlatform3x3((Level) worldIn, new BlockPos(pos.getX() - 1, pos.getY(), pos.getZ() + 1), pos);
                case 4:
                    destroyPlatform3x3((Level) worldIn, new BlockPos(pos.getX() + 1, pos.getY(), pos.getZ()), pos);
                case 5:
                    destroyPlatform3x3((Level) worldIn, pos, pos);
                case 6:
                    destroyPlatform3x3((Level) worldIn, new BlockPos(pos.getX() - 1, pos.getY(), pos.getZ()), pos);
                case 7:
                    destroyPlatform3x3((Level) worldIn, new BlockPos(pos.getX() + 1, pos.getY(), pos.getZ() - 1), pos);
                case 8:
                    destroyPlatform3x3((Level) worldIn, new BlockPos(pos.getX(), pos.getY(), pos.getZ() - 1), pos);
                case 9:
                    destroyPlatform3x3((Level) worldIn, new BlockPos(pos.getX() - 1, pos.getY(), pos.getZ() - 1), pos);
                case 11:
                    destroyPlatform4x4((Level) worldIn, new BlockPos(pos.getX() + 1, pos.getY(), pos.getZ() + 1), pos);
                case 12:
                    destroyPlatform4x4((Level) worldIn, new BlockPos(pos.getX(), pos.getY(), pos.getZ() + 1), pos);
                case 13:
                    destroyPlatform4x4((Level) worldIn, new BlockPos(pos.getX() - 1, pos.getY(), pos.getZ() + 1), pos);
                case 14:
                    destroyPlatform4x4((Level) worldIn, new BlockPos(pos.getX() - 2, pos.getY(), pos.getZ() + 1), pos);
                case 15:
                    destroyPlatform4x4((Level) worldIn, new BlockPos(pos.getX() + 1, pos.getY(), pos.getZ()), pos);
                case 16:
                    destroyPlatform4x4((Level) worldIn, pos, pos);
                case 17:
                    destroyPlatform4x4((Level) worldIn, new BlockPos(pos.getX() - 1, pos.getY(), pos.getZ()), pos);
                case 18:
                    destroyPlatform4x4((Level) worldIn, new BlockPos(pos.getX() - 2, pos.getY(), pos.getZ()), pos);
                case 19:
                    destroyPlatform4x4((Level) worldIn, new BlockPos(pos.getX() + 1, pos.getY(), pos.getZ() - 1), pos);
                case 20:
                    destroyPlatform4x4((Level) worldIn, new BlockPos(pos.getX(), pos.getY(), pos.getZ() - 1), pos);
                case 21:
                    destroyPlatform4x4((Level) worldIn, new BlockPos(pos.getX() - 1, pos.getY(), pos.getZ() - 1), pos);
                case 22:
                    destroyPlatform4x4((Level) worldIn, new BlockPos(pos.getX() - 2, pos.getY(), pos.getZ() - 1), pos);
                case 23:
                    destroyPlatform4x4((Level) worldIn, new BlockPos(pos.getX() + 1, pos.getY(), pos.getZ() - 2), pos);
                case 24:
                    destroyPlatform4x4((Level) worldIn, new BlockPos(pos.getX(), pos.getY(), pos.getZ() - 2), pos);
                case 25:
                    destroyPlatform4x4((Level) worldIn, new BlockPos(pos.getX() - 1, pos.getY(), pos.getZ() - 2), pos);
                case 26:
                    destroyPlatform4x4((Level) worldIn, new BlockPos(pos.getX() - 2, pos.getY(), pos.getZ() - 2), pos);
                default:

            }
        }
    }

    private void destroyPlatform3x3(Level world, BlockPos pos, BlockPos ignore) {
        destroyPlatform(world,pos,ignore,3);
    }

    private void destroyPlatform4x4(Level world, BlockPos pos, BlockPos ignore) {
        destroyPlatform(world,pos,ignore,4);
    }

    private void destroyPlatform(Level world, BlockPos pos, BlockPos ignore,Integer size) {
        for (int x = -1; x < size - 1; x++) {
            for (int z = -1; z < size - 1; z++) {
                BlockPos tmpPos = new BlockPos(pos.getX() + x, pos.getY(), pos.getZ() + z);
                if (!world.getBlockState(tmpPos).is(this)){
                    continue;
                }
                BlockState blockState;
                if (z >= 0 && z < size - 2 && x >= 0 && x < size - 2) {
                    blockState = Blocks.LAPIS_BLOCK.defaultBlockState();
                } else {
                    blockState = Blocks.CHISELED_STONE_BRICKS.defaultBlockState();
                }
                world.setBlock(tmpPos, blockState,1);
            }
        }
    }


}

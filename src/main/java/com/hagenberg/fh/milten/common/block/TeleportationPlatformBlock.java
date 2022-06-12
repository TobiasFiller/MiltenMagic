package com.hagenberg.fh.milten.common.block;

import com.hagenberg.fh.milten.common.tileEntity.TeleportationPlatformTileEntity;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.*;

public class TeleportationPlatformBlock extends Block {

    public static final IntegerProperty TILENUMBER = IntegerProperty.create("tile_number", 1, 26);
    public static final IntegerProperty TILEMAT = IntegerProperty.create("tile_mat", 1, 2);

    private static final List<Integer> isLapis = Arrays.asList(5, 16, 17, 20, 21);

    public TeleportationPlatformBlock() {
        super(AbstractBlock.Properties.create(Material.ROCK, MaterialColor.BLUE)
                .hardnessAndResistance(5f)
                .sound(SoundType.STONE)
        );
        this.setDefaultState(this.stateContainer.getBaseState().with(TILENUMBER, 10));
        this.setDefaultState(this.stateContainer.getBaseState().with(TILEMAT, 1));
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TeleportationPlatformTileEntity();
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    public static BlockState SetTileNumber(BlockState blockState, int number) {
        if (isLapis.contains(number)){
            blockState = blockState.with(TILEMAT,2);
        }
        return blockState.with(TILENUMBER, number);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(TILENUMBER);
        builder.add(TILEMAT);
    }

    @Override
    public void onExplosionDestroy(World worldIn, BlockPos pos, Explosion explosionIn) {
        List<BlockPos> blockPoses = explosionIn.getAffectedBlockPositions();

        for (BlockPos current : blockPoses) {
            if (worldIn.getBlockState(current).matchesBlock(this)) {
                onDestroy(worldIn, current, worldIn.getBlockState(current));
            }
        }
    }

    @Override
    public void onPlayerDestroy(IWorld worldIn, BlockPos pos, BlockState state) {
        onDestroy(worldIn,pos,state);

    }

    private void onDestroy(IWorld worldIn, BlockPos pos, BlockState state){
        if (state.matchesBlock(this)) {
            switch (state.get(TILENUMBER)) {
                case 1:
                    destroyPlatform3x3((World) worldIn, new BlockPos(pos.getX() + 1, pos.getY(), pos.getZ() + 1), pos);
                case 2:
                    destroyPlatform3x3((World) worldIn, new BlockPos(pos.getX(), pos.getY(), pos.getZ() + 1), pos);
                case 3:
                    destroyPlatform3x3((World) worldIn, new BlockPos(pos.getX() - 1, pos.getY(), pos.getZ() + 1), pos);
                case 4:
                    destroyPlatform3x3((World) worldIn, new BlockPos(pos.getX() + 1, pos.getY(), pos.getZ()), pos);
                case 5:
                    destroyPlatform3x3((World) worldIn, pos, pos);
                case 6:
                    destroyPlatform3x3((World) worldIn, new BlockPos(pos.getX() - 1, pos.getY(), pos.getZ()), pos);
                case 7:
                    destroyPlatform3x3((World) worldIn, new BlockPos(pos.getX() + 1, pos.getY(), pos.getZ() - 1), pos);
                case 8:
                    destroyPlatform3x3((World) worldIn, new BlockPos(pos.getX(), pos.getY(), pos.getZ() - 1), pos);
                case 9:
                    destroyPlatform3x3((World) worldIn, new BlockPos(pos.getX() - 1, pos.getY(), pos.getZ() - 1), pos);
                case 11:
                    destroyPlatform4x4((World) worldIn, new BlockPos(pos.getX() + 1, pos.getY(), pos.getZ() + 1), pos);
                case 12:
                    destroyPlatform4x4((World) worldIn, new BlockPos(pos.getX(), pos.getY(), pos.getZ() + 1), pos);
                case 13:
                    destroyPlatform4x4((World) worldIn, new BlockPos(pos.getX() - 1, pos.getY(), pos.getZ() + 1), pos);
                case 14:
                    destroyPlatform4x4((World) worldIn, new BlockPos(pos.getX() - 2, pos.getY(), pos.getZ() + 1), pos);
                case 15:
                    destroyPlatform4x4((World) worldIn, new BlockPos(pos.getX() + 1, pos.getY(), pos.getZ()), pos);
                case 16:
                    destroyPlatform4x4((World) worldIn, pos, pos);
                case 17:
                    destroyPlatform4x4((World) worldIn, new BlockPos(pos.getX() - 1, pos.getY(), pos.getZ()), pos);
                case 18:
                    destroyPlatform4x4((World) worldIn, new BlockPos(pos.getX() - 2, pos.getY(), pos.getZ()), pos);
                case 19:
                    destroyPlatform4x4((World) worldIn, new BlockPos(pos.getX() + 1, pos.getY(), pos.getZ() - 1), pos);
                case 20:
                    destroyPlatform4x4((World) worldIn, new BlockPos(pos.getX(), pos.getY(), pos.getZ() - 1), pos);
                case 21:
                    destroyPlatform4x4((World) worldIn, new BlockPos(pos.getX() - 1, pos.getY(), pos.getZ() - 1), pos);
                case 22:
                    destroyPlatform4x4((World) worldIn, new BlockPos(pos.getX() - 2, pos.getY(), pos.getZ() - 1), pos);
                case 23:
                    destroyPlatform4x4((World) worldIn, new BlockPos(pos.getX() + 1, pos.getY(), pos.getZ() - 2), pos);
                case 24:
                    destroyPlatform4x4((World) worldIn, new BlockPos(pos.getX(), pos.getY(), pos.getZ() - 2), pos);
                case 25:
                    destroyPlatform4x4((World) worldIn, new BlockPos(pos.getX() - 1, pos.getY(), pos.getZ() - 2), pos);
                case 26:
                    destroyPlatform4x4((World) worldIn, new BlockPos(pos.getX() - 2, pos.getY(), pos.getZ() - 2), pos);
                default:

            }
        }
    }

    private void destroyPlatform3x3(World world, BlockPos pos, BlockPos ignore) {
        destroyPlatform(world,pos,ignore,3);
    }

    private void destroyPlatform4x4(World world, BlockPos pos, BlockPos ignore) {
        destroyPlatform(world,pos,ignore,4);
    }

    private void destroyPlatform(World world, BlockPos pos, BlockPos ignore,Integer size) {
        for (int x = -1; x < size - 1; x++) {
            for (int z = -1; z < size - 1; z++) {
                BlockPos tmpPos = new BlockPos(pos.getX() + x, pos.getY(), pos.getZ() + z);
                if (!world.getBlockState(tmpPos).getBlock().matchesBlock(this)){
                    continue;
                }
                BlockState blockState;
                if (z >= 0 && z < size - 2 && x >= 0 && x < size - 2) {
                    blockState = Blocks.LAPIS_BLOCK.getDefaultState();
                } else {
                    blockState = Blocks.CHISELED_STONE_BRICKS.getDefaultState();
                }
                world.setBlockState(tmpPos, blockState);
            }
        }
    }
}

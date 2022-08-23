package net.tobiasfiller.miltenmagic.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MagicalLecternBlock extends LecternBlock {

    public static final VoxelShape SHAPE_BASE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);
    public static final VoxelShape SHAPE_POST = Block.box(6.0D, 3.0D, 6.0D, 10.0D, 18.0D, 10.0D);
    public static final VoxelShape SHAPE_COMMON = Shapes.or(SHAPE_BASE, SHAPE_POST);
    public static final VoxelShape SHAPE_TOP_PLATE = Block.box(0.0D, 19.0D, 0.0D, 16.0D, 19.0D, 16.0D);
    public static final VoxelShape SHAPE_COLLISION = Shapes.or(SHAPE_COMMON, SHAPE_TOP_PLATE);
    public static final VoxelShape SHAPE_WEST = Shapes.or(Block.box(0.0D, 14.0D, 1.0D, 5.333333D, 18.0D, 15.0D), Block.box(5.333333D, 16.0D, 1.0D, 9.666667D, 20.0D, 15.0D), Block.box(9.666667D, 18.0D, 1.0D, 14.0D, 22.0D, 15.0D), SHAPE_COMMON);
    public static final VoxelShape SHAPE_NORTH = Shapes.or(Block.box(1.0D, 14.0D, 0.0D, 15.0D, 18.0D, 5.333333D), Block.box(1.0D, 16.0D, 5.333333D, 15.0D, 20.0D, 9.666667D), Block.box(1.0D, 18.0D, 9.666667D, 15.0D, 22.0D, 14.0D), SHAPE_COMMON);
    public static final VoxelShape SHAPE_EAST = Shapes.or(Block.box(10.666667D, 14.0D, 1.0D, 15.0D, 18.0D, 15.0D), Block.box(6.333333D, 16.0D, 1.0D, 10.666667D, 20.0D, 15.0D), Block.box(2.0D, 18.0D, 1.0D, 6.333333D, 22.0D, 15.0D), SHAPE_COMMON);
    public static final VoxelShape SHAPE_SOUTH = Shapes.or(Block.box(1.0D, 14.0D, 10.666667D, 15.0D, 18.0D, 15.0D), Block.box(1.0D, 16.0D, 6.333333D, 15.0D, 20.0D, 10.666667D), Block.box(1.0D, 18.0D, 2.0D, 15.0D, 22.0D, 6.333333D), SHAPE_COMMON);

    public MagicalLecternBlock() {
        super(BlockBehaviour.Properties.of(Material.METAL).strength(2.5F).sound(SoundType.METAL).noOcclusion());
    }

    public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE_COLLISION;
    }

    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        switch((Direction)pState.getValue(FACING)) {
            case NORTH:
                return SHAPE_NORTH;
            case SOUTH:
                return SHAPE_SOUTH;
            case EAST:
                return SHAPE_EAST;
            case WEST:
                return SHAPE_WEST;
            default:
                return SHAPE_COMMON;
        }
    }
}

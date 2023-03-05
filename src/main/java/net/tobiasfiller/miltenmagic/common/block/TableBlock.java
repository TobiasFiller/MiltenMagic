package net.tobiasfiller.miltenmagic.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TableBlock extends Block {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final IntegerProperty SIDE = IntegerProperty.create("side", 0, 2); // 0 = single, 1 = double_left, 2 = double_right

    public static final VoxelShape TABLETOP = Block.box(0.0D,12.0D,0.0D,16.0D,16.0D,16.0D);
    public static final VoxelShape LEG_SW = Block.box(2.0D,0.0D,10.0D,6.0D,12.0D,14.0D);
    public static final VoxelShape LEG_SE = Block.box(10.0D,0.0D,10.0D,14.0D,12.0D,14.0D);
    public static final VoxelShape LEG_NW = Block.box(2.0D,0.0D,2.0D,6.0D,12.0D,6.0D);
    public static final VoxelShape LEG_NE = Block.box(10.0D,0.0D,2.0D,14.0D,12.0D,6.0D);
    public static final VoxelShape FOUR_LEGS = Shapes.or(LEG_SW,LEG_SE,LEG_NW,LEG_NE);
    public static final VoxelShape SINGLE_SHAPE_WEST_EAST = Shapes.or(Block.box(6.0D, 2.0D, 4.0D, 10.0D, 6.0D, 12.0D), TABLETOP, FOUR_LEGS);
    public static final VoxelShape SINGLE_SHAPE_NORTH_SOUTH = Shapes.or(Block.box(4.0D, 2.0D, 6.0D, 12.0D, 6.0D, 10.0D), TABLETOP, FOUR_LEGS);
    public static final VoxelShape DOUBLE_SHAPE_WEST = Shapes.or(Block.box(6.0D, 2.0D, 4.0D, 10.0D, 6.0D, 16.0D), TABLETOP, LEG_NW,LEG_NE);
    public static final VoxelShape DOUBLE_SHAPE_NORTH = Shapes.or(Block.box(0.0D, 2.0D, 6.0D, 12.0D, 6.0D, 10.0D), TABLETOP, LEG_NE,LEG_SE);
    public static final VoxelShape DOUBLE_SHAPE_EAST = Shapes.or(Block.box(6.0D, 2.0D, 0.0D, 10.0D, 6.0D, 12.0D), TABLETOP, LEG_SE,LEG_SW);
    public static final VoxelShape DOUBLE_SHAPE_SOUTH = Shapes.or(Block.box(4.0D, 2.0D, 6.0D, 16.0D, 6.0D, 10.0D), TABLETOP, LEG_NW,LEG_SW);

    public TableBlock() {
        super(BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(2.0F, 3.0F).sound(SoundType.WOOD));
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
        this.registerDefaultState(this.stateDefinition.any().setValue(SIDE, 0));
    }

    @Override
    public float getSpeedFactor() {
        return super.getSpeedFactor();
    }

    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        switch ((int)pState.getValue(SIDE)){
            case 0:
                switch((Direction)pState.getValue(FACING)) {
                    case NORTH:
                    case SOUTH:
                        return SINGLE_SHAPE_NORTH_SOUTH;
                    case EAST:
                    case WEST:
                        return SINGLE_SHAPE_WEST_EAST;
                    default:
                        return Block.box(0,0,0,16,16,16);
                }
            case 1:
                switch((Direction)pState.getValue(FACING)) {
                    case NORTH:
                        return DOUBLE_SHAPE_NORTH;
                    case SOUTH:
                        return DOUBLE_SHAPE_SOUTH;
                    case EAST:
                        return DOUBLE_SHAPE_EAST;
                    case WEST:
                        return DOUBLE_SHAPE_WEST;
                    default:
                        return Block.box(0,0,0,16,16,16);
                }
            case 2:
                switch((Direction)pState.getValue(FACING)) {
                    case NORTH:
                        return DOUBLE_SHAPE_SOUTH;
                    case SOUTH:
                        return DOUBLE_SHAPE_NORTH;
                    case EAST:
                        return DOUBLE_SHAPE_WEST;
                    case WEST:
                        return DOUBLE_SHAPE_EAST;
                    default:
                        return Block.box(0,0,0,16,16,16);
                }
            default:
                return Block.box(0,0,0,16,16,16);
        }
    }

    private boolean isSingle(BlockState blockstate) {
        return blockstate.getValue(SIDE) == 0;
    }

    private BlockState getDoubleBlock(int side, Direction direction) {
        return this.defaultBlockState().setValue(FACING, direction).setValue(SIDE, side);
    }

    @Override
    public void wasExploded(Level pLevel, BlockPos pPos, Explosion pExplosion) {
        List<BlockPos> blockPoses = pExplosion.getToBlow();

        for (BlockPos current : blockPoses) {
            if (pLevel.getBlockState(current).is(this)) {
                destroy(pLevel, current, pLevel.getBlockState(current));
            }
        }
    }

    @Override
    public void playerDestroy(Level pLevel, Player pPlayer, BlockPos pPos, BlockState pState, @Nullable BlockEntity pBlockEntity, ItemStack pTool) {
        super.playerDestroy(pLevel,pPlayer,pPos,pState,pBlockEntity,pTool);
        destroy(pLevel, pPos, pState);
    }

    @Override
    public void destroy(LevelAccessor pLevel, BlockPos pPos, BlockState pState) {
        switch (pState.getValue(SIDE)) {
            case 1:
                switch (pState.getValue(FACING)) {
                    case NORTH:
                        if (pLevel.getBlockState(pPos.west()).is(this)) {
                            pLevel.setBlock(pPos.west(), defaultBlockState().setValue(FACING, pState.getValue(FACING)), 1);
                        }
                        break;

                    case EAST:
                        if (pLevel.getBlockState(pPos.north()).is(this)) {
                            pLevel.setBlock(pPos.north(), defaultBlockState().setValue(FACING, pState.getValue(FACING)), 1);
                        }
                        break;
                    case SOUTH:
                        if (pLevel.getBlockState(pPos.east()).is(this)) {
                            pLevel.setBlock(pPos.east(), defaultBlockState().setValue(FACING, pState.getValue(FACING)), 1);
                        }
                        break;
                    case WEST:
                        if (pLevel.getBlockState(pPos.south()).is(this)) {
                            pLevel.setBlock(pPos.south(), defaultBlockState().setValue(FACING, pState.getValue(FACING)), 1);
                        }
                        break;
                }
                break;
            case 2:
                switch (pState.getValue(FACING)) {
                    case NORTH:
                        if (pLevel.getBlockState(pPos.east()).is(this)) {
                            pLevel.setBlock(pPos.east(), defaultBlockState().setValue(FACING, pState.getValue(FACING)), 1);
                        }
                        break;
                    case EAST:
                        if (pLevel.getBlockState(pPos.south()).is(this)) {
                            pLevel.setBlock(pPos.south(), defaultBlockState().setValue(FACING, pState.getValue(FACING)), 1);
                        }
                        break;
                    case SOUTH:
                        if (pLevel.getBlockState(pPos.west()).is(this)) {
                            pLevel.setBlock(pPos.west(), defaultBlockState().setValue(FACING, pState.getValue(FACING)), 1);
                        }
                        break;
                    case WEST:
                        if (pLevel.getBlockState(pPos.north()).is(this)) {
                            pLevel.setBlock(pPos.north(), defaultBlockState().setValue(FACING, pState.getValue(FACING)), 1);
                        }
                        break;
                }
                break;
            default:
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {

        Level level = pContext.getLevel();

        Player player = pContext.getPlayer();
        CollisionContext collisioncontext = player == null ? CollisionContext.empty() : CollisionContext.of(player);

        if (!level.isUnobstructed(this.defaultBlockState(),pContext.getClickedPos(),collisioncontext)) {
            return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite());
        }

        if (level.getBlockState(pContext.getClickedPos().north()).is(this)) {
            if (isSingle(level.getBlockState(pContext.getClickedPos().north()))) {
                level.setBlock(pContext.getClickedPos().north(), getDoubleBlock(1, Direction.WEST), 1);
                return getDoubleBlock(2, Direction.WEST);
            }
        }

        if (level.getBlockState(pContext.getClickedPos().east()).is(this)) {
            if (isSingle(level.getBlockState(pContext.getClickedPos().east()))) {
                level.setBlock(pContext.getClickedPos().east(), getDoubleBlock(1, Direction.NORTH), 1);
                return getDoubleBlock(2, Direction.NORTH);
            }
        }

        if (level.getBlockState(pContext.getClickedPos().south()).is(this)) {
            if (isSingle(level.getBlockState(pContext.getClickedPos().south()))) {
                level.setBlock(pContext.getClickedPos().south(), getDoubleBlock(1, Direction.EAST), 1);
                return getDoubleBlock(2, Direction.EAST);
            }
        }

        if (level.getBlockState(pContext.getClickedPos().west()).is(this)) {
            if (isSingle(level.getBlockState(pContext.getClickedPos().west()))) {
                level.setBlock(pContext.getClickedPos().west(), getDoubleBlock(1, Direction.SOUTH), 1);
                return getDoubleBlock(2, Direction.SOUTH);
            }
        }
        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite());
    }

    public BlockState rotate(BlockState pState, Rotation pRotation) {
        return pState.setValue(FACING, pRotation.rotate(pState.getValue(FACING)));
    }

    public BlockState mirror(BlockState pState, Mirror pMirror) {
        return pState.rotate(pMirror.getRotation(pState.getValue(FACING)));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING);
        pBuilder.add(SIDE);
    }

    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
        return false;
    }
}
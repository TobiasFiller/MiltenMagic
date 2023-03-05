package net.tobiasfiller.miltenmagic.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.tobiasfiller.miltenmagic.common.blockentity.StonePedestalBlockEntity;
import org.jetbrains.annotations.Nullable;

public class StonePedestalBlock extends BaseEntityBlock {


    public static final VoxelShape SHAPE = Shapes.or(Block.box(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D), Block.box(4.0D, 4.0D, 4.0D, 12.0D, 12.0D, 12.0D), Block.box(2.0D, 12.0D, 2.0D, 14.0D, 16.0D, 14.0D));

    public StonePedestalBlock() {
        super(BlockBehaviour.Properties.of(Material.STONE).destroyTime(1.2f).sound(SoundType.STONE));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new StonePedestalBlockEntity(pPos, pState);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pState.getBlock() != pNewState.getBlock()) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            if (blockEntity instanceof StonePedestalBlockEntity) {
                ((StonePedestalBlockEntity) blockEntity).drops();
            }
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        ItemStack stack = pPlayer.getItemInHand(pHand);

        BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
        if (blockEntity instanceof StonePedestalBlockEntity stonePedestalBlockEntity) {
            if (stack.isEmpty()) {
                pPlayer.setItemInHand(pHand, stonePedestalBlockEntity.getAndRemoveItem());
                return InteractionResult.SUCCESS;
            }

            if (stack.is(stonePedestalBlockEntity.getItem())){
                if (!pPlayer.isCreative()) {
                    if (stack.getMaxStackSize() > stack.getCount()) {
                        stack.grow(1);
                    } else {
                        pLevel.addFreshEntity(new ItemEntity(pLevel,pPos.getX() + 0.5d,pPos.getY() + 1d,pPos.getZ() + 0.5d,new ItemStack(stonePedestalBlockEntity.getItem())));
                    }
                }
                    stonePedestalBlockEntity.removeItem();
                    return InteractionResult.SUCCESS;

            }

            boolean result = stonePedestalBlockEntity.setItem(stack);
            if (result && !pPlayer.isCreative()) {
                stack.shrink(1);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.SUCCESS;
    }
}

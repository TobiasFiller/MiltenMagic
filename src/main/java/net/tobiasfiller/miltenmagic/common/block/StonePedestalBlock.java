package net.tobiasfiller.miltenmagic.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.tobiasfiller.miltenmagic.common.blockentity.StonePedestalBlockEntity;
import org.jetbrains.annotations.Nullable;

public class StonePedestalBlock extends BaseEntityBlock {

    public static final VoxelShape SHAPE = Shapes.or(Block.box(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D), Block.box(4.0D, 4.0D, 4.0D, 12.0D, 12.0D, 12.0D), Block.box(2.0D, 12.0D, 2.0D, 14.0D, 16.0D, 14.0D));

    public StonePedestalBlock(BlockBehaviour.Properties properties) {
        super(properties);
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
    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pNeighborBlock, BlockPos pNeighborPos, boolean pMovedByPiston) {
        super.neighborChanged(pState, pLevel, pPos, pNeighborBlock, pNeighborPos, pMovedByPiston);
        // A block was placed/changed above the pedestal: the item no longer has room,
        // so drop it on the ground (the pedestal itself stays placed).
        if (!pLevel.isClientSide && isCovered(pLevel, pPos)) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            if (blockEntity instanceof StonePedestalBlockEntity stonePedestalBlockEntity) {
                dropStoredItem(pLevel, pPos, stonePedestalBlockEntity);
            }
        }
    }

    /**
     * @return true when any non-air block sits directly above the pedestal, i.e. the
     *         pedestal has no room to display an item.
     */
    public static boolean isCovered(Level pLevel, BlockPos pPos) {
        return !pLevel.getBlockState(pPos.above()).isAir();
    }

    private static void dropStoredItem(Level pLevel, BlockPos pPos, StonePedestalBlockEntity pBlockEntity) {
        ItemStack stored = pBlockEntity.getAndRemoveItem();
        if (!stored.isEmpty()) {
            Containers.dropItemStack(pLevel, pPos.getX() + 0.5d, pPos.getY() + 1d, pPos.getZ() + 0.5d, stored);
        }
    }

    @Override
    public InteractionResult useWithoutItem(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, BlockHitResult pHit) {
        BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
        if (blockEntity instanceof StonePedestalBlockEntity stonePedestalBlockEntity) {
            // Only hand the item over when the pedestal actually holds something.
            // Calling setItemInHand with ItemStack.EMPTY unconditionally wiped the
            // player's held item whenever they clicked an empty pedestal.
            if (stonePedestalBlockEntity.getItemHandler().getStackInSlot(0).isEmpty()) {
                return InteractionResult.PASS;
            }
            pPlayer.setItemInHand(InteractionHand.MAIN_HAND, stonePedestalBlockEntity.getAndRemoveItem());
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public ItemInteractionResult useItemOn(ItemStack pStack, BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
        if (blockEntity instanceof StonePedestalBlockEntity stonePedestalBlockEntity) {
            if (pStack.is(stonePedestalBlockEntity.getItem())){
                if (!pPlayer.isCreative()) {
                    if (pStack.getMaxStackSize() > pStack.getCount()) {
                        pStack.grow(1);
                    } else {
                        // hand back the stored stack itself, so components (e.g. a linked teleport
                    // scroll in CUSTOM_DATA) survive instead of being rebuilt from the item type
                    ItemStack stored = stonePedestalBlockEntity.getItemHandler().getStackInSlot(0);
                    pLevel.addFreshEntity(new ItemEntity(pLevel,
                            pPos.getX() + 0.5d, pPos.getY() + 1d, pPos.getZ() + 0.5d, stored));
                    }
                }
                stonePedestalBlockEntity.removeItem();
                return ItemInteractionResult.SUCCESS;
            }

            boolean result = stonePedestalBlockEntity.setItem(pStack);
            if (result) {
                if (!pPlayer.isCreative()) {
                    pStack.shrink(1);
                }
                return ItemInteractionResult.SUCCESS;
            }
        }
        // Nothing was stored: let the vanilla/item interaction continue instead of
        // swallowing the click (consumesAction() would short-circuit the game mode).
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public com.mojang.serialization.MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(StonePedestalBlock::new);
    }
}

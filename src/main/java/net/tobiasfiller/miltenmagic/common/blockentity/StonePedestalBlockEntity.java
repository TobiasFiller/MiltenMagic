package net.tobiasfiller.miltenmagic.common.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.tobiasfiller.miltenmagic.core.registry.BlockEntityRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

//todo fix desynce bug when reloading a Pedestal that is already holding an Item
//todo fix tool durability bug

public class StonePedestalBlockEntity extends BlockEntity {
    protected ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    protected LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    private float age = 0.0f;
    public final float bobOffs;
    private final Random random = new Random();;


    public StonePedestalBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(BlockEntityRegistry.STONE_PEDESTAL_BLOCK_ENTITY.get(), pWorldPosition, pBlockState);
        this.bobOffs = this.random.nextFloat() * (float)Math.PI * 2.0F;
    }

    public boolean setItem(ItemStack stack){
        if (this.itemHandler.getStackInSlot(0).isEmpty()){
            stack = new ItemStack(stack.getItem(),1);
            this.itemHandler.setStackInSlot(0,stack);
            return true;
        }
        return false;
    }

    public Item getItem(){
        return this.itemHandler.getStackInSlot(0).getItem();
    }

    public ItemStack getAndRemoveItem(){
        if (!this.itemHandler.getStackInSlot(0).isEmpty()){
            ItemStack stack = this.itemHandler.getStackInSlot(0);
            this.itemHandler.setStackInSlot(0,ItemStack.EMPTY);
            return stack;
        }
        return this.itemHandler.getStackInSlot(0);
    }

    public void removeItem(){
        this.itemHandler.setStackInSlot(0,ItemStack.EMPTY);
    }

    public float getSpin(float pPartialTicks) {
        this.age += pPartialTicks;
        return (this.age) + this.bobOffs;
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {

        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY){
            return lazyItemHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("inventory", itemHandler.serializeNBT());

        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("inventory"));
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }
}

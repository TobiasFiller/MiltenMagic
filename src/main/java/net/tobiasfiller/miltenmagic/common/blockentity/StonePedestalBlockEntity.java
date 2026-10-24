package net.tobiasfiller.miltenmagic.common.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.tobiasfiller.miltenmagic.core.registry.BlockEntityRegistry;

import java.util.Random;

public class StonePedestalBlockEntity extends BlockEntity {
    protected final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            // also push the new content to the client; without this the client-side block
            // entity keeps an empty handler and the renderer never shows the held item
            if (StonePedestalBlockEntity.this.level != null
                    && !StonePedestalBlockEntity.this.level.isClientSide) {
                StonePedestalBlockEntity.this.level.sendBlockUpdated(
                        StonePedestalBlockEntity.this.worldPosition,
                        StonePedestalBlockEntity.this.getBlockState(),
                        StonePedestalBlockEntity.this.getBlockState(),
                        Block.UPDATE_CLIENTS);
            }
        }
    };

    public final float bobOffs;
    private float age = 0.0f;
    private final Random random = new Random();

    public StonePedestalBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(BlockEntityRegistry.STONE_PEDESTAL_BLOCK_ENTITY.get(), pWorldPosition, pBlockState);
        this.bobOffs = this.random.nextFloat() * (float)Math.PI * 2.0F;
    }

    /**
     * Exposed for the capability provider registered in
     * {@link net.tobiasfiller.miltenmagic.core.registry.CapabilityRegistry}.
     */
    public IItemHandler getItemHandler() {
        return this.itemHandler;
    }

    public boolean setItem(ItemStack stack){
        if (this.itemHandler.getStackInSlot(0).isEmpty()){
            // copy() keeps every DataComponent (CUSTOM_DATA holds the teleport link,
            // enchantments, custom names, damage, ...). Building a fresh
            // `new ItemStack(stack.getItem(), 1)` would keep only the item type and
            // silently strip all of them.
            ItemStack stored = stack.copy();
            stored.setCount(1);
            this.itemHandler.setStackInSlot(0, stored);
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

    /**
     * In 1.21.1 {@link BlockEntity#getUpdateTag} and {@link BlockEntity#getUpdatePacket}
     * are deliberately no-ops (they return an empty tag / null). Forge 1.18.2 filled these
     * in for you, so without overriding them the client-side block entity stays empty and
     * the renderer draws nothing until the player clicks the pedestal.
     */
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveCustomOnly(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        nbt.put("inventory", itemHandler.serializeNBT(registries));
        super.saveAdditional(nbt, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        itemHandler.deserializeNBT(registries, nbt.getCompound("inventory"));
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }
}
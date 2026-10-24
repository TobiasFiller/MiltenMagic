package net.tobiasfiller.miltenmagic.core.registry;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.core.registries.Registries;
import net.tobiasfiller.miltenmagic.MiltenMagic;
import net.tobiasfiller.miltenmagic.common.blockentity.MagicalLecternBlockEntity;
import net.tobiasfiller.miltenmagic.common.blockentity.StonePedestalBlockEntity;

public class BlockEntityRegistry {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MiltenMagic.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MagicalLecternBlockEntity>> MAGICAL_LECTERN_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("magical_lectern_block_entity", () -> BlockEntityType.Builder.of(MagicalLecternBlockEntity::new, BlockRegistry.MAGICAL_LECTERN.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<StonePedestalBlockEntity>> STONE_PEDESTAL_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("stone_pedestal_block_entity", () -> BlockEntityType.Builder.of(StonePedestalBlockEntity::new, BlockRegistry.STONE_PEDESTAL.get()).build(null));
}

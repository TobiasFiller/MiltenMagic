package net.tobiasfiller.miltenmagic.core.registry;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.tobiasfiller.miltenmagic.MiltenMagic;
import net.tobiasfiller.miltenmagic.common.blockentity.MagicalLecternBlockEntity;
import net.tobiasfiller.miltenmagic.common.blockentity.StonePedestalBlockEntity;

public class BlockEntityRegistry {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, MiltenMagic.MOD_ID);

    public static final RegistryObject<BlockEntityType<MagicalLecternBlockEntity>> MAGICAL_LECTERN_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("magical_lectern_block_entity", () -> BlockEntityType.Builder.of(MagicalLecternBlockEntity::new, BlockRegistry.MAGICAL_LECTERN.get()).build(null));

    public static final RegistryObject<BlockEntityType<StonePedestalBlockEntity>> STONE_PEDESTAL_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("stone_pedestal_block_entity", () -> BlockEntityType.Builder.of(StonePedestalBlockEntity::new, BlockRegistry.STONE_PEDESTAL.get()).build(null));

}

package net.tobiasfiller.miltenmagic.core.registry;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.core.registries.Registries;
import net.tobiasfiller.miltenmagic.MiltenMagic;
import net.tobiasfiller.miltenmagic.common.entity.FireArrowEntity;

public class EntityRegistry {
    public static DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, MiltenMagic.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<FireArrowEntity>> FIRE_ARROW = ENTITY_TYPES.register("fire_arrow",
            () -> EntityType.Builder.of((EntityType.EntityFactory<FireArrowEntity>) FireArrowEntity::new, MobCategory.MISC).sized(0.5F, 0.5F).build("fire_arrow"));
}

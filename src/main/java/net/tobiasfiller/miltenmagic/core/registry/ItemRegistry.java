package net.tobiasfiller.miltenmagic.core.registry;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.tobiasfiller.miltenmagic.MiltenMagic;
import net.tobiasfiller.miltenmagic.common.item.*;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ItemRegistry {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MiltenMagic.MOD_ID);

    // Scrolls
    public static final RegistryObject<Item> TELEPORTATION_SCROLL =
            ITEMS.register("teleportation_scroll", () -> new TeleportationSpellItem(true));

    public static final RegistryObject<Item> ABSORPTION_SCROLL =
            ITEMS.register("absorption_scroll",
                    () -> new BuffSpellItem(new Supplier<MobEffect>() {
                        @Override
                        public MobEffect get() {
                            return MobEffects.ABSORPTION;
                        }
                    }, 5000, 3, 10, 10));

    public static final RegistryObject<Item> RESISTANCE_SCROLL =
            ITEMS.register("resistance_scroll",
                    () -> new BuffSpellItem(new Supplier<MobEffect>() {
                        @Override
                        public MobEffect get() {
                            return MobEffects.DAMAGE_RESISTANCE;
                        }
                    }, 5000, 4, 30, 15));

    public static final RegistryObject<Item> MAGNETIC_SCROLL =
            ITEMS.register("magnetic_scroll",
                    () -> new BuffSpellItem(MobEffectRegistry.MAGNETIC_MOB_EFFECT, 5000, 0, 30, 10));

    public static final RegistryObject<Item> FIRE_CHARGE_SCROLL =
            ITEMS.register("fire_charge_scroll",
                    () -> new FireChargeSpell(30, 15));

    public static final RegistryObject<Item> LEVITATION_SCROLL =
            ITEMS.register("levitation_scroll",
                    () -> new LevitationSpell(10, 2));

    // Runes
    public static final RegistryObject<Item> TELEPORTATION_RUNE =
            ITEMS.register("teleportation_rune", () -> new TeleportationSpellItem(false));

    // Block Items
    public static final RegistryObject<Item> TELEPORTATION_PLATFORM = ITEMS.register("teleportation_platform", () -> new BlockItem(
            BlockRegistry.TELEPORTATION_PLATFORM_BLOCK.get(), new Item.Properties().tab(CostumCreativeModeTab.TAB_MILTEN_MAGIC)));

    public static final RegistryObject<Item> MAGICAL_LECTERN = ITEMS.register("magical_lectern", () -> new BlockItem(
            BlockRegistry.MAGICAL_LECTERN.get(), new Item.Properties().tab(CostumCreativeModeTab.TAB_MILTEN_MAGIC)));
    }
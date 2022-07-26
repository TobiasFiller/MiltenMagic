package net.tobiasfiller.miltenmagic.core.event;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.WritableBookItem;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tobiasfiller.miltenmagic.MiltenMagic;
import net.tobiasfiller.miltenmagic.common.block.MagicalLecternBlock;
import net.tobiasfiller.miltenmagic.common.item.BuffSpellItem;
import net.tobiasfiller.miltenmagic.core.registry.BlockRegistry;
import net.tobiasfiller.miltenmagic.core.registry.ItemRegistry;
import net.tobiasfiller.miltenmagic.core.registry.VillagerRegistry;

import java.util.List;

@Mod.EventBusSubscriber(modid = MiltenMagic.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EventHandler {

    @SubscribeEvent
    public static void addVillagerTrades(final VillagerTradesEvent event) {

        if (event.getType() == VillagerRegistry.FIRE_MAGE.get()) {
            Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();

            int villagerLevelOne = 1;
            int villagerLevelTwo = 2;
            int villagerLevelThree = 3;
            int villagerLevelFore = 4;

            trades.get(villagerLevelOne).add((trader, rand) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 10),
                    new ItemStack(Items.LAPIS_LAZULI, 1),
                    new ItemStack(ItemRegistry.LEVITATION_SCROLL.get(), 1),
                    16, 5, 0.01F));

            trades.get(villagerLevelOne).add((trader, rand) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 38),
                    new ItemStack(Items.LAPIS_LAZULI, 16),
                    new ItemStack(ItemRegistry.ABSORPTION_SCROLL.get(), 1),
                    4, 13, 0.01F));

            trades.get(villagerLevelTwo).add((trader, rand) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 42),
                    new ItemStack(Items.LAPIS_LAZULI, 16),
                    new ItemStack(ItemRegistry.RESISTANCE_SCROLL.get(), 1),
                    4, 15, 0.01F));

            trades.get(villagerLevelTwo).add((trader, rand) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 12),
                    new ItemStack(Items.LAPIS_LAZULI, 5),
                    new ItemStack(ItemRegistry.MAGNETIC_SCROLL.get(), 1),
                    8, 15, 0.01F));

            trades.get(villagerLevelThree).add((trader, rand) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 15),
                    new ItemStack(Items.LAPIS_LAZULI, 5),
                    new ItemStack(ItemRegistry.FIRE_CHARGE_SCROLL.get(), 1),
                    16, 15, 0.01F));

            trades.get(villagerLevelFore).add((trader, rand) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 30),
                    new ItemStack(Items.LAPIS_LAZULI, 5),
                    new ItemStack(ItemRegistry.TELEPORTATION_SCROLL.get(), 1),
                    16, 15, 0.01F));
        }
    }

    @SubscribeEvent
    public static void ItemUseOnEntity(final PlayerInteractEvent.EntityInteract event) {
        if (event.getItemStack().getItem() instanceof BuffSpellItem buffSpell) {
            Entity entity = event.getTarget();

            if (entity instanceof LivingEntity livingEntity) {
                Player player = event.getPlayer();

                if (player.isCreative() || player.experienceLevel >= buffSpell.getREQUIRED_EXP_LEVEL()) {

                    player.getCooldowns().addCooldown(event.getItemStack().getItem(), buffSpell.getCoolDown());
                    player.playSound(SoundEvents.BEACON_POWER_SELECT, 1, 1);
                    livingEntity.addEffect(new MobEffectInstance(buffSpell.getMobEffect(), buffSpell.getEffectDuration(), buffSpell.getEffectAmplifier()));

                    player.giveExperiencePoints(-buffSpell.getEXP_COST());

                    if (!player.isCreative()) {
                        event.getItemStack().shrink(1);
                    }
                } else {
                    player.displayClientMessage(new TranslatableComponent("message." + MiltenMagic.MOD_ID + ".spell.not_enough_exp"), true);
                }
                event.setCanceled(true);
            }
        }
    }

//    @SubscribeEvent
//    public static void useBookOnLectern(final PlayerInteractEvent.RightClickBlock event){
//        if (event.getWorld().getBlockState(event.getPos()).is(BlockRegistry.MAGICAL_LECTERN.get())){
//            if (event.getItemStack().getItem() instanceof WritableBookItem){
//                ((MagicalLecternBlock) event.getWorld().getBlockState(event.getPos()).getBlock()).tryPlaceBook(event.getPlayer(),event.getWorld(),event.getPos(),event.getWorld().getBlockState(event.getPos()), event.getItemStack());
//                event.setCanceled(true);
//            }
//        }
//    }
}
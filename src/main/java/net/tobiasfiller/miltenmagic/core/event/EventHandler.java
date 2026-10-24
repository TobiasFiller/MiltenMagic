package net.tobiasfiller.miltenmagic.core.event;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.neoforge.event.village.WandererTradesEvent;
import net.tobiasfiller.miltenmagic.MiltenMagic;
import net.tobiasfiller.miltenmagic.common.item.helperClasses.BuffSpellItem;
import net.tobiasfiller.miltenmagic.core.registry.ItemRegistry;
import net.tobiasfiller.miltenmagic.core.registry.MobEffectRegistry;
import net.tobiasfiller.miltenmagic.core.registry.VillagerRegistry;

import java.util.List;
import java.util.Optional;

@EventBusSubscriber(modid = MiltenMagic.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class EventHandler {

    @SubscribeEvent
    public static void addVillagerTrades(final VillagerTradesEvent event) {

        if (event.getType() == VillagerRegistry.FIRE_MAGE.get()) {
            Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();

            int villagerLevelOne = 1;
            int villagerLevelTwo = 2;
            int villagerLevelThree = 3;
            int villagerLevelFour = 4;
            int villagerLevelFive = 5;

            trades.get(villagerLevelOne).add((trader, rand) -> new MerchantOffer(
                    new ItemCost(Items.EMERALD, 10),
                    Optional.of(new ItemCost(Items.LAPIS_LAZULI, 1)),
                    new ItemStack(ItemRegistry.LEVITATION_SCROLL.get(), 1),
                    16, 5, 0.01F));

            trades.get(villagerLevelOne).add((trader, rand) -> new MerchantOffer(
                    new ItemCost(Items.EMERALD, 15),
                    Optional.of(new ItemCost(Items.LAPIS_LAZULI, 1)),
                    new ItemStack(ItemRegistry.FIRE_ARROW_SCROLL.get(), 2),
                    8, 13, 0.01F));

            trades.get(villagerLevelTwo).add((trader, rand) -> new MerchantOffer(
                    new ItemCost(Items.EMERALD, 12),
                    Optional.of(new ItemCost(Items.LAPIS_LAZULI, 5)),
                    new ItemStack(ItemRegistry.MAGNETIC_SCROLL.get(), 1),
                    8, 15, 0.01F));

            trades.get(villagerLevelThree).add((trader, rand) -> new MerchantOffer(
                    new ItemCost(Items.EMERALD, 25),
                    Optional.of(new ItemCost(Items.LAPIS_LAZULI, 5)),
                    new ItemStack(ItemRegistry.FIRE_CHARGE_SCROLL.get(), 1),
                    16, 15, 0.01F));

            trades.get(villagerLevelFour).add((trader, rand) -> new MerchantOffer(
                    new ItemCost(Items.EMERALD, 30),
                    Optional.of(new ItemCost(Items.LAPIS_LAZULI, 5)),
                    new ItemStack(ItemRegistry.TELEPORTATION_SCROLL.get(), 1),
                    16, 15, 0.01F));

            trades.get(villagerLevelFour).add((trader, rand) -> new MerchantOffer(
                    new ItemCost(Items.EMERALD, 40),
                    Optional.of(new ItemCost(Items.LAPIS_LAZULI, 10)),
                    new ItemStack(ItemRegistry.FIRE_RAINE_SCROLL.get(), 1),
                    4, 15, 0.01F));

            trades.get(villagerLevelFive).add((trader,rand) -> new MerchantOffer(
                    new ItemCost(Items.EMERALD, 50),
                    Optional.of(new ItemCost(Items.LAPIS_LAZULI, 10)),
                    new ItemStack(ItemRegistry.MAGICAL_PROTECTION_SCROLL.get(), 1),
                    1, 10, 0.01F));
        }
    }

    @SubscribeEvent
    public static void addWanderingTraderTrades(final WandererTradesEvent event) {
        List<VillagerTrades.ItemListing> trades = event.getRareTrades();
        trades.add((trader, rand) -> new MerchantOffer(
                new ItemCost(Items.EMERALD, 12),
                new ItemStack(ItemRegistry.DREAM_CALL.get(),1),1,8,0.2f
        ));
    }

    @SubscribeEvent
    public static void ItemUseOnEntity(final PlayerInteractEvent.EntityInteract event) {
        if (event.getItemStack().getItem() instanceof BuffSpellItem buffSpell) {
            Entity entity = event.getTarget();

            if (entity instanceof LivingEntity livingEntity) {
                Player player = event.getEntity();

                if (player.isCreative() || player.experienceLevel >= buffSpell.getREQUIRED_EXP_LEVEL()) {

                    player.getCooldowns().addCooldown(event.getItemStack().getItem(), buffSpell.getCoolDown());
                    player.playSound(SoundEvents.BEACON_POWER_SELECT, 1, 1);
                    livingEntity.addEffect(new MobEffectInstance(buffSpell.getMobEffect(), buffSpell.getEffectDuration(), buffSpell.getEffectAmplifier(),false,buffSpell.isVisible(),true));

                    player.giveExperiencePoints(-buffSpell.getEXP_COST());

                    if (!player.isCreative()) {
                        event.getItemStack().shrink(1);
                    }
                } else {
                    player.displayClientMessage(Component.translatable("message." + MiltenMagic.MOD_ID + ".spell.not_enough_exp"), true);
                }
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingEntityGetDamage (final LivingIncomingDamageEvent event){
        if (event.getEntity().hasEffect(MobEffectRegistry.MAGICAL_PROTECTION_MOB_EFFECT)){
            LivingEntity livingEntity = event.getEntity();
            float armor = livingEntity.getArmorCoverPercentage();
            float f = armor < 0.25f? 0.2f : armor <= 0.5f? 0.5f : 1;
            event.setAmount(event.getAmount() * f);
        }
    }
}

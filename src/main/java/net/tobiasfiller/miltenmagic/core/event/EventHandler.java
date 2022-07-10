package net.tobiasfiller.miltenmagic.core.event;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tobiasfiller.miltenmagic.MiltenMagic;
import net.tobiasfiller.miltenmagic.core.registry.ItemRegistry;
import net.tobiasfiller.miltenmagic.core.registry.VillagerRegistry;

import java.util.List;

@Mod.EventBusSubscriber(modid = MiltenMagic.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EventHandler {

    @SubscribeEvent
    public static void addVillagerTrades(final VillagerTradesEvent event) {

        if(event.getType() == VillagerRegistry.FIRE_MAGE.get()) {
            Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();
            ItemStack stack = new ItemStack(ItemRegistry.TELEPORTATION_SCROLL.get(), 1);
            int villagerLevel = 1;

            trades.get(villagerLevel).add((trader, rand) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 20),new ItemStack(Items.LAPIS_LAZULI,5),
                    stack,8,8,0.01F));
        }
    }

}
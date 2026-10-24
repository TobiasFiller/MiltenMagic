package net.tobiasfiller.miltenmagic.client;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.tobiasfiller.miltenmagic.MiltenMagic;
import net.tobiasfiller.miltenmagic.common.item.spellItems.TeleportationSpellItem;

/**
 * Client-side fixes that cannot live in {@code common/}.
 */
@EventBusSubscriber(modid = MiltenMagic.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientEvents {

    /**
     * The teleport channel holds the player in place with the vanilla slowness effect. Vanilla
     * computes the FOV modifier from {@code Attributes.MOVEMENT_SPEED} ({@code Player#getFovModifier}),
     * so slowness 9 collapses the field of view into a hard zoom. There is no separate FOV
     * attribute in 1.21, so the only way to keep the movement lock is to pin the modifier back to
     * "unchanged" while the spell is channeling.
     */
    @SubscribeEvent
    public static void onComputeFovModifier(ComputeFovModifierEvent event) {
        if (event.getPlayer() == Minecraft.getInstance().player
                && TeleportationSpellItem.isChanneling(event.getPlayer())) {
            event.setNewFovModifier(1.0F);
        }
    }
}

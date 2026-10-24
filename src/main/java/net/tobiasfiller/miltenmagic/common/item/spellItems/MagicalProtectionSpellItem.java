package net.tobiasfiller.miltenmagic.common.item.spellItems;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.tobiasfiller.miltenmagic.MiltenMagic;
import net.tobiasfiller.miltenmagic.common.item.helperClasses.BuffSpellItem;
import net.tobiasfiller.miltenmagic.core.registry.MobEffectRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MagicalProtectionSpellItem extends BuffSpellItem {

    public MagicalProtectionSpellItem( int effectDuration, int required_exp_level, int exp_cost) {
        super(() -> MobEffectRegistry.MAGICAL_PROTECTION_MOB_EFFECT, effectDuration, 0, required_exp_level, exp_cost, false);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flagIn) {

        if (Screen.hasShiftDown()){
            tooltip.add(Component.translatable("tooltip." + MiltenMagic.MOD_ID + ".magical_protection_spell.info_1"));
            tooltip.add(Component.translatable("tooltip." + MiltenMagic.MOD_ID + ".magical_protection_spell.info_2"));
            tooltip.add(Component.literal(""));
            tooltip.add(Component.translatable("tooltip." + MiltenMagic.MOD_ID + ".spell.lvl_required")
                    .append(Component.literal(
                            ChatFormatting.DARK_GREEN + (": " + REQUIRED_EXP_LEVEL))));
            tooltip.add(Component.translatable("tooltip." + MiltenMagic.MOD_ID + ".spell.exp_cost")
                    .append(Component.literal(
                            ChatFormatting.GREEN + (": " + EXP_COST))));
        } else {
            tooltip.add(Component.translatable("tooltip." + MiltenMagic.MOD_ID + ".spell.shift"));
        }
    }
}

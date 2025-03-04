package net.tobiasfiller.miltenmagic.common.item.helperClasses;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.tobiasfiller.miltenmagic.MiltenMagic;
import net.tobiasfiller.miltenmagic.common.item.CostumCreativeModeTab;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class SpellItem extends Item {

    protected final int REQUIRED_EXP_LEVEL;
    protected final int EXP_COST;
    protected final int COOL_DOWN;
    protected final boolean isScroll;
    protected final Random random = new Random();

    public SpellItem(int required_exp_level, int exp_cost, int cooldown, boolean isScroll) {
        super(new Item.Properties().tab(CostumCreativeModeTab.TAB_MILTEN_MAGIC).stacksTo(isScroll ? 64 : 1));
        this.isScroll = isScroll;
        REQUIRED_EXP_LEVEL = required_exp_level;
        EXP_COST = exp_cost;
        COOL_DOWN = cooldown;
    }

    public SpellItem(int required_exp_level, int exp_cost, boolean isScroll) {
        this(required_exp_level,exp_cost,0,isScroll);
    }

    public int getREQUIRED_EXP_LEVEL() {
        return REQUIRED_EXP_LEVEL;
    }

    public int getEXP_COST() {
        return EXP_COST;
    }

    public int getCoolDown(){
        return COOL_DOWN;
    }

    public void consumeSpell(Player pPlayer, ItemStack stack){
        consumeSpell(pPlayer,stack,EXP_COST);
    }

    public void consumeSpell(Player pPlayer, ItemStack stack, int exp_cost){
        pPlayer.getCooldowns().addCooldown(this, 20);
        pPlayer.awardStat(Stats.ITEM_USED.get(this));
        if (!pPlayer.isCreative()) {
            pPlayer.giveExperiencePoints(-exp_cost);
            if (isScroll){
                stack.shrink(1);
            }
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level worldIn, @NotNull List<Component> tooltip, @NotNull TooltipFlag flagIn) {

        if (Screen.hasShiftDown()){
            tooltip.add(new TranslatableComponent("tooltip." + MiltenMagic.MOD_ID + ".spell.lvl_required")
                    .append(new TextComponent(
                            ChatFormatting.DARK_GREEN + (": " + REQUIRED_EXP_LEVEL))));
            tooltip.add(new TranslatableComponent("tooltip." + MiltenMagic.MOD_ID + ".spell.exp_cost")
                    .append(new TextComponent(
                            ChatFormatting.GREEN + (": " + EXP_COST))));
        } else {
            tooltip.add(new TranslatableComponent("tooltip." + MiltenMagic.MOD_ID + ".spell.shift"));
        }
    }
}

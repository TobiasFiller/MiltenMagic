package net.tobiasfiller.miltenmagic.common.item;

import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.tobiasfiller.miltenmagic.MiltenMagic;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class BuffSpellItem extends SpellItem {

    public static final int USE_DURATION = 100;
    public static final int COOL_DOWN = 300;

    protected final Supplier<MobEffect> mobEffect;
    protected final int effectDuration;
    protected final int effectAmplifier;

    public BuffSpellItem(Supplier<MobEffect> mobEffect, int effectDuration, int effectAmplifier, int required_exp_level, int exp_cost) {
        super(required_exp_level,exp_cost, true);
        this.mobEffect = mobEffect;
        this.effectDuration = effectDuration;
        this.effectAmplifier = effectAmplifier;
    }

    public MobEffect getMobEffect() {
        return mobEffect.get();
    }

    public int getEffectDuration() {
        return effectDuration;
    }


    public int getCoolDown(){
        return COOL_DOWN;
    }

    public int getEffectAmplifier(){
        return effectAmplifier;
    }

    private void doEffect(Player pPlayer, ItemStack stack) {
        pPlayer.getCooldowns().addCooldown(this, COOL_DOWN);
        pPlayer.playSound(SoundEvents.BEACON_POWER_SELECT, 1, 1);
        pPlayer.addEffect(new MobEffectInstance(mobEffect.get(), effectDuration,effectAmplifier));

        pPlayer.awardStat(Stats.ITEM_USED.get(this));
        if (!pPlayer.isCreative() && isScroll) {
            pPlayer.giveExperiencePoints(-EXP_COST);
            stack.shrink(1);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack stack = pPlayer.getItemInHand(pUsedHand);

        if (pPlayer.isCreative() || pPlayer.experienceLevel >= REQUIRED_EXP_LEVEL) {
            doEffect(pPlayer, stack);

            return InteractionResultHolder.consume(stack);
        } else {
            pPlayer.displayClientMessage(new TranslatableComponent("message." + MiltenMagic.MOD_ID + ".spell.not_enough_exp"), true);
        }
        return InteractionResultHolder.fail(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        InteractionHand pUsedHand = pContext.getHand();
        Player pPlayer = pContext.getPlayer();
        assert pPlayer != null;

        ItemStack stack = pPlayer.getItemInHand(pUsedHand);

        if (pPlayer.isCreative() || pPlayer.experienceLevel >= REQUIRED_EXP_LEVEL) {
            doEffect(pPlayer, stack);

            return InteractionResult.CONSUME;
        } else {
            pPlayer.displayClientMessage(new TranslatableComponent("message." + MiltenMagic.MOD_ID + ".spell.not_enough_exp"), true);
        }
        return InteractionResult.FAIL;
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        return USE_DURATION;
    }
}

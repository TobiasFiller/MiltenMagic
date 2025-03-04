package net.tobiasfiller.miltenmagic.common.item.helperClasses;

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

    protected final Supplier<MobEffect> mobEffect;
    protected final int effectDuration;
    protected final int effectAmplifier;

    protected boolean visible;

    public BuffSpellItem(Supplier<MobEffect> mobEffect, int effectDuration, int effectAmplifier, int required_exp_level, int exp_cost) {
        this(mobEffect,effectDuration,effectAmplifier,required_exp_level,exp_cost,true);
    }

    public BuffSpellItem(Supplier<MobEffect> mobEffect, int effectDuration, int effectAmplifier, int required_exp_level, int exp_cost, boolean visible) {
        super(required_exp_level,exp_cost,300, true);
        this.mobEffect = mobEffect;
        this.effectDuration = effectDuration;
        this.effectAmplifier = effectAmplifier;
        this.visible = visible;
    }

    public MobEffect getMobEffect() {
        return mobEffect.get();
    }

    public int getEffectDuration() {
        return effectDuration;
    }



    public int getEffectAmplifier(){
        return effectAmplifier;
    }

    private void doEffect(Player pPlayer, ItemStack stack) {
        pPlayer.playSound(SoundEvents.BEACON_POWER_SELECT, 1, 1);
        pPlayer.addEffect(new MobEffectInstance(mobEffect.get(), effectDuration, effectAmplifier,false,visible,true));

        consumeSpell(pPlayer,stack);
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

    public boolean isVisible() {
        return visible;
    }


}

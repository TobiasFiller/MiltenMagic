package net.tobiasfiller.miltenmagic.common.item.helperClasses;

import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.tobiasfiller.miltenmagic.MiltenMagic;
import org.jetbrains.annotations.NotNull;

public class UseSpellItem extends SpellItem {

    public final int USE_DURATION;

    public UseSpellItem(int required_exp_level, int exp_cost, boolean isScroll, int use_Duration) {
        super(required_exp_level, exp_cost, isScroll);
        USE_DURATION = use_Duration;
    }

    @Override
    public @NotNull ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity) {
        if (pLivingEntity instanceof Player pPlayer && pLevel.isClientSide) {
            if (!pPlayer.isCreative() && isScroll) {
                pStack.shrink(1);
            }
        }
        return !pStack.isEmpty()? pStack: ItemStack.EMPTY;
    }

    @Override
    public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity, int pTimeCharged) {
        if (pLivingEntity instanceof Player pPlayer && pLevel.isClientSide) {
            if (!pPlayer.isCreative() && isScroll) {
                pStack.shrink(1);
            }
        }
    }

    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack stack = pPlayer.getItemInHand(pUsedHand);

        if (pPlayer.isCreative() || pPlayer.experienceLevel >= REQUIRED_EXP_LEVEL) {
            pPlayer.startUsingItem(pUsedHand);
        } else {
            pPlayer.displayClientMessage(new TranslatableComponent("message." + MiltenMagic.MOD_ID + ".spell.not_enough_exp"), true);
        }
        return InteractionResultHolder.fail(stack);
    }

    public int getUseDuration(ItemStack pStack) {
        return USE_DURATION;
    }

    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.BOW;
    }
}

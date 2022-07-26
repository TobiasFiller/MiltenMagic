package net.tobiasfiller.miltenmagic.common.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.tobiasfiller.miltenmagic.MiltenMagic;

public class LevitationSpell extends SpellItem {

    boolean hasBeenUsed = false;

    private static final int POWER_STEP_SIZE = 10;

    public LevitationSpell(int required_exp_level, int exp_cost) {
        super(required_exp_level, exp_cost, true);
    }

    @Override
    public void onUsingTick(ItemStack stack, LivingEntity player, int count) {
        if (player instanceof Player pPlayer && pPlayer.experienceLevel >= REQUIRED_EXP_LEVEL) {
            pPlayer.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 1, 1));

            Level pLevel = pPlayer.getLevel();
            hasBeenUsed = true;

            if (pLevel.isClientSide()) {

                double offset = 1.7d;
                double speed = 0.05d;

                double rX = (random.nextDouble() - 0.5) * offset;
                double rY = random.nextDouble() * 0.2;
                double rZ = (random.nextDouble() - 0.5) * offset;

                pLevel.addParticle(random.nextBoolean() ? ParticleTypes.ENCHANT : ParticleTypes.END_ROD,
                        pPlayer.getX() + rX,
                        pPlayer.getY() + rY,
                        pPlayer.getZ() + rZ,
                        (rX * -1.0d) * speed, speed*4, (rZ * -1.0d) * speed);

            } else {

                if (!pPlayer.isCreative() && count % POWER_STEP_SIZE == 0) {
                    pPlayer.giveExperiencePoints(-EXP_COST);
                }
            }
        }
    }

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        if (pEntity instanceof Player pPlayer && hasBeenUsed && !pIsSelected && pLevel.isClientSide) {
            if (!pPlayer.isCreative() && isScroll) {
                pStack.shrink(1);
                hasBeenUsed = false;
            }
        }
    }

    @Override
    public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity, int pTimeCharged) {
        if (pLivingEntity instanceof Player pPlayer && pLevel.isClientSide) {
            if (!pPlayer.isCreative() && isScroll) {
                pStack.shrink(1);
                hasBeenUsed = false;
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
        return 200;
    }

    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.BOW;
    }
}

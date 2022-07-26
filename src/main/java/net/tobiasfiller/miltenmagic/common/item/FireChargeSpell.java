package net.tobiasfiller.miltenmagic.common.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.tobiasfiller.miltenmagic.MiltenMagic;

import java.util.Random;

public class FireChargeSpell extends SpellItem {

    private static final int POWER_STEP_SIZE = 20;

    public static final int MIN_USE_DURATION = 5;

    private int storedEXP = 0;

    public FireChargeSpell(int required_exp_level, int exp_cost) {
        super(required_exp_level, exp_cost, true);
    }

    public int getUseDuration(ItemStack pStack) {
        return 72000;
    }

    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.BOW;
    }

    @Override
    public void onUsingTick(ItemStack pStack, LivingEntity player, int count) {
        if (player instanceof Player pPlayer) {
            Level pLevel = player.getLevel();

            int ii = this.getUseDuration(pStack) - count;
            ii -= MIN_USE_DURATION;

            if (ii < 0) {
                ii = -1;
            }

            if (pLevel.isClientSide()) {

                double offset = 2.0;
                double speed = 0.1d;

                for (int i = 0; i < 2; ++i) {

                    double rX = (random.nextDouble() - 0.5) * offset;
                    double rY = random.nextDouble() * 2;
                    double rZ = (random.nextDouble() - 0.5) * offset;

                    pLevel.addParticle(random.nextBoolean() ? ParticleTypes.SMALL_FLAME : ParticleTypes.FLAME,
                            pPlayer.getX() + rX,
                            pPlayer.getY() + rY,
                            pPlayer.getZ() + rZ,
                            (rX * -1.0d) * speed, (rY / 4 * -1.0d) * speed, (rZ * -1.0d) * speed);

                    if (ii % POWER_STEP_SIZE == 0) {
                        pPlayer.playSound(SoundEvents.BLAZE_BURN, 0.4f, 1.5f);
                    }
                }
            } else {
                int smoothness = 1;

                if (!pPlayer.isCreative() && ii >= 0 && ii % (POWER_STEP_SIZE / smoothness) == 0) {
                    storedEXP += (EXP_COST / smoothness) * (ii / POWER_STEP_SIZE + 1);
                    pPlayer.giveExperiencePoints(-EXP_COST * (ii / POWER_STEP_SIZE + 1));
                }
            }
        }
    }

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        if (pEntity instanceof Player pPlayer && storedEXP >= 0 && !pIsSelected && !pLevel.isClientSide) {
            pPlayer.giveExperiencePoints(storedEXP);
            storedEXP = 0;
        }
    }

    @Override
    public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity, int pTimeCharged) {
        if (pLivingEntity instanceof Player pPlayer) {

            int i = this.getUseDuration(pStack) - pTimeCharged;

            if (i < MIN_USE_DURATION) return;

            spawnFireCharge(pLevel, pPlayer, pStack, i);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack stack = pPlayer.getItemInHand(pUsedHand);

        if (pPlayer.isCreative() || pPlayer.experienceLevel >= REQUIRED_EXP_LEVEL) {
            pPlayer.startUsingItem(pUsedHand);

            return InteractionResultHolder.consume(stack);
        } else {
            pPlayer.displayClientMessage(new TranslatableComponent("message." + MiltenMagic.MOD_ID + ".spell.not_enough_exp"), true);
        }
        return InteractionResultHolder.fail(stack);
    }

    private void spawnFireCharge(Level pLevel, Player pPlayer, ItemStack stack, int pCharge) {
        if (!pLevel.isClientSide) {

            Fireball fireball =
                    new LargeFireball(pLevel,
                            pPlayer,
                            pPlayer.getLookAngle().x,
                            pPlayer.getLookAngle().y,
                            pPlayer.getLookAngle().z,
                            getPowerForTime(pCharge)); // explosion radius

            fireball.setPos(fireball.getX(), fireball.getY() + 1d, fireball.getZ());
            fireball.shoot(pPlayer.getLookAngle().x, pPlayer.getLookAngle().y, pPlayer.getLookAngle().z, 1, 0.01f);
            pLevel.addFreshEntity(fireball);
        }

        pLevel.playSound((Player) null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);

        storedEXP = 0;
        pPlayer.awardStat(Stats.ITEM_USED.get(this));
        if (!pPlayer.isCreative() && isScroll) {
            stack.shrink(1);
        }

        pPlayer.awardStat(Stats.ITEM_USED.get(this));
    }

    public static int getPowerForTime(int pCharge) {
        pCharge -= MIN_USE_DURATION;
        if (pCharge < 0) {
            pCharge = 0;
        }

        return pCharge / POWER_STEP_SIZE;
    }
}

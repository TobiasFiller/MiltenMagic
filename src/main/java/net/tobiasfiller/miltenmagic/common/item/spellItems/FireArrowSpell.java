package net.tobiasfiller.miltenmagic.common.item.spellItems;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.tobiasfiller.miltenmagic.MiltenMagic;
import net.tobiasfiller.miltenmagic.common.entity.FireArrowEntity;
import net.tobiasfiller.miltenmagic.common.item.helperClasses.SpellItem;
import net.tobiasfiller.miltenmagic.core.registry.SoundRegistry;

public class FireArrowSpell extends SpellItem {

    public static final int MIN_USE_DURATION = 1;

    public FireArrowSpell(int required_exp_level, int exp_cost) {
        super(required_exp_level, exp_cost,10, true);
    }

    public int getUseDuration(ItemStack pStack) {
        return 72000;
    }

    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.BOW;
    }

    @Override
    public void onUsingTick(ItemStack stack, LivingEntity player, int count) {
        Level pLevel = player.getLevel();

        int ii = this.getUseDuration(stack) - count;
        ii -= MIN_USE_DURATION;

        if (pLevel.isClientSide()) {

            double offset = 2.0;
            double speed = 0.1d;

            double rX = (random.nextDouble() - 0.5) * offset;
            double rY = random.nextDouble() * 2;
            double rZ = (random.nextDouble() - 0.5) * offset;

            pLevel.addParticle(ParticleTypes.SMALL_FLAME,
                    player.getX() + rX,
                    player.getY() + rY,
                    player.getZ() + rZ,
                    (rX * -1.0d) * speed, (rY / 4 * -1.0d) * speed, (rZ * -1.0d) * speed);

            if (ii % 20 == 0) {
                player.playSound(SoundEvents.BLAZE_BURN, 0.4f, 1.5f);
            }

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

    @Override
    public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity, int pTimeCharged) {
        if (pLivingEntity instanceof Player pPlayer) {

            int i = this.getUseDuration(pStack) - pTimeCharged;

            if (i < MIN_USE_DURATION) return;

            spawnFlamingArrow(pLevel, pPlayer, pStack);
        }
    }

    protected void spawnFlamingArrow(Level pLevel, Player pPlayer, ItemStack stack) {
        if (!pLevel.isClientSide) {

            FireArrowEntity arrow = new FireArrowEntity(pPlayer, pLevel);
            arrow.setBaseDamage(6);
            arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            arrow.shoot(
                    pPlayer.getLookAngle().x,
                    pPlayer.getLookAngle().y,
                    pPlayer.getLookAngle().z,
                    1.5f, 0.01f);

            pLevel.addFreshEntity(arrow);
        }

        pLevel.playSound((Player) null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), SoundRegistry.FIRE_ARROW.get(), SoundSource.PLAYERS, 1.0F, (float) (1.0F + (random.nextDouble() * 0.3F)));
        pLevel.playSound((Player) null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), SoundEvents.BLAZE_BURN, SoundSource.PLAYERS, 1.0F, 0.3F);

        consumeSpell(pPlayer,stack);
    }

}

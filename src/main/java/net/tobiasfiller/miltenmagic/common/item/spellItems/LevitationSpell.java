package net.tobiasfiller.miltenmagic.common.item.spellItems;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.tobiasfiller.miltenmagic.common.item.helperClasses.UseSpellItem;

public class LevitationSpell extends UseSpellItem {


    private static final int POWER_STEP_SIZE = 10;

    public LevitationSpell(int required_exp_level, int exp_cost) {
        super(required_exp_level, exp_cost, true, 400);
    }

    @Override
    public void onUsingTick(ItemStack stack, LivingEntity player, int count) {
        if (player instanceof Player pPlayer && pPlayer.experienceLevel >= REQUIRED_EXP_LEVEL) {
            pPlayer.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 1, 1));

            Level pLevel = pPlayer.getLevel();

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
}

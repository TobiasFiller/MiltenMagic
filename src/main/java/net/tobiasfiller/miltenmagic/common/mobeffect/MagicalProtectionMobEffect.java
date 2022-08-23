package net.tobiasfiller.miltenmagic.common.mobeffect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.Random;

public class MagicalProtectionMobEffect extends MobEffect {

    protected final Random random = new Random();

    public MagicalProtectionMobEffect() {
        super(MobEffectCategory.BENEFICIAL, 	14355814);
    }

    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        Level level = pLivingEntity.getLevel();
        if (level.isClientSide){
            double offset = 1;

            for (int i = 0; i < 1; ++i) {
                level.addParticle(ParticleTypes.ENCHANT,
                        pLivingEntity.getX() + (random.nextDouble() - 0.5) * offset,
                        pLivingEntity.getY() + random.nextDouble() * 2,
                        pLivingEntity.getZ() + (random.nextDouble() - 0.5) * offset,
                        0, 0, 0);
            }
        }

    }
}

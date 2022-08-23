package net.tobiasfiller.miltenmagic.common.item.spellItems;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.tobiasfiller.miltenmagic.common.item.helperClasses.UseSpellItem;
import net.tobiasfiller.miltenmagic.core.registry.ParticleRegistry;

import java.util.List;

public class FireRainSpell extends UseSpellItem {
    public FireRainSpell(int required_exp_level, int exp_cost, boolean isScroll) {
        super(required_exp_level, exp_cost, isScroll, 300);
    }

    private static final int POWER_STEP_SIZE = 10;

    @Override
    public void onUsingTick(ItemStack stack, LivingEntity player, int count) {
        Level level = player.getLevel();
        int realCount = getUseDuration(stack) - count;
        int maxA = 15;
        int baseA = 2;
        int a = Math.min(baseA + Math.abs(realCount / 20), maxA); // half the Size of the area

        if (!level.isClientSide) {

            if (player instanceof Player pPlayer && !pPlayer.isCreative() && count % POWER_STEP_SIZE == 0) {
                pPlayer.giveExperiencePoints(-EXP_COST);
            }

            AABB area = new AABB(player.getOnPos()).inflate(a,baseA,a);

            List<LivingEntity> livingEntities = level.getEntitiesOfClass(LivingEntity.class, area);

            for (LivingEntity livingEntity : livingEntities) {
                if (!livingEntity.equals(player) && random.nextDouble() < 0.3d && realCount > 5) {
                    livingEntity.hurt(DamageSource.ON_FIRE, 4);
                    if (!livingEntity.isOnFire()) {
                        livingEntity.setSecondsOnFire(4);
                    }
                }
            }
        } else {
                for (int x = 0; x < a * 2; ++x) {
                    for (int z = 0; z < a * 2; ++z) {
                        if (random.nextDouble() < (1.0d/a * 0.3d)) {
                        level.addParticle(ParticleRegistry.FIRE_RAIN_PARTICLE.get(),
                                random.nextDouble() - 0.5d + player.getOnPos().getX() + x - a,
                                player.getOnPos().getY() + 6,
                                random.nextDouble() - 0.5d + player.getOnPos().getZ() + z - a,
                                0, 0, 0);
                    }
                }
            }
        }
    }
}

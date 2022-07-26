package net.tobiasfiller.miltenmagic.common.mobeffect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class MagneticMobEffect extends MobEffect {

    protected static final int DEFAULT_WIDTH = 4;

    public MagneticMobEffect() {
        super(MobEffectCategory.BENEFICIAL, 14694187);
    }

    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        Level level = pLivingEntity.getLevel();

        if (level.isClientSide()) return;

        AABB area = new AABB(pLivingEntity.position().add(-DEFAULT_WIDTH + pAmplifier, -DEFAULT_WIDTH + pAmplifier, -DEFAULT_WIDTH + pAmplifier),
                pLivingEntity.position().add(DEFAULT_WIDTH + pAmplifier, DEFAULT_WIDTH + pAmplifier, DEFAULT_WIDTH + pAmplifier));

        List<ItemEntity> items = level.getEntities(EntityType.ITEM, area,
                item -> item.isAlive() && (!level.isClientSide || item.tickCount > 1) &&
                        (item.getThrower() == null || !item.getThrower().equals(pLivingEntity.getUUID()) || !item.hasPickUpDelay()) &&
                        !item.getItem().isEmpty() && !item.getPersistentData().contains("PreventRemoteMovement")
        );
        items.forEach(item -> item.setPos(pLivingEntity.getX(), pLivingEntity.getY(), pLivingEntity.getZ()));
    }
}

package net.tobiasfiller.miltenmagic.common.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.tobiasfiller.miltenmagic.core.registry.SoundRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Random;

public class SwampweedStem extends Item {

    protected final Random random = new Random();

    private final int experiencePoints;
    private final int duration;

    private final float probability;

    private final boolean isFoil;

    public SwampweedStem(int experiencePoints, int duration, float probability, boolean isFoil) {
        super(new Item.Properties()
                .stacksTo(16)
                .food(new FoodProperties.Builder()
                        .nutrition(1)
                        .saturationModifier(0.1f)
                        .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, duration), probability)
                        .alwaysEdible()
                        .build()));
        this.experiencePoints = experiencePoints;
        this.probability = probability;
        this.duration = duration;
        this.isFoil = isFoil;
    }

    @Override
    public SoundEvent getEatingSound() {
        return SoundRegistry.SWAMPWEED_SMOKE.get();
    }

    @Override
    public boolean isFoil(ItemStack pStack) {
        return isFoil;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity) {
        if (pLivingEntity instanceof Player player) {
            player.giveExperiencePoints(experiencePoints);

            player.awardStat(Stats.ITEM_USED.get(this));
            if (!player.isCreative()) {
                pStack.shrink(1);
                if (random.nextDouble() <= probability) {
                    if (player.hasEffect(MobEffects.CONFUSION)){
                        player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, duration + Objects.requireNonNull(player.getEffect(MobEffects.CONFUSION)).getDuration()));
                    } else {
                        player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, duration));
                    }

                }
            }
        }
        return pStack;
    }

    @Override
    public int getUseDuration(ItemStack pStack, LivingEntity entity) {
        return 64;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.BOW;
    }

    @Override
    public void onUseTick(Level level, LivingEntity player, ItemStack stack, int count) {

        if (level.isClientSide) {

            if (random.nextInt(15) == 0) {
                level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                        player.getX() + -0.25f + random.nextDouble() * 0.5f,
                        player.getY() + 1.0f,
                        player.getZ() + -0.25f + random.nextDouble() * 0.5f,
                        -0.005f + random.nextDouble() * 0.01f,
                        random.nextDouble() * 0.03f,
                        -0.005f + random.nextDouble() * 0.01f);
            }

            if (count % getUseDuration(stack, player)-30 == 0){
                player.playSound(SoundRegistry.SWAMPWEED_SMOKE.get(),0.8f,0.9f + random.nextFloat() * 0.2f);
            }

        }

    }
}

package net.tobiasfiller.miltenmagic.common.entity;

import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.tobiasfiller.miltenmagic.core.registry.EntityRegistry;

public class FireArrowEntity extends AbstractArrow {

    public FireArrowEntity(EntityType<FireArrowEntity> entityType, Level level) {
        super(entityType, level);
    }

    public FireArrowEntity(EntityType<FireArrowEntity> entityType, double x, double y, double z, Level level) {
        super(entityType, x, y, z, level, ItemStack.EMPTY, ItemStack.EMPTY);
    }

    public FireArrowEntity(EntityType<FireArrowEntity> entityType, LivingEntity shooter, Level level) {
        // 1.21.1 throws IllegalArgumentException("Invalid weapon firing an arrow") when the
        // pickup stack is empty, so the firing item must be passed through.
        this(entityType, shooter, level, shooter.getItemInHand(InteractionHand.MAIN_HAND));
    }

    public FireArrowEntity(EntityType<FireArrowEntity> entityType, LivingEntity shooter, Level level, ItemStack weapon) {
        super(entityType, shooter, level, weapon, weapon);
    }

    public FireArrowEntity(LivingEntity shooter, Level level) {
        this(EntityRegistry.FIRE_ARROW.get(), shooter, level);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide && random.nextInt(2) == 0){
            this.level().addParticle(ParticleTypes.FLAME,
                    this.position().x(),
                    this.position().y(),
                    this.position().z(),
                    -0.04f + random.nextDouble()*0.08f,-0.04f + random.nextDouble()*0.08f,-0.04f + random.nextDouble()*0.08f);
        }
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    /** Arrows spawned without a bow no longer have a default pickup item in 1.21.1. */
    @Override
    protected ItemStack getDefaultPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        super.onHitEntity(pResult);
        pResult.getEntity().igniteForSeconds(20);
    }

    @Override
    protected void onHitBlock(BlockHitResult pResult) {
        if (this.level().getBlockState(pResult.getBlockPos().above()).is(Blocks.AIR)){
            this.level().setBlockAndUpdate(pResult.getBlockPos().above(),Blocks.FIRE.defaultBlockState());
        } else if (this.level().getBlockState(pResult.getBlockPos()).isFlammable(this.level(),pResult.getBlockPos(), Direction.NORTH)){
            this.level().setBlockAndUpdate(pResult.getBlockPos(),Blocks.FIRE.defaultBlockState());
        }
        this.discard();
    }
}
package net.tobiasfiller.miltenmagic.common.entity;

import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.network.NetworkHooks;
import net.tobiasfiller.miltenmagic.core.registry.EntityRegistry;

public class FireArrowEntity extends AbstractArrow {
    public FireArrowEntity(EntityType<FireArrowEntity> entityType, Level world) {
        super(entityType, world);
    }

    public FireArrowEntity(EntityType<FireArrowEntity> entityType, double x, double y, double z, Level world) {
        super(entityType, x, y, z, world);
    }

    public FireArrowEntity(EntityType<FireArrowEntity> entityType, LivingEntity shooter, Level world) {
        super(entityType, shooter, world);
    }

    public FireArrowEntity(LivingEntity shooter, Level world) {
        super(EntityRegistry.FIRE_ARROW.get(), shooter, world);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level.isClientSide && random.nextInt(2) == 0){
            this.level.addParticle(ParticleTypes.FLAME,
                    this.position().x(),
                    this.position().y,
                    this.position().z,
                    -0.04f + random.nextDouble()*0.08f,-0.04f + random.nextDouble()*0.08f,-0.04f + random.nextDouble()*0.08f);
        }
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        super.onHitEntity(pResult);
        pResult.getEntity().setSecondsOnFire(20);
    }

    @Override
    protected void onHitBlock(BlockHitResult pResult) {
        if (this.level.getBlockState(pResult.getBlockPos().above()).is(Blocks.AIR)){
            this.level.setBlockAndUpdate(pResult.getBlockPos().above(),Blocks.FIRE.defaultBlockState());
        } else if (this.level.getBlockState(pResult.getBlockPos()).isFlammable(this.level,pResult.getBlockPos(), Direction.NORTH)){
            this.level.setBlockAndUpdate(pResult.getBlockPos(),Blocks.FIRE.defaultBlockState());
        }
        this.discard();
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
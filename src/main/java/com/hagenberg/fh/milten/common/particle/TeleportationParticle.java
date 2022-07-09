package com.hagenberg.fh.milten.common.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class TeleportationParticle extends PortalParticle {

    private TeleportationParticle(ClientLevel world, double x, double y, double z, double motionX, double motionY, double motionZ){
        super(world, x, y, z, motionX, motionY, motionZ);
        this.quadSize = (float)((double)this.quadSize * 1.5D);
        this.lifetime = (int)(Math.random() * 2.0D) + 30;
        float f = this.random.nextFloat() * 0.6F + 0.4F;
        this.rCol = f * 0.25F;
        this.gCol = f * 0.35F;
        this.bCol = f * 0.9F;
    }

    public float getScale(float scaleFactor) {
        float f = 1.0F - ((float)this.age + scaleFactor) / ((float)this.lifetime * 1.5F);
        return this.quadSize * f;
    }

    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            float f = (float)this.age / (float)this.lifetime;
            this.x += this.xd * (double)f;
            this.y += this.yd * (double)f;
            this.z += this.zd * (double)f;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet p_107570_) {
            this.sprite = p_107570_;
        }

        public Particle createParticle(@NotNull SimpleParticleType particleType, @NotNull ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            TeleportationParticle teleportationParticle = new TeleportationParticle(level, x, y, z, xSpeed, ySpeed, zSpeed);
            teleportationParticle.pickSprite(this.sprite);
            return teleportationParticle;
        }
    }

}

package com.hagenberg.fh.milten.common.particle;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TeleportationParticle extends PortalParticle {
    private TeleportationParticle(ClientWorld world, double x, double y, double z, double motionX, double motionY, double motionZ){
        super(world, x, y, z, motionX, motionY, motionZ);
        this.particleScale = (float)((double)this.particleScale * 1.5D);
        this.maxAge = (int)(Math.random() * 2.0D) + 30;
        float f = this.rand.nextFloat() * 0.6F + 0.4F;
        this.particleRed = f * 0.25F;
        this.particleGreen = f * 0.35F;
        this.particleBlue = f * 0.9F;
    }

    public float getScale(float scaleFactor) {
        float f = 1.0F - ((float)this.age + scaleFactor) / ((float)this.maxAge * 1.5F);
        return this.particleScale * f;
    }

    public void tick() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        if (this.age++ >= this.maxAge) {
            this.setExpired();
        } else {
            float f = (float)this.age / (float)this.maxAge;
            this.posX += this.motionX * (double)f;
            this.posY += this.motionY * (double)f;
            this.posZ += this.motionZ * (double)f;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements IParticleFactory<BasicParticleType> {
        private final IAnimatedSprite spriteSet;

        public Factory(IAnimatedSprite spriteSet) {
            this.spriteSet = spriteSet;
        }

        public Particle makeParticle(BasicParticleType typeIn, ClientWorld worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            TeleportationParticle teleportationParticle = new TeleportationParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed);
            teleportationParticle.selectSpriteRandomly(this.spriteSet);
            return teleportationParticle;
        }
    }

}

package com.hagenberg.fh.milten.common.tileEntity;

import com.hagenberg.fh.milten.core.init.ParticleRegistry;
import com.hagenberg.fh.milten.core.init.TileEntityRegistry;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;

import java.util.Random;


public class TeleportationPlatformTileEntity extends TileEntity implements ITickableTileEntity {

    protected static final Random random = new Random();

    public TeleportationPlatformTileEntity() {
        super(TileEntityRegistry.TELEPORTATION_PLATFORM_TILE_ENTITY.get());
    }

    @Override
    public void tick() {

        assert world != null;

        if (random.nextInt() % 30 == 0) {

            world.addParticle(ParticleRegistry.TELEPORTATION_PARTICLE.get(),
                    ((double) pos.getX()) + (random.nextDouble()),
                    ((double) pos.getY()) + 1 + (random.nextDouble() * 0.3),
                    ((double) pos.getZ()) + (random.nextDouble()),
                    0, random.nextDouble() * 0.02, 0);
        }
    }
}

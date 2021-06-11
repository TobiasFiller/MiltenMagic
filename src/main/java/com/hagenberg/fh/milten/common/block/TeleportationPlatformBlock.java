package com.hagenberg.fh.milten.common.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.Item;

public class TeleportationPlatformBlock extends AbstractBlock {

    public TeleportationPlatformBlock(Properties builder){
        super(builder);
    }

    @Override
    public Item asItem() {
        return null;
    }

    @Override
    protected Block getSelf() {
        return null;
    }
}

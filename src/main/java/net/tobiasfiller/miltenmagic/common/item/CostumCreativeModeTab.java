package net.tobiasfiller.miltenmagic.common.item;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CostumCreativeModeTab {

    public static final CreativeModeTab TAB_MILTEN_MAGIC = new CreativeModeTab("miltenmagictab") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(Items.LAPIS_LAZULI);
        }
    };

}

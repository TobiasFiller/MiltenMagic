package com.hagenberg.fh.milten.common.item;

import com.hagenberg.fh.milten.common.block.TeleportationPlatformBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.stats.Stats;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class TeleportationRuneItem extends Item {

    boolean oneUse;

    public TeleportationRuneItem(boolean _oneUse) {
        super(new Item.Properties().group(ItemGroup.TRANSPORTATION).maxStackSize(_oneUse ? 64 : 1));
        oneUse = _oneUse;
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {

        World world = context.getWorld();
        PlayerEntity playerIn = context.getPlayer();

        ItemStack stack = playerIn.getHeldItem(context.getHand());
        CompoundNBT tags = stack.getTag();

        if (tags == null) {
            stack.setTag(new CompoundNBT());
            tags = stack.getTag();
            BlockPos offPos = context.getPos();
            tags.putInt("x", offPos.getX());
            tags.putInt("y", offPos.getY());
            tags.putInt("z", offPos.getZ());
            //tags.putInt("dim", 0); //getDimension dose not work with player and or world
            tags.putFloat("direction", playerIn.rotationYaw);

            if (!world.isRemote) {
                playerIn.sendStatusMessage(new StringTextComponent(TextFormatting.GREEN + "This crystal is linked at:" + " " +
                        tags.getInt("x") + ", " +
                        tags.getInt("y") + ", " +
                        tags.getInt("z")), true);
            }

        }

        return ActionResultType.func_233537_a_(world.isRemote);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {

        ItemStack stack = playerIn.getHeldItem(handIn);
        CompoundNBT tags = stack.getTag();

        if (tags != null) {


            playerIn.playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1, 1);

            playerIn.getCooldownTracker().setCooldown(this, 60);


//            if (!worldIn.isRemote) {

                float pYaw = playerIn.rotationYaw;
                if (tags.contains("direction")) {
                    pYaw = tags.getFloat("direction");
                }

                playerIn.setPositionAndRotation(tags.getInt("x") + 0.5F, tags.getInt("y") + 1, tags.getInt("z") + 0.5F, pYaw, playerIn.prevRotationPitch);
                playerIn.setMotion(playerIn.getMotion().scale(0.5));
//            }
            playerIn.playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1, 1);

            playerIn.addStat(Stats.ITEM_USED.get(this));
            if (oneUse) {
                stack.shrink(1);
            }
        }

        return ActionResult.func_233538_a_(stack, worldIn.isRemote);
    }

    // todo: make the Teleportation on a Timer
//    @Override
//    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, LivingEntity entityLiving, int timeLeft) {
//
//    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        CompoundNBT tags = stack.getTag();

        if (tags == null) {
            tooltip.add(new StringTextComponent(
                    TextFormatting.RED + "Unlinked"));
            tooltip.add(new StringTextComponent(
                    TextFormatting.BLUE + "" + TextFormatting.ITALIC + "Right click on a Teleportation Platform to link"));

        } else {
            tooltip.add(new StringTextComponent(
                    TextFormatting.GREEN + "Linked: " + tags.getInt("x") + ", " +
                            tags.getInt("y") + ", " + tags.getInt("z")));
        }
    }
}

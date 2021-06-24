package com.hagenberg.fh.milten.common.item;

import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
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
import java.util.logging.Logger;

public class TeleportationRuneItem extends Item {

    private Logger out = Logger.getLogger("SEARCHCORNERS");
    boolean oneUse;
    private final int maxSize = 15;

    private int maxX;
    private boolean set = false;

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
                createPlatform(offPos,world,playerIn);
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

    /**
     * searches for the north west and south east corner of the platform
     * @return an empty array if the blocks dont match, else the 2 corners
     */
    private void createPlatform(BlockPos start, World worldIn,PlayerEntity PlayerIn){
        if(!worldIn.isRemote){
            if (checkLapis(start,worldIn)){
                //finds the corners of the lapis rect
                BlockPos NorthWestL = searchForLapisCorners(start,worldIn, -1, -1);
                BlockPos SouthEastL = searchForLapisCorners(start,worldIn,1,1);
                out.info("NW: " + NorthWestL.toString());
                out.info("SE: " + SouthEastL.toString());
                 BlockPos [] Corners = createCorners(NorthWestL, SouthEastL,worldIn);
                 if(Corners == null){
                     PlayerIn.sendStatusMessage(new StringTextComponent(TextFormatting.RED +
                             "The Platform needs to be a solid Rectangle surrounded with Chiseled Stone Bricks")
                             ,true);
                     return;
                 }
            }
            PlayerIn.sendStatusMessage(new StringTextComponent(TextFormatting.RED +
                    "Rightclick a rectangular Platform made of Lapis Blocks in the middle, surrounded on all sides by Chiseled Stone Bricks to create a Teleportation Platform")
                    ,true);
        }
    }


    private BlockPos searchForLapisCorners(BlockPos toCheck, World worldIn, int up, int left){
        if(set && toCheck.getX() >= maxX && left > 0 ){
            /*out.info("/////// Tocheck X is too big" + toCheck.toString());
            out.info("maxX: "+ maxX);
            out.info("left: " + left);
             */
            return null;
        }
        else if(set && toCheck.getX() <= maxX && left < 0){
            /*out.info("|||||||| Tocheck X is too small" + toCheck.toString());
            out.info("maxX: "+ maxX);
            out.info("left: " + left);

             */
            return null;
        }
        //out.info("Checking at: " + toCheck.toString());
        if(checkLapis(toCheck,worldIn)){
            BlockPos checked = searchForLapisCorners(toCheck.add(left,0,0),worldIn,up,left);
            if(checked == null){
                if(!set) {
                    set = true;
                    maxX = toCheck.getX() + left;
                    //out.info("MAX SET TO: "+maxX);
                }
                checked = searchForLapisCorners(toCheck.add(0,0,up),worldIn,up,left);

                if(checked==null){
                    set = false;
                    //out.info("#*#*#*# final found:" + toCheck.toString());
                    return toCheck;
                }
                else {

                    //out.info("####### final found and passing z:" + checked.toString());
                    return checked;

                }
            }
            else {
                //out.info("****** final found and passing x:" + checked.toString());
                return checked;
            }
        }
        //out.info("no things found at: "+ toCheck.toString());
        return null;
    }

    private boolean checkLapis(BlockPos pos, World worldIn){
        if(worldIn.getBlockState(pos).getBlock().equals(Blocks.LAPIS_BLOCK)){
            return true;
        }
        return false;
    }

    private BlockPos[] createCorners(BlockPos nw, BlockPos se,World worldIn){
        BlockPos [] out = {
                nw.add(-1,0,-1),
                new BlockPos(se.getX()+1,nw.getY(),nw.getZ()+1),
                se.add(1,0,1),
                new BlockPos(nw.getX()-1,nw.getY(),se.getZ()-1)
        };
        for(BlockPos pos : out){
            if (!worldIn.getBlockState(pos).getBlock().equals(Blocks.CHISELED_STONE_BRICKS)){
                return null;
            }
        }
        return out;
    }
}

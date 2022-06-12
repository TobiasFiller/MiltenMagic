package com.hagenberg.fh.milten.common.item;

import com.hagenberg.fh.milten.common.block.TeleportationPlatformBlock;
import com.hagenberg.fh.milten.core.init.BlockRegistry;
import com.hagenberg.fh.milten.core.init.ParticleRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.stats.Stats;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;


//? arrival particle effect -> try an event
//? player freeze without slowness zoom -> create a new slowness effect
//? Mount teleportation without dismount?
//? inta Dimensional Teleportation? -> lookup ServerPlayerEntity#changeDimension

public class TeleportationRuneItem extends Item {

    protected static final int REQUIRED_EXP_LEVEL = 5;
    protected static final int EXP_COST = 30;

    protected final boolean oneUse;
    private boolean doTeleportation;
    private Vector3d currentDest;
    private final int TELEPORT_DELAY = 200; // number of Ticks that delay the teleport
    private int teleportDelayDelta = 0;

    public TeleportationRuneItem(boolean _oneUse) {
        super(new Item.Properties().group(ItemGroup.TRANSPORTATION).maxStackSize(_oneUse ? 64 : 1));
        oneUse = _oneUse;
        doTeleportation = false;
        currentDest = null;
    }

    public boolean canPlayerBreakBlockWhileHolding(BlockState state, World worldIn, BlockPos pos, PlayerEntity player) {
        return !player.isCreative();
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        World world = context.getWorld();

        PlayerEntity playerIn = context.getPlayer();

        ItemStack stack = playerIn.getHeldItem(context.getHand());
        CompoundNBT tags = stack.getTag();


        if (tags == null || tags.isEmpty()) {
            BlockPos offPos = context.getPos();
            if (!world.isRemote) {
                Vector3d center = findAndCreatePlatform(offPos, world);
                if (center != null) {
                    setNBTTags(stack, center, playerIn, world.getDimensionKey().toString());
                } else {
                    inValidPlatform(playerIn);
                }
            }
        } else if (!world.isRemote && !doTeleportation) {
            if (world.getDimensionKey().toString().equals(tags.getString("dim"))) {
                if (checkTeleportationPlatform(world, new BlockPos(tags.getInt("x"), tags.getInt("y"), tags.getInt("z")))) {
                    if (!checkTeleportationPlatformBlocked(world, new BlockPos(tags.getInt("x"), tags.getInt("y") + 2, tags.getInt("z")))) {
                        if (playerIn.isCreative() || playerIn.experienceLevel >= REQUIRED_EXP_LEVEL) {
                            doTeleportation = true;
                            currentDest = getVector3dfromTags(tags);
                            return ActionResult.resultConsume(stack).getType();
                        } else {
                            playerIn.sendStatusMessage(new StringTextComponent(TextFormatting.DARK_GREEN + "Higher experience level required"), true);
                        }
                    } else {
                        playerIn.sendStatusMessage(new StringTextComponent(TextFormatting.RED + "The Teleportation Platform is blocked"), true);
                    }
                } else {
                    tags.remove("x");
                    tags.remove("y");
                    tags.remove("z");
                    tags.remove("direction");
                    tags.remove("dim");
                    playerIn.sendStatusMessage(new StringTextComponent(TextFormatting.RED + "Teleportation Platform has been destroyed"), true);
                    return ActionResult.resultFail(stack).getType();
                }
            } else {
                playerIn.sendStatusMessage(new StringTextComponent(TextFormatting.RED + "Wrong Dimension"), true);
            }
        }

        return ActionResultType.func_233537_a_(world.isRemote);
    }

    private boolean checkTeleportationPlatformBlocked(World world, BlockPos blockPos) {
        return !(world.getBlockState(blockPos).matchesBlock(Blocks.AIR) || world.getBlockState(blockPos).matchesBlock(Blocks.WATER));
    }


    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        CompoundNBT tags = stack.getTag();

        if (tags != null && !tags.isEmpty() && !worldIn.isRemote && !doTeleportation) {
            if (worldIn.getDimensionKey().toString().equals(tags.getString("dim"))) {
                if (checkTeleportationPlatform(worldIn, new BlockPos(tags.getInt("x"), tags.getInt("y"), tags.getInt("z")))) {
                    if (!checkTeleportationPlatformBlocked(worldIn, new BlockPos(tags.getInt("x"), tags.getInt("y") + 2, tags.getInt("z")))) {
                        if (playerIn.isCreative() || playerIn.experienceLevel >= REQUIRED_EXP_LEVEL) {
                            doTeleportation = true;
                            currentDest = getVector3dfromTags(tags);
                            return ActionResult.resultConsume(stack);
                        } else {
                            playerIn.sendStatusMessage(new StringTextComponent(TextFormatting.DARK_GREEN + "Higher experience level required"), true);
                        }
                    } else {
                        playerIn.sendStatusMessage(new StringTextComponent(TextFormatting.RED + "The teleportation platform is blocked"), true);
                    }

                } else {
                    tags.remove("x");
                    tags.remove("y");
                    tags.remove("z");
                    tags.remove("direction");
                    tags.remove("dim");
                    playerIn.sendStatusMessage(new StringTextComponent(TextFormatting.RED + "Your teleportation platform has been destroyed"), true);
                    return ActionResult.func_233538_a_(stack, false);
                }
            } else {
                playerIn.sendStatusMessage(new StringTextComponent(TextFormatting.RED + "Wrong dimension"), true);
            }
        }
//        return  ActionResult.resultFail(stack);
        return ActionResult.func_233538_a_(stack, worldIn.isRemote);
    }


    @Override
    public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (doTeleportation && entityIn instanceof PlayerEntity) {
            PlayerEntity playerIn = ((PlayerEntity) entityIn);
            if (isInMainHand(playerIn) && getVector3dfromTags(Objects.requireNonNull(playerIn.getHeldItemMainhand().getTag())).equals(currentDest)) {
                if (worldIn.isRemote) {

                    particleGenerator(worldIn, playerIn, teleportDelayDelta);
                    int teleportSoundPre = 10;
                    if (teleportDelayDelta == (TELEPORT_DELAY - teleportSoundPre)) {
                        playerIn.playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                    }

                } else {

                    teleportDelayDelta++;
                    playerIn.addPotionEffect(new EffectInstance(Effects.SLOWNESS, 1, 9));

                    if (teleportDelayDelta >= TELEPORT_DELAY) {
                        doTeleportation = false;
                        teleportDelayDelta = 0;
                        doTeleportation(worldIn, playerIn, playerIn.getHeldItemMainhand());
                    }
                }
            } else {
                doTeleportation = false;
                teleportDelayDelta = 0;
            }
        } /*else if (entityIn instanceof PlayerEntity) {
            PlayerEntity playerIn = ((PlayerEntity) entityIn);
            if (isInMainHand(playerIn) && (worldIn.isRemote)) {
                idelParticleGenerator(worldIn,playerIn,stack);
            }
        }*/
        super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
    }

    private boolean isInMainHand(PlayerEntity playerIn) {
        return playerIn.getHeldItemMainhand().getItem().equals(this);
    }

    private Vector3d getVector3dfromTags(CompoundNBT tags) {
        return new Vector3d(tags.getDouble("x"), tags.getDouble("y"), tags.getDouble("z"));
    }

    private void particleGenerator(World worldIn, Entity entityIn, int delta) {

        double offset = 1.2;

        for (int i = 0; i < 2; ++i) {
            worldIn.addParticle(ParticleTypes.ENCHANT, entityIn.getPosX() + (random.nextDouble() - 0.5) * offset, entityIn.getPosY() + random.nextDouble() * 2, entityIn.getPosZ() + (random.nextDouble() - 0.5) * offset, 0, 0, 0);
        }

        offset = 0.6;
        for (int i = 0; i < 8; ++i) {
            worldIn.addParticle(ParticleRegistry.TELEPORTATION_PARTICLE.get(), entityIn.getPosX() + (random.nextDouble() - 0.5) * offset, entityIn.getPosY() + random.nextDouble() * 2, entityIn.getPosZ() + (random.nextDouble() - 0.5) * offset, 0, 0, 0);
        }

        offset = 0.3;
        double ang = 0.0d;
        double r = 1.2;

        //particles only spawn every 2t tick and half of the teleportDelay
        if (/*delta % 3 > 0.2 &&*/ delta < TELEPORT_DELAY / 5 * 3) {
            for (double i = 0; i < 40; ++i) {

                worldIn.addParticle(ParticleRegistry.TELEPORTATION_PARTICLE.get(),
                        entityIn.getPosX() + (Math.cos(ang) * r) + ((random.nextDouble() - 0.5d) * offset),
                        entityIn.getPosY() + (((double) delta / (double) (TELEPORT_DELAY)) * ((1.0d / 0.6d) * 2.0d)) + offset,
                        entityIn.getPosZ() + (Math.sin(ang) * r) + ((random.nextDouble() - 0.5d) * offset),
                        0, 0.05, 0);

                ang += (Math.PI * 2) / 20;

            }
        }
    }


    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.EAT;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 40;
    }

    private Vector3d blockPosToVector3d(BlockPos p) {
        return blockPosToVector3d(p, 0.0d);
    }

    private Vector3d blockPosToVector3d(BlockPos p, Double offset) {
        return new Vector3d(p.getX() + offset, p.getY(), p.getZ() + offset);
    }

    private Vector3d findAndCreatePlatform(BlockPos pos, World world) {
        if (isLapisBlock(pos, world)) {
            if (isValidPlatform3x3(pos, world)) {
                createNewPlatform3x3(pos, world);
                return blockPosToVector3d(pos);
            } else if (isValidPlatform4x4(pos, world)) {
                createNewPlatform4x4(pos, world);
                return blockPosToVector3d(pos, 0.5d);
            } else if (isValidPlatform4x4(new BlockPos(pos.getX() - 1, pos.getY(), pos.getZ()), world)) {
                BlockPos blockPos = new BlockPos(pos.getX() - 1, pos.getY(), pos.getZ());
                createNewPlatform4x4(blockPos, world);
                return blockPosToVector3d(blockPos, 0.5d);
            } else if (isValidPlatform4x4(new BlockPos(pos.getX(), pos.getY(), pos.getZ() - 1), world)) {
                BlockPos blockPos = new BlockPos(pos.getX(), pos.getY(), pos.getZ() - 1);
                createNewPlatform4x4(blockPos, world);
                return blockPosToVector3d(blockPos, 0.5d);
            } else if (isValidPlatform4x4(new BlockPos(pos.getX() - 1, pos.getY(), pos.getZ() - 1), world)) {
                BlockPos blockPos = new BlockPos(pos.getX() - 1, pos.getY(), pos.getZ() - 1);
                createNewPlatform4x4(blockPos, world);
                return blockPosToVector3d(blockPos, 0.5d);
            } else {
                return null;
            }
        } else if (isChiseldStoneBlock(pos, world)) {
            BlockPos neighbor = checkNeighbors(pos, world);
            if (neighbor != null) {
                return findAndCreatePlatform(neighbor, world);
            }
        } else if (isTeleportationPlatformBlock(pos, world)) {
            return getCenterFromExistingPlatform(pos, world, 0);
        }
        return null;
    }

    private Vector3d getCenterFromExistingPlatform(BlockPos pos, World world, Integer recursionLevel) {
        if (recursionLevel >= 5) return null;

        BlockState current = world.getBlockState(pos);
        if (current.get(TeleportationPlatformBlock.TILENUMBER) == 5) {
            return blockPosToVector3d(pos);
        } else if (current.get(TeleportationPlatformBlock.TILENUMBER) == 16) {
            return blockPosToVector3d(pos, 0.5d);
        } else {
            BlockPos newPos = new BlockPos(pos.getX() - 1, pos.getY(), pos.getZ());
            Vector3d tmp = null;
            if (isTeleportationPlatformBlock(newPos, world)) {
                tmp = getCenterFromExistingPlatform(newPos, world, recursionLevel + 1);
            }
            if (tmp != null) return tmp;

            newPos = new BlockPos(pos.getX(), pos.getY(), pos.getZ() - 1);
            if (isTeleportationPlatformBlock(newPos, world)) {
                tmp = getCenterFromExistingPlatform(newPos, world, recursionLevel + 1);
            }
            if (tmp != null) return tmp;

            newPos = new BlockPos(pos.getX() + 1, pos.getY(), pos.getZ());
            if (isTeleportationPlatformBlock(newPos, world)) {
                tmp = getCenterFromExistingPlatform(newPos, world, recursionLevel + 1);
            }
            if (tmp != null) return tmp;

            newPos = new BlockPos(pos.getX(), pos.getY(), pos.getZ() + 1);
            if (isTeleportationPlatformBlock(newPos, world)) {
                tmp = getCenterFromExistingPlatform(newPos, world, recursionLevel + 1);
            }
            return tmp;
        }
    }

    /**
     * Checks all neighboring blocks (3x3) if they are a Lapisblock
     *
     * @param pos   the pos of the current block
     * @param world the current world
     * @return the blockpos of the neighboring block that is a Lapisblock or null if there is none
     */
    private BlockPos checkNeighbors(BlockPos pos, World world) {
        for (int x = -1; x < 2; x++) {
            for (int z = -1; z < 2; z++) {
                if (z == 0 && x == 0) {
                    continue;
                }
                BlockPos blockpos = new BlockPos(pos.getX() + x, pos.getY(), pos.getZ() + z);
                if (isLapisBlock(blockpos, world)) {
                    return blockpos;
                }
            }
        }
        return null;
    }

    private void createNewPlatform3x3(BlockPos pos, World world) {
        createNewPlatform(pos, world, 3);
    }

    private void createNewPlatform4x4(BlockPos pos, World world) {
        createNewPlatform(pos, world, 4, 10);
    }


    private void createNewPlatform(BlockPos pos, World world, Integer size, Integer tileNumberOffSet) {
        for (int x = -1; x < size - 1; x++) {
            for (int z = -1; z < size - 1; z++) {
                BlockPos tmpPos = new BlockPos(pos.getX() + x, pos.getY(), pos.getZ() + z);

                BlockState blockState = BlockRegistry.TELEPORTATION_PLATFORM_BLOCK.get().getDefaultState();
                blockState = TeleportationPlatformBlock
                        .SetTileNumber(blockState, (x + 2) + (z + 1) * size + tileNumberOffSet);
                world.setBlockState(tmpPos, blockState);
            }
        }
    }

    private void createNewPlatform(BlockPos pos, World world, Integer size) {
        createNewPlatform(pos, world, size, 0);
    }

    private boolean isValidPlatform4x4(BlockPos pos, World world) {
        return isValidPlatform(pos, world, 4);
    }

    private boolean isValidPlatform3x3(BlockPos pos, World world) {
        return isValidPlatform(pos, world, 3);
    }

    /**
     * Checks if the Platform is valid
     *
     * @param pos   has to be pos of the north west Lapisblock
     * @param world the world
     * @param size  the size of the Platform - must be >= 3
     * @return if it is a valid Platform
     */
    private boolean isValidPlatform(BlockPos pos, World world, Integer size) {
        boolean isValid = true;
        if (size < 3) {
            return false;
        }

        for (int x = -1; x < size - 1; x++) {
            for (int z = -1; z < size - 1; z++) {
                if (!isValid) {
                    break;
                }
                if (z >= 0 && z < size - 2 && x >= 0 && x < size - 2) {
                    isValid = isLapisBlock(new BlockPos(pos.getX() + x, pos.getY(), pos.getZ() + z), world);
                    continue;
                } else {
                    isValid = isChiseldStoneBlock(new BlockPos(pos.getX() + x, pos.getY(), pos.getZ() + z), world);
                }

            }
        }
        return isValid;
    }

    private void inValidPlatform(PlayerEntity playerIn) {
        playerIn.sendStatusMessage(new StringTextComponent(TextFormatting.RED + "Invalid Platform"), true);
    }

    private void setNBTTags(ItemStack stack, Vector3d offPos, PlayerEntity playerIn, String dim) {
        stack.setTag(new CompoundNBT());
        CompoundNBT tags = stack.getTag();

        tags.putDouble("x", offPos.getX());
        tags.putDouble("y", offPos.getY());
        tags.putDouble("z", offPos.getZ());
        tags.putString("dim", dim);
        tags.putFloat("direction", playerIn.rotationYaw);

        playerIn.sendStatusMessage(new StringTextComponent(TextFormatting.GREEN + "Linked at:" + " " +
                        tags.getDouble("x") + " / " +
                        tags.getDouble("y") + " / " +
                        tags.getDouble("z"))
                , true);
    }

    public void doTeleportation(World worldIn, PlayerEntity playerIn, ItemStack stack) {

        CompoundNBT tags = stack.getTag();
        if (!worldIn.isRemote) {

            playerIn.getCooldownTracker().setCooldown(this, 500);

            float pYaw = playerIn.rotationYaw;
            if (tags.contains("direction")) {
                pYaw = tags.getFloat("direction");
            }

            double OffsetY = (checkTeleportationPlatformBlocked(worldIn, new BlockPos(tags.getDouble("x"), tags.getDouble("y") + 1, tags.getDouble("z")))) ? 1.0d : 0.0d;

            if (playerIn instanceof ServerPlayerEntity) {
                if (playerIn.isPassenger()) {
                    Entity mount = playerIn.getRidingEntity();
                    mount.removePassengers();
                    if (mount instanceof LivingEntity) {
                        ((LivingEntity) mount).attemptTeleport(tags.getDouble("x") + 0.5d, tags.getDouble("y") + 1.0d + OffsetY, tags.getDouble("z") + 0.5d, false);
                    }
                }
                ((ServerPlayerEntity) playerIn).teleport((ServerWorld) worldIn, tags.getDouble("x") + 0.5d, tags.getDouble("y") + 1.0d + OffsetY, tags.getDouble("z") + 0.5d, pYaw, playerIn.prevRotationPitch);
            }
            playerIn.setMotion(playerIn.getMotion().scale(0.5));
            playerIn.giveExperiencePoints(-EXP_COST);

            playerIn.addStat(Stats.ITEM_USED.get(this));
            if (oneUse && !playerIn.isCreative()) {
                stack.shrink(1);
            }
        } else {
            worldIn.playSound(tags.getDouble("x"), tags.getDouble("y"), tags.getDouble("z"), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1, 1, true);
        }
    }

    private boolean checkTeleportationPlatform(World worldIn, BlockPos pos) {
        if (worldIn.isAreaLoaded(pos, 1)) {
            return worldIn.getBlockState(pos).matchesBlock(BlockRegistry.TELEPORTATION_PLATFORM_BLOCK.get());
        } else {
            return ((ServerWorld) worldIn).getBlockState(pos).matchesBlock(BlockRegistry.TELEPORTATION_PLATFORM_BLOCK.get());
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        CompoundNBT tags = stack.getTag();

        if (tags == null || tags.isEmpty()) {
            tooltip.add(new StringTextComponent(
                    TextFormatting.RED + "Unlinked"));
            tooltip.add(new StringTextComponent(
                    TextFormatting.BLUE + "" + TextFormatting.ITALIC + "Right click on a Teleportation Platform to link"));

        } else {
            tooltip.add(new StringTextComponent(
                    TextFormatting.GREEN + "Linked: " + tags.getDouble("x") + " / " +
                            tags.getDouble("y") + " / " + tags.getDouble("z")));
            tooltip.add(new StringTextComponent(
                    TextFormatting.DARK_GREEN + ("Level required: " + REQUIRED_EXP_LEVEL)));
        }
    }

    private boolean isLapisBlock(BlockPos pos, World worldIn) {
        return (worldIn.getBlockState(pos).getBlock().equals(Blocks.LAPIS_BLOCK));
    }

    private boolean isChiseldStoneBlock(BlockPos pos, World worldIn) {
        return (worldIn.getBlockState(pos).getBlock().equals(Blocks.CHISELED_STONE_BRICKS));
    }

    private boolean isTeleportationPlatformBlock(BlockPos pos, World worldIn) {
        return (worldIn.getBlockState(pos).getBlock().equals(BlockRegistry.TELEPORTATION_PLATFORM_BLOCK.get()));
    }

}
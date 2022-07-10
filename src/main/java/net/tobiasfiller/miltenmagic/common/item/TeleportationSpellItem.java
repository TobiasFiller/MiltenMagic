package net.tobiasfiller.miltenmagic.common.item;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;
import net.tobiasfiller.miltenmagic.MiltenMagic;
import net.tobiasfiller.miltenmagic.common.block.TeleportationPlatformBlock;
import net.tobiasfiller.miltenmagic.core.registry.BlockRegistry;
import net.tobiasfiller.miltenmagic.core.registry.ParticleRegistry;
import com.mojang.math.Vector3d;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Random;


//? arrival particle effect -> try an event
//? player freeze without slowness zoom -> create a new slowness effect
//? Mount teleportation without dismount?
//? inta Dimensional Teleportation? -> lookup ServerPlayerEntity#changeDimension

public class TeleportationSpellItem extends Item {

    protected static final int REQUIRED_EXP_LEVEL = 5;
    protected static final int EXP_COST = 30;
    protected final Random random = new Random();

    protected final boolean isScroll;
    private boolean doTeleportation;
    private Vec3 currentDest;
    private final int TELEPORT_DELAY = 200; // number of Ticks that delay the teleport
    private int teleportDelayDelta = 0;

    public TeleportationSpellItem(boolean isScroll) {
        super(new Item.Properties().tab(CostumCreativeModeTab.TAB_MILTEN_MAGIC).stacksTo(isScroll ? 64 : 1));
        this.isScroll = isScroll;
        this.doTeleportation = false;
        this.currentDest = null;
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();

        Player playerIn = context.getPlayer();

        assert playerIn != null;
        ItemStack stack = playerIn.getItemInHand(context.getHand());
        CompoundTag tags = stack.getTag();


        if (tags == null || tags.isEmpty()) {
            BlockPos offPos = context.getClickedPos();
            if (!world.isClientSide) {
                Vector3d center = findAndCreatePlatform(offPos, world);
                if (center != null) {
                    setNBTTags(stack, center, playerIn, world.dimension().toString());
                } else {
                    inValidPlatform(playerIn);
                }
            }
        } else if (!world.isClientSide && !doTeleportation) {
            if (world.dimension().toString().equals(tags.getString("dim"))) {
                if (checkTeleportationPlatform(world, new BlockPos(tags.getInt("x"), tags.getInt("y"), tags.getInt("z")))) {
                    if (!checkTeleportationPlatformBlocked(world, new BlockPos(tags.getInt("x"), tags.getInt("y") + 2, tags.getInt("z")))) {
                        if (playerIn.isCreative() || playerIn.experienceLevel >= REQUIRED_EXP_LEVEL) {
                            doTeleportation = true;
                            currentDest = getVector3dfromTags(tags);
                            return InteractionResult.CONSUME;
                        } else {
                            playerIn.displayClientMessage(new TranslatableComponent("message." + MiltenMagic.MOD_ID + ".teleportation_spell.not_enough_exp"), true);
                        }
                    } else {
                        playerIn.displayClientMessage(new TranslatableComponent("message." + MiltenMagic.MOD_ID + ".teleportation_spell.platform_blocked"), true);
                    }
                } else {
                    tags.remove("x");
                    tags.remove("y");
                    tags.remove("z");
                    tags.remove("direction");
                    tags.remove("dim");
                    playerIn.displayClientMessage(new TranslatableComponent("message." + MiltenMagic.MOD_ID + ".teleportation_spell.platform_destroyed"), true);
                    return InteractionResult.FAIL;
                }
            } else {
                playerIn.displayClientMessage(new TranslatableComponent("message." + MiltenMagic.MOD_ID + ".teleportation_spell.wrong_dimension"), true);
            }
        }

        return InteractionResult.sidedSuccess(world.isClientSide);
    }

    private boolean checkTeleportationPlatformBlocked(Level world, BlockPos blockPos) {
        return !(world.getBlockState(blockPos).is(Blocks.AIR) || world.getBlockState(blockPos).is(Blocks.WATER));
    }


    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level worldIn, Player playerIn, @NotNull InteractionHand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);
        CompoundTag tags = stack.getTag();

        if (tags != null && !tags.isEmpty() && !worldIn.isClientSide && !doTeleportation) {
            if (worldIn.dimension().toString().equals(tags.getString("dim"))) {
                if (checkTeleportationPlatform(worldIn, new BlockPos(tags.getInt("x"), tags.getInt("y"), tags.getInt("z")))) {
                    if (!checkTeleportationPlatformBlocked(worldIn, new BlockPos(tags.getInt("x"), tags.getInt("y") + 2, tags.getInt("z")))) {
                        if (playerIn.isCreative() || playerIn.experienceLevel >= REQUIRED_EXP_LEVEL) {
                            doTeleportation = true;
                            currentDest = getVector3dfromTags(tags);
                            return InteractionResultHolder.consume(stack);
                        } else {
                            playerIn.displayClientMessage(new TranslatableComponent("message." + MiltenMagic.MOD_ID + ".teleportation_spell.not_enough_exp"), true);
                        }
                    } else {
                        playerIn.displayClientMessage(new TranslatableComponent("message." + MiltenMagic.MOD_ID + ".teleportation_spell.platform_blocked"), true);
                    }

                } else {
                    tags.remove("x");
                    tags.remove("y");
                    tags.remove("z");
                    tags.remove("direction");
                    tags.remove("dim");
                    playerIn.displayClientMessage(new TranslatableComponent("message." + MiltenMagic.MOD_ID + ".teleportation_spell.platform_destroyed"), true);
                    return InteractionResultHolder.fail(stack);
                }
            } else {
                playerIn.displayClientMessage(new TranslatableComponent("message." + MiltenMagic.MOD_ID + ".teleportation_spell.wrong_dimension"), true);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, worldIn.isClientSide);
    }


    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level worldIn, @NotNull Entity entityIn, int itemSlot, boolean isSelected) {
        if (doTeleportation && entityIn instanceof Player playerIn) {
            if (isInMainHand(playerIn) && getVector3dfromTags(Objects.requireNonNull(playerIn.getMainHandItem().getTag())).equals(currentDest)) {
                if (worldIn.isClientSide) {

                    particleGenerator(worldIn, playerIn, teleportDelayDelta);
                    int teleportSoundPre = 10;
                    if (teleportDelayDelta == (TELEPORT_DELAY - teleportSoundPre)) {
                        playerIn.playSound(SoundEvents.ENDERMAN_TELEPORT, 1, 1);
                    }

                } else {

                    teleportDelayDelta++;
                    playerIn.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 1, 9));

                    if (teleportDelayDelta >= TELEPORT_DELAY) {
                        doTeleportation = false;
                        teleportDelayDelta = 0;
                        doTeleportation(worldIn, playerIn, playerIn.getMainHandItem());
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

    private boolean isInMainHand(Player playerIn) {
        return playerIn.getMainHandItem().getItem().equals(this);
    }

    private Vec3 getVector3dfromTags(CompoundTag tags) {
        return new Vec3(tags.getDouble("x"), tags.getDouble("y"), tags.getDouble("z"));
    }

    private void particleGenerator(Level worldIn, Entity entityIn, int delta) {

        double offset = 1.2;

        for (int i = 0; i < 2; ++i) {
            worldIn.addParticle(ParticleTypes.ENCHANT, entityIn.getX() + (random.nextDouble() - 0.5) * offset, entityIn.getY() + random.nextDouble() * 2, entityIn.getZ() + (random.nextDouble() - 0.5) * offset, 0, 0, 0);
        }

        offset = 0.6;
        for (int i = 0; i < 8; ++i) {
            worldIn.addParticle(ParticleRegistry.TELEPORTATION_PARTICLE.get(),
                    entityIn.getX() + (random.nextDouble() - 0.5) * offset,
                    entityIn.getY() + random.nextDouble() * 2,
                    entityIn.getZ() + (random.nextDouble() - 0.5) * offset,
                    0, 0, 0);
        }

        offset = 0.3;
        double ang = 0.0d;
        double r = 1.2;

        //particles only spawn every 2t tick and half of the teleportDelay
        if (/*delta % 3 > 0.2 &&*/ delta < TELEPORT_DELAY / 5 * 3) {
            for (double i = 0; i < 40; ++i) {

                worldIn.addParticle(ParticleRegistry.TELEPORTATION_PARTICLE.get(),
                        entityIn.getX() + (Math.cos(ang) * r) + ((random.nextDouble() - 0.5d) * offset),
                        entityIn.getY() + (((double) delta / (double) (TELEPORT_DELAY)) * ((1.0d / 0.6d) * 2.0d)) + offset,
                        entityIn.getZ() + (Math.sin(ang) * r) + ((random.nextDouble() - 0.5d) * offset),
                        0, 0.05, 0);

                ang += (Math.PI * 2) / 20;

            }
        }
    }


    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        return 40;
    }

    private Vector3d blockPosToVector3d(BlockPos p) {
        return blockPosToVector3d(p, 0.0d);
    }

    private Vector3d blockPosToVector3d(BlockPos p, Double offset) {
        return new Vector3d(p.getX() + offset, p.getY(), p.getZ() + offset);
    }

    private Vector3d findAndCreatePlatform(BlockPos pos, Level world) {
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

    private Vector3d getCenterFromExistingPlatform(BlockPos pos, Level world, Integer recursionLevel) {
        if (recursionLevel >= 5) return null;

        BlockState current = world.getBlockState(pos);
        if (current.getValue(TeleportationPlatformBlock.TILENUMBER) == 5) {
            return blockPosToVector3d(pos);
        } else if (current.getValue(TeleportationPlatformBlock.TILENUMBER) == 16) {
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
    private BlockPos checkNeighbors(BlockPos pos, Level world) {
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

    private void createNewPlatform3x3(BlockPos pos, Level world) {
        createNewPlatform(pos, world, 3,0);
    }

    private void createNewPlatform4x4(BlockPos pos, Level world) {
        createNewPlatform(pos, world, 4, 10);
    }


    private void createNewPlatform(BlockPos pos, Level world, Integer size, Integer tileNumberOffSet) {
        for (int x = -1; x < size - 1; x++) {
            for (int z = -1; z < size - 1; z++) {
                BlockPos tmpPos = new BlockPos(pos.getX() + x, pos.getY(), pos.getZ() + z);

                BlockState blockState = BlockRegistry.TELEPORTATION_PLATFORM_BLOCK.get().defaultBlockState();
                blockState = TeleportationPlatformBlock
                        .SetTileNumber(blockState, (x + 2) + (z + 1) * size + tileNumberOffSet);
                world.setBlock(tmpPos, blockState,3);
            }
        }
    }

    private boolean isValidPlatform4x4(BlockPos pos, Level world) {
        return isValidPlatform(pos, world, 4);
    }

    private boolean isValidPlatform3x3(BlockPos pos, Level world) {
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
    private boolean isValidPlatform(BlockPos pos, Level world, Integer size) {
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
                } else {
                    isValid = isChiseldStoneBlock(new BlockPos(pos.getX() + x, pos.getY(), pos.getZ() + z), world);
                }

            }
        }
        return isValid;
    }

    private void inValidPlatform(Player playerIn) {
        playerIn.displayClientMessage(new TranslatableComponent("message." + MiltenMagic.MOD_ID + ".teleportation_spell.invalid_platform"), true);
    }

    private void setNBTTags(ItemStack stack, Vector3d offPos, Player playerIn, String dim) {
        stack.setTag(new CompoundTag());
        CompoundTag tags = stack.getTag();

        assert tags != null;
        tags.putDouble("x", offPos.x);
        tags.putDouble("y", offPos.y);
        tags.putDouble("z", offPos.z);
        tags.putString("dim", dim);
        tags.putFloat("direction", playerIn.rotA);

        playerIn.displayClientMessage(new TranslatableComponent("message." + MiltenMagic.MOD_ID + ".teleportation_spell.linked_at")
                        .append(new TextComponent(ChatFormatting.GREEN + ": " +
                        tags.getDouble("x") + " / " +
                        tags.getDouble("y") + " / " +
                        tags.getDouble("z")))
                , true);
    }

    public void doTeleportation(Level worldIn, Player playerIn, ItemStack stack) {

        CompoundTag tags = stack.getTag();
        if (!worldIn.isClientSide) {

            playerIn.getCooldowns().addCooldown(this, 500);

            float pYaw = playerIn.rotA;
            assert tags != null;
            if (tags.contains("direction")) {
                pYaw = tags.getFloat("direction");
            }

            double OffsetY = (checkTeleportationPlatformBlocked(worldIn, new BlockPos(tags.getDouble("x"), tags.getDouble("y") + 1, tags.getDouble("z")))) ? 1.0d : 0.0d;

            if (playerIn instanceof ServerPlayer) {
                if (playerIn.isPassenger()) {
                    Entity mount = playerIn.getVehicle();
                    assert mount != null;
                    mount.ejectPassengers();
                    if (mount instanceof LivingEntity) {
                        ((LivingEntity) mount).randomTeleport(tags.getDouble("x") + 0.5d, tags.getDouble("y") + 1.0d + OffsetY, tags.getDouble("z") + 0.5d, false);
                    }
                }
                ((ServerPlayer) playerIn).teleportTo((ServerLevel) worldIn, tags.getDouble("x") + 0.5d, tags.getDouble("y") + 1.0d + OffsetY, tags.getDouble("z") + 0.5d, pYaw, playerIn.xRotO);
            }
            playerIn.setDeltaMovement(playerIn.getDeltaMovement().scale(0.5));
            playerIn.giveExperiencePoints(-EXP_COST);

            playerIn.awardStat(Stats.ITEM_USED.get(this));
            if (isScroll && !playerIn.isCreative()) {
                stack.shrink(1);
            }
        } else {
            assert tags != null;
            worldIn.playSound(playerIn,tags.getDouble("x"), tags.getDouble("y"), tags.getDouble("z"), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1, 1);
        }
    }

    private boolean checkTeleportationPlatform(Level worldIn, BlockPos pos) {
        if (worldIn.isAreaLoaded(pos, 1)) {
            return worldIn.getBlockState(pos).is(BlockRegistry.TELEPORTATION_PLATFORM_BLOCK.get());
        } else {
            return ((ServerLevel) worldIn).getBlockState(pos).is(BlockRegistry.TELEPORTATION_PLATFORM_BLOCK.get());
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, @NotNull List<Component> tooltip, @NotNull TooltipFlag flagIn) {
        CompoundTag tags = stack.getTag();

        if (tags == null || tags.isEmpty()) {
            tooltip.add(new TranslatableComponent("tooltip." + MiltenMagic.MOD_ID + ".teleportation_spell.unlinked"));
            tooltip.add(new TranslatableComponent("tooltip." + MiltenMagic.MOD_ID + ".teleportation_spell.right_click"));
            if (Screen.hasShiftDown()){
                tooltip.add(new TextComponent(""));
                tooltip.add(new TranslatableComponent("tooltip." + MiltenMagic.MOD_ID + ".teleportation_spell.lvl_required")
                        .append(new TextComponent(
                        ChatFormatting.DARK_GREEN + (": " + REQUIRED_EXP_LEVEL))));
                tooltip.add(new TranslatableComponent("tooltip." + MiltenMagic.MOD_ID + ".teleportation_spell.exp_cost")
                        .append(new TextComponent(
                                ChatFormatting.GREEN + (": " + EXP_COST))));
            } else {
                tooltip.add(new TranslatableComponent("tooltip." + MiltenMagic.MOD_ID + ".teleportation_spell.shift"));
            }
        } else {
            if (Screen.hasShiftDown()){
                tooltip.add(new TranslatableComponent("tooltip." + MiltenMagic.MOD_ID + ".teleportation_spell.linked")
                        .append(new TextComponent(ChatFormatting.BLUE + ": " + tags.getDouble("x") + " / " +
                                tags.getDouble("y") + " / " + tags.getDouble("z"))));
                tooltip.add(new TextComponent(""));
                tooltip.add(new TranslatableComponent("tooltip." + MiltenMagic.MOD_ID + ".teleportation_spell.lvl_required")
                        .append(new TextComponent(
                                ChatFormatting.DARK_GREEN + (": " + REQUIRED_EXP_LEVEL))));
                tooltip.add(new TranslatableComponent("tooltip." + MiltenMagic.MOD_ID + ".teleportation_spell.exp_cost")
                        .append(new TextComponent(
                                ChatFormatting.GREEN + (": " + EXP_COST))));
            } else {
                tooltip.add(new TranslatableComponent("tooltip." + MiltenMagic.MOD_ID + ".teleportation_spell.linked"));
                tooltip.add(new TranslatableComponent("tooltip." + MiltenMagic.MOD_ID + ".teleportation_spell.shift"));
            }

        }
    }



    private boolean isLapisBlock(BlockPos pos, Level worldIn) {
        return (worldIn.getBlockState(pos).getBlock().equals(Blocks.LAPIS_BLOCK));
    }

    private boolean isChiseldStoneBlock(BlockPos pos, Level worldIn) {
        return (worldIn.getBlockState(pos).getBlock().equals(Blocks.CHISELED_STONE_BRICKS));
    }

    private boolean isTeleportationPlatformBlock(BlockPos pos, Level worldIn) {
        return (worldIn.getBlockState(pos).getBlock().equals(BlockRegistry.TELEPORTATION_PLATFORM_BLOCK.get()));
    }

}
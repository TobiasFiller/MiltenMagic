package net.tobiasfiller.miltenmagic.common.item.spellItems;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.tobiasfiller.miltenmagic.MiltenMagic;
import net.tobiasfiller.miltenmagic.common.block.TeleportationPlatformBlock;
import net.tobiasfiller.miltenmagic.common.item.helperClasses.SpellItem;
import net.tobiasfiller.miltenmagic.core.registry.BlockRegistry;
import net.tobiasfiller.miltenmagic.core.registry.ParticleRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TeleportationSpellItem extends SpellItem {

    private final int TELEPORT_DELAY = 200;

    /**
     * The pending-teleport state (armed flag + countdown) lives in the stack's CUSTOM_DATA
     * instead of on the item instance.
     *
     * <p>Items are singletons shared by every stack, and {@link Item#inventoryTick} is called
     * once per non-empty stack in the player's inventory. Keeping the counter on the item
     * therefore advanced it N times per tick for N carried scrolls, so the delay silently
     * shrank (200 ticks for one stack, 40 for five).
     */
    private static final String TAG_PENDING = "tpPending";
    private static final String TAG_DELTA = "tpDelta";

    public TeleportationSpellItem(boolean isScroll) {
        super(5,30,isScroll);
    }

    // ---------------------------------------------------------------------
    // Item data storage.
    // In 1.21.1 items no longer store raw NBT; arbitrary tags live in the
    // DataComponents.CUSTOM_DATA component instead of ItemStack#getOrCreateTag().
    // ---------------------------------------------------------------------

    private static CompoundTag readTags(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? new CompoundTag() : data.copyTag();
    }

    /**
     * Writes the link data onto the stack.
     * <p>
     * IMPORTANT: readTags() hands out a detached copy (ItemStack#get returns a component
     * value, not a reference), so callers must call this with the copy they modified.
     * Never mutate a CompoundTag obtained from readTags() and expect the stack to change.
     */
private static void writeTags(ItemStack stack, CompoundTag tags) {
        if (tags.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tags));
        }
    }

    private static void clearTags(ItemStack stack) {
        stack.remove(DataComponents.CUSTOM_DATA);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();

        Player playerIn = context.getPlayer();

        assert playerIn != null;
        ItemStack stack = playerIn.getItemInHand(context.getHand());
        CompoundTag tags = readTags(stack);


        if (tags.isEmpty()) {
            BlockPos offPos = context.getClickedPos();
            if (!world.isClientSide) {
                Vec3 center = findAndCreatePlatform(offPos, world);
                if (center != null) {
                    setNBTTags(stack, center, playerIn, world.dimension().location().toString());
                } else {
                    inValidPlatform(playerIn);
                }
            }
        } else if (!world.isClientSide && !isTeleporting(stack)) {
            if (world.dimension().location().toString().equals(tags.getString("dim"))) {
                if (checkTeleportationPlatform(world, new BlockPos(tags.getInt("x"), tags.getInt("y"), tags.getInt("z")))) {
                    if (!checkTeleportationPlatformBlocked(world, new BlockPos(tags.getInt("x"), tags.getInt("y") + 2, tags.getInt("z")))) {
                        if (playerIn.isCreative() || playerIn.experienceLevel >= REQUIRED_EXP_LEVEL) {
                            armTeleportation(stack);
                            return InteractionResult.CONSUME;
                        } else {
                            playerIn.displayClientMessage(Component.translatable("message." + MiltenMagic.MOD_ID + ".spell.not_enough_exp"), true);
                        }
                    } else {
                        playerIn.displayClientMessage(Component.translatable("message." + MiltenMagic.MOD_ID + ".teleportation_spell.platform_blocked"), true);
                    }
                } else {
                    clearTags(stack);
                    playerIn.displayClientMessage(Component.translatable("message." + MiltenMagic.MOD_ID + ".teleportation_spell.platform_destroyed"), true);
                    return InteractionResult.FAIL;
                }
            } else {
                playerIn.displayClientMessage(Component.translatable("message." + MiltenMagic.MOD_ID + ".teleportation_spell.wrong_dimension"), true);
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
        CompoundTag tags = readTags(stack);

        if (!tags.isEmpty() && !worldIn.isClientSide && !isTeleporting(stack)) {
            if (worldIn.dimension().location().toString().equals(tags.getString("dim"))) {
                if (checkTeleportationPlatform(worldIn, new BlockPos(tags.getInt("x"), tags.getInt("y"), tags.getInt("z")))) {
                    if (!checkTeleportationPlatformBlocked(worldIn, new BlockPos(tags.getInt("x"), tags.getInt("y") + 2, tags.getInt("z")))) {
                        if (playerIn.isCreative() || playerIn.experienceLevel >= REQUIRED_EXP_LEVEL) {
                            armTeleportation(stack);
                            return InteractionResultHolder.consume(stack);
                        } else {
                            playerIn.displayClientMessage(Component.translatable("message." + MiltenMagic.MOD_ID + ".spell.not_enough_exp"), true);
                        }
                    } else {
                        playerIn.displayClientMessage(Component.translatable("message." + MiltenMagic.MOD_ID + ".teleportation_spell.platform_blocked"), true);
                    }

                } else {
                    clearTags(stack);
                    playerIn.displayClientMessage(Component.translatable("message." + MiltenMagic.MOD_ID + ".teleportation_spell.platform_destroyed"), true);
                    return InteractionResultHolder.fail(stack);
                }
            } else {
                playerIn.displayClientMessage(Component.translatable("message." + MiltenMagic.MOD_ID + ".teleportation_spell.wrong_dimension"), true);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, worldIn.isClientSide);
    }


    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level worldIn, @NotNull Entity entityIn, int itemSlot, boolean isSelected) {
        // inventoryTick runs once per stack in the inventory. Only the stack actually held in
        // the main hand may advance the countdown, otherwise every extra scroll carried would
        // tick the countdown down faster and shorten the delay.
        if (entityIn instanceof Player playerIn
                && playerIn.getMainHandItem() == stack
                && isTeleporting(stack)) {

            CompoundTag tags = readTags(stack);
            int delta = tags.getInt(TAG_DELTA);
            if (worldIn.isClientSide) {

                particleGenerator(worldIn, playerIn, delta);
                int teleportSoundPre = 10;
                if (delta == (TELEPORT_DELAY - teleportSoundPre)) {
                    playerIn.playSound(SoundEvents.ENDERMAN_TELEPORT, 1, 1);
                }

            } else {

                delta++;
                tags.putInt(TAG_DELTA, delta);
                writeTags(stack, tags);
                playerIn.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 1, 9));

                if (delta >= TELEPORT_DELAY) {
                    disarmTeleportation(stack);
                    doTeleportation(worldIn, playerIn, stack);
                }
            }
        }
        super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
    }

    private boolean isTeleporting(ItemStack stack) {
        return readTags(stack).getBoolean(TAG_PENDING);
    }

    private void armTeleportation(ItemStack stack) {
        CompoundTag tags = readTags(stack);
        tags.putBoolean(TAG_PENDING, true);
        tags.putInt(TAG_DELTA, 0);
        writeTags(stack, tags);
    }

    private void disarmTeleportation(ItemStack stack) {
        CompoundTag tags = readTags(stack);
        tags.remove(TAG_PENDING);
        tags.remove(TAG_DELTA);
        writeTags(stack, tags);
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

        if (delta < TELEPORT_DELAY / 5 * 3) {
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
    public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity entity) {
        return 40;
    }

    private Vec3 blockPosToVector3d(BlockPos p) {
        return blockPosToVector3d(p, 0.0d);
    }

    private Vec3 blockPosToVector3d(BlockPos p, Double offset) {
        return new Vec3(p.getX() + offset, p.getY(), p.getZ() + offset);
    }

    private Vec3 findAndCreatePlatform(BlockPos pos, Level world) {
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

    private Vec3 getCenterFromExistingPlatform(BlockPos pos, Level world, Integer recursionLevel) {
        if (recursionLevel >= 5) return null;

        BlockState current = world.getBlockState(pos);
        if (current.getValue(TeleportationPlatformBlock.TILENUMBER) == 5) {
            return blockPosToVector3d(pos);
        } else if (current.getValue(TeleportationPlatformBlock.TILENUMBER) == 16) {
            return blockPosToVector3d(pos, 0.5d);
        } else {
            BlockPos newPos = new BlockPos(pos.getX() - 1, pos.getY(), pos.getZ());
            Vec3 tmp = null;
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
        playerIn.displayClientMessage(Component.translatable("message." + MiltenMagic.MOD_ID + ".teleportation_spell.invalid_platform"), true);
    }

    private void setNBTTags(ItemStack stack, Vec3 offPos, Player playerIn, String dim) {
        CompoundTag tags = readTags(stack);
        tags.putDouble("x", offPos.x);
        tags.putDouble("y", offPos.y);
        tags.putDouble("z", offPos.z);
        tags.putString("dim", dim);
        tags.putFloat("direction", playerIn.rotA);
        writeTags(stack, tags);

        playerIn.displayClientMessage(Component.translatable("message." + MiltenMagic.MOD_ID + ".teleportation_spell.linked_at")
                        .append(Component.literal(ChatFormatting.GREEN + ": " +
                        tags.getDouble("x") + " / " +
                        tags.getDouble("y") + " / " +
                        tags.getDouble("z")))
                , true);
    }

    public void doTeleportation(Level worldIn, Player playerIn, ItemStack stack) {

        CompoundTag tags = readTags(stack);

        double destX = tags.getDouble("x");
        double destY = tags.getDouble("y");
        double destZ = tags.getDouble("z");

        if (!worldIn.isClientSide) {

            playerIn.getCooldowns().addCooldown(this, 500);

            float pYaw = playerIn.rotA;
            if (tags.contains("direction")) {
                pYaw = tags.getFloat("direction");
            }

            double OffsetY = checkTeleportationPlatformBlocked(worldIn, BlockPos.containing(destX, destY + 1, destZ)) ? 1.0d : 0.0d;

            if (playerIn instanceof ServerPlayer) {
                if (playerIn.isPassenger()) {
                    Entity mount = playerIn.getVehicle();
                    assert mount != null;
                    mount.ejectPassengers();
                    if (mount instanceof LivingEntity) {
                        ((LivingEntity) mount).randomTeleport(destX + 0.5d, destY + 1.0d + OffsetY, destZ + 0.5d, false);
                    }
                }
                ((ServerPlayer) playerIn).teleportTo((ServerLevel) worldIn, destX + 0.5d, destY + 1.0d + OffsetY, destZ + 0.5d, pYaw, playerIn.xRotO);
            }
            playerIn.setDeltaMovement(playerIn.getDeltaMovement().scale(0.5));

            playerIn.awardStat(Stats.ITEM_USED.get(this));
            if (isScroll && !playerIn.isCreative()) {
                playerIn.giveExperiencePoints(-EXP_COST);
                stack.shrink(1);
            }
        } else {
            worldIn.playSound(playerIn,destX, destY, destZ, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1, 1);
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
    public void appendHoverText(ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flagIn) {
        CompoundTag tags = readTags(stack);

        if (tags.isEmpty()) {
            tooltip.add(Component.translatable("tooltip." + MiltenMagic.MOD_ID + ".teleportation_spell.unlinked"));
            tooltip.add(Component.translatable("tooltip." + MiltenMagic.MOD_ID + ".teleportation_spell.right_click"));
            if (Screen.hasShiftDown()){
                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("tooltip." + MiltenMagic.MOD_ID + ".spell.lvl_required")
                        .append(Component.literal(
                        ChatFormatting.DARK_GREEN + (": " + REQUIRED_EXP_LEVEL))));
                tooltip.add(Component.translatable("tooltip." + MiltenMagic.MOD_ID + ".spell.exp_cost")
                        .append(Component.literal(
                                ChatFormatting.GREEN + (": " + EXP_COST))));
            } else {
                tooltip.add(Component.translatable("tooltip." + MiltenMagic.MOD_ID + ".spell.shift"));
            }
        } else {
            if (Screen.hasShiftDown()){
                tooltip.add(Component.translatable("tooltip." + MiltenMagic.MOD_ID + ".teleportation_spell.linked")
                        .append(Component.literal(ChatFormatting.BLUE + ": " + tags.getDouble("x") + " / " +
                                tags.getDouble("y") + " / " + tags.getDouble("z"))));
                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("tooltip." + MiltenMagic.MOD_ID + ".spell.lvl_required")
                        .append(Component.literal(
                                ChatFormatting.DARK_GREEN + (": " + REQUIRED_EXP_LEVEL))));
                tooltip.add(Component.translatable("tooltip." + MiltenMagic.MOD_ID + ".spell.exp_cost")
                        .append(Component.literal(
                                ChatFormatting.GREEN + (": " + EXP_COST))));
            } else {
                tooltip.add(Component.translatable("tooltip." + MiltenMagic.MOD_ID + ".teleportation_spell.linked"));
                tooltip.add(Component.translatable("tooltip." + MiltenMagic.MOD_ID + ".spell.shift"));
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

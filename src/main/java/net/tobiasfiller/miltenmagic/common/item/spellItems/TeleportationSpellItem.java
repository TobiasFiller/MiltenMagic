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
import net.neoforged.neoforge.common.NeoForge;
import net.tobiasfiller.miltenmagic.core.event.TeleportationEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TeleportationSpellItem extends SpellItem {

    /** 6 seconds of channeling before the teleport fires. */
    private static final int TELEPORT_DELAY = 120;

    /** Ticks before the teleport fires at which the "about to go" sound plays. */
    private static final int TELEPORT_SOUND_LEAD = 10;

    /**
     * Slowness amplifier used to hold the player in place while channeling.
     * <p>
     * This is the vanilla movement-speed modifier, so it really does stop movement. The catch: the
     * vanilla FOV modifier is computed from that same attribute ({@code Player#getFovModifier}),
     * which is why the camera zoomed in while channeling. The client therefore pins the FOV
     * modifier back to 1.0 for channeling players (see {@code ClientEvents#onComputeFovModifier}).
     */
    private static final int CHANNEL_SLOWNESS_AMPLIFIER = 9;

    /** How often (in ticks) the short-lived slowness is refreshed while channeling. */
    private static final int CHANNEL_SLOWNESS_REFRESH = 4;

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

    /**
     * UUID of the player that started the channel. If the armed stack ends up in somebody else's
     * inventory (dropped on death, moved through a chest, ...) it must not continue ticking there.
     */
    private static final String TAG_OWNER = "tpOwner";

    public TeleportationSpellItem(boolean isScroll) {
        super(5, 30, 500, isScroll);
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
        // Vanilla also calls the offhand item when the main hand does not consume the click.
        // Without this guard a linked scroll sitting in the offhand armed itself on every right
        // click (placing a block, opening a door, ...) even though the player never used it.
        if (context.getHand() != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        Level world = context.getLevel();

        Player playerIn = context.getPlayer();
        if (playerIn == null) {
            return InteractionResult.PASS;
        }

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
                            armTeleportation(stack, playerIn);
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

        // see useOn(): the countdown only ever runs in the main hand, so the offhand must not
        // be able to arm the spell in the first place
        if (handIn != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(stack);
        }

        CompoundTag tags = readTags(stack);

        if (!tags.isEmpty() && !worldIn.isClientSide && !isTeleporting(stack)) {
            if (worldIn.dimension().location().toString().equals(tags.getString("dim"))) {
                if (checkTeleportationPlatform(worldIn, new BlockPos(tags.getInt("x"), tags.getInt("y"), tags.getInt("z")))) {
                    if (!checkTeleportationPlatformBlocked(worldIn, new BlockPos(tags.getInt("x"), tags.getInt("y") + 2, tags.getInt("z")))) {
                        if (playerIn.isCreative() || playerIn.experienceLevel >= REQUIRED_EXP_LEVEL) {
                            armTeleportation(stack, playerIn);
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
        // inventoryTick runs once per stack in the inventory.
        if (entityIn instanceof Player playerIn && isTeleporting(stack)) {
            if (worldIn instanceof ServerLevel serverLevel) {
                if (!isChannelOwner(stack, playerIn)) {
                    // the stack was handed to another player (death drop, chest, ...); never
                    // continue somebody else's channel
                    disarmTeleportation(stack);
                } else if (playerIn.getMainHandItem() != stack) {
                    // The channel only advances in the main hand. Cancel it instead of silently
                    // pausing: pausing meant that switching away and later selecting the scroll
                    // again resumed the old countdown, so the teleport fired without a fresh click.
                    disarmTeleportation(stack);
                    playerIn.displayClientMessage(Component.translatable("message." + MiltenMagic.MOD_ID + ".teleportation_spell.interrupted"), true);
                } else {
                    CompoundTag tags = readTags(stack);
                    int delta = tags.getInt(TAG_DELTA) + 1;
                    tags.putInt(TAG_DELTA, delta);
                    writeTags(stack, tags);

                    // Hold the player in place. Refreshed instead of applied every tick so the
                    // server does not send an effect update packet 20 times per second.
                    if (delta % CHANNEL_SLOWNESS_REFRESH == 1) {
                        playerIn.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                                CHANNEL_SLOWNESS_REFRESH + 2, CHANNEL_SLOWNESS_AMPLIFIER, false, false, false));
                    }

                    spawnChannelParticles(serverLevel, playerIn, delta);

                    if (delta == TELEPORT_DELAY - TELEPORT_SOUND_LEAD) {
                        serverLevel.playSound(null, playerIn.getX(), playerIn.getY(), playerIn.getZ(),
                                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
                    }

                    if (delta >= TELEPORT_DELAY) {
                        disarmTeleportation(stack);
                        doTeleportation(serverLevel, playerIn, stack);
                    }
                }
            }
        }
        super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
    }

    private static boolean isChannelOwner(ItemStack stack, Player player) {
        String owner = readTags(stack).getString(TAG_OWNER);
        // scrolls armed before the owner tag existed keep working for their holder
        return owner.isEmpty() || owner.equals(player.getStringUUID());
    }

    /**
     * Cancels any channel the player currently has armed. Called when the player dies or logs out
     * so the pending teleport cannot resume on respawn / next login without a fresh click.
     */
    public static void cancelChannel(Player player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (isArmed(stack)) {
                disarmTeleportation(stack);
            }
        }
    }

    /**
     * A dropped scroll must not keep a half-finished channel: picking it up again later would
     * resume the countdown and teleport the new holder without a click.
     */
    @Override
    public boolean onDroppedByPlayer(ItemStack item, Player player) {
        if (isTeleporting(item)) {
            disarmTeleportation(item);
        }
        return true;
    }

    private static boolean isTeleporting(ItemStack stack) {
        return readTags(stack).getBoolean(TAG_PENDING);
    }

    /**
     * True while the player is channeling a teleport (either hand). Used by the client to cancel
     * the FOV change caused by the channeling slowness.
     */
    public static boolean isChanneling(Player player) {
        return isArmed(player.getMainHandItem()) || isArmed(player.getOffhandItem());
    }

    /**
     * True while this stack holds a running teleport channel.
     * <p>
     * The flag lives in the stack's {@code CUSTOM_DATA}, which vanilla syncs to the client
     * (inventory slot for the caster, equipment packets for everyone else), so the cast
     * animation can be driven from it without custom networking.
     */
    public static boolean isArmed(ItemStack stack) {
        if (!(stack.getItem() instanceof TeleportationSpellItem)) {
            return false;
        }
        // no tag copy here: this is polled every client frame by the animation controller
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && data.contains(TAG_PENDING);
    }

    private static void armTeleportation(ItemStack stack, Player player) {
        CompoundTag tags = readTags(stack);
        tags.putBoolean(TAG_PENDING, true);
        tags.putInt(TAG_DELTA, 0);
        tags.putString(TAG_OWNER, player.getStringUUID());
        writeTags(stack, tags);
    }

    private static void disarmTeleportation(ItemStack stack) {
        CompoundTag tags = readTags(stack);
        tags.remove(TAG_PENDING);
        tags.remove(TAG_DELTA);
        tags.remove(TAG_OWNER);
        writeTags(stack, tags);
    }

    private Vec3 getVector3dfromTags(CompoundTag tags) {
        return new Vec3(tags.getDouble("x"), tags.getDouble("y"), tags.getDouble("z"));
    }

    /**
     * Channel particles are emitted server-side so every nearby player sees them, not just the
     * caster. One {@code sendParticles} call per shape keeps the network cost low.
     */
    private void spawnChannelParticles(ServerLevel level, Player player, int delta) {
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();

        level.sendParticles(ParticleTypes.ENCHANT, x, y + 1.0D, z, 2, 0.6D, 1.0D, 0.6D, 0.0D);
        level.sendParticles(ParticleRegistry.TELEPORTATION_PARTICLE.get(), x, y + 1.0D, z, 8, 0.3D, 1.0D, 0.3D, 0.0D);

        // blue ring that spirals upwards for the whole channel, climbing two and a half blocks:
        // faster than the caster rises and clearly above his head when the burst takes over
        double radius = 1.2D;
        double rise = ((double) delta / (double) TELEPORT_DELAY) * 2.5D;
        for (int i = 0; i < 16; i++) {
            double angle = (Math.PI * 2.0D) * ((double) i / 16.0D);
            level.sendParticles(ParticleRegistry.TELEPORTATION_PARTICLE.get(),
                    x + Math.cos(angle) * radius,
                    y + rise + 0.3D,
                    z + Math.sin(angle) * radius,
                    2, 0.15D, 0.05D, 0.15D, 0.0D);
        }
    }

    /**
     * Spawns the departure burst at the origin, where the caster's body dissolves.
     * <p>
     * Without a viewer the burst is broadcast the vanilla way, which only reaches players within
     * 32 blocks. The client additionally drops particles farther than 32 blocks from the camera
     * ({@code LevelRenderer}) unless the packet is flagged {@code longDistance}. Both teleport
     * packets arrive in the same client tick while the camera still sits at the origin, so for a
     * long jump the caster's own arrival burst was created and immediately discarded. Passing a
     * viewer sends the burst to exactly that player with {@code longDistance = true}, which skips
     * both limits.
     */
    private void spawnTeleportBurst(ServerLevel level, double x, double y, double z, ServerPlayer onlyViewer) {
        if (onlyViewer != null) {
            level.sendParticles(onlyViewer, ParticleRegistry.TELEPORTATION_PARTICLE.get(), true, x, y + 1.0D, z, 50, 0.5D, 1.0D, 0.5D, 0.1D);
            return;
        }

        level.sendParticles(ParticleRegistry.TELEPORTATION_PARTICLE.get(), x, y + 1.0D, z, 50, 0.5D, 1.0D, 0.5D, 0.1D);
    }

    /**
     * Spawns the arrival effect at the destination: instead of a burst around the caster the
     * teleport particles fill his body, so the jump reads as the particles having carried him
     * there.
     * <p>
     * {@code sendParticles} offsets each particle by {@code spread * random.nextGaussian()}, so
     * the spread values are standard deviations, not radii. 0.12 keeps the cloud inside the
     * 0.6 wide hitbox (3 sigma), 0.30 spans the 1.8 high body around {@code y + 0.9}. The low
     * speed makes the particles stay in place instead of flying apart.
     */
    private void spawnArrivalParticles(ServerLevel level, double x, double y, double z, ServerPlayer onlyViewer) {
        if (onlyViewer != null) {
            level.sendParticles(onlyViewer, ParticleRegistry.TELEPORTATION_PARTICLE.get(), true,
                    x, y + 0.9D, z, 60, 0.12D, 0.30D, 0.12D, 0.02D);
            return;
        }

        level.sendParticles(ParticleRegistry.TELEPORTATION_PARTICLE.get(),
                x, y + 0.9D, z, 60, 0.12D, 0.30D, 0.12D, 0.02D);
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

    public void doTeleportation(ServerLevel level, Player playerIn, ItemStack stack) {

        CompoundTag tags = readTags(stack);

        double destX = tags.getDouble("x");
        double destY = tags.getDouble("y");
        double destZ = tags.getDouble("z");

        // the platform may have been broken during the 6 second channel
        if (!checkTeleportationPlatform(level, BlockPos.containing(destX, destY, destZ))) {
            playerIn.displayClientMessage(Component.translatable("message." + MiltenMagic.MOD_ID + ".teleportation_spell.platform_destroyed"), true);
            return;
        }

        float pYaw = playerIn.rotA;
        if (tags.contains("direction")) {
            pYaw = tags.getFloat("direction");
        }

        double offsetY = checkTeleportationPlatformBlocked(level, BlockPos.containing(destX, destY + 1.0D, destZ)) ? 1.0D : 0.0D;

        Vec3 origin = playerIn.position();
        Vec3 destination = new Vec3(destX + 0.5D, destY + 1.0D + offsetY, destZ + 0.5D);

        // other features may veto the teleport; if they do, the scroll and the experience stay
        TeleportationEvent.Pre preEvent = new TeleportationEvent.Pre(playerIn, stack, origin, destination, level.dimension());
        if (NeoForge.EVENT_BUS.post(preEvent).isCanceled()) {
            return;
        }

        playerIn.getCooldowns().addCooldown(this, COOL_DOWN);

        if (playerIn.isPassenger()) {
            Entity mount = playerIn.getVehicle();
            assert mount != null;
            mount.ejectPassengers();
            if (mount instanceof LivingEntity livingMount) {
                livingMount.randomTeleport(destination.x, destination.y, destination.z, false);
            }
        }

        ServerPlayer caster = playerIn instanceof ServerPlayer serverPlayer ? serverPlayer : null;
        // Mirrors the range checks used by ServerLevel for the particle broadcast (32 blocks) and
        // the sound broadcast (16 blocks); both are evaluated from the pre-teleport position.
        boolean casterInBurstRange = caster != null
                && caster.blockPosition().closerToCenterThan(destination, 32.0D);
        boolean casterInSoundRange = caster != null
                && origin.distanceToSqr(destination) < 16.0D * 16.0D;

        // The arrival particles are broadcast while the caster still stands at the origin, so his
        // old position decides whether the normal broadcast includes him.
        spawnArrivalParticles(level, destination.x, destination.y, destination.z, null);

        if (caster != null) {
            caster.teleportTo(level, destination.x, destination.y, destination.z, pYaw, playerIn.xRotO);
        }
        playerIn.setDeltaMovement(playerIn.getDeltaMovement().scale(0.5));

        spawnTeleportBurst(level, origin.x, origin.y, origin.z, null);
        if (caster != null && !casterInBurstRange) {
            // A jump farther than the 32 block particle range: the client would discard the
            // broadcast because its camera is still at the origin, so address the caster directly.
            spawnArrivalParticles(level, destination.x, destination.y, destination.z, caster);
        }
        level.playSound(null, origin.x, origin.y, origin.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        level.playSound(null, destination.x, destination.y, destination.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        if (caster != null && !casterInSoundRange) {
            // playSound only reaches players within 16 blocks of the destination; a long jump
            // needs the arrival sound sent straight to the caster as well.
            caster.playNotifySound(SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        playerIn.awardStat(Stats.ITEM_USED.get(this));
        if (isScroll && !playerIn.isCreative()) {
            playerIn.giveExperiencePoints(-EXP_COST);
            stack.shrink(1);
        }

        NeoForge.EVENT_BUS.post(new TeleportationEvent.Post(playerIn, stack, origin, destination, level.dimension()));
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

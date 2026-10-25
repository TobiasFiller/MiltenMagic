package net.tobiasfiller.miltenmagic.core.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.tobiasfiller.miltenmagic.MiltenMagic;
import net.tobiasfiller.miltenmagic.common.block.TableBlock;
import net.tobiasfiller.miltenmagic.common.blockentity.StonePedestalBlockEntity;
import net.tobiasfiller.miltenmagic.common.item.spellItems.FireArrowSpell;
import net.tobiasfiller.miltenmagic.common.item.spellItems.TeleportationSpellItem;
import net.tobiasfiller.miltenmagic.core.registry.BlockRegistry;
import net.tobiasfiller.miltenmagic.core.registry.ItemRegistry;
import net.tobiasfiller.miltenmagic.core.registry.VillagerRegistry;

/**
 * Headless regression tests for the reported gameplay bugs.
 * Run with: gradlew runGameTestServer
 */
@PrefixGameTestTemplate(false)
public class MiltenMagicGameTests {

    // ---- Bug 3: tables must connect to each other ---------------------------------

    @GameTest(templateNamespace = MiltenMagic.MOD_ID, template = "empty")
    public static void tablesConnect(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.CREATIVE);
        BlockPos first = new BlockPos(1, 1, 1);
        BlockPos second = new BlockPos(1, 1, 2); // south of first

        placeTable(helper, player, first);
        placeTable(helper, player, second);

        int firstSide = helper.getBlockState(first).getValue(TableBlock.SIDE);
        int secondSide = helper.getBlockState(second).getValue(TableBlock.SIDE);

        helper.assertTrue(firstSide != 0,
                "first table should have been upgraded to a double table, SIDE=" + firstSide);
        helper.assertTrue(secondSide != 0,
                "second table should have been placed as a double table, SIDE=" + secondSide);
        helper.assertTrue(firstSide != secondSide,
                "the two table halves must use different SIDE values, got " + firstSide + " and " + secondSide);

        helper.succeed();
    }

    private static void placeTable(GameTestHelper helper, Player player, BlockPos relative) {
        // the player must click the TOP FACE of a solid block below, otherwise
        // BlockPlaceContext treats the (air) block it hit as "replace me"
        helper.setBlock(relative.below(), Blocks.STONE);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemRegistry.OAK_TABLE.get()));

        BlockPos abs = helper.absolutePos(relative);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(abs.below()), Direction.UP, abs.below(), false);
        helper.useBlock(relative, player, hit);
    }

    @GameTest(templateNamespace = MiltenMagic.MOD_ID, template = "empty")
    public static void tablesConnectWhilePlayerStandsThere(GameTestHelper helper) {
        // real players stand where they place; makeMockPlayer sits at (0,0,0) and
        // therefore never triggers the isUnobstructed() early-out in getStateForPlacement
        net.minecraft.server.level.ServerPlayer player = helper.makeMockServerPlayerInLevel();
        BlockPos first = new BlockPos(1, 1, 1);
        BlockPos second = new BlockPos(1, 1, 2);

        helper.setBlock(first.below(), Blocks.STONE);
        // stand next to the tables, not inside them (a real player never occupies the
        // target position; the block below is what gets clicked)
        player.setPos(helper.absolutePos(first).getX() - 1.5,
                helper.absolutePos(first).getY(),
                helper.absolutePos(first).getZ() + 0.5);

        placeTable(helper, player, first);
        placeTable(helper, player, second);

        int firstSide = helper.getBlockState(first).getValue(TableBlock.SIDE);
        int secondSide = helper.getBlockState(second).getValue(TableBlock.SIDE);

        helper.assertTrue(firstSide != 0 && secondSide != 0,
                "tables did not connect while the player occupied the spot (first SIDE=" + firstSide
                        + ", second SIDE=" + secondSide + ")");

        helper.succeed();
    }

    // ---- Bug 4: stone pedestal must persist the item it holds ---------------------

    @GameTest(templateNamespace = MiltenMagic.MOD_ID, template = "empty")
    public static void pedestalSavesItem(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, BlockRegistry.STONE_PEDESTAL.get());

        StonePedestalBlockEntity be = helper.getBlockEntity(pos);
        be.setItem(new ItemStack(Items.DIAMOND, 1));

        HolderLookup.Provider registries = helper.getLevel().registryAccess();
        CompoundTag saved = be.saveWithFullMetadata(registries);

        // round-trip into a fresh block entity, exactly like a chunk reload does
        helper.setBlock(pos, Blocks.AIR);
        helper.setBlock(pos, BlockRegistry.STONE_PEDESTAL.get());
        StonePedestalBlockEntity reloaded = helper.getBlockEntity(pos);
        reloaded.loadWithComponents(saved, registries);

        ItemStack stored = reloaded.getItemHandler().getStackInSlot(0);
        helper.assertTrue(stored.is(Items.DIAMOND),
                "pedestal lost its stored item across save/load, got: " + stored);

        helper.succeed();
    }

    // ---- Bug 4: what the CLIENT renderer sees after a world reload ----------------
    //
    // The server keeps the item (clicking gives it back), but the renderer draws nothing.
    // This reproduces the client side exactly: a freshly constructed block entity that
    // only ever receives the update tag, like LevelChunk.replaceWithPacketData() does.
    @GameTest(templateNamespace = MiltenMagic.MOD_ID, template = "empty")
    public static void pedestalUpdateTagCarriesItem(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, BlockRegistry.STONE_PEDESTAL.get());

        StonePedestalBlockEntity server = helper.getBlockEntity(pos);
        server.setItem(new ItemStack(Items.DIAMOND, 1));

        HolderLookup.Provider registries = helper.getLevel().registryAccess();

        // this is the tag the server sends to the client on chunk load / block update
        CompoundTag updateTag = server.getUpdateTag(registries);

        helper.assertTrue(updateTag.contains("inventory"),
                "getUpdateTag() does not carry the 'inventory' key the renderer needs");

        // fresh client-side block entity, loaded ONLY from the update tag
        StonePedestalBlockEntity clientSide =
                new StonePedestalBlockEntity(helper.absolutePos(pos), helper.getBlockState(pos));
        clientSide.loadWithComponents(updateTag, registries);

        helper.assertTrue(clientSide.getItemHandler().getStackInSlot(0).is(Items.DIAMOND),
                "a block entity loaded from getUpdateTag() has no item -> renderer draws nothing, "
                        + "but slot holds: " + clientSide.getItemHandler().getStackInSlot(0));

        helper.succeed();
    }

    // ---- pedestal must not hold an item when a block sits directly above it ------

    @GameTest(templateNamespace = MiltenMagic.MOD_ID, template = "empty")
    public static void pedestalRejectsItemWhenCovered(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, BlockRegistry.STONE_PEDESTAL.get());
        helper.setBlock(pos.above(), Blocks.STONE);

        Player player = helper.makeMockPlayer(GameType.CREATIVE);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.EMERALD, 3));

        BlockPos abs = helper.absolutePos(pos);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(abs), Direction.UP, abs, false);
        BlockState pedestal = helper.getLevel().getBlockState(abs);
        pedestal.useItemOn(new ItemStack(Items.EMERALD, 3), helper.getLevel(), player,
                InteractionHand.MAIN_HAND, hit);

        StonePedestalBlockEntity be = helper.getBlockEntity(pos);
        helper.assertTrue(be.getItemHandler().getStackInSlot(0).isEmpty(),
                "pedestal accepted an item although a block sits directly above it, got: "
                        + be.getItemHandler().getStackInSlot(0));

        helper.succeed();
    }

    @GameTest(templateNamespace = MiltenMagic.MOD_ID, template = "empty")
    public static void pedestalDropsItemWhenCovered(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, BlockRegistry.STONE_PEDESTAL.get());
        StonePedestalBlockEntity be = helper.getBlockEntity(pos);
        helper.assertTrue(be.setItem(new ItemStack(Items.DIAMOND, 1)),
                "pedestal refused the diamond before a block was placed above it");

        // placing a block above must push the stored item out onto the ground
        helper.setBlock(pos.above(), Blocks.STONE);

        helper.assertTrue(be.getItemHandler().getStackInSlot(0).isEmpty(),
                "pedestal kept the item although a block was placed directly above it");

        BlockPos abs = helper.absolutePos(pos);
        boolean foundDiamond = helper.getLevel()
                .getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                        net.minecraft.world.phys.AABB.ofSize(Vec3.atCenterOf(abs), 6.0d, 6.0d, 6.0d))
                .stream()
                .anyMatch(entity -> entity.getItem().is(Items.DIAMOND));
        helper.assertTrue(foundDiamond,
                "the item stored on the covered pedestal was not dropped into the world");

        helper.succeed();
    }

    // ---- fire mage villager: can an idle villager ever acquire the job? ----------
    //
    // A villager only takes a job if there is a POI whose profession is acquirable.
    // In 1.21.1 VillagerProfession.ALL_ACQUIRABLE_JOBS checks the data-driven
    // minecraft:acquirable_job_site POI tag, so our POI must be added to that tag.
    @GameTest(templateNamespace = MiltenMagic.MOD_ID, template = "empty")
    public static void fireMageIsAcquirable(GameTestHelper helper) {
        net.minecraft.world.entity.npc.VillagerProfession fireMage =
                VillagerRegistry.FIRE_MAGE.get();

        helper.assertTrue(
                net.minecraft.world.entity.npc.VillagerProfession.ALL_ACQUIRABLE_JOBS.test(
                        helper.getLevel().registryAccess()
                                .lookupOrThrow(net.minecraft.core.registries.Registries.POINT_OF_INTEREST_TYPE)
                                .getOrThrow(VillagerRegistry.FIRE_MAGE_POI_KEY)),
                "ALL_ACQUIRABLE_JOBS rejects the fire mage POI -> no villager will ever "
                        + "acquire the fire_mage profession");

        helper.succeed();
    }

    // ---- linked teleport scroll must survive a pedestal round-trip ---------------
    //
    // setItem() rebuilt the stack with `new ItemStack(stack.getItem(), 1)`, which keeps only
    // the item type. In 1.21.1 the teleport link lives in DataComponents.CUSTOM_DATA, so the
    // pedestal silently stripped it and the scroll came back unlinked.
    @GameTest(templateNamespace = MiltenMagic.MOD_ID, template = "empty")
    public static void pedestalPreservesLinkedScroll(GameTestHelper helper) {
        BlockPos pedestalPos = new BlockPos(3, 1, 3);
        helper.setBlock(pedestalPos, BlockRegistry.STONE_PEDESTAL.get());
        StonePedestalBlockEntity pedestal = helper.getBlockEntity(pedestalPos);

        ItemStack scroll = new ItemStack(ItemRegistry.TELEPORTATION_SCROLL.get());
        CompoundTag link = new CompoundTag();
        link.putDouble("x", 123.5d);
        link.putDouble("y", 64.0d);
        link.putDouble("z", -77.25d);
        link.putString("dim", "minecraft:overworld");
        scroll.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.of(link));

        helper.assertTrue(pedestal.setItem(scroll), "pedestal refused the scroll");

        ItemStack stored = pedestal.getItemHandler().getStackInSlot(0);
        CompoundTag storedLink = stored.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA) == null
                ? new CompoundTag()
                : stored.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA).copyTag();
        helper.assertTrue(storedLink.contains("dim"),
                "pedestal stripped the teleport link while storing (stored tag: " + storedLink + ")");

        // take it back out again - it must still be linked
        ItemStack returned = pedestal.getAndRemoveItem();
        CompoundTag returnedLink = returned.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA) == null
                ? new CompoundTag()
                : returned.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA).copyTag();

        helper.assertTrue(returnedLink.contains("dim") && returnedLink.getDouble("x") == 123.5d,
                "scroll came back unlinked from the pedestal (tag: " + returnedLink + ")");

        helper.succeed();
    }

    // ---- Bug 4b: placing an item onto the pedestal must store it ------------------

    @GameTest(templateNamespace = MiltenMagic.MOD_ID, template = "empty")
    public static void pedestalStoresPlacedItem(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, BlockRegistry.STONE_PEDESTAL.get());

        Player player = helper.makeMockPlayer(GameType.CREATIVE);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.EMERALD, 3));

        BlockPos abs = helper.absolutePos(pos);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(abs), Direction.UP, abs, false);
        BlockState pedestal = helper.getLevel().getBlockState(abs);
        pedestal.useItemOn(new ItemStack(Items.EMERALD, 3), helper.getLevel(), player,
                InteractionHand.MAIN_HAND, hit);

        StonePedestalBlockEntity be = helper.getBlockEntity(pos);
        helper.assertTrue(be.getItemHandler().getStackInSlot(0).is(Items.EMERALD),
                "pedestal did not store the item placed on it, got: "
                        + be.getItemHandler().getStackInSlot(0));

        helper.succeed();
    }

    // ---- Bug 2: fire arrow scroll must spawn a projectile -----------------------

    @GameTest(templateNamespace = MiltenMagic.MOD_ID, template = "empty")
    public static void fireArrowSpawns(GameTestHelper helper) {
        net.minecraft.server.level.ServerPlayer player = helper.makeMockServerPlayerInLevel();
        BlockPos abs = helper.absolutePos(new BlockPos(1, 1, 1));
        player.setPos(abs.getX() + 0.5, abs.getY(), abs.getZ() + 0.5);

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemRegistry.FIRE_ARROW_SCROLL.get()));
        ItemStack stack = player.getMainHandItem();

        int before = countFireArrows(helper);

        stack.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        if (!player.isUsingItem()) {
            helper.fail("fire arrow scroll never entered the 'using item' state");
            return;
        }
        int duration = stack.getUseDuration(player);

        // the player holds right-click for 20 ticks, then lets go. ServerPlayerGameMode does
        // releaseUsingItem() -> LivingEntity passes getUseItemRemainingTicks(), i.e. REMAINING.
        player.releaseUsingItem();

        // and now the same thing after 20 ticks of charge:
        stack.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        int afterInstant = countFireArrows(helper);
        player.releaseUsingItem();
        int afterInstantRelease = countFireArrows(helper);

        stack.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        stack.releaseUsing(helper.getLevel(), player, duration - 20);
        int after = countFireArrows(helper);

        helper.assertTrue(after > afterInstantRelease,
                "no projectile after a 20 tick charge (duration=" + duration
                        + ", before=" + before + ", after=" + after + ")");

        helper.succeed();
    }

    private static int countFireArrows(GameTestHelper helper) {
        net.minecraft.world.phys.AABB box = new net.minecraft.world.phys.AABB(
                Vec3.atCenterOf(helper.absolutePos(new BlockPos(0, 0, 0))),
                Vec3.atCenterOf(helper.absolutePos(new BlockPos(7, 7, 7))));
        return helper.getLevel().getEntities(
                net.minecraft.world.level.entity.EntityTypeTest.forClass(
                        net.tobiasfiller.miltenmagic.common.entity.FireArrowEntity.class),
                box, e -> true).size();
    }

    // ---- Teleport delay must not depend on how many scroll stacks you carry -----
    //
    // doTeleportation/teleportDelayDelta live on the Item INSTANCE, which is a singleton
    // shared by every stack. inventoryTick() runs once per stack in the inventory, so the
    // 120 tick countdown would tick N times per tick for N stacks -> wildly varying delay.
    @GameTest(templateNamespace = MiltenMagic.MOD_ID, template = "empty")
    public static void teleportDelayIndependentOfStackCount(GameTestHelper helper) {
        int single = measureTeleportTicks(helper, 1);
        int stacked = measureTeleportTicks(helper, 5);

        com.mojang.logging.LogUtils.getLogger().info(
                "[T3] ticks with 1 stack={} , ticks with 5 stacks={}", single, stacked);

        helper.assertTrue(single == stacked,
                "teleport delay depends on the number of scroll stacks in the inventory: "
                        + single + " ticks with 1 stack vs " + stacked + " ticks with 5");
        helper.assertValueEqual(120, stacked, "expected a 120 tick (6s) delay");
        helper.succeed();
    }

    /** Links a scroll to the platform and returns how many ticks the teleport needs. */
    private static int measureTeleportTicks(GameTestHelper helper, int stackCount) {
        net.minecraft.server.level.ServerPlayer player = helper.makeMockServerPlayerInLevel();
        BlockPos target = new BlockPos(3, 1, 3);

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                helper.setBlock(target.offset(x, 0, z),
                        x == 0 && z == 0 ? Blocks.LAPIS_BLOCK : Blocks.CHISELED_STONE_BRICKS);
            }
        }
        BlockPos abs = helper.absolutePos(target);

        ItemStack linked = new ItemStack(ItemRegistry.TELEPORTATION_SCROLL.get());
        player.setPos(abs.getX() + 12.5, abs.getY(), abs.getZ() + 0.5);
        player.setItemInHand(InteractionHand.MAIN_HAND, linked);
        helper.useBlock(target, player,
                new BlockHitResult(Vec3.atCenterOf(abs), Direction.UP, abs, false));

        // arm the teleport (right-click air)
        player.setPos(abs.getX() + 12.5, abs.getY(), abs.getZ() + 0.5);
        linked.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);

        // put the SAME linked stack plus (stackCount-1) extra copies into the inventory
        player.getInventory().setItem(0, linked);
        for (int i = 1; i < stackCount; i++) {
            player.getInventory().setItem(i, new ItemStack(ItemRegistry.TELEPORTATION_SCROLL.get()));
        }

        double startX = player.getX();
        double startZ = player.getZ();

        // simulate ticks; every tick the game calls inventoryTick once PER stack
        for (int tick = 1; tick <= 2000; tick++) {
            for (int slot = 0; slot < stackCount; slot++) {
                ItemStack inSlot = player.getInventory().getItem(slot);
                if (!inSlot.isEmpty()) {
                    inSlot.inventoryTick(helper.getLevel(), player, slot, slot == 0);
                }
            }
            if (Math.abs(player.getX() - startX) > 3 || Math.abs(player.getZ() - startZ) > 3) {
                return tick;
            }
        }
        return -1;
    }

    // ---- Teleport must never arm itself from the offhand or after being unequipped ----

    /**
     * Vanilla tries the offhand item when the main hand does not consume a right click. A linked
     * scroll in the offhand therefore used to arm the spell on every right click and later, once
     * it ended up in the main hand, teleport without the player using it.
     */
    @GameTest(templateNamespace = MiltenMagic.MOD_ID, template = "empty")
    public static void teleportIsNotArmedFromOffhand(GameTestHelper helper) {
        net.minecraft.server.level.ServerPlayer player = helper.makeMockServerPlayerInLevel();
        ItemStack linked = new ItemStack(ItemRegistry.TELEPORTATION_SCROLL.get());

        CompoundTag link = new CompoundTag();
        link.putDouble("x", 0.0d);
        link.putDouble("y", 64.0d);
        link.putDouble("z", 0.0d);
        link.putString("dim", helper.getLevel().dimension().location().toString());
        linked.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.of(link));

        player.setItemInHand(InteractionHand.OFF_HAND, linked);
        linked.use(helper.getLevel(), player, InteractionHand.OFF_HAND);

        helper.assertFalse(hasPendingTeleport(linked),
                "using the scroll from the offhand must not arm the teleport");
        helper.succeed();
    }

    /**
     * Switching away used to only pause the countdown; selecting the scroll again later resumed
     * it and the teleport fired without a new click.
     */
    @GameTest(templateNamespace = MiltenMagic.MOD_ID, template = "empty", timeoutTicks = 600)
    public static void teleportCancelsWhenScrollLeavesMainHand(GameTestHelper helper) {
        net.minecraft.server.level.ServerPlayer player = helper.makeMockServerPlayerInLevel();
        BlockPos target = new BlockPos(3, 1, 3);
        helper.setBlock(target, BlockRegistry.TELEPORTATION_PLATFORM_BLOCK.get());
        BlockPos abs = helper.absolutePos(target);

        ItemStack linked = new ItemStack(ItemRegistry.TELEPORTATION_SCROLL.get());
        CompoundTag link = new CompoundTag();
        link.putDouble("x", abs.getX());
        link.putDouble("y", abs.getY());
        link.putDouble("z", abs.getZ());
        link.putString("dim", helper.getLevel().dimension().location().toString());
        linked.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.of(link));

        player.setPos(abs.getX() + 10.5, abs.getY(), abs.getZ() + 0.5);
        player.setItemInHand(InteractionHand.MAIN_HAND, linked);
        linked.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(hasPendingTeleport(linked), "the scroll should be armed after use()");

        // Arming is fire-and-forget: the player right-clicks once and does not hold the button.
        // The animation is driven by the armed flag on the stack (replicated by vanilla).
        helper.assertFalse(player.isUsingItem(),
                "arming the scroll must not require holding the use button");
        helper.assertTrue(TeleportationSpellItem.isArmed(linked),
                "the armed flag must be readable from the stack so clients can animate the channel");

        double startX = player.getX();
        double startZ = player.getZ();

        // switch to another hotbar slot
        player.getInventory().setItem(0, ItemStack.EMPTY);
        player.getInventory().setItem(4, linked);
        linked.inventoryTick(helper.getLevel(), player, 4, false);

        helper.assertFalse(hasPendingTeleport(linked),
                "switching away from the scroll must cancel the channel, not pause it");
        helper.assertFalse(TeleportationSpellItem.isArmed(linked),
                "cancelling the channel must clear the flag the animation reads");

        // select it again much later: nothing may fire without a new click
        for (int t = 0; t < 250; t++) {
            linked.inventoryTick(helper.getLevel(), player, 4, false);
        }
        player.getInventory().setItem(0, linked);
        player.getInventory().setItem(4, ItemStack.EMPTY);
        for (int t = 0; t < 300; t++) {
            linked.inventoryTick(helper.getLevel(), player, 0, true);
        }

        helper.assertTrue(Math.abs(player.getX() - startX) < 1 && Math.abs(player.getZ() - startZ) < 1,
                "a canceled channel teleported the player later: now at "
                        + player.getX() + "/" + player.getZ());

        helper.succeed();
    }

    private static boolean hasPendingTeleport(ItemStack stack) {
        net.minecraft.world.item.component.CustomData data =
                stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        return data != null && data.copyTag().getBoolean("tpPending");
    }

    // ---- Teleport events: Pre is cancelable, Post fires on arrival ----------------

    @GameTest(templateNamespace = MiltenMagic.MOD_ID, template = "empty", timeoutTicks = 600)
    public static void teleportEventsFireAndPreCanCancel(GameTestHelper helper) {
        net.minecraft.server.level.ServerPlayer player = helper.makeMockServerPlayerInLevel();
        BlockPos target = new BlockPos(3, 1, 3);

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                helper.setBlock(target.offset(x, 0, z),
                        x == 0 && z == 0 ? Blocks.LAPIS_BLOCK : Blocks.CHISELED_STONE_BRICKS);
            }
        }
        BlockPos abs = helper.absolutePos(target);

        ItemStack linked = new ItemStack(ItemRegistry.TELEPORTATION_SCROLL.get());
        player.setPos(abs.getX() + 10.5, abs.getY(), abs.getZ() + 0.5);
        player.setItemInHand(InteractionHand.MAIN_HAND, linked);
        helper.useBlock(target, player,
                new BlockHitResult(Vec3.atCenterOf(abs), Direction.UP, abs, false));

        double startX = player.getX();
        double startZ = player.getZ();

        // phase 1: a listener vetoes the teleport
        java.util.concurrent.atomic.AtomicInteger preEvents = new java.util.concurrent.atomic.AtomicInteger();
        java.util.function.Consumer<net.tobiasfiller.miltenmagic.core.event.TeleportationEvent.Pre> veto =
                event -> {
                    preEvents.incrementAndGet();
                    event.setCanceled(true);
                };
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
                net.tobiasfiller.miltenmagic.core.event.TeleportationEvent.Pre.class, veto);
        try {
            linked.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
            for (int t = 0; t < 210; t++) {
                linked.inventoryTick(helper.getLevel(), player, 0, true);
            }

            helper.assertValueEqual(1, preEvents.get(), "Pre event must be posted exactly once");
            helper.assertTrue(Math.abs(player.getX() - startX) < 1 && Math.abs(player.getZ() - startZ) < 1,
                    "a canceled Pre event must abort the teleport, player is at "
                            + player.getX() + "/" + player.getZ());
            helper.assertTrue(linked.getCount() == 1,
                    "a canceled teleport must not consume the scroll, count=" + linked.getCount());
        } finally {
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(veto);
        }

        // phase 2: no veto -> teleport happens and Post fires
        java.util.concurrent.atomic.AtomicInteger postEvents = new java.util.concurrent.atomic.AtomicInteger();
        java.util.function.Consumer<net.tobiasfiller.miltenmagic.core.event.TeleportationEvent.Post> counter =
                event -> postEvents.incrementAndGet();
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
                net.tobiasfiller.miltenmagic.core.event.TeleportationEvent.Post.class, counter);
        try {
            linked.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
            for (int t = 0; t < 210; t++) {
                linked.inventoryTick(helper.getLevel(), player, 0, true);
            }

            helper.assertValueEqual(1, postEvents.get(), "Post event must be posted exactly once");
            helper.assertTrue(Math.abs(player.getX() - (abs.getX() + 0.5)) < 3
                            && Math.abs(player.getZ() - (abs.getZ() + 0.5)) < 3,
                    "player did not arrive at the platform: " + player.getX() + "/" + player.getZ());
        } finally {
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(counter);
        }

        helper.succeed();
    }

    // ---- First teleport must grant the advancement --------------------------------

    @GameTest(templateNamespace = MiltenMagic.MOD_ID, template = "empty")
    public static void firstTeleportAwardsAdvancement(GameTestHelper helper) {
        net.minecraft.server.level.ServerPlayer player = helper.makeMockServerPlayerInLevel();

        net.minecraft.advancements.AdvancementHolder advancement = helper.getLevel().getServer()
                .getAdvancements().get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                        MiltenMagic.MOD_ID, "first_teleport"));
        helper.assertTrue(advancement != null,
                "data/miltenmagic/advancement/first_teleport.json is missing or failed to load");

        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(
                new net.tobiasfiller.miltenmagic.core.event.TeleportationEvent.Post(
                        player,
                        new ItemStack(ItemRegistry.TELEPORTATION_SCROLL.get()),
                        player.position(),
                        player.position(),
                        helper.getLevel().dimension()));

        helper.assertTrue(player.getAdvancements().getOrStartProgress(advancement).isDone(),
                "a successful teleport must grant miltenmagic:first_teleport");
        helper.succeed();
    }

    // ---- Data folders must use the 1.21 singular names ---------------------------

    /**
     * 1.21 renamed the datapack folders to singular (`recipe`, `loot_table`, `advancement`, ...).
     * With the old plural names the files are silently ignored: recipes do not show up and broken
     * blocks drop nothing. This test fails loudly if the folders regress.
     */
    @GameTest(templateNamespace = MiltenMagic.MOD_ID, template = "empty")
    public static void modDataFilesLoad(GameTestHelper helper) {
        net.minecraft.server.MinecraftServer server = helper.getLevel().getServer();

        net.minecraft.resources.ResourceLocation recipeId =
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MiltenMagic.MOD_ID, "oak_table");
        helper.assertTrue(server.getRecipeManager().byKey(recipeId).isPresent(),
                "data/miltenmagic/recipe/oak_table.json did not load (folder must be 'recipe' in 1.21)");

        net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable> lootId =
                net.minecraft.resources.ResourceKey.create(
                        net.minecraft.core.registries.Registries.LOOT_TABLE,
                        net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MiltenMagic.MOD_ID, "blocks/oak_table"));
        helper.assertTrue(server.reloadableRegistries().getLootTable(lootId)
                        != net.minecraft.world.level.storage.loot.LootTable.EMPTY,
                "data/miltenmagic/loot_table/blocks/oak_table.json did not load "
                        + "(folder must be 'loot_table' in 1.21)");

        helper.succeed();
    }

    // ---- Dyeing the teleportation scroll/rune -----------------------------------

    /**
     * The dye recipe must colour the item, keep the platform link and refuse ambiguous grids.
     * The colour itself is a data component, so the recipe is fully testable headless.
     */
    @GameTest(templateNamespace = MiltenMagic.MOD_ID, template = "empty")
    public static void dyeSpellItemRecipe(GameTestHelper helper) {
        net.minecraft.resources.ResourceLocation id = net.minecraft.resources.ResourceLocation
                .fromNamespaceAndPath(MiltenMagic.MOD_ID, "dye_spell_item");
        net.minecraft.world.item.crafting.CraftingRecipe recipe = helper.getLevel().getServer()
                .getRecipeManager().byKey(id)
                .map(net.minecraft.world.item.crafting.RecipeHolder::value)
                .filter(net.minecraft.world.item.crafting.CraftingRecipe.class::isInstance)
                .map(net.minecraft.world.item.crafting.CraftingRecipe.class::cast)
                .orElse(null);
        helper.assertTrue(recipe != null, "data/miltenmagic/recipe/dye_spell_item.json did not load");

        ItemStack scroll = new ItemStack(ItemRegistry.TELEPORTATION_SCROLL.get());
        CompoundTag link = new CompoundTag();
        link.putInt("x", 42);
        scroll.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.of(link));

        net.minecraft.world.item.crafting.CraftingInput input =
                net.minecraft.world.item.crafting.CraftingInput.of(2, 1,
                        java.util.List.of(scroll, new ItemStack(Items.RED_DYE)));
        helper.assertTrue(recipe.matches(input, helper.getLevel()), "scroll + dye must match the dye recipe");

        ItemStack result = recipe.assemble(input, helper.getLevel().registryAccess());
        net.minecraft.world.item.component.DyedItemColor dyed =
                result.get(net.minecraft.core.component.DataComponents.DYED_COLOR);
        helper.assertTrue(dyed != null, "a dyed scroll must carry the dyed_color component");
        helper.assertValueEqual(net.minecraft.world.item.DyeColor.RED.getTextureDiffuseColor() & 0xFFFFFF,
                dyed.rgb() & 0xFFFFFF, "a red dye must colour the scroll red");
        helper.assertValueEqual(1, result.getCount(), "dyeing must not duplicate the item");

        net.minecraft.world.item.component.CustomData data =
                result.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        helper.assertTrue(data != null && data.copyTag().getInt("x") == 42,
                "dyeing must keep the platform link stored in CUSTOM_DATA");

        net.minecraft.world.item.crafting.CraftingInput twoScrolls =
                net.minecraft.world.item.crafting.CraftingInput.of(2, 1,
                        java.util.List.of(scroll.copy(), scroll.copy()));
        helper.assertTrue(!recipe.matches(twoScrolls, helper.getLevel()),
                "two scrolls are not a dye recipe");

        net.minecraft.world.item.crafting.CraftingInput twoDyes =
                net.minecraft.world.item.crafting.CraftingInput.of(3, 1,
                        java.util.List.of(scroll.copy(), new ItemStack(Items.RED_DYE), new ItemStack(Items.BLUE_DYE)));
        helper.assertTrue(!recipe.matches(twoDyes, helper.getLevel()),
                "two dyes must not be mixed into one colour");

        net.minecraft.world.item.crafting.CraftingInput rune =
                net.minecraft.world.item.crafting.CraftingInput.of(2, 1,
                        java.util.List.of(new ItemStack(ItemRegistry.TELEPORTATION_RUNE.get()),
                                new ItemStack(Items.LIME_DYE)));
        helper.assertTrue(recipe.matches(rune, helper.getLevel()), "rune + dye must match the dye recipe");

        helper.succeed();
    }

    // ---- Bug 1: teleport scroll must link and then teleport ---------------------

    @GameTest(templateNamespace = MiltenMagic.MOD_ID, template = "empty", timeoutTicks = 600)
    public static void teleportScrollLinks(GameTestHelper helper) {
        net.minecraft.server.level.ServerPlayer player = helper.makeMockServerPlayerInLevel();
        BlockPos target = new BlockPos(1, 1, 1);

        // a valid 3x3 platform = 1 lapis block in the centre surrounded by a
        // ring of chiseled stone bricks (see TeleportationSpellItem#isValidPlatform)
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                helper.setBlock(target.offset(x, 0, z),
                        x == 0 && z == 0 ? Blocks.LAPIS_BLOCK : Blocks.CHISELED_STONE_BRICKS);
            }
        }
        BlockPos abs = helper.absolutePos(target);
        player.setPos(abs.getX() + 10.5, abs.getY(), abs.getZ() + 0.5);

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemRegistry.TELEPORTATION_SCROLL.get()));
        ItemStack stack = player.getMainHandItem();

        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(abs), Direction.UP, abs, false);
        helper.useBlock(target, player, hit);

        CompoundTag tags = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA) == null
                ? new CompoundTag()
                : stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA).copyTag();
        helper.assertTrue(!tags.isEmpty(),
                "teleport scroll did not store any link data after right-clicking a lapis platform");

        double destX = tags.getDouble("x");
        double destY = tags.getDouble("y");
        double destZ = tags.getDouble("z");

        // now walk away and right-click again -> should charge up and teleport
        player.setPos(abs.getX() + 10.5, abs.getY(), abs.getZ() + 0.5);
        BlockPos other = new BlockPos(5, 1, 5);
        helper.setBlock(other.below(), Blocks.STONE);
        BlockPos otherAbs = helper.absolutePos(other);
        BlockHitResult hit2 = new BlockHitResult(Vec3.atCenterOf(otherAbs.below()),
                Direction.UP, otherAbs.below(), false);

        helper.runAfterDelay(1, () -> {
            // right-clicking AIR with a linked scroll is what Item.use() handles
            ItemStack same = player.getMainHandItem();
            same.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);

            // TeleportationSpellItem counts 120 ticks in inventoryTick(); drive it by hand so
            // the test does not depend on the gametest harness ticking the mock player
            for (int t = 0; t < 260; t++) {
                same.inventoryTick(helper.getLevel(), player, 0, true);
            }

            double px = player.getX();
            double py = player.getY();
            double pz = player.getZ();
            helper.assertTrue(Math.abs(px - destX) < 3 && Math.abs(pz - destZ) < 3,
                    "player did not teleport: still at " + px + "/" + py + "/" + pz
                            + " but link points to " + destX + "/" + destY + "/" + destZ);
            helper.succeed();
        });
    }
}
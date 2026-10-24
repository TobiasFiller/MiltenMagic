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
    // 200 tick countdown would tick N times per tick for N stacks -> wildly varying delay.
    @GameTest(templateNamespace = MiltenMagic.MOD_ID, template = "empty")
    public static void teleportDelayIndependentOfStackCount(GameTestHelper helper) {
        int single = measureTeleportTicks(helper, 1);
        int stacked = measureTeleportTicks(helper, 5);

        com.mojang.logging.LogUtils.getLogger().info(
                "[T3] ticks with 1 stack={} , ticks with 5 stacks={}", single, stacked);

        helper.assertTrue(single == stacked,
                "teleport delay depends on the number of scroll stacks in the inventory: "
                        + single + " ticks with 1 stack vs " + stacked + " ticks with 5");
        helper.assertValueEqual(200, stacked, "expected a 200 tick delay");
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

            // TeleportationSpellItem counts 200 ticks in inventoryTick(); drive it by hand so
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
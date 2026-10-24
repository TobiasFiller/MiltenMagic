package net.tobiasfiller.miltenmagic.core.event;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Fired by the teleportation spell when a linked teleport is actually executed.
 *
 * <p>{@link Pre} is fired before the player is moved and is cancelable: canceling it aborts the
 * teleport without consuming the scroll or the experience. {@link Post} is fired after the player
 * has arrived.
 *
 * <p>Both are posted on the {@link NeoForge#EVENT_BUS game bus}. They exist so other features
 * (other spells, protection zones, statistics, achievements, ...) can react to a teleport without
 * the spell item having to know about them.
 */
public class TeleportationEvent extends PlayerEvent {

    private final ItemStack scroll;
    private final Vec3 origin;
    private final Vec3 destination;
    private final ResourceKey<Level> destinationDimension;

    protected TeleportationEvent(@NotNull Player player, @NotNull ItemStack scroll, @NotNull Vec3 origin,
                                 @NotNull Vec3 destination, @NotNull ResourceKey<Level> destinationDimension) {
        super(player);
        this.scroll = scroll;
        this.origin = origin;
        this.destination = destination;
        this.destinationDimension = destinationDimension;
    }

    /** The stack that triggered the teleport (may already be shrunk after a successful teleport). */
    public @NotNull ItemStack getScroll() {
        return scroll;
    }

    /** Where the player stood when the teleport started. */
    public @NotNull Vec3 getOrigin() {
        return origin;
    }

    /** Where the player will be / was placed. */
    public @NotNull Vec3 getDestination() {
        return destination;
    }

    public @NotNull ResourceKey<Level> getDestinationDimension() {
        return destinationDimension;
    }

    /**
     * Fired before the player is moved. Cancel it to abort the teleport; the scroll and the
     * experience cost stay untouched.
     */
    public static class Pre extends TeleportationEvent implements ICancellableEvent {
        public Pre(@NotNull Player player, @NotNull ItemStack scroll, @NotNull Vec3 origin,
                   @NotNull Vec3 destination, @NotNull ResourceKey<Level> destinationDimension) {
            super(player, scroll, origin, destination, destinationDimension);
        }
    }

    /** Fired after the player has arrived at the destination. */
    public static class Post extends TeleportationEvent {
        public Post(@NotNull Player player, @NotNull ItemStack scroll, @NotNull Vec3 origin,
                    @NotNull Vec3 destination, @NotNull ResourceKey<Level> destinationDimension) {
            super(player, scroll, origin, destination, destinationDimension);
        }
    }
}

package net.tobiasfiller.miltenmagic.client;

import com.zigythebird.playeranim.animation.PlayerAnimResources;
import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranim.api.PlayerAnimationFactory;
import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.animation.AnimationController;
import com.zigythebird.playeranimcore.animation.RawAnimation;
import com.zigythebird.playeranimcore.animation.layered.modifier.AbstractFadeModifier;
import com.zigythebird.playeranimcore.api.firstPerson.FirstPersonConfiguration;
import com.zigythebird.playeranimcore.api.firstPerson.FirstPersonMode;
import com.zigythebird.playeranimcore.easing.EasingType;
import com.zigythebird.playeranimcore.enums.PlayState;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.tobiasfiller.miltenmagic.MiltenMagic;
import net.tobiasfiller.miltenmagic.common.item.spellItems.TeleportationSpellItem;

/**
 * Plays the teleportation cast animation through the Player Animation Library (PAL).
 * <p>
 * The teleport channel is fire-and-forget: one right click arms it, and it runs for its full
 * duration without the button being held. The animation therefore cannot use vanilla's "using
 * item" state (which ends as soon as the mouse button is released). It is driven by the
 * {@code tpPending} flag in the held stack's {@code CUSTOM_DATA} instead - vanilla replicates
 * item components to the caster's inventory slots and to every other client as equipment, so no
 * custom networking is needed.
 */
@EventBusSubscriber(modid = MiltenMagic.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class TeleportationAnimation {

    private static final ResourceLocation LAYER_ID =
            ResourceLocation.fromNamespaceAndPath(MiltenMagic.MOD_ID, "teleport_channel");
    private static final ResourceLocation ANIMATION_ID =
            ResourceLocation.fromNamespaceAndPath(MiltenMagic.MOD_ID, "teleport_channel");

    /** Gameplay animations use 1000; staying above that keeps emotes from overriding the channel. */
    private static final int PRIORITY = 1500;

    /** How long the pose takes to fade back into the vanilla one when the channel is interrupted. */
    private static final int CANCEL_FADE_TICKS = 8;

    private TeleportationAnimation() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // PAL requires the registration on the client thread, not during mod construction.
        event.enqueueWork(TeleportationAnimation::register);
    }

    private static void register() {
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(LAYER_ID, PRIORITY, player -> {
            // One state object per player/controller pair: the state handler runs for every rendered
            // player, so "am I currently casting?" must not live in a static field.
            ChannelState state = new ChannelState();

            PlayerAnimationController controller = new PlayerAnimationController(player,
                    (animationController, animationData, animSetter) ->
                            handle(player, animationController, animSetter, state));

            // Render the third person model in first person so the player sees their own arms.
            controller.setFirstPersonMode(FirstPersonMode.THIRD_PERSON_MODEL);
            controller.setFirstPersonConfiguration(new FirstPersonConfiguration()
                    .setShowRightArm(true)
                    .setShowLeftArm(true)
                    .setShowRightItem(true)
                    .setShowLeftItem(true));
            controller.setFirstPersonFollowsCamera(true);
            controller.setFirstPersonTransitionLength(6);
            return controller;
        });
    }

    private static PlayState handle(AbstractClientPlayer player, AnimationController controller,
                                    AnimationController.AnimationSetter animSetter, ChannelState state) {
        if (isChanneling(player)) {
            if (!state.channeling) {
                state.channeling = true;
                state.fadingOut = false;

                // A finished play_once animation stays "current" on the controller, and
                // setAnimation() ignores an animation that equals the current one. Without this
                // reset the second cast - and every cast after it - would silently stay stopped.
                controller.removeAllModifiers();
                controller.forceAnimationReset();
            }

            // Resolved on every call: animations are (re)loaded with the resource manager, so caching
            // the Animation across a resource reload would keep a stale instance.
            Animation animation = PlayerAnimResources.getAnimation(ANIMATION_ID);
            if (animation == null) {
                return PlayState.STOP;
            }

            return animSetter.setAnimation(RawAnimation.begin().then(animation, Animation.LoopType.PLAY_ONCE));
        }

        if (state.channeling) {
            // Channel was interrupted (key released, item swapped, teleport done, death, ...).
            // Fade from whatever pose the animation is in right now back to the vanilla pose
            // instead of snapping the arms down in a single frame.
            state.channeling = false;
            state.fadingOut = true;
            controller.replaceAnimationWithFade(
                    AbstractFadeModifier.standardFadeIn(CANCEL_FADE_TICKS, EasingType.EASE_IN_OUT_SINE),
                    RawAnimation.begin().thenWait(CANCEL_FADE_TICKS));
            return PlayState.CONTINUE;
        }

        if (state.fadingOut) {
            // PAL removes the fade modifier by itself once it has finished; until then the
            // controller must keep ticking so the blend can run.
            if (controller.getModifierCount() > 0) {
                return PlayState.CONTINUE;
            }
            state.fadingOut = false;
        }

        return PlayState.STOP;
    }

    /**
     * The channel only runs while the armed scroll is in the main hand; vanilla disarms it on
     * slot changes, drops, death and when the teleport fires.
     */
    private static boolean isChanneling(AbstractClientPlayer player) {
        return TeleportationSpellItem.isArmed(player.getMainHandItem());
    }

    /** Per-player animation bookkeeping; created for each controller in the factory. */
    private static final class ChannelState {
        private boolean channeling;
        private boolean fadingOut;
    }
}

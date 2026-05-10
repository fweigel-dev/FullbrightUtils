package dev.fweigel.fullbrightutils.mixin;

import dev.fweigel.fullbrightutils.FullbrightUtilsConfig;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fakes night-vision for the local player when FullbrightUtils's night vision is enabled.
 * Works purely client-side: the server never sees these fake effect instances.
 *
 * <p>Both {@code hasEffect} and {@code getEffect} are intercepted so the rendering code
 * sees a consistent view (duration = 600 ticks avoids the flicker near expiry).
 */
@Mixin(LivingEntity.class)
public class NightVisionMixin {

    @Inject(method = "hasEffect", at = @At("HEAD"), cancellable = true)
    private void fullbrightutils_hasEffect(Holder<MobEffect> effect, CallbackInfoReturnable<Boolean> cir) {
        if (FullbrightUtilsConfig.isNightVisionActive()
                && (Object) this instanceof LocalPlayer
                && effect == MobEffects.NIGHT_VISION) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getEffect", at = @At("RETURN"), cancellable = true)
    private void fullbrightutils_getEffect(Holder<MobEffect> effect,
                                       CallbackInfoReturnable<@Nullable MobEffectInstance> cir) {
        if (FullbrightUtilsConfig.isNightVisionActive()
                && (Object) this instanceof LocalPlayer
                && effect == MobEffects.NIGHT_VISION
                && cir.getReturnValue() == null) {
            // Duration 600 ticks (30 s) keeps the rendering code well away from the flicker zone.
            cir.setReturnValue(new MobEffectInstance(MobEffects.NIGHT_VISION, 600, 0, false, false));
        }
    }
}

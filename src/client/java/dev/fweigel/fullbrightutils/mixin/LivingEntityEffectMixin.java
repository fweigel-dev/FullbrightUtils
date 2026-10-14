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
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Applies the darkness and blindness resistance on the local player, on {@link LivingEntity}
 * itself instead of on the vanilla call sites that happen to read the effect.
 *
 * <p>Vanilla asks {@code hasEffect} for the things it draws itself: the blindness and darkness fog
 * come out of {@code MobEffectFogEnvironment.isApplicable}, the sky is suppressed from the flags
 * {@code Camera.extractRenderState} builds, and {@code FogRenderer} asks the same question about
 * night vision. {@code LightmapRenderStateExtractor} reads the darkness blend factor from
 * {@code getEffectBlendFactor} to dim the lightmap. Scaling those three vanilla readers is all the
 * mod used to do, and it is all that is needed without a shader pack.
 *
 * <p>A shader pack is not on that path. Vitrail and Iris each publish the OptiFine uniforms
 * {@code blindness}, {@code darknessFactor} and {@code darknessLightFactor} to the pack, and each
 * fills them from {@code getEffect(MobEffects.BLINDNESS)} and
 * {@code getEffectBlendFactor(MobEffects.DARKNESS, ..)} instead — Vitrail
 * {@code render/FrameState}, Iris {@code uniforms/CommonUniforms}. None of them goes through the
 * vanilla fog or the vanilla lightmap, so the resistance was simply invisible to a pack: the effect
 * came back the moment a shader was enabled.
 *
 * <p>Answering on {@code LivingEntity} covers both worlds at once, because every reader — vanilla
 * or pack — has to come through here.
 *
 * <h2>Why 100 % is a hide and not a scale</h2>
 * The resistance is a percentage, but a pack's {@code blindness} uniform is not: it is built from
 * the presence of the effect, so the only two things the mod can say to a pack are "blindness" and
 * "no blindness". Full resistance therefore hides the effect — {@code hasEffect} false and
 * {@code getEffect} null — which is what the mod's own UI means by 100 %. Below that the effect
 * stays present and only its blend factor is scaled, which is the part vanilla (and Vitrail's
 * darkness uniform) can actually express.
 */
@Mixin(LivingEntity.class)
public class LivingEntityEffectMixin {

    @Inject(method = "hasEffect", at = @At("HEAD"), cancellable = true)
    private void fullbrightutils_hideSuppressedEffect(Holder<MobEffect> effect,
                                                      CallbackInfoReturnable<Boolean> cir) {
        if (fullbrightutils_isFullyResisted(effect)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "getEffect", at = @At("HEAD"), cancellable = true)
    private void fullbrightutils_hideSuppressedEffectInstance(Holder<MobEffect> effect,
                                                              CallbackInfoReturnable<@Nullable MobEffectInstance> cir) {
        if (fullbrightutils_isFullyResisted(effect)) {
            cir.setReturnValue(null);
        }
    }

    /**
     * Scales the darkness (and blindness) blend factor at its source, so the vanilla lightmap and
     * every shader uniform derived from it see the reduced value. This replaces the call-site
     * redirect that used to sit in {@code LightmapRenderStateExtractor}: keeping both would have
     * multiplied the factor by the resistance twice.
     */
    @Inject(method = "getEffectBlendFactor", at = @At("RETURN"), cancellable = true)
    private void fullbrightutils_scaleEffectBlendFactor(Holder<MobEffect> effect, float partialTick,
                                                       CallbackInfoReturnable<Float> cir) {
        if ((Object) this instanceof LocalPlayer) {
            float scale = fullbrightutils_blendScale(effect);
            if (scale < 0.999f) {
                cir.setReturnValue(cir.getReturnValueF() * scale);
            }
        }
    }

    /**
     * Only the two purely visual effects the mod owns. {@code getEffectBlendFactor} is also read
     * for nausea (portal overlay, {@code Hud} and {@code LevelExtractor}) and must not be touched,
     * which is why the answer for anything else is 1.0.
     *
     * <p>Compared by identity, the same way {@link NightVisionMixin} does it: {@code MobEffects}'
     * constants are the canonical registry holders, which is what {@code LivingEntity} stores in
     * {@code activeEffects} and looks up with. {@code Holder#is(Holder)} would say the same thing
     * but is deprecated in 26.3.
     */
    @Unique
    private static float fullbrightutils_blendScale(Holder<MobEffect> effect) {
        if (effect == MobEffects.DARKNESS)  return (float) FullbrightUtilsConfig.getDarknessScale();
        if (effect == MobEffects.BLINDNESS) return (float) FullbrightUtilsConfig.getBlindnessScale();
        return 1.0f;
    }

    /** The local player only, matching the mod's existing scope: another entity's eyes keep their blindness. */
    @Unique
    private boolean fullbrightutils_isFullyResisted(Holder<MobEffect> effect) {
        return (Object) this instanceof LocalPlayer && fullbrightutils_blendScale(effect) <= 0.001f;
    }
}

package dev.fweigel.fullbrightutils.mixin;

import dev.fweigel.fullbrightutils.FullbrightUtilsConfig;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Lightmap-side tweaks that have no other home. The darkness blend factor used to be scaled here
 * as well, on the {@code getEffectBlendFactor} call inside {@code extract}; that now happens in
 * {@link LivingEntityEffectMixin}, where every reader (vanilla lightmap, Vitrail, Iris) picks it up
 * and where it is applied exactly once.
 */
@Mixin(LightmapRenderStateExtractor.class)
public class LightmapMixin {

    @Redirect(method = "extract", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(FF)F"))
    private float fullbrightutils_unclampBrightness(float zero, float gammaMinusDarkness) {
        if (FullbrightUtilsConfig.getGammaBoost() < -0.001) {
            return gammaMinusDarkness;
        }
        return Math.max(zero, gammaMinusDarkness);
    }

    @Redirect(method = "calculateDarknessScale", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/util/Mth;cos(D)F"))
    private float fullbrightutils_scaleDarknessPulse(double angle) {
        // when pulse is disabled: cos=1.0 locks at maximum darkness, no oscillation
        return FullbrightUtilsConfig.isDarkPulseEnabled() ? Mth.cos(angle) : 1.0f;
    }

    // nightVisionScale() is what actually sets nightVisionEffectIntensity in the render state —
    // getEffectBlendFactor is not called for night vision in extract.
    @Redirect(method = "extract", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/GameRenderer;nightVisionScale(Lnet/minecraft/world/entity/LivingEntity;F)F"))
    private float fullbrightutils_scaleNightVision(LivingEntity entity, float tickDelta) {
        return GameRenderer.nightVisionScale(entity, tickDelta) * (float) FullbrightUtilsConfig.getNightVisionStrength();
    }
}

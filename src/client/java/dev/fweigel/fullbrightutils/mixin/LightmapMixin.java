package dev.fweigel.fullbrightutils.mixin;

import dev.fweigel.fullbrightutils.FullbrightUtilsConfig;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LightmapRenderStateExtractor.class)
public class LightmapMixin {

    @Redirect(method = "extract", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(FF)F"))
    private float fullbrightutils_unclampBrightness(float zero, float gammaMinusDarkness) {
        if (FullbrightUtilsConfig.getGammaBoost() < -0.001) {
            return gammaMinusDarkness;
        }
        return Math.max(zero, gammaMinusDarkness);
    }

    @Redirect(method = "extract", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/player/LocalPlayer;getEffectBlendFactor(Lnet/minecraft/core/Holder;F)F"))
    private float fullbrightutils_scaleDarknessBlend(LocalPlayer player, Holder<MobEffect> effect, float tickDelta) {
        return player.getEffectBlendFactor(effect, tickDelta) * (float) FullbrightUtilsConfig.getDarknessScale();
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

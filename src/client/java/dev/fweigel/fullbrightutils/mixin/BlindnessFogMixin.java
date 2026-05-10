package dev.fweigel.fullbrightutils.mixin;

import dev.fweigel.fullbrightutils.FullbrightUtilsConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.BlindnessFogEnvironment;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlindnessFogEnvironment.class)
public class BlindnessFogMixin {

    @Inject(method = "setupFog", at = @At("RETURN"))
    private void fullbrightutils_scaleBlindnessFog(FogData fogData, Camera camera, ClientLevel level,
                                                    float renderDistance, DeltaTracker deltaTracker, CallbackInfo ci) {
        float scale = (float) FullbrightUtilsConfig.getBlindnessScale();
        if (scale < 0.999f) {
            fogData.environmentalStart = Mth.lerp(scale, renderDistance, fogData.environmentalStart);
            fogData.environmentalEnd   = Mth.lerp(scale, renderDistance, fogData.environmentalEnd);
            fogData.skyEnd             = Mth.lerp(scale, renderDistance, fogData.skyEnd);
            fogData.cloudEnd           = Mth.lerp(scale, renderDistance, fogData.cloudEnd);
        }
    }
}

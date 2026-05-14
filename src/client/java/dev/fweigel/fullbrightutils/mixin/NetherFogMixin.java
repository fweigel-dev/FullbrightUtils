package dev.fweigel.fullbrightutils.mixin;

import dev.fweigel.fullbrightutils.FullbrightUtilsConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AtmosphericFogEnvironment.class)
public class NetherFogMixin {

    @Inject(method = "setupFog", at = @At("RETURN"))
    private void fullbrightutils_scaleNetherFog(FogData fogData, Camera camera, ClientLevel level,
                                                 float renderDistance, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!level.dimension().equals(Level.NETHER)) return;
        float scale = (float) FullbrightUtilsConfig.getNetherFogScale();
        if (scale < 0.999f) {
            fogData.environmentalStart = Mth.lerp(scale, renderDistance, fogData.environmentalStart);
            fogData.environmentalEnd   = Mth.lerp(scale, renderDistance, fogData.environmentalEnd);
            fogData.skyEnd             = Mth.lerp(scale, renderDistance, fogData.skyEnd);
            fogData.cloudEnd           = Mth.lerp(scale, renderDistance, fogData.cloudEnd);
        }
    }
}

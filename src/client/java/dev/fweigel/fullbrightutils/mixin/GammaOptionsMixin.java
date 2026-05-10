package dev.fweigel.fullbrightutils.mixin;

import dev.fweigel.fullbrightutils.FullbrightUtilsConfig;
import dev.fweigel.fullbrightutils.OptionInstanceAccessor;
import dev.fweigel.fullbrightutils.OptionsAccessor;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Protects {@code options.txt} from storing a boosted gamma value.
 * Before each save, temporarily restores the user's base gamma; after save, re-applies the boost.
 * Also exposes the private {@code gamma} field via {@link OptionsAccessor}.
 */
@Mixin(Options.class)
public class GammaOptionsMixin implements OptionsAccessor {

    @Shadow
    private OptionInstance<Double> gamma;

    @Unique
    @Override
    public OptionInstance<Double> fullbrightutils_getGamma() {
        return gamma;
    }

    @Inject(method = "save", at = @At("HEAD"))
    private void fullbrightutils_beforeSave(CallbackInfo ci) {
        if (Math.abs(FullbrightUtilsConfig.getGammaBoost()) > 0.001) {
            setRaw(FullbrightUtilsConfig.getBaseGamma());
        }
    }

    @Inject(method = "save", at = @At("RETURN"))
    private void fullbrightutils_afterSave(CallbackInfo ci) {
        if (Math.abs(FullbrightUtilsConfig.getGammaBoost()) > 0.001) {
            setRaw(boostedGamma());
        }
    }

    private void setRaw(double v) {
        ((OptionInstanceAccessor<Double>) (Object) gamma).fullbrightutils_setRawValue(v);
    }

    private static double boostedGamma() {
        return Math.min(FullbrightUtilsConfig.getBaseGamma() + FullbrightUtilsConfig.getGammaBoost(), 5.0);
    }
}

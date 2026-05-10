package dev.fweigel.fullbrightutils;

import net.minecraft.client.OptionInstance;

/**
 * Duck-typing interface injected onto {@code net.minecraft.client.Options}
 * by {@link dev.fweigel.fullbrightutils.mixin.GammaOptionsMixin}.
 * Exposes the private {@code gamma} field for use outside the mixin.
 */
public interface OptionsAccessor {
    OptionInstance<Double> fullbrightutils_getGamma();
}

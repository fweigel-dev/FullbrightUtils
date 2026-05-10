package dev.fweigel.fullbrightutils;

/**
 * Duck-typing interface injected onto {@code net.minecraft.client.OptionInstance}
 * by {@link dev.fweigel.fullbrightutils.mixin.OptionInstanceMixin}.
 * Allows writing the raw value field, bypassing the validator so gamma can be
 * set beyond the vanilla 0–1 slider range.
 */
public interface OptionInstanceAccessor<T> {
    void fullbrightutils_setRawValue(T value);
}

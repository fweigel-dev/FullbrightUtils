package dev.fweigel.fullbrightutils.mixin;

import dev.fweigel.fullbrightutils.OptionInstanceAccessor;
import net.minecraft.client.OptionInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Exposes the private {@code value} field of {@link OptionInstance} for direct writes.
 * Used by FullbrightUtils to push gamma values beyond the 0–1 vanilla slider range without
 * going through the validator (which would reject and reset out-of-range values).
 */
@Mixin(OptionInstance.class)
public class OptionInstanceMixin<T> implements OptionInstanceAccessor<T> {

    @Shadow
    private T value;

    @Unique
    @Override
    public void fullbrightutils_setRawValue(T newValue) {
        this.value = newValue;
    }
}

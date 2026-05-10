package dev.fweigel.fullbrightutils;

import dev.fweigel.mobutils.core.client.ui.ModOptionsList;
import dev.fweigel.mobutils.core.client.ui.ModOptionsList.CardSpec;
import dev.fweigel.mobutils.core.client.ui.ModSettingsScreen;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class FullbrightUtilsScreen extends ModSettingsScreen {

    private static final double BOOST_RANGE = FullbrightUtilsConfig.BOOST_MAX - FullbrightUtilsConfig.BOOST_MIN;

    // ── Preview image banks (allocated once at class-load) ──────────────────────

    // gamma_boost_<tenths>.png  where tenths ∈ [-50 .. +50]  →  index = tenths + 50
    private static final Identifier[] GAMMA_FRAMES = new Identifier[101];
    // night_vision_<pct>.png, darkness_<pct>.png, blindness_<pct>.png  pct ∈ [000,005,...,100]
    private static final Identifier[] NV_FRAMES    = new Identifier[21];
    private static final Identifier[] DARK_FRAMES  = new Identifier[21];
    private static final Identifier[] BLIND_FRAMES = new Identifier[21];

    private static final Identifier[] DARK_PULSE_ON_FRAMES  = buildFrames("dark_pulse_on_%03d.png",  122);
    private static final Identifier[] DARK_PULSE_OFF_FRAMES = buildFrames("dark_pulse_off_%03d.png", 135);
    private static final long DARK_PULSE_FRAME_MS = 100L;

    static {
        for (int i = 0; i <= 100; i++) {
            GAMMA_FRAMES[i] = id("gamma_boost_" + (i - 50) + ".png");
        }
        for (int i = 0; i <= 20; i++) {
            int pct = i * 5;
            NV_FRAMES[i]    = id(String.format("night_vision_%03d.png", pct));
            DARK_FRAMES[i]  = id(String.format("darkness_%03d.png",     pct));
            BLIND_FRAMES[i] = id(String.format("blindness_%03d.png",    pct));
        }
    }

    public FullbrightUtilsScreen() {
        super(Component.translatable("fullbrightutils.screen.title"));
    }

    @Override
    protected void addOptions(ModOptionsList list) {
        AbstractSliderButton gammaSlider = new AbstractSliderButton(0, 0, CARD_W, BUTTON_HEIGHT,
                gammaLabel(FullbrightUtilsConfig.getGammaBoost()),
                boostToValue(FullbrightUtilsConfig.getGammaBoost())) {
            @Override protected void updateMessage() { setMessage(gammaLabel(valueToBoost(value))); }
            @Override protected void applyValue() {
                FullbrightUtilsConfig.setGammaBoost(valueToBoost(value));
                FullbrightUtilsStorage.save();
            }
        };
        gammaSlider.setTooltip(Tooltip.create(Component.translatable("fullbrightutils.tooltip.gamma_boost")));

        AbstractSliderButton nvSlider = new AbstractSliderButton(0, 0, CARD_W, BUTTON_HEIGHT,
                effectLabel("fullbrightutils.screen.night_vision", FullbrightUtilsConfig.getNightVisionStrength()),
                FullbrightUtilsConfig.getNightVisionStrength()) {
            @Override protected void updateMessage() { setMessage(effectLabel("fullbrightutils.screen.night_vision", roundScale(value))); }
            @Override protected void applyValue() {
                FullbrightUtilsConfig.setNightVisionStrength(roundScale(value));
                FullbrightUtilsStorage.save();
            }
        };
        nvSlider.setTooltip(Tooltip.create(Component.translatable("fullbrightutils.tooltip.night_vision")));

        double darknessResist = 1.0 - FullbrightUtilsConfig.getDarknessScale();
        AbstractSliderButton darknessSlider = new AbstractSliderButton(0, 0, CARD_W, BUTTON_HEIGHT,
                effectLabel("fullbrightutils.screen.darkness_scale", darknessResist),
                darknessResist) {
            @Override protected void updateMessage() { setMessage(effectLabel("fullbrightutils.screen.darkness_scale", roundScale(value))); }
            @Override protected void applyValue() {
                FullbrightUtilsConfig.setDarknessScale(1.0 - roundScale(value));
                FullbrightUtilsStorage.save();
            }
        };
        darknessSlider.setTooltip(Tooltip.create(Component.translatable("fullbrightutils.tooltip.darkness_scale")));

        double blindnessResist = 1.0 - FullbrightUtilsConfig.getBlindnessScale();
        AbstractSliderButton blindnessSlider = new AbstractSliderButton(0, 0, CARD_W, BUTTON_HEIGHT,
                effectLabel("fullbrightutils.screen.blindness_scale", blindnessResist),
                blindnessResist) {
            @Override protected void updateMessage() { setMessage(effectLabel("fullbrightutils.screen.blindness_scale", roundScale(value))); }
            @Override protected void applyValue() {
                FullbrightUtilsConfig.setBlindnessScale(1.0 - roundScale(value));
                FullbrightUtilsStorage.save();
            }
        };
        blindnessSlider.setTooltip(Tooltip.create(Component.translatable("fullbrightutils.tooltip.blindness_scale")));

        list.addSplitCard(
            CardSpec.image(FullbrightUtilsScreen::gammaPreviewId),
            gammaSlider,
            CardSpec.image(FullbrightUtilsScreen::nvPreviewId),
            nvSlider
        );

        list.addSplitCard(
            CardSpec.image(FullbrightUtilsScreen::darknessPreviewId),
            darknessSlider,
            CardSpec.image(FullbrightUtilsScreen::blindnessPreviewId),
            blindnessSlider
        );

        Button pulseBtn = buildWideButton(
                () -> Component.translatable("fullbrightutils.screen.dark_pulse",
                        Component.translatable(FullbrightUtilsConfig.isDarkPulseEnabled()
                                ? "fullbrightutils.state.on" : "fullbrightutils.state.off")),
                () -> {
                    FullbrightUtilsConfig.setDarkPulseEnabled(!FullbrightUtilsConfig.isDarkPulseEnabled());
                    FullbrightUtilsStorage.save();
                });
        pulseBtn.setTooltip(Tooltip.create(Component.translatable("fullbrightutils.tooltip.dark_pulse")));
        list.addSingleCard(
                CardSpec.animated(
                        () -> FullbrightUtilsConfig.isDarkPulseEnabled() ? DARK_PULSE_ON_FRAMES : DARK_PULSE_OFF_FRAMES,
                        DARK_PULSE_FRAME_MS, true),
                pulseBtn);
    }

    // ── Preview ID lookups (array lookup, no allocation per frame) ─────────────

    private static Identifier gammaPreviewId() {
        int idx = (int) Math.round(FullbrightUtilsConfig.getGammaBoost() * 10) + 50;
        return GAMMA_FRAMES[Math.max(0, Math.min(100, idx))];
    }

    private static Identifier nvPreviewId() {
        int idx = (int) Math.round(FullbrightUtilsConfig.getNightVisionStrength() * 20);
        return NV_FRAMES[Math.max(0, Math.min(20, idx))];
    }

    private static Identifier darknessPreviewId() {
        double resist = 1.0 - FullbrightUtilsConfig.getDarknessScale();
        int idx = (int) Math.round(resist * 20);
        return DARK_FRAMES[Math.max(0, Math.min(20, idx))];
    }

    private static Identifier blindnessPreviewId() {
        double resist = 1.0 - FullbrightUtilsConfig.getBlindnessScale();
        int idx = (int) Math.round(resist * 20);
        return BLIND_FRAMES[Math.max(0, Math.min(20, idx))];
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private static Identifier id(String path) {
        return Identifier.parse("fullbrightutils:textures/gui/preview/" + path);
    }

    private static Component gammaLabel(double boost) {
        String value = Math.abs(boost) < 0.001
                ? Component.translatable("fullbrightutils.gamma.off").getString()
                : String.format("%+.1f", boost);
        return Component.translatable("fullbrightutils.screen.gamma_boost", value);
    }

    private static Component effectLabel(String key, double scale) {
        String pct = scale < 0.001
                ? Component.translatable("fullbrightutils.effect.off").getString()
                : String.format("%.0f%%", scale * 100);
        return Component.translatable(key, pct);
    }

    private static double boostToValue(double boost) {
        return (boost - FullbrightUtilsConfig.BOOST_MIN) / BOOST_RANGE;
    }

    private static double valueToBoost(double value) {
        return Math.round((value * BOOST_RANGE + FullbrightUtilsConfig.BOOST_MIN) * 10.0) / 10.0;
    }

    private static double roundScale(double value) {
        return Math.round(value * 20.0) / 20.0;
    }

    private static Identifier[] buildFrames(String pattern, int count) {
        Identifier[] frames = new Identifier[count];
        for (int i = 0; i < count; i++) frames[i] = id(String.format(pattern, i + 1));
        return frames;
    }
}

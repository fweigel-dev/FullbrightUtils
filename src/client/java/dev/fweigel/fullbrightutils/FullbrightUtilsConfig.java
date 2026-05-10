package dev.fweigel.fullbrightutils;

public class FullbrightUtilsConfig {

    public static final double BOOST_STEP  = 0.1;
    public static final double BOOST_MIN   = -5.0;
    public static final double BOOST_MAX   =  5.0;
    public static final double EFFECT_STEP = 0.05;

    private static double  gammaBoost           = 0.0;
    private static double  nightVisionStrength  = 0.0;
    private static double  darknessScale        = 1.0;
    private static double  blindnessScale       = 1.0;
    private static boolean darkPulseEnabled      = true;

    // The user's actual MC gamma (0.0–1.0), tracked so we can restore it on save.
    private static double baseGamma = 1.0;

    public static double  getGammaBoost()              { return gammaBoost; }
    public static double  getNightVisionStrength()      { return nightVisionStrength; }
    public static boolean isNightVisionActive()         { return nightVisionStrength > 0.001; }
    public static double  getDarknessScale()            { return darknessScale; }
    public static double  getBlindnessScale()           { return blindnessScale; }
    public static double  getBaseGamma()                { return baseGamma; }
    public static boolean isDarkPulseEnabled()           { return darkPulseEnabled; }

    public static void setGammaBoost(double v)           { gammaBoost          = round(Math.max(BOOST_MIN, Math.min(BOOST_MAX, v))); }
    public static void setNightVisionStrength(double v)  { nightVisionStrength = Math.max(0.0, Math.min(1.0, v)); }
    public static void setDarknessScale(double v)        { darknessScale       = Math.max(0.0, Math.min(1.0, v)); }
    public static void setBlindnessScale(double v)       { blindnessScale      = Math.max(0.0, Math.min(1.0, v)); }
    public static void setBaseGamma(double v)            { baseGamma           = Math.max(0.0, Math.min(1.0, v)); }
    public static void setDarkPulseEnabled(boolean v)    { darkPulseEnabled    = v; }

    public static void increaseBoost() { setGammaBoost(gammaBoost + BOOST_STEP); }
    public static void decreaseBoost() { setGammaBoost(gammaBoost - BOOST_STEP); }

    public static void increaseNightVision()    { setNightVisionStrength(roundEffect(nightVisionStrength + EFFECT_STEP)); }
    public static void decreaseNightVision()    { setNightVisionStrength(roundEffect(nightVisionStrength - EFFECT_STEP)); }

    // Keybinds express "resistance" (inverse of scale), so increasing resistance means decreasing the scale.
    public static void increaseDarknessResist() { setDarknessScale(roundEffect(darknessScale - EFFECT_STEP)); }
    public static void decreaseDarknessResist() { setDarknessScale(roundEffect(darknessScale + EFFECT_STEP)); }

    public static void increaseBlindnessResist() { setBlindnessScale(roundEffect(blindnessScale - EFFECT_STEP)); }
    public static void decreaseBlindnessResist() { setBlindnessScale(roundEffect(blindnessScale + EFFECT_STEP)); }

    public static void reset() {
        gammaBoost          = 0.0;
        nightVisionStrength = 0.0;
        darknessScale       = 1.0;
        blindnessScale      = 1.0;
        baseGamma           = 1.0;
        darkPulseEnabled    = true;
    }

    private static double round(double v)       { return Math.round(v * 10.0) / 10.0; }
    private static double roundEffect(double v) { return Math.round(v * 20.0) / 20.0; }
}

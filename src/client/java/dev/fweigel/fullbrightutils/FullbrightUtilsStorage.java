package dev.fweigel.fullbrightutils;

import dev.fweigel.mobutils.core.client.storage.WorldScopedStorage;
import net.minecraft.client.Minecraft;
import org.slf4j.LoggerFactory;

public final class FullbrightUtilsStorage {

    private static final WorldScopedStorage<SaveData> STORAGE =
            new WorldScopedStorage<>("fullbrightutils", SaveData.class,
                    LoggerFactory.getLogger(FullbrightUtilsStorage.class));

    private static class SaveData {
        Double  gammaBoost;
        Double  nightVisionStrength;
        Double  darknessScale;
        Double  blindnessScale;
        Double  baseGamma;
        Boolean darkPulseEnabled;
    }

    private FullbrightUtilsStorage() {}

    public static void loadForWorld(Minecraft client) {
        STORAGE.loadForWorld(client).ifPresentOrElse(data -> {
            if (data.gammaBoost          != null) FullbrightUtilsConfig.setGammaBoost(data.gammaBoost);
            if (data.nightVisionStrength != null) FullbrightUtilsConfig.setNightVisionStrength(data.nightVisionStrength);
            if (data.darknessScale       != null) FullbrightUtilsConfig.setDarknessScale(data.darknessScale);
            if (data.blindnessScale      != null) FullbrightUtilsConfig.setBlindnessScale(data.blindnessScale);
            if (data.baseGamma           != null) FullbrightUtilsConfig.setBaseGamma(data.baseGamma);
            if (data.darkPulseEnabled    != null) FullbrightUtilsConfig.setDarkPulseEnabled(data.darkPulseEnabled);
        }, FullbrightUtilsConfig::reset);
    }

    public static void save() {
        SaveData data = new SaveData();
        data.gammaBoost          = FullbrightUtilsConfig.getGammaBoost();
        data.nightVisionStrength = FullbrightUtilsConfig.getNightVisionStrength();
        data.darknessScale       = FullbrightUtilsConfig.getDarknessScale();
        data.blindnessScale      = FullbrightUtilsConfig.getBlindnessScale();
        data.baseGamma           = FullbrightUtilsConfig.getBaseGamma();
        data.darkPulseEnabled    = FullbrightUtilsConfig.isDarkPulseEnabled();
        STORAGE.save(data);
    }
}

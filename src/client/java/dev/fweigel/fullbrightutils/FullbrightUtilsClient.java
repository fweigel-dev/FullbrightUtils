package dev.fweigel.fullbrightutils;

import dev.fweigel.mobutils.core.client.util.ConfigKeyHelper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import java.util.Locale;

public class FullbrightUtilsClient implements ClientModInitializer {

    private static KeyMapping configKey;
    private static KeyMapping increaseKey;
    private static KeyMapping decreaseKey;
    private static KeyMapping nvIncreaseKey;
    private static KeyMapping nvDecreaseKey;
    private static KeyMapping darkIncreaseKey;
    private static KeyMapping darkDecreaseKey;
    private static KeyMapping blindIncreaseKey;
    private static KeyMapping blindDecreaseKey;

    @Override
    public void onInitializeClient() {
        configKey = ConfigKeyHelper.register("fullbrightutils", "key.fullbrightutils.config", GLFW.GLFW_KEY_F4);

        KeyMapping.Category category = configKey.getCategory();
        increaseKey      = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.fullbrightutils.increase",       InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UP,      category, 1));
        decreaseKey      = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.fullbrightutils.decrease",       InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_DOWN,    category, 2));
        nvIncreaseKey    = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.fullbrightutils.nv_increase",    InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, category, 3));
        nvDecreaseKey    = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.fullbrightutils.nv_decrease",    InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, category, 4));
        darkIncreaseKey  = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.fullbrightutils.dark_increase",  InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, category, 5));
        darkDecreaseKey  = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.fullbrightutils.dark_decrease",  InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, category, 6));
        blindIncreaseKey = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.fullbrightutils.blind_increase", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, category, 7));
        blindDecreaseKey = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.fullbrightutils.blind_decrease", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, category, 8));

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) ->
                FullbrightUtilsStorage.loadForWorld(client));

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) ->
                FullbrightUtilsStorage.save());

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (configKey.consumeClick()) {
                client.setScreen(new FullbrightUtilsScreen());
            }
            while (increaseKey.consumeClick()) {
                FullbrightUtilsConfig.increaseBoost();
                FullbrightUtilsStorage.save();
                showHud(client, hudGamma());
            }
            while (decreaseKey.consumeClick()) {
                FullbrightUtilsConfig.decreaseBoost();
                FullbrightUtilsStorage.save();
                showHud(client, hudGamma());
            }
            while (nvIncreaseKey.consumeClick()) {
                FullbrightUtilsConfig.increaseNightVision();
                FullbrightUtilsStorage.save();
                showHud(client, hudEffect("fullbrightutils.screen.night_vision", FullbrightUtilsConfig.getNightVisionStrength()));
            }
            while (nvDecreaseKey.consumeClick()) {
                FullbrightUtilsConfig.decreaseNightVision();
                FullbrightUtilsStorage.save();
                showHud(client, hudEffect("fullbrightutils.screen.night_vision", FullbrightUtilsConfig.getNightVisionStrength()));
            }
            while (darkIncreaseKey.consumeClick()) {
                FullbrightUtilsConfig.increaseDarknessResist();
                FullbrightUtilsStorage.save();
                showHud(client, hudEffect("fullbrightutils.screen.darkness_scale", 1.0 - FullbrightUtilsConfig.getDarknessScale()));
            }
            while (darkDecreaseKey.consumeClick()) {
                FullbrightUtilsConfig.decreaseDarknessResist();
                FullbrightUtilsStorage.save();
                showHud(client, hudEffect("fullbrightutils.screen.darkness_scale", 1.0 - FullbrightUtilsConfig.getDarknessScale()));
            }
            while (blindIncreaseKey.consumeClick()) {
                FullbrightUtilsConfig.increaseBlindnessResist();
                FullbrightUtilsStorage.save();
                showHud(client, hudEffect("fullbrightutils.screen.blindness_scale", 1.0 - FullbrightUtilsConfig.getBlindnessScale()));
            }
            while (blindDecreaseKey.consumeClick()) {
                FullbrightUtilsConfig.decreaseBlindnessResist();
                FullbrightUtilsStorage.save();
                showHud(client, hudEffect("fullbrightutils.screen.blindness_scale", 1.0 - FullbrightUtilsConfig.getBlindnessScale()));
            }

            if (client.player == null) return;
            applyGammaBoost(client);
        });
    }

    private static void showHud(Minecraft client, Component message) {
        if (client.player != null) client.player.sendOverlayMessage(message);
    }

    private static Component hudGamma() {
        double boost = FullbrightUtilsConfig.getGammaBoost();
        String value = Math.abs(boost) < 0.001
                ? Component.translatable("fullbrightutils.gamma.off").getString()
                : String.format(Locale.US, "%+.1f", boost);
        return Component.translatable("fullbrightutils.screen.gamma_boost", value);
    }

    private static Component hudEffect(String key, double resistance) {
        String pct = resistance < 0.001
                ? Component.translatable("fullbrightutils.effect.off").getString()
                : String.format(Locale.US, "%.0f%%", resistance * 100);
        return Component.translatable(key, pct);
    }

    // positive boost only: if raw gamma drifted back to ≤1.0, the user moved the vanilla slider — adopt it as the new base
    private static void applyGammaBoost(Minecraft client) {
        OptionInstance<Double> gammaOption =
                ((OptionsAccessor) (Object) client.options).fullbrightutils_getGamma();
        OptionInstanceAccessor<Double> gammaAccess =
                (OptionInstanceAccessor<Double>) (Object) gammaOption;

        double currentRaw = gammaOption.get();
        double boost = FullbrightUtilsConfig.getGammaBoost();

        if (Math.abs(boost) > 0.001) {
            if (boost > 0 && currentRaw <= 1.001) {
                FullbrightUtilsConfig.setBaseGamma(currentRaw);
            }
            double target = Math.min(FullbrightUtilsConfig.getBaseGamma() + boost, 5.0);
            if (Math.abs(currentRaw - target) > 0.001) {
                gammaAccess.fullbrightutils_setRawValue(target);
            }
        } else {
            if (Math.abs(currentRaw - FullbrightUtilsConfig.getBaseGamma()) > 0.001) {
                gammaAccess.fullbrightutils_setRawValue(FullbrightUtilsConfig.getBaseGamma());
            }
        }
    }
}

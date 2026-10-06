package cc.avie.esad.feature.fullbright;

import cc.avie.esad.EssentialAdditions;
import cc.avie.esad.config.EsadConfig;
import cc.avie.esad.config.FullbrightConfig;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * Fullbright: everything is lit as if you had night vision. Works through the night vision strength of the
 * light map, so it needs no gamma values above the vanilla limit and looks like the vanilla effect.
 */
public final class Fullbright {
	public static final KeyMapping KEY = new KeyMapping("key.esad.fullbright", InputConstants.KEY_G, EssentialAdditions.KEY_CATEGORY);

	private Fullbright() {
	}

	public static void register() {
		KeyMappingHelper.registerKeyMapping(KEY);
		ClientTickEvents.END_CLIENT_TICK.register(Fullbright::tick);
	}

	private static void tick(Minecraft minecraft) {
		while (KEY.consumeClick()) {
			boolean enabled = !FullbrightConfig.ENABLED.get();
			FullbrightConfig.ENABLED.set(enabled);
			EsadConfig.CONFIG.save();
			if (minecraft.player != null && FullbrightConfig.MESSAGE.get()) {
				minecraft.player.sendOverlayMessage(Component.translatable(enabled ? "message.esad.fullbright.on" : "message.esad.fullbright.off"));
			}
		}
	}

	/** How strong the fake night vision is (0 to 1), 0 when fullbright is off. */
	public static float strength() {
		return FullbrightConfig.ENABLED.get() ? FullbrightConfig.STRENGTH.get() / 100f : 0;
	}
}

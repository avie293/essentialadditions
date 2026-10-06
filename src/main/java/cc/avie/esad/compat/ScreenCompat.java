package cc.avie.esad.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jspecify.annotations.Nullable;

// Version specific: before 26.2 the screen belongs to Minecraft itself
public final class ScreenCompat {
	private ScreenCompat() {
	}

	public static void setScreen(@Nullable Screen screen) {
		Minecraft.getInstance().gui.setScreen(screen);
	}

	public static @Nullable Screen currentScreen() {
		return Minecraft.getInstance().gui.screen();
	}
}

package cc.avie.esad.config;

import cc.avie.esad.feature.hud.Keystrokes;
import cc.avie.esad.gui.FeatureIcon;
import me.avie29.tabbylib.api.ConfigCategory;
import me.avie29.tabbylib.api.HudPosition;
import me.avie29.tabbylib.api.option.BooleanOption;
import me.avie29.tabbylib.api.option.ColorOption;
import net.minecraft.world.item.Items;

public final class KeystrokesConfig implements FeatureConfig {
	public static final KeystrokesConfig INSTANCE = new KeystrokesConfig();

	public static final BooleanOption ENABLED = BooleanOption.builder("keystrokesEnabled", false).hidden().build();

	public static final BooleanOption MOUSE = BooleanOption.builder("keystrokesMouse", true).build();
	public static final BooleanOption CPS = BooleanOption.builder("keystrokesCps", true).dependsOn(MOUSE).build();
	public static final BooleanOption SPACE = BooleanOption.builder("keystrokesSpace", true).build();

	public static final ColorOption KEY_COLOR = ColorOption.builder("keystrokesKeyColor", 0x80000000).alpha().build();
	public static final ColorOption PRESSED_COLOR = ColorOption.builder("keystrokesPressedColor", 0xC0FFFFFF).alpha().build();
	public static final ColorOption TEXT_COLOR = ColorOption.builder("keystrokesTextColor", 0xFFFFFFFF).build();
	public static final ColorOption PRESSED_TEXT_COLOR = ColorOption.builder("keystrokesPressedTextColor", 0xFF000000).build();

	public static final HudStyle STYLE = new HudStyle("keystrokes", HudPosition.of(HudPosition.START, HudPosition.CENTER, 4, 0),
		Keystrokes::preview, false);

	private KeystrokesConfig() {
	}

	@Override
	public String key() {
		return "keystrokes";
	}

	@Override
	public ConfigCategory category() {
		return new ConfigCategory(this.key()).file("esad/keystrokes")
			.add(ENABLED, MOUSE, CPS, SPACE)
			.group("keystrokesColors", group -> group.add(KEY_COLOR, PRESSED_COLOR, TEXT_COLOR, PRESSED_TEXT_COLOR))
			.group("keystrokesAppearance", STYLE::addTo);
	}

	@Override
	public BooleanOption toggle() {
		return ENABLED;
	}

	@Override
	public FeatureIcon icon() {
		return FeatureIcon.of(Items.LEVER, "block/lever");
	}
}

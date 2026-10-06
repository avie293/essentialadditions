package cc.avie.esad.config;

import cc.avie.esad.feature.togglesprint.ToggleSprint;
import cc.avie.esad.gui.FeatureIcon;
import me.avie29.tabbylib.api.ConfigCategory;
import me.avie29.tabbylib.api.HudPosition;
import me.avie29.tabbylib.api.option.BooleanOption;
import me.avie29.tabbylib.api.option.ColorOption;
import me.avie29.tabbylib.api.option.KeyBindOption;
import net.minecraft.world.item.Items;

public final class ToggleSprintConfig implements FeatureConfig {
	public static final ToggleSprintConfig INSTANCE = new ToggleSprintConfig();

	public static final BooleanOption ENABLED = BooleanOption.builder("toggleSprintEnabled", false).hidden().build();

	public static final KeyBindOption SPRINT_KEY = KeyBindOption.builder("toggleSprintKey", ToggleSprint.SPRINT_KEY).build();
	public static final KeyBindOption SNEAK_KEY = KeyBindOption.builder("toggleSneakKey", ToggleSprint.SNEAK_KEY).build();
	public static final BooleanOption ALWAYS_SPRINT = BooleanOption.builder("toggleSprintAlways", false).build();
	public static final BooleanOption REMEMBER = BooleanOption.builder("toggleSprintRemember", false).build();

	public static final BooleanOption HUD = BooleanOption.builder("toggleSprintHud", true).build();
	public static final BooleanOption HUD_VANILLA = BooleanOption.builder("toggleSprintHudVanilla", false).dependsOn(HUD).build();
	public static final ColorOption HUD_COLOR = ColorOption.builder("toggleSprintHudColor", 0xFFFFFFFF).dependsOn(HUD).build();
	public static final HudStyle STYLE = new HudStyle("toggleSprintHud", HudPosition.of(HudPosition.CENTER, HudPosition.START, 0, 4),
		ToggleSprint::preview, false);

	/** The toggled states, saved when "remember" is on. Not shown in the list. */
	public static final BooleanOption SPRINT_STATE = BooleanOption.builder("toggleSprintState", false).hidden().build();
	public static final BooleanOption SNEAK_STATE = BooleanOption.builder("toggleSneakState", false).hidden().build();

	private ToggleSprintConfig() {
	}

	@Override
	public String key() {
		return "toggleSprint";
	}

	@Override
	public ConfigCategory category() {
		return new ConfigCategory(this.key()).file("esad/togglesprint")
			.add(ENABLED, SPRINT_KEY, SNEAK_KEY, ALWAYS_SPRINT, REMEMBER, SPRINT_STATE, SNEAK_STATE)
			.group("toggleSprintHudGroup", group -> {
				group.add(HUD, HUD_VANILLA, HUD_COLOR);
				STYLE.addTo(group);
			});
	}

	@Override
	public BooleanOption toggle() {
		return ENABLED;
	}

	@Override
	public FeatureIcon icon() {
		return FeatureIcon.of(Items.FEATHER);
	}
}

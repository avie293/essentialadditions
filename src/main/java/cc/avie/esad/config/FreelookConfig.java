package cc.avie.esad.config;

import cc.avie.esad.feature.freelook.Freelook;
import cc.avie.esad.feature.zoom.ZoomMode;
import cc.avie.esad.gui.FeatureIcon;
import me.avie29.tabbylib.api.ConfigCategory;
import me.avie29.tabbylib.api.option.BooleanOption;
import me.avie29.tabbylib.api.option.EnumOption;
import me.avie29.tabbylib.api.option.KeyBindOption;
import net.minecraft.world.item.Items;

public final class FreelookConfig implements FeatureConfig {
	public static final FreelookConfig INSTANCE = new FreelookConfig();

	public static final BooleanOption ENABLED = BooleanOption.builder("freelookEnabled", true).hidden().build();

	public static final KeyBindOption KEY = KeyBindOption.builder("freelookKey", Freelook.KEY).build();
	/** Same modes as the zoom: hold the key or press it once. */
	public static final EnumOption<ZoomMode> MODE = EnumOption.builder("freelookMode", ZoomMode.HOLD).build();
	public static final BooleanOption THIRD_PERSON = BooleanOption.builder("freelookThirdPerson", true).build();
	public static final BooleanOption INVERT_PITCH = BooleanOption.builder("freelookInvertPitch", false).build();

	private FreelookConfig() {
	}

	@Override
	public String key() {
		return "freelook";
	}

	@Override
	public ConfigCategory category() {
		return new ConfigCategory(this.key()).file("esad/freelook")
			.add(ENABLED, KEY, MODE, THIRD_PERSON, INVERT_PITCH);
	}

	@Override
	public BooleanOption toggle() {
		return ENABLED;
	}

	@Override
	public FeatureIcon icon() {
		return FeatureIcon.of(Items.ENDER_EYE);
	}
}

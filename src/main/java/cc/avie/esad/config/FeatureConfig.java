package cc.avie.esad.config;

import cc.avie.esad.gui.FeatureIcon;
import me.avie29.tabbylib.api.ConfigCategory;
import me.avie29.tabbylib.api.option.BooleanOption;

/**
 * Settings of one feature: its category (with its own file in config/esad/), the switch on its card in the
 * settings window and the icon of the card. Newer features implement this, so EsadConfig only lists them.
 */
public interface FeatureConfig {
	/** Key of the category, also used in translation keys. */
	String key();

	/** Builds the category with all options, called once while the config is built. */
	ConfigCategory category();

	/** Turns the whole feature on or off, shown as switch on the card instead of a row. */
	BooleanOption toggle();

	FeatureIcon icon();
}

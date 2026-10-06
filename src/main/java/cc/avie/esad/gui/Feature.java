package cc.avie.esad.gui;

import me.avie29.tabbylib.api.ConfigCategory;
import me.avie29.tabbylib.api.option.BooleanOption;
import net.minecraft.network.chat.Component;

/**
 * One card in the settings window: a config category with an icon and the toggle that turns the feature
 * on or off. The toggle is hidden in the option list, it is the switch on the card.
 */
public record Feature(ConfigCategory category, FeatureIcon icon, BooleanOption toggle) {
	public Component name() {
		return this.category.getName();
	}

	public Component description() {
		return Component.translatable("esad.feature." + this.category.getKey() + ".description");
	}
}

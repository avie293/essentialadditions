package cc.avie.esad.config;

import cc.avie.esad.feature.hud.ArmorHud;
import cc.avie.esad.gui.FeatureIcon;
import me.avie29.tabbylib.api.ConfigCategory;
import me.avie29.tabbylib.api.HudPosition;
import me.avie29.tabbylib.api.option.BooleanOption;
import me.avie29.tabbylib.api.option.EnumOption;
import me.avie29.tabbylib.api.option.IntOption;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

public final class ArmorHudConfig implements FeatureConfig {
	public static final ArmorHudConfig INSTANCE = new ArmorHudConfig();

	public enum Layout {
		VERTICAL,
		HORIZONTAL
	}

	public enum DurabilityText {
		NUMBER,
		PERCENT,
		NONE
	}

	public static final BooleanOption ENABLED = BooleanOption.builder("armorHudEnabled", false).hidden().build();

	public static final EnumOption<Layout> LAYOUT = EnumOption.builder("armorHudLayout", Layout.VERTICAL).build();
	public static final BooleanOption HANDS = BooleanOption.builder("armorHudHands", true).build();
	public static final BooleanOption ITEM_COUNT = BooleanOption.builder("armorHudItemCount", true).dependsOn(HANDS).build();
	public static final EnumOption<DurabilityText> DURABILITY = EnumOption.builder("armorHudDurability", DurabilityText.NUMBER).build();
	public static final BooleanOption DURABILITY_BAR = BooleanOption.builder("armorHudDurabilityBar", true).build();

	public static final IntOption WARNING_PERCENT = IntOption.builder("armorHudWarningPercent", 10)
		.slider(0, 50, 5).formatter(value -> Component.literal(value + "%")).build();
	public static final BooleanOption WARNING_TEXT = BooleanOption.builder("armorHudWarningText", true).build();

	public static final HudStyle STYLE = new HudStyle("armorHud", HudPosition.of(HudPosition.END, HudPosition.END, -4, -24), ArmorHud::preview);

	private ArmorHudConfig() {
	}

	@Override
	public String key() {
		return "armorHud";
	}

	@Override
	public ConfigCategory category() {
		return new ConfigCategory(this.key()).file("esad/armorhud")
			.add(ENABLED, LAYOUT, HANDS, ITEM_COUNT, DURABILITY, DURABILITY_BAR)
			.group("armorHudWarning", group -> group.add(WARNING_PERCENT, WARNING_TEXT))
			.group("armorHudAppearance", STYLE::addTo);
	}

	@Override
	public BooleanOption toggle() {
		return ENABLED;
	}

	@Override
	public FeatureIcon icon() {
		return FeatureIcon.of(Items.IRON_CHESTPLATE);
	}
}

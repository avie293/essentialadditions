package cc.avie.esad.config;

import cc.avie.esad.feature.sort.InventorySort;
import cc.avie.esad.gui.FeatureIcon;
import me.avie29.tabbylib.api.ConfigCategory;
import me.avie29.tabbylib.api.option.BooleanOption;
import me.avie29.tabbylib.api.option.EnumOption;
import me.avie29.tabbylib.api.option.KeyBindOption;
import net.minecraft.world.item.Items;

public final class InventorySortConfig implements FeatureConfig {
	public static final InventorySortConfig INSTANCE = new InventorySortConfig();

	public enum Order {
		/** Alphabetical by item id, which keeps items of one kind (e.g. all logs) close together. */
		ID,
		/** Alphabetical by the shown name. */
		NAME,
		/** Biggest stacks first. */
		COUNT
	}

	public static final BooleanOption ENABLED = BooleanOption.builder("inventorySortEnabled", true).hidden().build();

	public static final KeyBindOption KEY = KeyBindOption.builder("inventorySortKey", InventorySort.KEY).build();
	public static final BooleanOption MIDDLE_CLICK = BooleanOption.builder("inventorySortMiddleClick", true).build();
	public static final EnumOption<Order> ORDER = EnumOption.builder("inventorySortOrder", Order.ID).build();
	public static final BooleanOption HOTBAR = BooleanOption.builder("inventorySortHotbar", false).build();
	public static final BooleanOption MERGE = BooleanOption.builder("inventorySortMerge", true).build();

	private InventorySortConfig() {
	}

	@Override
	public String key() {
		return "inventorySort";
	}

	@Override
	public ConfigCategory category() {
		return new ConfigCategory(this.key()).file("esad/inventorysort")
			.add(ENABLED, KEY, MIDDLE_CLICK, ORDER, HOTBAR, MERGE);
	}

	@Override
	public BooleanOption toggle() {
		return ENABLED;
	}

	@Override
	public FeatureIcon icon() {
		return FeatureIcon.of(Items.BUNDLE);
	}
}

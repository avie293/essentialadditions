package cc.avie.esad.config;

import cc.avie.esad.gui.FeatureIcon;
import me.avie29.tabbylib.api.ConfigCategory;
import me.avie29.tabbylib.api.option.BooleanOption;
import me.avie29.tabbylib.api.option.IntOption;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

public final class SleepReminderConfig implements FeatureConfig {
	public static final SleepReminderConfig INSTANCE = new SleepReminderConfig();

	public static final BooleanOption ENABLED = BooleanOption.builder("sleepReminderEnabled", false).hidden().build();

	public static final BooleanOption NIGHT = BooleanOption.builder("sleepReminderNight", true).build();
	public static final BooleanOption THUNDER = BooleanOption.builder("sleepReminderThunder", true).build();
	public static final BooleanOption PHANTOMS = BooleanOption.builder("sleepReminderPhantoms", true).build();
	public static final IntOption PHANTOM_NIGHTS = IntOption.builder("sleepReminderPhantomNights", 3)
		.slider(1, 5, 1).formatter(value -> Component.translatable("config.esad.sleepReminderPhantomNights.value", value))
		.dependsOn(PHANTOMS).build();
	public static final BooleanOption SOUND = BooleanOption.builder("sleepReminderSound", true).build();
	public static final BooleanOption CHAT = BooleanOption.builder("sleepReminderChat", false).build();

	private SleepReminderConfig() {
	}

	@Override
	public String key() {
		return "sleepReminder";
	}

	@Override
	public ConfigCategory category() {
		return new ConfigCategory(this.key()).file("esad/sleepreminder")
			.add(ENABLED, NIGHT, THUNDER, PHANTOMS, PHANTOM_NIGHTS, SOUND, CHAT);
	}

	@Override
	public BooleanOption toggle() {
		return ENABLED;
	}

	@Override
	public FeatureIcon icon() {
		return FeatureIcon.of(Items.PHANTOM_MEMBRANE);
	}
}

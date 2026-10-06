package cc.avie.esad.config;

import cc.avie.esad.gui.FeatureIcon;
import me.avie29.tabbylib.api.ConfigCategory;
import me.avie29.tabbylib.api.option.BooleanOption;
import me.avie29.tabbylib.api.option.ColorOption;
import me.avie29.tabbylib.api.option.IntOption;
import net.minecraft.world.item.Items;

public final class ChatConfig implements FeatureConfig {
	public static final ChatConfig INSTANCE = new ChatConfig();

	public static final BooleanOption ENABLED = BooleanOption.builder("chatEnabled", false).hidden().build();

	public static final BooleanOption TIMESTAMPS = BooleanOption.builder("chatTimestamps", true).build();
	public static final BooleanOption TIMESTAMP_SECONDS = BooleanOption.builder("chatTimestampSeconds", false).dependsOn(TIMESTAMPS).build();
	public static final ColorOption TIMESTAMP_COLOR = ColorOption.builder("chatTimestampColor", 0xFFAAAAAA).dependsOn(TIMESTAMPS).build();

	public static final IntOption HISTORY = IntOption.builder("chatHistory", 500).slider(100, 2000, 100).build();

	public static final BooleanOption STACK = BooleanOption.builder("chatStack", true).build();
	public static final ColorOption STACK_COLOR = ColorOption.builder("chatStackColor", 0xFF808080).dependsOn(STACK).build();

	private ChatConfig() {
	}

	@Override
	public String key() {
		return "chat";
	}

	@Override
	public ConfigCategory category() {
		return new ConfigCategory(this.key()).file("esad/chat")
			.add(ENABLED, TIMESTAMPS, TIMESTAMP_SECONDS, TIMESTAMP_COLOR, HISTORY, STACK, STACK_COLOR);
	}

	@Override
	public BooleanOption toggle() {
		return ENABLED;
	}

	@Override
	public FeatureIcon icon() {
		return FeatureIcon.of(Items.OAK_SIGN);
	}
}

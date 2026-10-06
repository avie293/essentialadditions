package cc.avie.esad.feature.chat;

import cc.avie.esad.config.ChatConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jspecify.annotations.Nullable;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/** Timestamps, a longer chat history and repeated messages stacked into one line with "(x3)". */
public final class ChatTweaks {
	private static final DateTimeFormatter MINUTES = DateTimeFormatter.ofPattern("HH:mm");
	private static final DateTimeFormatter SECONDS = DateTimeFormatter.ofPattern("HH:mm:ss");

	private static @Nullable String lastText;
	private static int repeats;

	private ChatTweaks() {
	}

	/** Vanilla keeps 100 messages, more are allowed while the feature is on. */
	public static int history(int original) {
		return ChatConfig.ENABLED.get() ? Math.max(original, ChatConfig.HISTORY.get()) : original;
	}

	/**
	 * Whether this message repeats the last one. The caller then removes the last message, so only the stacked
	 * line stays. Must be called once per message, before {@link #decorate}.
	 */
	public static boolean isRepeat(Component message) {
		String text = message.getString();
		boolean repeat = ChatConfig.ENABLED.get() && ChatConfig.STACK.get() && text.equals(lastText);
		repeats = repeat ? repeats + 1 : 1;
		lastText = text;
		return repeat;
	}

	/** Adds "(x3)" for repeated messages and the timestamp in front. */
	public static Component decorate(Component message) {
		if (!ChatConfig.ENABLED.get()) {
			return message;
		}
		MutableComponent result = Component.empty();
		if (ChatConfig.TIMESTAMPS.get()) {
			String time = LocalTime.now().format(ChatConfig.TIMESTAMP_SECONDS.get() ? SECONDS : MINUTES);
			int color = ChatConfig.TIMESTAMP_COLOR.get() & 0xFFFFFF;
			result.append(Component.literal("[" + time + "] ").withStyle(style -> style.withColor(color)));
		}
		result.append(message);
		if (repeats > 1) {
			int color = ChatConfig.STACK_COLOR.get() & 0xFFFFFF;
			result.append(Component.literal(" (x" + repeats + ")").withStyle(style -> style.withColor(color)));
		}
		return result;
	}

	/** Forgets the last message, e.g. when the chat is cleared. */
	public static void reset() {
		lastText = null;
		repeats = 0;
	}
}

package cc.avie.esad.feature.tooltips;

import net.minecraft.ChatFormatting;

/** Color of the changing part of the durability line (numbers, bar or text). */
public enum DurabilityColor {
	/** Green, gold or red depending on the remaining durability. */
	VARYING,
	GOLD,
	/** Same gray as the rest of the line. */
	GRAY;

	public ChatFormatting color(int durability, int maxDurability) {
		return switch (this) {
			case VARYING -> durability >= 0.4f * maxDurability ? ChatFormatting.GREEN
				: durability >= 0.1f * maxDurability ? ChatFormatting.GOLD : ChatFormatting.RED;
			case GOLD -> ChatFormatting.GOLD;
			case GRAY -> ChatFormatting.GRAY;
		};
	}
}

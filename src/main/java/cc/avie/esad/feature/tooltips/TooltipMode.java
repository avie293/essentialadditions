package cc.avie.esad.feature.tooltips;

import net.minecraft.client.Minecraft;

/** When an extra tooltip line is shown. */
public enum TooltipMode {
	ALWAYS,
	/** Only while Shift is held down. */
	SHIFT,
	OFF;

	/** Whether the info is shown right now. */
	public boolean isVisible() {
		return this == ALWAYS || (this == SHIFT && Minecraft.getInstance().hasShiftDown());
	}

	/** Whether the info exists but needs Shift, used for the "hold Shift" hint. */
	public boolean isHiddenByShift() {
		return this == SHIFT && !Minecraft.getInstance().hasShiftDown();
	}
}

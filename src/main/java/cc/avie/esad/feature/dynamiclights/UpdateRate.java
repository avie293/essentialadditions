package cc.avie.esad.feature.dynamiclights;

/** How often the light sources are updated. Every update rebuilds the chunk sections around moving lights. */
public enum UpdateRate {
	/** Every 10 ticks (0.5 seconds). */
	SLOW(10),
	/** Every 5 ticks (0.25 seconds). */
	FAST(5),
	/** Every tick. */
	REALTIME(1);

	private final int interval;

	UpdateRate(int interval) {
		this.interval = interval;
	}

	/** Ticks between two updates. */
	public int interval() {
		return this.interval;
	}
}

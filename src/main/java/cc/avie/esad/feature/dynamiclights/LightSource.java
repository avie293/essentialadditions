package cc.avie.esad.feature.dynamiclights;

/** A moving light at a position, e.g. a player holding a torch. */
record LightSource(int entityId, double x, double y, double z, int luminance) {
	/** Whether the light changed enough to rebuild the chunk sections around it. */
	boolean differsFrom(LightSource other) {
		double dx = this.x - other.x;
		double dy = this.y - other.y;
		double dz = this.z - other.z;
		return this.luminance != other.luminance || dx * dx + dy * dy + dz * dz > 0.01;
	}
}

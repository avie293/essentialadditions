package cc.avie.esad.feature.zoom;

/** Curve of the zoom in / out animation. All curves are symmetric, so zooming out mirrors zooming in. */
public enum ZoomEasing {
	LINEAR {
		@Override
		public double apply(double t) {
			return t;
		}
	},
	SINE {
		@Override
		public double apply(double t) {
			return -(Math.cos(Math.PI * t) - 1) / 2;
		}
	},
	QUAD {
		@Override
		public double apply(double t) {
			return t < 0.5 ? 2 * t * t : 1 - Math.pow(-2 * t + 2, 2) / 2;
		}
	},
	CUBIC {
		@Override
		public double apply(double t) {
			return t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2;
		}
	},
	EXPO {
		@Override
		public double apply(double t) {
			if (t <= 0 || t >= 1) {
				return t <= 0 ? 0 : 1;
			}
			return t < 0.5 ? Math.pow(2, 20 * t - 10) / 2 : (2 - Math.pow(2, -20 * t + 10)) / 2;
		}
	};

	/** Maps the linear progress (0 to 1) to the eased progress (0 to 1). */
	public abstract double apply(double t);
}

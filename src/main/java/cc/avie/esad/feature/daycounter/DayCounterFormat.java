package cc.avie.esad.feature.daycounter;

public enum DayCounterFormat {
	/** "Day 12" */
	DAY,
	/** "12" */
	NUMBER,
	/** "Day 12 - 06:30" */
	DAY_AND_TIME,
	/** Own text with {day} and {time} */
	CUSTOM
}

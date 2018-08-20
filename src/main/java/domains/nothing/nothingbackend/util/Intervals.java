package domains.nothing.nothingbackend.util;

/**
 * Represents MySQL intervals
 */
public enum Intervals {
	SECOND(1),
	MINUTE(60),
	HOUR(3600),
	DAY(24 * 3600),
	MONTH(30 * 24 * 3600),
	YEAR(12 * 30 * 24 * 3600);

	private int seconds;

	Intervals(int seconds) {
		this.seconds = seconds;
	}

	/**
	 * @param count the amount of the current interval
	 * @return the interval in seconds
	 */
	public double inSeconds(double count) {
		return seconds * count;
	}

	/**
	 * @param seconds the amount of seconds
	 * @return the count of second as if it were represented in this unit
	 */
	private double fromSeconds(double seconds) {
		return seconds / this.seconds;
	}

	/**
	 * @param to new interval
	 * @param count the amount of the current interval
	 * @return the interval in seconds
	 */
	public double to(Intervals to, double count) {
		return to.fromSeconds(inSeconds(count));
	}

	/**
	 * @param to new interval
	 * @param count the amount of the current interval
	 * @return the interval in seconds
	 */
	public double from(Intervals to, double count) {
		return fromSeconds(to.inSeconds(count));
	}
}

package org.reldb.toolbox.utilities;

public interface ProgressIndicator {
	/** Initialise the indicator with the number of steps. Set steps to -1 if unknown number of steps. */
	void initialise(int steps);

	/** Move the indicator to the given 0-based step. additionalInformation supplies additional progress text; null if not needed. */
	void move(int step, String additionalInformation);
	
	/** Get the current indicator position. */
	int getValue();

	/** Get the most recent non-null additionalInformation. */
	String getLastMessage();
}

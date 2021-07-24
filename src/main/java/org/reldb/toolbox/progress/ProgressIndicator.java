package org.reldb.toolbox.progress;

/**
 * Definition of a progress indicator.
 */
public interface ProgressIndicator {
    /**
     * Initialise the indicator with the number of steps.
     *
     * @param steps Number of steps or -1 if unknown. Should be non-zero.
     */
    void initialise(int steps);

    /**
     * Move the indicator to the given 0-based step. additionalInformation supplies additional progress text; null if not needed.
     *
     * @param step Move the indicator to this step number.
     * @param additionalInformation Provide optional additional information about this step.
     */
    void move(int step, String additionalInformation);

    /**
     * Get the current indicator position.
     *
     * @return Current indicator position.
     */
    int getValue();

    /**
     * Get the most recent non-null additional information.
     *
     * @return Most recent non-null additional information string.
     */
    String getLastMessage();
}

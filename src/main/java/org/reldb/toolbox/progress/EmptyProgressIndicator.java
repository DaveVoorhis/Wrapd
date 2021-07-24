package org.reldb.toolbox.progress;

/**
 * A silent ProgressIndicator, for where a ProgressIndicator is required but you don't want to know.
 */
public class EmptyProgressIndicator implements ProgressIndicator {
    private int position;
    private String lastMessage;

    @Override
    public void initialise(int steps) {
        position = 0;
        lastMessage = "";
    }

    @Override
    public void move(int step, String additionalInformation) {
        position = step;
        lastMessage = additionalInformation;
    }

    @Override
    public int getValue() {
        return position;
    }

    @Override
    public String getLastMessage() {
        return lastMessage;
    }
}

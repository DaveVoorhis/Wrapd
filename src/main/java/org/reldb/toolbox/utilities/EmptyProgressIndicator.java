package org.reldb.toolbox.utilities;

public class EmptyProgressIndicator implements ProgressIndicator {
    private int position;
    private String lastMessage;

    public void initialise(int steps) {
        position = 0;
        lastMessage = "";
    }

    public void move(int step, String additionalInformation) {
        position = step;
        lastMessage = additionalInformation;
    }

    public int getValue() {
        return position;
    }

    public String getLastMessage() {
        return lastMessage;
    }
}

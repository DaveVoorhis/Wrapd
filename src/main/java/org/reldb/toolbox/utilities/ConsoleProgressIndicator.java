package org.reldb.toolbox.utilities;

public class ConsoleProgressIndicator implements ProgressIndicator {
    private int stepCount = 0;
    private int position = 0;
    private String lastMessage = "";
    private String messagePrefix = "";

    public ConsoleProgressIndicator(String messagePrefix) {
        this.messagePrefix = messagePrefix;
    }

    public ConsoleProgressIndicator() {
        this("");
    }

    private String getPercent() {
        return (double)position / (double)stepCount * 100.0 + "%";
    }

    @Override
    public void initialise(int steps) {
        stepCount = steps;
    }

    @Override
    public void move(int step, String additionalInformation) {
        position = step;
        lastMessage = additionalInformation;
        System.out.println(messagePrefix + additionalInformation + ": " + getPercent() + " complete.");
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

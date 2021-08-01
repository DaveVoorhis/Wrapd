package org.reldb.toolbox.events;

/**
 * A generic listener lambda for use with EventHandler.
 *
 * @param <Event> The event type.
 */
@FunctionalInterface
public interface EventListener<Event> {
	/**
	 * Send a notification to a listener.
	 *
	 * @param event The notification being sent.
	 */
	void notify(Event event);
}

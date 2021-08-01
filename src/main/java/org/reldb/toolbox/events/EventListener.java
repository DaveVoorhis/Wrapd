package org.reldb.toolbox.events;

/**
 * A generic listener lambda for use with EventHandler.
 *
 * @param <Event> The event type.
 */
@FunctionalInterface
public interface EventListener<Event> {
	void notify(Event event);
}

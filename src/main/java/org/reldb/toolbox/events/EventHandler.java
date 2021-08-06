package org.reldb.toolbox.events;

import java.util.Vector;

/**
 * A generic event handler. Typically, declared in the event-generating class like the following:
 * <blockquote>
 *    <pre>public final EventHandler&lt;MyEvent&gt; myEvent = new EventHandler&lt;&gt;();</pre>
 * </blockquote>
 * To distribute a message to listeners:
 * <blockquote>
 *    <pre>myEvent.distribute(new MyEvent());</pre>
 * </blockquote>
 * To add a listener:
 * <blockquote>
 *    <pre>myEvent.addListener(event -> { do something here });</pre>
 * </blockquote>
 * @param <Event> The event type.
 */
public class EventHandler<Event> {

	private final Vector<EventListener<Event>> listeners = new Vector<>();
	
	/** 
	 * Add a listener.
	 * 
	 * @param listener The listener to add.
	 */
	public void addListener(EventListener<Event> listener) {
		listeners.add(listener);
	}
	
	/**
	 * Remove a listener.
	 * 
	 * @param listener The listener to remove.
	 */
	public void removeListener(EventListener<Event> listener) {
		listeners.remove(listener);
	}

	/**
	 * Distribute an event to all listeners.
	 * 
	 * @param event The event to send to all listeners.
	 */
	public void distribute(Event event) {
		listeners.forEach(listener -> listener.notify(event));
	}

}

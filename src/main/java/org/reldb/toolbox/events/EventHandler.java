package org.reldb.toolbox.events;

import java.util.Vector;

/**
 * A generic event handler. Typically, declared like the following:
 * 
 *    public final EventHandler<MyEvent> myEvent = new EventHandler<>();
 *    
 * To distribute a message to listeners:
 * 
 *    myEvent.distribute(new MyEvent());
 *    
 * To add a listener:
 * 
 *    myEvent.addListener(event -> { do something here });
 *
 * @param <Event> The event type.
 */
public class EventHandler<Event> {

	private Vector<EventListener<Event>> listeners = new Vector<>();
	
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

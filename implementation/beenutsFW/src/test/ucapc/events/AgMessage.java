package test.ucapc.events;

import java.util.Collection;


public class AgMessage extends APCEvent {

	public AgMessage(Object message, Collection<String> recipients) {
		super( APCEvent.E_AG_MESSAGE );
		this.message = message;
	}
	
	public final Object message;

}

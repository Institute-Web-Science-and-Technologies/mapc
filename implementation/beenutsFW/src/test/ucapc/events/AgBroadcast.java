package test.ucapc.events;

public class AgBroadcast extends APCEvent {

	public AgBroadcast(Object message) {
		super( APCEvent.E_AG_BROADCAST );
		this.message = message;
	}

	public final Object message;
}

package rsb.rsocket.bidirectional;

import lombok.Data;

@Data
public class ClientHealthState {

	public static final String STARTED = "started";

	public static final String STOPPED = "stopped";

	private final String state;

	public ClientHealthState() {
		this(STARTED);
	}

	public ClientHealthState(String s) {
		this.state = s;
	}

}

package rsb.rsocket.bidirectional.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.Lifecycle;
import org.springframework.messaging.rsocket.RSocketRequester;
import rsb.rsocket.bidirectional.GreetingRequest;
import rsb.rsocket.bidirectional.GreetingResponse;

@Log4j2
@RequiredArgsConstructor
class Client implements Lifecycle {

	private final RSocketRequester rSocketRequester;

	private final String uid;

	@Override
	public void start() {

		this.rSocketRequester//
				.route("greetings")//
				.data(new GreetingRequest("Client #" + this.uid))//
				.retrieveFlux(GreetingResponse.class)//
				.subscribe(log::info);
	}

	@Override
	public void stop() {
	}

	@Override
	public boolean isRunning() {
		return true;
	}

}

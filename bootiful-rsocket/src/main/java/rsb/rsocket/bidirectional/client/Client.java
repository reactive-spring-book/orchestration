package rsb.rsocket.bidirectional.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Flux;
import rsb.rsocket.bidirectional.GreetingRequest;
import rsb.rsocket.bidirectional.GreetingResponse;

@Log4j2
@RequiredArgsConstructor
class Client {

	private final RSocketRequester rSocketRequester;

	private final String uid;

	Flux<GreetingResponse> start() {
		return this.rSocketRequester//
				.route("greetings")//
				.data(new GreetingRequest("Client #" + this.uid))//
				.retrieveFlux(GreetingResponse.class);
	}

}

package rsb.rsocket.encoding.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Component;
import rsb.rsocket.bidirectional.GreetingRequest;
import rsb.rsocket.encoding.GreetingResponse;

@Log4j2
@Component
@RequiredArgsConstructor
class Client implements ApplicationListener<ApplicationReadyEvent> {

	private final RSocketRequester rSocketRequester;

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		this.rSocketRequester//
				.route("greetings")//
				.data(new GreetingRequest("Spring fans"))//
				.retrieveMono(GreetingResponse.class)//
				.subscribe(log::info);
	}

}

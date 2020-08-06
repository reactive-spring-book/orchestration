package rsb.rsocket.errors.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
class Client implements ApplicationListener<ApplicationReadyEvent> {

	private final RSocketRequester rSocketRequester;

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		this.rSocketRequester//
				.route("greetings")//
				.data("Spring Fans")//
				.retrieveFlux(String.class)//
				.doOnError(log::error)//
				.subscribe(log::info);
	}

}

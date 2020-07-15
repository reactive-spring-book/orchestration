package rsb.rsocket.requestresponse.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

@Log4j2
@Component
@RequiredArgsConstructor
class Client implements ApplicationListener<ApplicationReadyEvent> {

	private final RSocketRequester rSocketRequester;

	@Override
	public void onApplicationEvent(ApplicationReadyEvent are) {
		var map = Map.<String, Object>of("client", Client.class.getName(), "date",
				new Date());
		this.rSocketRequester//
				.route("greeting")//
				.data("Reactive Spring")//
				.retrieveMono(String.class)//
				.subscribe(System.out::println);
	}

}

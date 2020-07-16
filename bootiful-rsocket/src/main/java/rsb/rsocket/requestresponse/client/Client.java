package rsb.rsocket.requestresponse.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.Map;

@Log4j2
@Component
@RequiredArgsConstructor
class Client implements ApplicationListener<ApplicationReadyEvent> {

	private final RSocketRequester rSocketRequester;

	@Override
	public void onApplicationEvent(ApplicationReadyEvent are) {
		var rsocket = this.rSocketRequester.rsocket(); // much easier!
		var availability = rsocket.availability();
		Assert.isTrue(availability == 1.0,
				"the availability must be 1.0 in order to proceed!");
		log.info("the data mimeType is " + this.rSocketRequester.dataMimeType());
		log.info("the metadata mimeType is " + this.rSocketRequester.metadataMimeType());

		this.rSocketRequester//
				.route("greeting")//
				.data("Reactive Spring")//
				.retrieveMono(String.class)//
				.subscribe(System.out::println);
	}

}

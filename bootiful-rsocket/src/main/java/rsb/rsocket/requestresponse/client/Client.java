package rsb.rsocket.requestresponse.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Log4j2
@Component
@RequiredArgsConstructor
class Client implements ApplicationListener<ApplicationReadyEvent> {

	private final RSocketRequester rSocketRequester;

	@Override
	public void onApplicationEvent(ApplicationReadyEvent are) {
		var rsocket = this.rSocketRequester.rsocket(); // <1>
		var availability = rsocket.availability();// <2>
		Assert.isTrue(availability == 1.0,
				"the availability must be 1.0 in order to proceed!");
		log.info("the data mimeType is " + this.rSocketRequester.dataMimeType());// <3>
		log.info("the metadata mimeType is " + this.rSocketRequester.metadataMimeType());
		this.rSocketRequester//
				.route("greeting")// <4>
				.data("Reactive Spring")// <5>
				.retrieveMono(String.class)// <6>
				.subscribe(System.out::println);
	}

}

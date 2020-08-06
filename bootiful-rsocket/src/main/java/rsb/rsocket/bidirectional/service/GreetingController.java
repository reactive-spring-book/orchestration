package rsb.rsocket.bidirectional.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rsb.rsocket.bidirectional.ClientHealthState;
import rsb.rsocket.bidirectional.GreetingRequest;
import rsb.rsocket.bidirectional.GreetingResponse;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.Stream;

@Log4j2
@Controller
@RequiredArgsConstructor
class GreetingController {

	@MessageMapping("greetings")
	Flux<GreetingResponse> greetings(RSocketRequester client, // <1>
			@Payload GreetingRequest greetingRequest) {

		var clientHealthStateFlux = client//
				.route("health")// <2>
				.data(Mono.empty())//
				.retrieveFlux(ClientHealthState.class)//
				.filter(chs -> chs.getState().equalsIgnoreCase(ClientHealthState.STOPPED))// <3>
				.doOnNext(chs -> log.info(chs.toString()));

		var replyPayloadFlux = Flux// <4>
				.fromStream(Stream.generate(() -> new GreetingResponse("Hello, "
						+ greetingRequest.getName() + " @ " + Instant.now() + "!")))
				.delayElements(
						Duration.ofSeconds(Math.max(3, (long) (Math.random() * 10))));

		return replyPayloadFlux//
				.takeUntilOther(clientHealthStateFlux)// <5>
				.doOnNext(gr -> log.info(gr.toString()));
	}

}

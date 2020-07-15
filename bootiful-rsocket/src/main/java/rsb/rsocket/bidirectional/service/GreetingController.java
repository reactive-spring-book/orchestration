package rsb.rsocket.bidirectional.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rsb.rsocket.bidirectional.ClientHealthState;
import rsb.rsocket.bidirectional.GreetingRequest;
import rsb.rsocket.bidirectional.GreetingResponse;

import java.util.Map;

import static rsb.rsocket.bidirectional.ClientHealthState.STOPPED;

@Log4j2
@Controller
@RequiredArgsConstructor
class GreetingController {

	private final GreetingService greetingService;

	@MessageMapping("greetings")
	Flux<GreetingResponse> greetings(RSocketRequester client,
			@Payload GreetingRequest greetingRequest,
			@Headers Map<String, Object> headers) {

		log.info("greetings(" + greetingRequest.toString() + ")");
		headers.forEach((k, v) -> log.info(k + '=' + v));
		var clientHealthStateFlux = client//
				.route("health")//
				.data(Mono.empty())//
				.retrieveFlux(ClientHealthState.class)//
				.filter(chs -> chs.getState().equalsIgnoreCase(STOPPED))//
				.doOnNext(chs -> log.info(chs.toString()));
		var replyPayloadFlux = this.greetingService.greet(greetingRequest);
		return replyPayloadFlux//
				.takeUntilOther(clientHealthStateFlux)//
				.doOnNext(gr -> log.info(gr.toString()));
	}

}

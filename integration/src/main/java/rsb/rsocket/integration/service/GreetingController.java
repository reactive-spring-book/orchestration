package rsb.rsocket.integration.service;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import rsb.rsocket.integration.GreetingRequest;
import rsb.rsocket.integration.GreetingResponse;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.Stream;

@Controller
class GreetingController {

	@MessageMapping("greetings")
	Flux<GreetingResponse> greet(GreetingRequest request) {
		return Flux//
				.fromStream(Stream.generate(() -> new GreetingResponse(
						"Hello, " + request.getName() + " @ " + Instant.now() + "!")))//
				.take(10)// <1>
				.delayElements(Duration.ofSeconds(1));
	}

}

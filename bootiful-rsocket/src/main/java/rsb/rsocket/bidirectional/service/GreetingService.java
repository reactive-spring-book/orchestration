package rsb.rsocket.bidirectional.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import rsb.rsocket.bidirectional.GreetingRequest;
import rsb.rsocket.bidirectional.GreetingResponse;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
class GreetingService {

	Flux<GreetingResponse> greet(GreetingRequest request) {
		return Flux
				.fromStream(Stream.generate(() -> new GreetingResponse(
						"Hello, " + request.getName() + " @ " + Instant.now() + "!")))
				.delayElements(
						Duration.ofSeconds(Math.max(3, (long) (Math.random() * 10))));
	}

}

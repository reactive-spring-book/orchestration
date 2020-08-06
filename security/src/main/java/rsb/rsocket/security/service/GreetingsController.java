package rsb.rsocket.security.service;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rsb.rsocket.security.GreetingRequest;
import rsb.rsocket.security.GreetingResponse;

import java.time.Duration;
import java.util.stream.Stream;

@Controller
class GreetingsController {

	@MessageMapping("greetings")
	Flux<GreetingResponse> greet(@AuthenticationPrincipal Mono<UserDetails> user) {
		return user.map(UserDetails::getUsername).map(GreetingRequest::new)
				.flatMapMany(this::greet);
	}

	private Flux<GreetingResponse> greet(GreetingRequest request) {
		return Flux
				.fromStream(Stream.generate(
						() -> new GreetingResponse("Hello, " + request.getName() + "!")))
				.delayElements(Duration.ofSeconds(1));
	}

}

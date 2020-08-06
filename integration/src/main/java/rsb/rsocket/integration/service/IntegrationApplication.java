package rsb.rsocket.integration.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import rsb.rsocket.integration.GreetingRequest;
import rsb.rsocket.integration.GreetingResponse;

import java.time.Duration;
import java.util.stream.Stream;

@SpringBootApplication
public class IntegrationApplication {

	public static void main(String args[]) {
		SpringApplication.run(IntegrationApplication.class, args);
	}

}

@Controller
class GreetingController {

	@MessageMapping("greetings")
	Flux<GreetingResponse> greet(GreetingRequest request) {
		return Flux//
				.fromStream(Stream.generate(
						() -> new GreetingResponse("Hello, " + request.getName() + "!")))//
				.delayElements(Duration.ofSeconds(1));
	}

}

package rsb.rsocket.rsocketrequester.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@SpringBootApplication
@RequiredArgsConstructor
public class RSocketRequesterApplication {

	public static void main(String[] args) {
		System.setProperty("spring.profiles.active", "service");
		SpringApplication.run(RSocketRequesterApplication.class, args);
	}

}

@Controller
class GreetingsController {

	@MessageMapping("greetings")
	Mono<String> hello(String name) {
		return Mono.just("Hello " + name + "!");
	}

}

package rsb.rsocket.errors.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.stream.Stream;

@SpringBootApplication
public class ErrorApplication {

	public static void main(String[] args) {
		System.setProperty("spring.profiles.active", "service");
		SpringApplication.run(ErrorApplication.class, args);
	}

}

@Log4j2
@Controller
class ErrorController {

	@MessageMapping("greetings")
	Flux<String> greet(String name) {
		return Flux//
				.fromStream(Stream.generate(() -> "hello " + name + "!"))//
				.flatMap(message -> {
					if (Math.random() > .5) {
						return Mono.error(new IllegalArgumentException("Ooops!"));
					} //
					else {
						return Mono.just(message);
					}
				})//
				.delayElements(Duration.ofSeconds(1));
	}

	@MessageExceptionHandler
	void exception(Exception exception) {
		log.error("the exception is " + exception.getMessage());
	}

}
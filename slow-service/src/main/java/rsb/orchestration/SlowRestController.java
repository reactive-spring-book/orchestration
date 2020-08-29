package rsb.orchestration;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
@RestController
class SlowRestController {

	private final long dtim;

	private final AtomicInteger port = new AtomicInteger();

	// <1>
	SlowRestController(@Value("${rsb.slow-service.delay}") long dtim) {
		this.dtim = dtim;
	}

	// <2>
	@EventListener
	public void web(WebServerInitializedEvent event) {
		port.set(event.getWebServer().getPort());
		if (log.isInfoEnabled()) {
			log.info("configured rsb.slow-service.delay=" + dtim + " on port "
					+ port.get());
		}
	}

	// <3>
	@GetMapping("/greetings")
	Mono<GreetingResponse> greet(
			@RequestParam(required = false, defaultValue = "world") String name) {
		var now = Instant.now().toString();
		var message = "Hello, %s! (from %s started at %s and finished at %s)";
		return Mono
				.just(new GreetingResponse(String.format(message, port, name, now,
						Instant.now().toString())))
				.doOnNext(r -> log.info(r.toString()))
				.delaySubscription(Duration.ofSeconds(dtim));
	}

}

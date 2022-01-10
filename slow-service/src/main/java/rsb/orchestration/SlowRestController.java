package rsb.orchestration;

import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@RestController
class SlowRestController {

	private final long slowServiceDelay;

	private final AtomicInteger port = new AtomicInteger();

	// <1>
	SlowRestController(@Value("${rsb.slow-service.delay}") long slowServiceDelay) {
		this.slowServiceDelay = slowServiceDelay;
	}

	// <2>
	@EventListener
	public void web(WebServerInitializedEvent event) {
		port.set(event.getWebServer().getPort());
		if (log.isInfoEnabled()) {
			log.info("configured rsb.slow-service.delay=" + slowServiceDelay + " on port " + port.get());
		}
	}

	// <3>
	@GetMapping("/greetings")
	Mono<GreetingResponse> greet(@RequestParam(required = false, defaultValue = "world") String name) {
		var now = Instant.now().toString();
		var message = "Hello, %s! (from %s started at %s and finished at %s)";
		return Mono.just(new GreetingResponse(String.format(message, port, name, now, Instant.now().toString())))
				.doOnNext(r -> log.info(r.toString())).delaySubscription(Duration.ofSeconds(slowServiceDelay));
	}

}

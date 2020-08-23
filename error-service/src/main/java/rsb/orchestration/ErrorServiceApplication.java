package rsb.orchestration;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
@RestController
@SpringBootApplication
public class ErrorServiceApplication {

	private final AtomicInteger port = new AtomicInteger();

	private final Map<String, AtomicInteger> countOfClientCalls = new ConcurrentHashMap<>();

	@GetMapping("/error")
	Mono<Map<String, String>> getMessageMaybe(
			@RequestParam(required = false) String uid) {
		this.countOfClientCalls.putIfAbsent(uid, new AtomicInteger(0));

		AtomicInteger individualCalls = this.countOfClientCalls.get(uid);
		individualCalls.incrementAndGet();

		return Math.random() >= .5
				? Mono.just(Map.of("greeting",
						"It works (from port " + this.port.get() + "): attempt #"
								+ individualCalls.get()))
				: Mono.error(new IllegalArgumentException());
	}

	@EventListener
	public void web(WebServerInitializedEvent event) {
		port.set(event.getWebServer().getPort());
		if (log.isInfoEnabled()) {
			log.info("running on port " + port.get());
		}
	}

	public static void main(String args[]) {
		SpringApplication.run(ErrorServiceApplication.class, args);
	}

}

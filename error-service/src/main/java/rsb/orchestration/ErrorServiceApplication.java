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

	private final Map<String, AtomicInteger> clientCounts = new ConcurrentHashMap<>();

	@GetMapping("/retry")
	Mono<Map<String, String>> retryEndpoint(@RequestParam(required = false) String uid) {
		this.clientCounts.putIfAbsent(uid, new AtomicInteger(0));
		var individualCalls = this.clientCounts.get(uid);
		var countThusFar = individualCalls.incrementAndGet();
		return countThusFar > 2
				? Mono.just(Map.of("greeting",
						String.format("greeting attempt # %s from port %s", countThusFar,
								this.port.get())))
				: Mono.error(new IllegalArgumentException());
	}

	@EventListener
	public void web(WebServerInitializedEvent event) {
		port.set(event.getWebServer().getPort());
	}

	public static void main(String args[]) {
		SpringApplication.run(ErrorServiceApplication.class, args);
	}

}

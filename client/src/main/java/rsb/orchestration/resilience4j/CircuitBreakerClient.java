package rsb.orchestration.resilience4j;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class CircuitBreakerClient implements ApplicationListener<ApplicationReadyEvent> {

	private final CircuitBreaker circuitBreaker = CircuitBreaker
			.ofDefaults("greetings-cb");

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {

	}

}

package rsb.orchestration.resilience4j;

import io.github.resilience4j.ratelimiter.RateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class RateLimiterClient implements ApplicationListener<ApplicationReadyEvent> {

	private final RateLimiter rateLimiter = RateLimiter.ofDefaults("greetings-rl");

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {

	}

}

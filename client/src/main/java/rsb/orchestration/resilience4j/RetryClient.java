package rsb.orchestration.resilience4j;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("retry")
class RetryClient {

	private final Retry retry = Retry.of("greetings-retry", RetryConfig//
			.custom() //
			.waitDuration(Duration.ofMillis(1000)) // <1>
			.intervalFunction(IntervalFunction.ofExponentialBackoff(Duration.ofMillis(500L), 2d))// <2>
			.maxAttempts(3) // <3>
			.build());

	private final String uid = UUID.randomUUID().toString();

	private final WebClient http;

	@EventListener(ApplicationReadyEvent.class)
	public void ready() {
		Mono<String> retry = GreetingClientUtils.getGreetingFor(this.http, this.uid, "retry")
				.transformDeferred(RetryOperator.of(this.retry));// <4>
		retry.subscribe(log::info);
	}

}

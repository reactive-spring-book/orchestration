package rsb.orchestration.resilience4j;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

/*
 * This client demonstrates using the RetryOperator. The endpoint that we're invoking is
 * configured to fail for the first 2 times, and return a value the third time.
 * Accordingly, this R4J client is configured to give up after 3 attempts. So it _should_
 * get a result just in the nick of time. You can try lowering the threshold to see what
 * happens if you don't get a result.
 */
@Log4j2
@Component
@Profile("retry")
@RequiredArgsConstructor
class RetryClient implements ApplicationListener<ApplicationReadyEvent> {

	private final Retry retry = Retry.of("greetings-retry", RetryConfig//
			.custom() //
			.waitDuration(Duration.ofMillis(1000)) //
			.intervalFunction(IntervalFunction
					.ofExponentialBackoff(IntervalFunction.DEFAULT_INITIAL_INTERVAL, 2d))//
			.maxAttempts(3) //
			.build());

	private final String uid = UUID.randomUUID().toString();

	private final WebClient http;

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		Mono<String> retry = GreetingClientUtils
				.getGreetingFor(this.http, this.uid, "retry")
				.transformDeferred(RetryOperator.of(this.retry));
		retry.subscribe(log::info);
	}

}

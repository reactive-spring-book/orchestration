package rsb.orchestration.resilience4j;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
@Component
@Profile("rl")
@RequiredArgsConstructor
class RateLimiterClient implements ApplicationListener<ApplicationReadyEvent> {

	private final String uid = UUID.randomUUID().toString();

	private final WebClient http;

	private final RateLimiter rateLimiter = RateLimiter.of("greetings-rl",
			RateLimiterConfig//
					.custom() //
					.limitForPeriod(10)// <1>
					.limitRefreshPeriod(Duration.ofSeconds(1))// <2>
					.timeoutDuration(Duration.ofMillis(25))//
					.build());

	@SneakyThrows
	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {

		var max = 20;
		var cdl = new CountDownLatch(max);
		var result = new AtomicInteger();
		var errors = new AtomicInteger();
		for (var i = 0; i < max; i++)
			this.buildRequest(cdl, result, errors, rateLimiter, i).subscribe();

		cdl.await();

		log.info("there were " + errors.get() + " errors");
		log.info("there were " + result.get() + " results");

	}

	private Mono<String> buildRequest(CountDownLatch cdl, AtomicInteger results,
			AtomicInteger errors, RateLimiter rateLimiter, int count) {
		return GreetingClientUtils//
				.getGreetingFor(this.http, this.uid, "ok")//
				.transformDeferred(RateLimiterOperator.of(rateLimiter))//
				.doOnError(ex -> {
					errors.incrementAndGet();
					log.info("oops! got an error of type " + ex.getClass().getName());
				})///
				.doOnNext(reply -> {
					results.incrementAndGet();
					log.info("count is " + count + " @ " + Instant.now() + "(" + reply
							+ ")");
				})//
				.doOnTerminate(cdl::countDown);

	}

}

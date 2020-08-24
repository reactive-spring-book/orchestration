package rsb.orchestration.resilience4j;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
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
@RequiredArgsConstructor
class RateLimiterClient implements ApplicationListener<ApplicationReadyEvent> {

	/*
	 * This measures how many requests we can make in a distinct period. I've configured
	 * the RateLimiter to have a _very_ low threshold below. It'll allow no more than 10
	 * requests for any given second. I want to test this so i've fired off 20 requests
	 * which should -- all things being equal - have more than enough time to begin and
	 * even return a response. If for whatever reason that's not the case, you can ramp
	 * down the `limitForPeriod` value below or ramp up the `limitRefreshPeriod` value
	 * from 1 second to 5 seconds. Ive then configured two atomic numbers to keep track of
	 * either valid responses _or_ RequestNotPermitted responses. If we observe a valid
	 * value then well increment the results counter, otherwise the errors counter.
	 *
	 * For more: people are advised to consider Spring Cloud Gateway's rate limiter and
	 * the Alibaba Sentinel rate limiter.
	 */

	private final String uid = UUID.randomUUID().toString();

	private final WebClient http;

	@SneakyThrows
	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {

		var result = new AtomicInteger();
		var errors = new AtomicInteger();
		var rateLimiter = RateLimiter.of("greetings-rl",
				RateLimiterConfig.custom().limitRefreshPeriod(Duration.ofSeconds(1))
						.limitForPeriod(10).timeoutDuration(Duration.ofMillis(25))
						.build());

		var max = 20;
		var cdl = new CountDownLatch(max);
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

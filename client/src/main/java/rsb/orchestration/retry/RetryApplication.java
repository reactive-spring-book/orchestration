package rsb.orchestration.retry;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.reactor.bulkhead.operator.BulkheadOperator;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Log4j2
@SpringBootApplication
public class RetryApplication {

	public static void main(String[] args) {
		SpringApplication.run(RetryApplication.class, args);
	}

	@Bean
	WebClient loadBalancedWebClient(WebClient.Builder builder) {
		return builder.build();
	}

	@Bean
	@LoadBalanced
	WebClient.Builder builder() {
		return WebClient.builder();
	}

	@Bean
	ApplicationListener<ApplicationReadyEvent> demo(GreetingClient greetingClient) {
		return events -> {
			var start = new AtomicLong();
			greetingClient.greet().doOnError(exception -> log.error("oops!", exception))
					.doOnSubscribe(
							(subscription) -> start.set(System.currentTimeMillis()))
					.doOnNext((greeting) -> log.info(String.format("total time: %s",
							System.currentTimeMillis() - start.get())))
					.subscribe(System.out::println);
		};
	}

}

class SpringCloudCircuitBreakerGreetingClient implements GreetingClient {

	private final WebClient http;

	private final ReactiveCircuitBreaker errors;

	private final String uid = UUID.randomUUID().toString();

	SpringCloudCircuitBreakerGreetingClient(WebClient http, ReactiveCircuitBreaker cb) {
		this.http = http;
		this.errors = cb;
	}

	@Override
	public Mono<String> greet() {
		var parameterizedTypeReference = new ParameterizedTypeReference<Map<String, String>>() {
		};

		var configName = "greetings-";

		Retry retry = Retry.ofDefaults(configName + "retry");
		RetryOperator<String> retryOperator = RetryOperator.of(retry);

		RateLimiter rateLimiter = RateLimiter.ofDefaults(configName + "rl");
		RateLimiterOperator<String> rateLimiterOperator = RateLimiterOperator
				.of(rateLimiter);

		Bulkhead bulkhead = Bulkhead.ofDefaults(configName + "bulkhead");
		BulkheadOperator<String> bulkheadOperator = BulkheadOperator.of(bulkhead);

		var greeting = this.http.get()//
				.uri("http://error-service/error?uid=" + this.uid)//
				.retrieve() //
				.bodyToMono(parameterizedTypeReference)//
				.map(map -> map.get("greeting")).transform(rateLimiterOperator)
				.transform(bulkheadOperator).transform(retryOperator);//

		return greeting;
		/*
		 * return this.errors .run(greeting, exception -> Mono.just("umm..."));
		 */
	}

}

interface GreetingClient {

	Mono<String> greet();

}

@Configuration
@Profile({ "sscb", "default" })
class SpringCloudCircuitbreakerConfiguration {

	@Bean
	Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
		return factory -> factory
				.configureDefault(
						id -> new Resilience4JConfigBuilder(id)
								.circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
								.timeLimiterConfig(TimeLimiterConfig.custom()
										.timeoutDuration(Duration.ofSeconds(4)).build())
								.build());
	}

	/*
	 * @Bean ReactiveCircuitBreakerFactory circuitBreakerFactory() { var factory = new
	 * ReactiveResilience4JCircuitBreakerFactory(); factory.configureDefault(s -> {
	 *
	 * TimeLimiterConfig tlConfig = TimeLimiterConfig.custom()
	 * .timeoutDuration(Duration.ofSeconds(10)).build();
	 *
	 * CircuitBreakerConfig cbConfig = CircuitBreakerConfig// .custom()//
	 * .permittedNumberOfCallsInHalfOpenState(10) .failureRateThreshold(50F)// .build();
	 *
	 * return new Resilience4JConfigBuilder(s) .timeLimiterConfig(tlConfig)
	 * .circuitBreakerConfig(cbConfig).build(); }); return factory; }
	 */

	@Bean
	SpringCloudCircuitBreakerGreetingClient springCloudCircuitBreakerGreetingClient(
			WebClient http, ReactiveCircuitBreakerFactory cbf) {
		var greetings = cbf.create("greetings-cb");
		return new SpringCloudCircuitBreakerGreetingClient(http, greetings);
	}

}

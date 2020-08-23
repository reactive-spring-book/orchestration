package rsb.orchestration.retry;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.reactor.bulkhead.operator.BulkheadOperator;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Log4j2
@SpringBootApplication
public class ResilientClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(ResilientClientApplication.class, args);
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
			greetingClient.greet()//
					.doOnError(exception -> log.error("oops!", exception))//
					.doOnSubscribe(
							(subscription) -> start.set(System.currentTimeMillis())) //
					.doOnNext((greeting) -> log.info("total time: {} ",
							System.currentTimeMillis() - start.get()))
					.subscribe(System.out::println);
		};
	}

}

class Resilience4jGreetingClient implements GreetingClient {

	private final String uid = UUID.randomUUID().toString();

	private final String configName = "greetings-";

	private final Retry retry = Retry.ofDefaults(configName + "retry");

	private final Bulkhead bulkhead = Bulkhead.ofDefaults(configName + "bulkhead");

	private final RateLimiter rateLimiter = RateLimiter.ofDefaults(configName + "rl");

	private final WebClient http;

	Resilience4jGreetingClient(WebClient http) {
		this.http = http;
	}

	@Override
	public Mono<String> greet() {
		var parameterizedTypeReference = new ParameterizedTypeReference<Map<String, String>>() {
		};
		var greeting = this.http.get()//
				.uri("http://error-service/error?uid=" + this.uid)//
				.retrieve() //
				.bodyToMono(parameterizedTypeReference)//
				.map(map -> map.get("greeting"))//
				.transform(RateLimiterOperator.of(rateLimiter))//
				.transform(BulkheadOperator.of(bulkhead))//
				.transform(RetryOperator.of(retry));//
		return greeting;
	}

}

interface GreetingClient {

	Mono<String> greet();

}

@Configuration
@Profile({ "r4j", "default" })
class Resilience4jConfiguration {

	@Bean
	Resilience4jGreetingClient springCloudCircuitBreakerGreetingClient(WebClient http) {
		return new Resilience4jGreetingClient(http);
	}

}

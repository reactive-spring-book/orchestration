package rsb.orchestration.resilience4j;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.reactor.bulkhead.operator.BulkheadOperator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.UUID;

@Log4j2
@Component
@Profile("bulkhead")
@RequiredArgsConstructor
class BulkheadClient implements ApplicationListener<ApplicationReadyEvent> {

	private final String uid = UUID.randomUUID().toString();

	private final int availableProcessors = Runtime.getRuntime().availableProcessors();

	private final int maxCalls = availableProcessors / 2;

	private final WebClient http;

	private final Bulkhead bulkhead = Bulkhead.of("greetings-bulkhead", BulkheadConfig //
			.custom() //
			.writableStackTraceEnabled(true) //
			.maxConcurrentCalls(this.maxCalls)// <1>
			.maxWaitDuration(Duration.ofMillis(5)) //
			.build());

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		log.info("there are " + availableProcessors
				+ " available, therefore there should be " + availableProcessors
				+ " in the default thread pool");
		var immediate = Schedulers.immediate();
		for (var i = 0; i < availableProcessors; i++) {
			buildRequest(immediate, i).subscribe();
		}
	}

	private Mono<String> buildRequest(Scheduler scheduler, int i) {
		log.info("bulkhead attempt #" + i);
		return GreetingClientUtils //
				.getGreetingFor(this.http, this.uid, "ok") //
				.transform(BulkheadOperator.of(this.bulkhead)) //
				.subscribeOn(scheduler)//
				.publishOn(scheduler) //
				.onErrorResume(throwable -> {
					log.info("the bulkhead kicked in for request #" + i
							+ ". Received the following exception "
							+ throwable.getClass().getName() + '.');
					return Mono.empty();
				}) //
				.onErrorStop();
	}

}

package rsb.orchestration.resilience4j;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.reactor.bulkhead.operator.BulkheadOperator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Log4j2
@Component
@RequiredArgsConstructor
class BulkheadClient implements ApplicationListener<ApplicationReadyEvent> {

	private final Bulkhead bulkhead = Bulkhead.ofDefaults("greetings-bulkhead");

	private final String uid = UUID.randomUUID().toString();

	private final WebClient http;

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		Mono<String> retry = GreetingClientUtils
				.getGreetingFor(this.http, this.uid, "bulkhead")
				.transform(BulkheadOperator.of(this.bulkhead));
		// retry.subscribe(log::info);
	}

}

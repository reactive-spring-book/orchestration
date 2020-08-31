package rsb.orchestration.reactor;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
@RequiredArgsConstructor
class RetryWhenClient implements ApplicationListener<ApplicationReadyEvent> {

	private final OrderClient client;

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		this.client.getOrders(1, 2)//
				.retryWhen(Retry.backoff(10, Duration.ofSeconds(1)))// <1>
				.subscribe(System.out::println);
	}

}

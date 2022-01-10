package rsb.orchestration.reactor;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
record RetryWhenClient(OrderClient client) {

	@EventListener(ApplicationReadyEvent.class)
	public void ready() {
		this.client.getOrders(1, 2)//
				.retryWhen(Retry.backoff(10, Duration.ofSeconds(1)))// <1>
				.subscribe(System.out::println);
	}

}

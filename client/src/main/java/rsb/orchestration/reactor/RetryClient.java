package rsb.orchestration.reactor;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
record RetryClient(OrderClient client) {

	@EventListener(ApplicationReadyEvent.class)
	public void ready() {
		this.client.getOrders(1, 2)//
				.retry(10)// <1>
				.subscribe(System.out::println);

	}

}

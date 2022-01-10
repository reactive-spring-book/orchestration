package rsb.orchestration.reactor;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
record TimeoutClient(OrderClient client) {

	@EventListener(ApplicationReadyEvent.class)
	public void ready() {
		this.client.getOrders(1, 2)//
				.timeout(Duration.ofSeconds(10))//
				.subscribe(System.out::println);
	}

}

package rsb.orchestration.reactor;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
record DegradingClient(OrderClient client) {

	@EventListener(ApplicationReadyEvent.class)
	public void ready() {
		this.client.getOrders(1, 2)//
				.onErrorResume(exception -> Flux.empty()) // <1>
				.subscribe(System.out::println);
	}

}

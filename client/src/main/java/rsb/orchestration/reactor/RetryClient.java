package rsb.orchestration.reactor;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class RetryClient implements ApplicationListener<ApplicationReadyEvent> {

	private final OrderClient client;

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		this.client.getOrders(1, 2)//
				.retry(10)// <1>
				.subscribe(System.out::println);

	}

}

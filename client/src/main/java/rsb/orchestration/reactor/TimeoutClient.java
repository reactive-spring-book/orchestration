package rsb.orchestration.reactor;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
class TimeoutClient implements ApplicationListener<ApplicationReadyEvent> {

	private final OrderClient client;

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		this.client.getOrders(1, 2)//
				.timeout(Duration.ofSeconds(10))//
				.subscribe(System.out::println);
	}

}

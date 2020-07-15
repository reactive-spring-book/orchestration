package rsb.rsocket.bidirectional.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@Slf4j
@Component
@RequiredArgsConstructor
class ClientLauncher implements ApplicationListener<ApplicationReadyEvent> {

	private final RSocketRequester rSocketRequester;

	private final int maxClients = 10;

	private final ScheduledExecutorService executorService = Executors
			.newScheduledThreadPool(maxClients);

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		var nestedMax = Math.max(5, (int) (Math.random() * maxClients));
		log.info("launching " + nestedMax + " clients.");
		IntStream //
				.range(0, nestedMax)//
				.forEach(value -> {
					var uid = Long.toString(value);
					var delay = Math.max(1000L,
							(long) (Math.random() * (1000 * nestedMax)));
					var clientNode = new Client(this.rSocketRequester, uid);
					this.executorService.schedule(clientNode::start, delay,
							TimeUnit.MILLISECONDS);
				});
	}

}

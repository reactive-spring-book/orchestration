package rsb.rsocket.bidirectional.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import rsb.rsocket.BootifulProperties;
import rsb.rsocket.EncodingUtils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@Slf4j
@Component
@RequiredArgsConstructor
class ClientLauncher implements ApplicationListener<ApplicationReadyEvent> {

	private final EncodingUtils encodingUtils;

	private final int maxClients = 10;

	private final BootifulProperties properties;

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
					var client = new Client(this.encodingUtils, uid,
							this.properties.getRsocket().getHostname(),
							this.properties.getRsocket().getPort());
					this.executorService.schedule(client::start, delay,
							TimeUnit.MILLISECONDS);
				});
	}

}

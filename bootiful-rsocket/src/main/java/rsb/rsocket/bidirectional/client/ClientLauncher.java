package rsb.rsocket.bidirectional.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import rsb.rsocket.bidirectional.GreetingResponse;

import java.time.Duration;
import java.util.stream.IntStream;

@Slf4j
@Component
@RequiredArgsConstructor
class ClientLauncher implements ApplicationListener<ApplicationReadyEvent> {

	private final RSocketRequester rSocketRequester;

	private final int maxClients = 10;

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		var nestedMax = Math.max(5, (int) (Math.random() * maxClients));
		log.info("launching " + nestedMax + " clients.");
		Flux.fromStream(IntStream.range(0, nestedMax).boxed())//
				.map(id -> new Client(this.rSocketRequester, Long.toString(id)))//
				.flatMap(client -> Flux.just(client)
						.delayElements(Duration.ofSeconds((long) (30 * Math.random()))))//
				.flatMap(Client::start)// <5>
				.subscribeOn(Schedulers.elastic())// <6>
				.map(GreetingResponse::toString).subscribe(log::info);
	}

}

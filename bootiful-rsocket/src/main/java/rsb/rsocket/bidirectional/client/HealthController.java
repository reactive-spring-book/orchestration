package rsb.rsocket.bidirectional.client;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import rsb.rsocket.bidirectional.ClientHealthState;

import java.time.Duration;
import java.util.Date;
import java.util.stream.Stream;

import static rsb.rsocket.bidirectional.ClientHealthState.STARTED;
import static rsb.rsocket.bidirectional.ClientHealthState.STOPPED;

@Controller
class HealthController {

	@MessageMapping("health")
	Flux<ClientHealthState> health() {
		var start = new Date().getTime();
		var delayInSeconds = ((long) (Math.random() * 30)) * 1000;
		return Flux//
				.fromStream(Stream//
						.generate(() -> {
							var now = new Date().getTime();
							var stop = ((start + delayInSeconds) < now)
									&& Math.random() > .8;
							return new ClientHealthState(stop ? STOPPED : STARTED);
						}))//
				.delayElements(Duration.ofSeconds(5));
	}

}

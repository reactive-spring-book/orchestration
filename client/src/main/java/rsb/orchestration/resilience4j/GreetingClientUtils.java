package rsb.orchestration.resilience4j;

import lombok.extern.log4j.Log4j2;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Log4j2
abstract class GreetingClientUtils {

	static Mono<String> getGreetingFor(WebClient http, String clientUid, String path) {
		var parameterizedTypeReference = new ParameterizedTypeReference<Map<String, String>>() {
		};
		var monoFromHttpCall = http //
				.get()//
				.uri("http://error-service/" + path + "?uid=" + clientUid)//
				.retrieve() //
				.bodyToMono(parameterizedTypeReference)//
				.map(map -> map.get("greeting"));
		return monitor(monoFromHttpCall);
	}

	private static Mono<String> monitor(Mono<String> configMono) {
		var start = new AtomicLong();
		return configMono//
				.doOnError(exception -> log.error("oops!", exception))//
				.doOnSubscribe((subscription) -> start.set(System.currentTimeMillis())) //
				.doOnNext((greeting) -> log.info("total time: {}",
						System.currentTimeMillis() - start.get()));
	}

}

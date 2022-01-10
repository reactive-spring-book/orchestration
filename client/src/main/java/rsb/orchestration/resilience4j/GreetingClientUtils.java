package rsb.orchestration.resilience4j;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

import static rsb.orchestration.TimerUtils.monitor;

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

}

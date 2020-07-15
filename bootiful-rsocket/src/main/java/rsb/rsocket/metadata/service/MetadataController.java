package rsb.rsocket.metadata.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;
import rsb.rsocket.metadata.Constants;

import java.util.Map;

@Log4j2
@Controller
@RequiredArgsConstructor
class MetadataController {

	private static void log(StringBuilder stringBuilder, String clientId, String string) {
		stringBuilder//
				.append(String.format("(%s) %s", clientId, string))//
				.append(System.lineSeparator());
	}

	@ConnectMapping
	Mono<Void> onMetadataPush(@Headers Map<String, Object> metadata) {
		var clientId = (String) metadata.get(Constants.CLIENT_ID_HEADER);
		var stringBuilder = new StringBuilder().append(System.lineSeparator());
		log(stringBuilder, clientId, "---------------------------------");
		metadata.forEach((k, v) -> log(stringBuilder, clientId, k + '=' + v));
		log.info(stringBuilder.toString());
		return Mono.empty();
	}

}

package rsb.rsocket.metadata.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;
import rsb.rsocket.metadata.Constants;

import java.util.Map;

@Log4j2
@Controller
@RequiredArgsConstructor
class MetadataController {

	@ConnectMapping
	// <1>
	Mono<Void> setup(@Headers Map<String, Object> metadata) {
		log.info("## setup");
		return enumerate(metadata);
	}

	@MessageMapping("message")
	// <2>
	Mono<Void> message(@Header(Constants.CLIENT_ID_HEADER) String clientId,
			@Headers Map<String, Object> metadata) {
		log.info("## message for " + Constants.CLIENT_ID_HEADER + ' ' + clientId);
		return enumerate(metadata);
	}

	private Mono<Void> enumerate(Map<String, Object> headers) {
		headers.forEach((header, value) -> log.info(header + ':' + value));
		return Mono.empty();
	}

}
